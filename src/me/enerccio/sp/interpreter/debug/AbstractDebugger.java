/*
 * SimplePython - embeddable python interpret in java
 * Copyright (c) Peter Vanusanik, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package me.enerccio.sp.interpreter.debug;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

import me.enerccio.sp.compiler.Bytecode;
import me.enerccio.sp.interpret.AbstractPythonInterpreter;
import me.enerccio.sp.interpret.CompiledBlockObject.DebugInformation;
import me.enerccio.sp.interpret.FrameObject;

public class AbstractDebugger implements Debugger {

	@Override
	public void bind(AbstractPythonInterpreter pythonInterpreter) {

	}

	private Set<Breakpoint> breakpoints = new HashSet<Breakpoint>();

	@Override
	public void debugNextOperation(AbstractPythonInterpreter i, Bytecode b,
			FrameObject f, int cpc) {

		DebugInformation di = f.getCompiled().getDebugInformation(cpc);
		for (Breakpoint bp : breakpoints) {
			if (bp.applies(di)) {
				doBreak(i, b, f, cpc);
				return;
			}
		}

		if (breakNextInstruction) {
			doBreak(i, b, f, cpc);
		} else if (breakNextLine) {
			if (nextLineBreak.applies(di)) {
				doBreak(i, b, f, cpc);
			} else if (b == Bytecode.D_RETURN) {
				if (f == nextLineFrame) {
					doBreak(i, b, f, cpc);
				}
			}
		} else if (breakIntoCall) {
			if (b == Bytecode.D_STARTFUNC) {
				if (i.currentFrame.size() > 1) {
					FrameObject previousFrame = i.currentFrame
							.get(i.currentFrame.size() - 2);
					if (previousFrame == nextIntoCall) {
						doBreak(i, b, f, cpc);
					}
				}
			}
		} else if (breakReturn) {
			if (b == Bytecode.D_RETURN) {
				if (f == nextLineFrame) {
					doBreak(i, b, f, cpc);
				}
			}
		}
	}

	public void breakOnNextInstruction() {
		breakNextInstruction = true;
	}

	public void breakNextLine() {
		DebugInformation di = f.getCompiled().getDebugInformation(cpc);
		nextLineBreak = new Breakpoint(di.module.getName(),
				di.module.getPackageResolve(), di.lineno + 1);
		nextLineFrame = i.currentFrame.size() == 1 ? null : i.currentFrame
				.get(i.currentFrame.size() - 2);
		breakNextLine = true;
	}

	public void breakIntoCall() {
		nextIntoCall = i.currentFrame.getLast();
		breakIntoCall = true;
	}

	public void breakReturn() {
		nextReturn = i.currentFrame.size() == 1 ? null : i.currentFrame
				.get(i.currentFrame.size() - 2);
		if (nextReturn != null)
			breakReturn = true;
	}

	protected boolean breakNextInstruction = false;
	protected boolean breakNextLine = false;
	protected Breakpoint nextLineBreak = null;
	protected FrameObject nextLineFrame = null;
	protected boolean breakIntoCall = false;
	protected FrameObject nextIntoCall = null;
	protected boolean breakReturn = false;
	protected FrameObject nextReturn = null;

	protected void clearRegisters() {
		breakNextInstruction = false;
		breakNextLine = false;
		breakIntoCall = false;
		nextIntoCall = null;
		nextLineBreak = null;
		nextLineFrame = null;
		breakReturn = false;
		nextReturn = null;
	}

	public final Semaphore breakingSemaphore = new Semaphore(0);
	public final Semaphore waitingSemaphore = new Semaphore(0);

	protected void doBreak(AbstractPythonInterpreter i, Bytecode b,
			FrameObject f, int cpc) {
		clearRegisters();
		initializeBreak(i, b, f, cpc);
		breakingSemaphore.release();
		try {
			waitingSemaphore.acquire();
		} catch (InterruptedException e) {

		}
		cleanup();
	}

	protected void cleanup() {
		i = null;
		cbc = null;
		cpc = -1;
		f = null;
	}

	public AbstractPythonInterpreter i;
	public Bytecode cbc;
	public int cpc;
	public FrameObject f;

	protected void initializeBreak(AbstractPythonInterpreter i, Bytecode b,
			FrameObject f, int cpc) {
		this.i = i;
		this.cbc = b;
		this.cpc = cpc;
		this.f = f;
	}

	@Override
	public void unbind(AbstractPythonInterpreter pythonInterpreter) {

	}

	public Breakpoint installBreakpoint(String moduleName, String modulePath,
			int line) {
		Breakpoint bp = new Breakpoint(moduleName, modulePath, line);
		breakpoints.add(bp);
		return bp;
	}

	public List<FrameObject> getFrames() {
		List<FrameObject> frl = new ArrayList<FrameObject>();
		for (FrameObject fo : i.currentFrame) {
			if (fo.parentFrame == null) {
				frl.add(fo);
			}
		}
		return frl;
	}

	public List<String> getVariables(FrameObject frame) {
		List<String> names = new ArrayList<String>();
		if (frame.environment == null)
			return names;
		Set<String> vNames = frame.environment.getTop().keySet();
		for (String v : vNames) {
			names.add(v);
		}
		Collections.sort(names);
		if (names.contains("self")) {
			names.remove("self");
			names.add(0, "self");
		}
		return names;
	}

	public String valueToString(FrameObject frame, String variable) {
		return frame.environment.getLocals().getVariable(variable).toString();
	}
}
