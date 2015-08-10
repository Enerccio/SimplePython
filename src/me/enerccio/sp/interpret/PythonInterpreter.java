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
package me.enerccio.sp.interpret;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import sun.net.www.protocol.http.HttpURLConnection.TunnelState;
import me.enerccio.sp.compiler.Bytecode;
import me.enerccio.sp.compiler.PythonBytecode;
import me.enerccio.sp.compiler.PythonBytecode.*;
import me.enerccio.sp.interpret.CompiledBlockObject.DebugInformation;
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.types.AugumentedPythonObject;
import me.enerccio.sp.types.ModuleObject;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.BoolObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.base.NumberObject;
import me.enerccio.sp.types.callables.CallableObject;
import me.enerccio.sp.types.callables.ClassObject;
import me.enerccio.sp.types.callables.UserFunctionObject;
import me.enerccio.sp.types.callables.UserMethodObject;
import me.enerccio.sp.types.iterators.GeneratorObject;
import me.enerccio.sp.types.iterators.InternalIterator;
import me.enerccio.sp.types.iterators.InternallyIterable;
import me.enerccio.sp.types.iterators.XRangeIterator;
import me.enerccio.sp.types.mappings.DictObject;
import me.enerccio.sp.types.mappings.PythonProxy;
import me.enerccio.sp.types.pointer.PointerObject;
import me.enerccio.sp.types.properties.PropertyObject;
import me.enerccio.sp.types.sequences.ListObject;
import me.enerccio.sp.types.sequences.SequenceObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.types.sequences.XRangeObject;
import me.enerccio.sp.types.types.ObjectTypeObject;
import me.enerccio.sp.utils.Utils;

@SuppressWarnings("unused")
/**
 * PythonInterpret. Interprets bytecode. One per thread and gets automatically constructed the moment something wants to access it in a thread.
 * @author Enerccio
 *
 */
public class PythonInterpreter extends PythonObject {
	private static final long serialVersionUID = -8039667108607710165L;
	public static final boolean TRACE_ENABLED = System.getenv("SPY_TRACE_ENABLED") != null;
	/** Thread local accessor to the interpret */
	public static final transient ThreadLocal<PythonInterpreter> interpreter = new ThreadLocal<PythonInterpreter>(){

		@Override
		protected PythonInterpreter initialValue() {
			try {
				PythonRuntime.runtime.waitForNewInterpretAvailability();
			} catch (InterruptedException e){
				
			}
			
			PythonInterpreter i = new PythonInterpreter();
			i.newObject();
			interpreters.add(i);
			return i;
		}
		
	};
	
	/** Collection of all interprets created */
	public static final Set<PythonInterpreter> interpreters = Collections.synchronizedSet(new HashSet<PythonInterpreter>());
	
	public PythonInterpreter(){
		bind();
	}
	
	/**
	 * Binds the interpret to this thread
	 */
	public void bind(){
		interpreter.set(this);
	}
	
	private EnvironmentObject nullEnv  = new EnvironmentObject();
	{
		nullEnv.add(PythonRuntime.runtime.getGlobals());
	}
	
	/** current frame stack. Topmost element represents currently interpreted frame */
	public LinkedList<FrameObject> currentFrame = new LinkedList<FrameObject>();
	/** Number of times this interpret is accessed by itself. If >0, interpret can't be serialized */
	private volatile int accessCount = 0;
	/** Represents currrently passed arguments to the function */
	private DictObject args = null;
	/** Represents value returned by a call */
	private PythonObject returnee;
	private List<DictObject> currentClosure;
	public PythonObject lastPushedValue;
	
	/**
	 * whether this interpret can be stopped at safe place or not
	 * @return
	 */
	public boolean isInterpretStoppable(){
		return accessCount == 0;
	}

	/**
	 * executes single call identified by function in current environment and returns value, if any
	 * @param internal 
	 * @param function
	 * @param data
	 * @return
	 */
	public PythonObject executeCall(boolean internal, String function, PythonObject... data) {
		if (currentFrame.size() == 0)
			return returnee = execute(internal, PythonRuntime.runtime.getGlobals().doGet(function), null, data);
		return returnee = execute(internal, environment().getBuiltin(function), null, data);
	}

	/**
	 * Returns current environment or null
	 * @return
	 */
	public EnvironmentObject environment() {
		if (currentFrame.size() == 0)
			return nullEnv;
		return currentFrame.getLast().environment;
	}
	
	/**
	 * Returns current local context or null
	 * @return
	 */
	public PythonObject getLocalContext() {
		if (currentFrame.size() == 0)
			return NoneObject.NONE;
		PythonObject p = currentFrame.getLast().localContext;
		if (p == null)
			return NoneObject.NONE;
		return p;
	}

	/**
	 * Executes call to the callable. 
	 * @param internalCall if true and call is to a python code, it will wait until that code will finish
	 * @param callable callable
	 * @param args arguments
	 * @return
	 */
	public PythonObject execute(boolean internalCall, PythonObject callable, KwArgs kwargs, PythonObject... args) {
		if (callable instanceof CallableObject){
			if (((callable instanceof UserFunctionObject) || (callable instanceof UserMethodObject)) && internalCall){
				int cfc = currentFrame.size();
				((CallableObject)callable).call(new TupleObject(args), kwargs);
				return executeAll(cfc);
			} else if (internalCall) {
				int cfc = currentFrame.size();
				returnee = ((CallableObject)callable).call(new TupleObject(args), kwargs);
				if (cfc < currentFrame.size()){
					returnee = executeAll(cfc);
				}
				return returnee;
			} else {
				return returnee = ((CallableObject)callable).call(new TupleObject(args), kwargs);
			}
		} else {
			PythonObject callableArg = callable.get(CallableObject.__CALL__, getLocalContext());
			if (callableArg == null)
				throw Utils.throwException("TypeError", callable.toString() + " is not callable");
			return returnee = execute(false, callableArg, kwargs, args);
		}
	}

	/**
	 * Returns exception of the topmost frame or null if either no frame is there or no exception has happened
	 * @return
	 */
	public PythonObject exception() {
		if (currentFrame.size() != 0)
			return currentFrame.peekLast().exception;
		return null;
	}

	/**
	 * invokes callable with args
	 * @param callable
	 * @param args
	 * @return
	 */
	public PythonObject invoke(PythonObject callable, KwArgs kwargs, TupleObject args) {
		return execute(false, callable, kwargs, args.getObjects());
	}

	/**
	 * returns global value for variable key or null
	 * @param key
	 * @return
	 */
	public PythonObject getGlobal(String key) {
		return environment().get(new StringObject(key), true, false);
	}

	/**
	 * Pushes new frame on the stack that contains this bytecode.
	 * @param frame
	 */
	public void executeBytecode(CompiledBlockObject frame) {
		FrameObject n;
		currentFrame.add(n = new FrameObject());
		n.compiled = frame;
		n.dataStream = ByteBuffer.wrap(frame.getBytedata());
		
//		System.out.println(CompiledBlockObject.dis(frame));
	}

	/**
	 * Executes single instruction
	 * @return
	 */
	public ExecutionResult executeOnce(){
		ExecutionResult r = doExecuteOnce();
		if (r == ExecutionResult.EOF)
			if (currentFrame.size() == 0)
				return ExecutionResult.FINISHED;
		return r;
	}

	private ExecutionResult doExecuteOnce() {
		try {
			PythonRuntime.runtime.waitIfSaving(this);
		} catch (InterruptedException e) {
			return ExecutionResult.INTERRUPTED;
		}
		
		if (Thread.interrupted()){
			return ExecutionResult.INTERRUPTED;
		}
		
		if (exception() != null){
			handleException(new PythonExecutionException(exception()));
			return ExecutionResult.EOF;
		}
		
		++accessCount;
		try {
			if (currentFrame.size() == 0)
				return ExecutionResult.FINISHED;
			FrameObject o = currentFrame.getLast();
			if (o == null)
				return ExecutionResult.FINISHED;
			if (o.pc >= o.compiled.getBytedata().length){
				removeLastFrame();
				returnee = null;
				return ExecutionResult.EOF;
			}
			try {
				return executeSingleInstruction(o);
			} catch (PythonExecutionException e){
				handleException(e);
				return ExecutionResult.EOF;
			}
		} finally {
			--accessCount;
		}
	}

	/**
	 * Handles the exception chain.
	 * @param e
	 */
	private void handleException(PythonExecutionException e) {
		PythonObject pe = e.getException();
		currentFrame.peekLast().exception = pe;
		PythonObject stack = pe.get("stack", null);
		if (stack instanceof ListObject){
			ListObject s = (ListObject)stack;
			s.objects.add(makeStack());
		}
		removeLastFrame();
	}

	/**
	 * Inserts current instruction into stack
	 * @return
	 */
	private PythonException.StackElement makeStack() {
		FrameObject o = currentFrame.getLast();
		if (o == null)
			return PythonException.LAST_FRAME; 
		if (o.debugLine < 0)
			return PythonException.SYSTEM_FRAME;
		return new PythonException.StackElement(o.debugModule, o.debugFunction, o.debugLine, o.debugInLine);
	}

	/**
	 * Creates new stack line element of the current stack
	 * @return
	 */
	private String makeStackString() {
		FrameObject o = currentFrame.getLast();
		if (o == null)
			return "<last frame>";
		if (o.debugLine < 0)
			return "<system-frame>";
		return String.format("<at module %s, line %s, char %s>", o.debugModule, o.debugLine, o.debugInLine);
	}

	/**
	 * Executes current instruction
	 * @param o current frame
	 * @return execution result
	 */
	private ExecutionResult executeSingleInstruction(FrameObject o) {
		int spc = o.pc;
		o.dataStream.position(spc);

		DebugInformation dd = o.compiled.getDebugInformation(spc);

		o.debugModule = dd.module;
		o.debugLine = dd.lineno;
		o.debugFunction = dd.function;
		o.debugInLine = dd.charno;
		
		Bytecode opcode = o.nextOpcode();
		Stack<PythonObject> stack = o.stack;
		
		if (o.accepts_return){
			o.accepts_return = false;
			if (returnee == null)
				pushOntoStack(o, NoneObject.NONE);
			else
				pushOntoStack(o, returnee);
		}
		
		if (TRACE_ENABLED)
			System.err.println(CompiledBlockObject.dis(o.compiled, true, spc) + " " + stack);
		
		switch (opcode){
		case NOP:
			// do nothing
			break;
		case RESOLVE_CLOSURE:
			UserFunctionObject fnc = (UserFunctionObject)stack.peek();
			fnc.setClosure(environment().toClosure());
			break;
		case OPEN_LOCALS:{
			// adds new dict to env as empty locals
				EnvironmentObject env = currentFrame.getLast().environment;
				env.pushLocals(new DictObject());
			} break;
		case PUSH_LOCALS:{
			// retrieves locals of this call and pushes them onto stack
				EnvironmentObject env = currentFrame.getLast().environment;
				pushOntoStack(o, env.getLocals());
			} break;
		case PUSH_ENVIRONMENT:
			// pushes new environment onto environment stack. 
			// also sets flag on the current frame to later pop the environment when frame itself is popped
			o.environment = new EnvironmentObject();
			if (currentClosure != null){
				o.environment.add(currentClosure);
				currentClosure = null;
			} else if (!PythonRuntime.runtime.buildingGlobals()){
				o.environment.add(PythonRuntime.runtime.getGlobals());
			}
			break;
		case CALL: {
			// calls the runnable with arguments
			// values are gathered from the stack in reverse order, with lowest being the callable itself.
			// if call(x)'s x is negative, it is vararg call, thus last argument must be a tuple that will be
			// expanded to fill the arguments
			int pbint = o.nextInt();
			int argl = pbint >= 0 ? pbint : -pbint;
			boolean va = false;
			if (pbint < 0)
				va = true;
			PythonObject[] args = new PythonObject[argl];
			
			for (int i=args.length-1; i>=0; i--)
				args[i] = stack.pop();
			PythonObject runnable = stack.pop();
			
			if (va){
				PythonObject[] va2 = args;
				PythonObject iterable = va2[va2.length-1];
				ListObject lo = (ListObject) PythonRuntime.LIST_TYPE.call(new TupleObject(iterable), null);
				
				
				args = new PythonObject[va2.length - 1 + lo.objects.size()];
				for (int i=0; i<va2.length - 1; i++)
					args[i] = va2[i];
				for (int i=0; i<lo.objects.size(); i++)
					args[i + va2.length - 1] = lo.objects.get(i);
			}
			
			returnee = execute(false, runnable, o.kwargs, args);
			o.kwargs = null;
			o.accepts_return = true;
			break;
		}
		case SETUP_LOOP:
			// Grabs object from stack. It it is something that can be iterated internally, pushes
			// iterator back there. Otherwise, calls iter method.
			PythonObject runnable;
			PythonObject value = stack.pop();
			int jv = o.nextInt();
			if (value instanceof InternallyIterable) {
				// TODO: Interface or something like that
				pushOntoStack(o, ((InternallyIterable)value).__iter__());
				o.pc = jv;
			} else {
				runnable = environment().get(new StringObject("iter"), true, false);				
				returnee = execute(true, runnable, null, value);
				o.accepts_return = true;
			}
			break;
		case ACCEPT_ITER:
			// Replaces frame left by ECALL with returnee value.
			// If StopIteration was raised, jumps to specified bytecode
			// Any other exception is rised again
			FrameObject frame = (FrameObject) stack.pop();
			jv = o.nextInt();
			if (frame.exception != null) {
				if (PythonRuntime.isinstance(frame.exception, PythonRuntime.STOP_ITERATION).truthValue()) {
					o.pc = jv;
					o.exception = frame.exception = null;
					break;
				}
				throw new PythonExecutionException(frame.exception);
			}
			if (returnee == null)
				pushOntoStack(o, NoneObject.NONE);
			else
				pushOntoStack(o, returnee);
			break;
		case GET_ITER:
			jv = o.nextInt();
			value = stack.peek();
			if (value instanceof InternalIterator) {
				value = ((InternalIterator)value).nextInternal();
				if (value == null) {
					// StopIteration is not actually thrown, only emulated
					o.pc = jv;
				} else {
					pushOntoStack(o, value);
					o.pc += 5; // ACCEPT_ITER _always_ follows GET_ITER and this one will skip it.
				}
				break;
			}
			// Note: Falls down to ECALL. This is not by mistake.
		case ECALL:
			// Leaves called method on top of stack
			// Pushes frame in which call was called
			runnable = stack.peek();
			try {
				returnee = execute(true, runnable, null);
				frame = currentFrame.peekLast();
				if (frame != o)
					frame.parentFrame = o;
				else
					pushOntoStack(o, o);
			} catch (PythonExecutionException e) {
				frame = new FrameObject();
				frame.parentFrame = o;
				frame.exception = e.getException();
				pushOntoStack(o, frame);
			}
			break;
		case KCALL:
		case RCALL: {
			// I have no idea what this shit is
			PythonObject[] args = new PythonObject[o.nextInt()];
			
			for (int i=0; i<args.length; i++)
				args[i] = stack.pop();
			runnable = stack.pop();
			
			returnee = execute(false, runnable, null, args);
			o.accepts_return = (opcode == Bytecode.RCALL);	// KCALL ignores returned value
			break;
		}
		case GOTO:
			// modifies the current pc to the value
			o.pc = o.nextInt();
			break;
		case JUMPIFFALSE:
			// modifies the current pc to the value if value on stack is false
			jv = o.nextInt();
			if (!stack.pop().truthValue())
				o.pc = jv;
			break;
		case JUMPIFTRUE:
			// modifies the current pc to the value if value on stack is true
			jv = o.nextInt();
			if (stack.pop().truthValue())
				o.pc = jv;
			break;
		case JUMPIFNONE:
			// Peeks, leaves value on stack
			jv = o.nextInt();
			if (stack.peek() == NoneObject.NONE)
				o.pc = jv;
			break;
		case JUMPIFNORETURN:
			jv = o.nextInt();
			frame = (FrameObject) stack.peek();
			if (!frame.returnHappened)
				// Frame ended without return, jump to specified label and keep frame on stack
				o.pc = jv;
			break;
		case LOAD: 
			// pushes variable onto stack
			String svl = ((StringObject)o.compiled.getConstant(o.nextInt())).value;
			value = environment().get(new StringObject(svl), false, false);
			if (value == null)
				throw Utils.throwException("NameError", "name " + svl + " is not defined");
			pushOntoStack(o, value);
			break;
		case LOADGLOBAL:
			// pushes global variable onto stack
			svl = ((StringObject)o.compiled.getConstant(o.nextInt())).value;
			value = environment().get(new StringObject(svl), true, false);
			if (value == null)
				throw Utils.throwException("NameError", "name " + svl + " is not defined");
			pushOntoStack(o, value);
			break;
		case POP:
			// pops value off the stack
			stack.pop();
			break;
		case TRUTH_VALUE:
			value = stack.pop();
			jv = o.nextInt();
			if (value instanceof NumberObject) {
				if (jv == 1)
					pushOntoStack(o, value.truthValue() ? BoolObject.FALSE : BoolObject.TRUE);
				else
					pushOntoStack(o, value.truthValue() ? BoolObject.TRUE : BoolObject.FALSE);
				break;
			} else if (value.fields.containsKey("__nonzero__")) {
				runnable = value.fields.get("__nonzero__").object;
				returnee = execute(true, runnable, null);
				o.accepts_return = true;
				if (jv == 1)
					o.pc-=5;
				break;
			} else if (value.fields.containsKey("__len__")) {
				runnable = value.fields.get("__len__").object;
				returnee = execute(true, runnable, null);
				o.accepts_return = true;
				o.pc-=5;
				break;
			} else {
				if (jv == 1)
					pushOntoStack(o, value.truthValue() ? BoolObject.FALSE : BoolObject.TRUE);
				else
					pushOntoStack(o, value.truthValue() ? BoolObject.TRUE : BoolObject.FALSE);
			}
			break;
		case PUSH:
			// pushes constant onto stack
			pushOntoStack(o, o.compiled.getConstant(o.nextInt()));
			break;
		case RETURN:
			// removes the frame and returns value
			if (o.ownedGenerator != null)
				if (!o.yielding)
					throw Utils.throwException("StopIteration");
			if (o.nextInt() == 1) {
				o.returnHappened = true;
				PythonObject retVal = stack.pop();
				returnee = retVal;
			}
			removeLastFrame();
			o.yielding = false;
			return ExecutionResult.EOF;
		case SAVE:
			// saves value into environment as variable
			environment().set(((StringObject)o.compiled.getConstant(o.nextInt())), stack.pop(), false, false);
			break;
		case KWARG:
			// stores kwargs using stored list of key names
			jv = o.nextInt();
			boolean doingNew = o.kwargs == null;
			if (doingNew)
				o.kwargs = new KwArgs.HashMapKWArgs();
			for (int i=0; i<jv; i++) {
				String key = o.compiled.getConstant(o.nextInt()).toString();
				if (!doingNew)
					if (o.kwargs.contains(key))
						throw Utils.throwException("TypeError", "got multiple values for keyword argument '" + key + "'");
				o.kwargs.put(key, stack.pop());
			}
			break;
		case SAVE_LOCAL:
			// saves the value exactly into locals (used by def and clas)
			environment().getLocals().backingMap.put(((StringObject)o.compiled.getConstant(o.nextInt())), stack.pop());
			break;
		case SAVEGLOBAL:
			// saves the value to the global variable
			environment().set(((StringObject)o.compiled.getConstant(o.nextInt())), stack.pop(), true, false);
			break;
		case DUP:
			// duplicates stack x amount of times
			jv = o.nextInt();
			if (jv == 0)
				pushOntoStack(o, stack.peek());
			else
				pushOntoStack(o, stack.get(stack.size() - 1 - jv));
			break;
		case IMPORT:
			// import bytecode
			String s1 = ((StringObject)o.compiled.getConstant(o.nextInt())).value;
			String s2 = ((StringObject)o.compiled.getConstant(o.nextInt())).value;
			pythonImport(environment(), s1, s2, null);
			break;
		case SWAP_STACK: {
			// swaps head of the stack with value below it
			PythonObject top = stack.pop();
			PythonObject bot = stack.pop();
			pushOntoStack(o, top);
			pushOntoStack(o, bot);
			break;
		}
		case UNPACK_SEQUENCE:
			// unpacks sequence onto stack
			int cfc = currentFrame.size();
			PythonObject seq = stack.pop();
			int count = o.nextInt();
			
			ListObject lo = (ListObject) PythonRuntime.LIST_TYPE.call(new TupleObject(seq), null);
			if (lo.objects.size() > count)
				throw Utils.throwException("TypeError", "too many values to unpack");
			if (lo.objects.size() < count)
				throw Utils.throwException("TypeError", "too few values to unpack");
			
			Collections.reverse(lo.objects);
			for (PythonObject obj : lo.objects)
				pushOntoStack(o, obj);
			
			break;
		case UNPACK_KWARG: {
			value = stack.pop();
			PythonObject keysFn = value.get("keys", null);
			PythonObject getItemFn = value.get("__getitem__", null);
			if ((keysFn == null) || (getItemFn == null))
				Utils.throwException("TypeError", "argument after ** must be a mapping, not " + value.toString());
			PythonObject iterator = Utils.run("iter", execute(true, keysFn, null)).get(GeneratorObject.NEXT, null);
			if (o.kwargs == null)
				o.kwargs = new KwArgs.HashMapKWArgs();
			try {
				while (true) {
					PythonObject key = execute(true, iterator, null);
					returnee = execute(true, getItemFn, null, key);
					o.kwargs.put(key.toString(), returnee);
				}
			} catch (PythonExecutionException e){
				if (PythonRuntime.isinstance(e.getException(), PythonRuntime.STOP_ITERATION).truthValue()){
					; // nothing
				} else
					throw e;
			}
			break;
		}
		case PUSH_LOCAL_CONTEXT:
			// pushes value from stack into currentContex and makrs the push into frame
			o.localContext = stack.pop();
			break;
		case RESOLVE_ARGS:
			// resolves args into locals
			synchronized (this.args.backingMap){
				for (PythonProxy key : this.args.backingMap.keySet()){
					environment().getLocals().backingMap.put((StringObject) key.o, this.args.backingMap.get(key));
				}
			}
			break;
		case GETATTR: {
			try {
				PythonObject apo;
				StringObject field = (StringObject) o.compiled.getConstant(o.nextInt());
				value = stack.pop();	// object to get attribute from
				apo = value.get("__getattribute__", getLocalContext()); 
				if (apo != null && !(value instanceof ClassObject)) {
					// There is __getattribute__ defined, call it directly
					returnee = execute(false, apo, null, field);
					o.accepts_return = true;
					break;
				} else {
					// Try to grab argument normally...
					apo = value.get(field.value, getLocalContext());
					if (apo != null) {
						returnee = apo;
						o.accepts_return = true;
						break;
					}				
					// ... and if that fails, use __getattr__ if available
					apo = value.get("__getattr__", getLocalContext()); 
					if (apo != null) {
						// There is __getattribute__ defined, call it directly
						returnee = execute(false, apo, null, field);
						o.accepts_return = true;
						break;
					}
					throw Utils.throwException("AttributeError", "" + value.getType() + " object has no attribute '" + field + "'");
				}
			} finally {
				if (returnee instanceof PropertyObject){
					returnee = ((PropertyObject)returnee).get();
				}
			}
		}
		case DELATTR:{
			runnable = environment().getBuiltin("delattr");
			PythonObject[] args = new PythonObject[2];
			args[1] = new StringObject(((StringObject)o.compiled.getConstant(o.nextInt())).value);	// attribute
			args[0] = stack.pop();																	// object
			returnee = execute(false, runnable, null, args);
		} break;
		case SETATTR: {
			runnable = environment().getBuiltin("setattr");
			PythonObject[] args = new PythonObject[3];
			// If argument for SETATTR is not set, attribute name is pop()ed from stack   
			PythonObject po = o.compiled.getConstant(o.nextInt());
			if (po == NoneObject.NONE) {
				args[1] = stack.pop();	// attribute
				args[0] = stack.pop();	// object
				args[2] = stack.pop();	// value
			} else {
				args[1] = new StringObject(((StringObject)po).value);	// attribute
				args[0] = stack.pop();									// object
				args[2] = stack.pop();									// value
			} 
			returnee = execute(false, runnable, null, args);
			break;
		}
		case ISINSTANCE:
			// something stupid
			PythonObject type = stack.pop();
			value = stack.peek();
			pushOntoStack(o, BoolObject.fromBoolean(PythonRuntime.doIsInstance(value, type, true)));
			break;
		case RAISE: {
			// raises python exception
			PythonObject s = Utils.peek(stack);
			if (s == null)
				throw Utils.throwException("TypeError", "no exception is being handled but raise called");
			else if (PythonRuntime.isinstance(s, PythonRuntime.ERROR).truthValue()) {
				// Throw exception normally
				throw new PythonExecutionException(s);
			} else  if (PythonRuntime.isderived(s, PythonRuntime.ERROR)) {
				// Throw new exception instance
				s = ((ClassObject)s).call(TupleObject.EMPTY, KwArgs.EMPTY);
				throw new PythonExecutionException(s);
			} else
				throw Utils.throwException("TypeError", "exceptions must be Error instance or class derived from Error, not " + s.toString());
		}
		case RERAISE: {
			PythonObject s = stack.pop();
			if (s != NoneObject.NONE)
				throw new PythonExecutionException(s);
			break;
		}
		case PUSH_FRAME:
			// inserts new subframe onto frame stack
			o.accepts_return = true;
			FrameObject nf = new FrameObject();
			nf.newObject();
			nf.parentFrame = o;
			nf.compiled = o.compiled;
			nf.localContext = o.localContext;
			nf.environment = o.environment;
			nf.dataStream = ByteBuffer.wrap(nf.compiled.getBytedata());
			nf.pc = o.nextInt();
			currentFrame.add(nf);
			break;
		case PUSH_EXCEPTION:
			// who has any idea what this shit does call 1-555-1337
			frame = (FrameObject) stack.peek();
			if (frame.exception == null)
				pushOntoStack(o, NoneObject.NONE);
			else
				pushOntoStack(o, frame.exception);
			break;
		case YIELD:
			String name = ((StringObject) o.compiled.getConstant(o.nextInt())).value;
			if (o.ownedGenerator == null){
				List<FrameObject> ol = new ArrayList<FrameObject>();
				FrameObject oo = o;
				while (oo != null){
					ol.add(oo.cloneFrame());
					oo = oo.parentFrame;
				}
				for (int i=0; i<ol.size(); i++)
					if (i != ol.size()-1)
						ol.get(i).parentFrame = ol.get(i+1);
				ol.get(0).pc -= 5;
				Collections.reverse(ol);
				GeneratorObject generator = new GeneratorObject(name, ol);
				generator.newObject();
				for (FrameObject fr : ol)
					fr.ownedGenerator = generator;
				returnee = generator;
				o.returnHappened = true;
				o.yielding = true;
				removeLastFrame();
				return ExecutionResult.EOF;
			} else {
				PythonObject sentValue = o.sendValue;
				o.sendValue = null;
				o.returnHappened = true;
				PythonObject retVal = stack.pop();
				returnee = retVal;	
				pushOntoStack(o, sentValue);
				
				GeneratorObject generator = o.ownedGenerator;
				List<FrameObject> ol = new ArrayList<FrameObject>();
				FrameObject oo = o;
				while (oo != null){
					ol.add(oo.cloneFrame());
					oo = oo.parentFrame;
				}
				Collections.reverse(ol);
				generator.storedFrames = ol;
				for (FrameObject fr : ol)
					fr.ownedGenerator = generator;
				
				o.returnHappened = true;
				o.yielding = true;
				removeLastFrame();
				return ExecutionResult.EOF;
			}
		case LOADDYNAMIC:{
			value = null;
			boolean found = false;
			StringObject variable = (StringObject) o.compiled.getConstant(o.nextInt());
			for (int i=currentFrame.size()-1; i>=0; i--){
				FrameObject oo = currentFrame.get(i);
				DictObject locals = oo.environment.getLocals();
				if (locals.contains(variable.value)){
					pushOntoStack(o, locals.doGet(variable));
					found = true;
					break;
				}
			}
			if (!found)
				throw Utils.throwException("NameError", "dynamic variable '" + variable.value + "' is undefined");
		} break;
		case SAVEDYNAMIC: {
			value = stack.pop();
			StringObject variable = (StringObject) o.compiled.getConstant(o.nextInt());
			boolean found = false;
			for (int i=currentFrame.size()-1; i>=0; i--){
				FrameObject oo = currentFrame.get(i);
				DictObject locals = oo.environment.getLocals();
				if (locals.contains(variable.value)){
					locals.backingMap.put(variable, value);
					found = true;
					break;
				}
			}
			if (!found){
				DictObject locals = o.environment.getLocals();
				locals.backingMap.put(variable, value);
			}
		} break;
		case LOADBUILTIN:{
			String vname = ((StringObject) o.compiled.getConstant(o.nextInt())).value;
			value = environment().getBuiltin(vname);
			if (value == null)
				throw Utils.throwException("NameError", "builtin name '" + vname + "' is undefined");
			pushOntoStack(o, value);
		} break;
		case DEL: {
			StringObject vname = (StringObject) o.compiled.getConstant(o.nextInt());
			boolean isGlobal = o.nextInt() == 1;
			environment().delete(vname, isGlobal);
		} break;
		default:
			Utils.throwException("InterpretError", "unhandled bytecode " + opcode.toString());
		}
			
		return ExecutionResult.OK;
	}

	private void pushOntoStack(FrameObject o, PythonObject value) {
		o.stack.push(value);
		lastPushedValue = value;
	}

	/**
	 * Removes last frame from frame stack
	 */
	private void removeLastFrame() {
		FrameObject o = this.currentFrame.removeLast();
		if (o.parentFrame != null){
			o.parentFrame.returnHappened = o.returnHappened;
			o.parentFrame.yielding = o.yielding;
			o.parentFrame.sendValue = o.sendValue;
			o.yielding = false;
			o.parentFrame.stack.add(o);
		} else {
			if (currentFrame.size() == 0) {
				if (o.exception != null) {
					if (o.exception.get("__exception__", null) != null)
						throw new PythonExecutionException(o.exception, (Throwable)((PointerObject)o.exception.get("__exception__", null)).getObject());
					throw new PythonExecutionException(o.exception);
				}
			} else
				currentFrame.peekLast().exception = o.exception;
		}
	}

	/**
	 * Imports based on the values on the Import bytecode
	 * @param environment
	 * @param variable
	 * @param modulePath
	 * @param target
	 */
	private void pythonImport(EnvironmentObject environment, String variable,
			String modulePath, PythonObject target) {
		if (modulePath == null || modulePath.equals("")){
			if (target == null){
				synchronized (PythonRuntime.runtime){
					target = PythonRuntime.runtime.getModule(variable, null);
				}
			} else if (!variable.equals("*")){
				environment.set(new StringObject(variable), 
						target,
						true, false);
			} else {
				DictObject dict = (DictObject) target.fields.get(ModuleObject.__DICT__).object;
				synchronized (dict){
					synchronized (dict.backingMap){
						for (PythonProxy key : dict.backingMap.keySet()){
							if (key.o instanceof StringObject){
								String kkey = ((StringObject)key.o).value;
								if (!kkey.startsWith("__"))
									environment.set((StringObject)key.o, 
											dict.backingMap.get(key),
											true, false);
							}
						}
					}
				}
				
			}
		} else {
			String[] split = modulePath.split("\\.");
			String mm = split[0];
			modulePath = modulePath.replaceFirst(mm, "");
			modulePath = modulePath.replaceFirst("\\.", "");
			if (target == null){
				target = environment.get(new StringObject(mm), false, false);
				if (target == null || !(target instanceof ModuleObject)){
					ModuleObject thisModule = (ModuleObject) environment.get(new StringObject("__thismodule__"), true, false);
					target = null;
					synchronized (PythonRuntime.runtime){
						String resolvePath = thisModule == null ? null : thisModule.provider.getPackageResolve();
						if (resolvePath == null)
							target = PythonRuntime.runtime.root.get(mm) != null ? PythonRuntime.runtime.root.get(mm).module : null;
						if (target == null)
							target = PythonRuntime.runtime.getModule(mm, resolvePath == null ? null : new StringObject(resolvePath));
					}
				}
			} else {
				if (target instanceof ModuleObject){
					ModuleObject mod = (ModuleObject)target;
					PythonObject target2 = ((DictObject)mod.fields.get(ModuleObject.__DICT__).object).doGet(mm);
					if (target2 != null){
						pythonImport(environment, variable, modulePath, target2);
						return;
					}
				} 
				if (target.fields.containsKey(mm)) {
					pythonImport(environment, variable, modulePath, target.fields.get(mm).object);
					return;
				} else {
					target = PythonRuntime.runtime.getModule(mm, new StringObject(((ModuleObject)target).provider.getPackageResolve()));
				}
			}
			pythonImport(environment, variable, modulePath, target);
		}
	}

	@Override
	public boolean truthValue() {
		return true;
	}

	@Override
	protected String doToString() {
		return "<bound python interpret for thread 0x" + Long.toHexString(Thread.currentThread().getId()) + ">";
	}

	/**
	 * Sets the arguments for the next RESOLVE_ARGS call
	 * @param a
	 */
	public void setArgs(DictObject a) {
		args = a;
	}

	/**
	 * Returns current frame
	 * @return
	 */
	public FrameObject frame() {
		return currentFrame.peekLast();
	}

	/**
	 * Executes bytecode until cfc equals the number of frames. Used to fully finish execution of newly pushed stack
	 * @param cfc
	 * @param pushStack 
	 * @return
	 */
	public PythonObject executeAll(int cfc) {
		return executeAll(cfc, false);
	}
	
	public PythonObject executeAll(int cfc, boolean getRemainingStack) {
		if (cfc == currentFrame.size())
			return returnee;
		while (true){
			FrameObject o = currentFrame.getLast();
			ExecutionResult res = executeOnce();
			if (res == ExecutionResult.INTERRUPTED)
				return null;
			if (res == ExecutionResult.FINISHED || res == ExecutionResult.EOF)
				if (currentFrame.size() == cfc){
					if (exception() != null){
						PythonObject e = exception();
						currentFrame.peekLast().exception = null;
						throw new PythonExecutionException(e);
					}
					if (getRemainingStack)
						if (o.stack.size() == 1)
							return o.stack.pop();
					return returnee;
				}
		}
	}

	public int getAccessCount() {
		return accessCount;
	}

	public void setClosure(List<DictObject> closure) {
		this.currentClosure = closure;
	}
}
