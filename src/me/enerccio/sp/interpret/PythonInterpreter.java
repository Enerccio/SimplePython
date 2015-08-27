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
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import me.enerccio.sp.compiler.Bytecode;
import me.enerccio.sp.errors.AttributeError;
import me.enerccio.sp.errors.BasePythonError;
import me.enerccio.sp.errors.InterpreterError;
import me.enerccio.sp.errors.NameError;
import me.enerccio.sp.errors.PythonException;
import me.enerccio.sp.errors.RuntimeError;
import me.enerccio.sp.errors.StopIteration;
import me.enerccio.sp.errors.TypeError;
import me.enerccio.sp.interpret.CompiledBlockObject.DebugInformation;
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.types.ModuleObject;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.BoolObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.base.NumberObject;
import me.enerccio.sp.types.callables.CallableObject;
import me.enerccio.sp.types.callables.ClassObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.types.callables.UserFunctionObject;
import me.enerccio.sp.types.callables.UserMethodObject;
import me.enerccio.sp.types.iterators.GeneratorObject;
import me.enerccio.sp.types.iterators.InternalIterator;
import me.enerccio.sp.types.iterators.InternallyIterable;
import me.enerccio.sp.types.mappings.StringDictObject;
import me.enerccio.sp.types.pointer.PointerObject;
import me.enerccio.sp.types.properties.PropertyObject;
import me.enerccio.sp.types.sequences.ListObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.types.system.FutureObject;
import me.enerccio.sp.types.system.PythonFutureObject;
import me.enerccio.sp.utils.CastFailedException;
import me.enerccio.sp.utils.Coerce;
import me.enerccio.sp.utils.Utils;

@SuppressWarnings("unused")
/**
 * PythonInterpret. Interprets bytecode. One per thread and gets automatically constructed the moment something wants to access it in a thread.
 * @author Enerccio
 *
 */
public class PythonInterpreter extends PythonObject {
	private static final long serialVersionUID = -8039667108607710165L;
	public static final boolean TRACE_ENABLED = System
			.getenv("SPY_TRACE_ENABLED") != null;
	public static final int MAX_DEEP_STACK;

	static {
		if (System.getenv("DEEP_STACK_LIMIT") != null)
			MAX_DEEP_STACK = Integer
					.parseInt(System.getenv("DEEP_STACK_LIMIT"));
		else
			MAX_DEEP_STACK = -1;
	}

	private boolean handlingOverflow = false;

	public void checkOverflow() {
		if (MAX_DEEP_STACK > 0) {
			if (currentFrame.size() > MAX_DEEP_STACK && !handlingOverflow) {
				handlingOverflow = true;
				throw new RuntimeError("maximum recursion depth exceeded");
			} else if (currentFrame.size() < MAX_DEEP_STACK - 1) {
				handlingOverflow = false;
			}
		}
	}

	/** Thread local accessor to the interpret */
	public static final transient ThreadLocal<PythonInterpreter> interpreter = new ThreadLocal<PythonInterpreter>() {

		@Override
		protected PythonInterpreter initialValue() {
			try {
				PythonRuntime.runtime.waitForNewInterpretAvailability();
			} catch (InterruptedException e) {

			}

			PythonInterpreter i = new PythonInterpreter();
			interpreters.add(i);
			return i;
		}

	};

	/** Collection of all interprets created */
	public static final Set<PythonInterpreter> interpreters = Collections
			.synchronizedSet(new HashSet<PythonInterpreter>());
	public static final Map<Thread, PythonInterpreter> interpreterMap = Collections
			.synchronizedMap(new HashMap<Thread, PythonInterpreter>());

	public PythonInterpreter() {
		super(true);
		bind(Thread.currentThread());
	}

	private Thread currentOwnerThread;
	/**
	 * Binds the interpret to this thread
	 */
	public void bind(Thread t) {
		interpreterMap.remove(t);
		currentOwnerThread = t;
		interpreter.set(this);
		interpreterMap.put(t, this);
	}

	private EnvironmentObject nullEnv = new EnvironmentObject();
	{
		nullEnv.add(PythonRuntime.runtime.getGlobals());
	}

	/**
	 * current frame stack. Topmost element represents currently interpreted
	 * frame
	 */
	public LinkedList<FrameObject> currentFrame = new LinkedList<FrameObject>();
	/**
	 * Number of times this interpret is accessed by itself. If >0, interpret
	 * can't be serialized
	 */
	private volatile int accessCount = 0;
	/** Represents currrently passed arguments to the function */
	private InternalDict args = null;
	/** Represents value returned by a call */
	private PythonObject returnee;
	private List<InternalDict> currentClosure;

	private final InterpreterMathExecutorHelper mathHelper = new InterpreterMathExecutorHelper();

	/**
	 * whether this interpret can be stopped at safe place or not
	 * 
	 * @return
	 */
	public boolean isInterpretStoppable() {
		return accessCount == 0;
	}

	/**
	 * executes single call identified by function in current environment and
	 * returns value, if any
	 * 
	 * @param internal
	 * @param function
	 * @param data
	 * @return
	 */
	public PythonObject executeCall(boolean internal, String function,
			PythonObject... data) {
		if (currentFrame.size() == 0)
			return returnee = execute(internal, PythonRuntime.runtime
					.getGlobals().doGet(function), null, data);
		return returnee = execute(internal, environment().getBuiltin(function),
				null, data);
	}

	/**
	 * Returns current environment or null
	 * 
	 * @return
	 */
	public EnvironmentObject environment() {
		if (currentFrame.size() == 0)
			return nullEnv;
		return currentFrame.getLast().environment;
	}

	/**
	 * Returns current local context or null
	 * 
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
	 * 
	 * @param internalCall
	 *            if true and call is to a python code, it will wait until that
	 *            code will finish
	 * @param callable
	 *            callable
	 * @param args
	 *            arguments
	 * @return
	 */
	public PythonObject execute(boolean internalCall, PythonObject callable,
			KwArgs kwargs, PythonObject... args) {
		if (callable instanceof CallableObject) {
			if (((callable instanceof UserFunctionObject) || (callable instanceof UserMethodObject))
					&& internalCall) {
				int cfc = currentFrame.size();
				((CallableObject) callable).call(new TupleObject(true, args),
						kwargs);
				return executeAll(cfc);
			} else if (internalCall) {
				int cfc = currentFrame.size();
				returnee = ((CallableObject) callable).call(new TupleObject(
						true, args), kwargs);
				if (cfc < currentFrame.size()) {
					returnee = executeAll(cfc);
				}
				return returnee;
			} else {
				return returnee = ((CallableObject) callable).call(
						new TupleObject(true, args), kwargs);
			}
		} else {
			PythonObject callableArg = callable.get(CallableObject.__CALL__,
					getLocalContext());
			if (callableArg == null)
				throw new TypeError(callable.toString() + " is not callable");
			return returnee = execute(internalCall, callableArg, kwargs, args);
		}
	}

	/**
	 * Returns exception of the topmost frame or null if either no frame is
	 * there or no exception has happened
	 * 
	 * @return
	 */
	public PythonObject exception() {
		if (currentFrame.size() != 0)
			return currentFrame.peekLast().exception;
		return null;
	}

	/**
	 * invokes callable with args
	 * 
	 * @param callable
	 * @param args
	 * @return
	 */
	public PythonObject invoke(PythonObject callable, KwArgs kwargs,
			TupleObject args) {
		return execute(false, callable, kwargs, args.getObjects());
	}

	/**
	 * returns global value for variable key or null
	 * 
	 * @param key
	 * @return
	 */
	public PythonObject getGlobal(String key) {
		return environment().get(key, true, false);
	}

	/**
	 * Pushes new frame on the stack that contains this bytecode.
	 * 
	 * @param frame
	 */
	public void executeBytecode(CompiledBlockObject frame) {
		checkOverflow();

		FrameObject n;
		currentFrame.add(n = new FrameObject());
		n.compiled = frame;
		n.dataStream = frame.getBytedataAsNativeBuffer();

		// System.out.println(CompiledBlockObject.dis(frame));
	}

	/**
	 * Executes single instruction
	 * 
	 * @return
	 */
	public ExecutionResult executeOnce() {
		ExecutionResult r = doExecuteOnce();
		if (r == ExecutionResult.EOF)
			if (currentFrame.size() == 0)
				return ExecutionResult.FINISHED;
		return r;
	}
	
	private volatile boolean askedForInterrupt; 
	private AtomicBoolean waitingForSetup = new AtomicBoolean(false);
	private Semaphore setupDone = new Semaphore(0);
	private Lock interruptInProgress = new ReentrantLock();
	
	public void interruptInterpret(PythonInterpreter i, CallableObject co, TupleObject args, KwArgs kwargs){
		i.interruptInProgress.lock();
		i.askedForInterrupt = true;
		
		PythonInterpreter ci = interpreter.get();
		
		if (ci.equals(i))
			throw new TypeError("can't signal itself");
		
		Thread t = i.getThread();
		try {
			i.bind(Thread.currentThread());
			i.doInstallSignal(co, args, kwargs);
		} finally {
			i.bind(t);
			ci.bind(Thread.currentThread());
			synchronized (i){
				i.askedForInterrupt = false;
				i.setupDone.release();
				i.interruptInProgress.unlock();
			}
		}
	}
	
	private void waitUntilSetupFinishes() {
		try {
			parkSafe();
		} catch (InterruptedException e){
			
		}
	}

	private void parkSafe() throws InterruptedException {
		setupDone.acquire();
	}

	private Thread getThread() {
		return currentOwnerThread;
	}

	private void doInstallSignal(CallableObject co, TupleObject args,
			KwArgs kwargs) {
		FrameObject o = currentFrame.getLast();
		o.storedReturnee = returnee;
		try {
			execute(false, co, kwargs, args.getObjects());
		} catch (Throwable t){
			FrameObject o2 = currentFrame.getLast();
			if (o2.equals(o)){
				// no new frame, restore immediately
				returnee = o.storedReturnee;
			}
			throw t;
		}
		FrameObject o2 = currentFrame.getLast();
		if (o2.equals(o)){
			// no new frame, restore immediately
			returnee = o.storedReturnee;
		} else {
			o2.isSignal = true;
		}
	}

	private ExecutionResult doExecuteOnce() {
		if (askedForInterrupt){
			waitUntilSetupFinishes();
		}
		
		try {
			PythonRuntime.runtime.waitIfSaving(this);
		} catch (InterruptedException e) {
			return ExecutionResult.INTERRUPTED;
		}

		if (Thread.interrupted()) {
			return ExecutionResult.INTERRUPTED;
		}

		if (exception() != null) {
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
			if (o.pc >= o.compiled.getBytedata().length) {
				removeLastFrame();
				returnee = null;
				return ExecutionResult.EOF;
			}
			try {
				try {
					return executeSingleInstruction(o);
				} catch (BasePythonError e) {
					throw Utils.throwException(e.type, e.message, e);
				}
			} catch (PythonExecutionException e) {
				handleException(e);
				return ExecutionResult.EOF;
			}
		} finally {
			--accessCount;
		}
	}

	/**
	 * Handles the exception chain.
	 * 
	 * @param e
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void handleException(PythonExecutionException e) {
		PythonObject pe = e.getException();
		currentFrame.peekLast().exception = pe;
		PythonObject stack = pe.get("stack", null);
		if (stack instanceof ListObject && !e.noStackGeneration) {
			ListObject s = (ListObject) stack;
			s.objects.add(makeStack());
			if (e.getCause() instanceof PythonException) {
				((PythonException) e.getCause())
						.addPythonStack((List) s.objects);
			}
		}
		removeLastFrame();
	}

	/**
	 * Inserts current instruction into stack
	 * 
	 * @return
	 */
	private StackElement makeStack() {
		FrameObject o = currentFrame.getLast();
		if (o == null)
			return StackElement.LAST_FRAME;
		DebugInformation dd = o.compiled.getDebugInformation(o.prevPc);
		if (dd.lineno < 0)
			return StackElement.SYSTEM_FRAME;
		return new StackElement(dd.module, dd.function, dd.lineno, dd.charno);
	}

	/**
	 * Creates new stack line element of the current stack
	 * 
	 * @return
	 */
	private String makeStackString() {
		FrameObject o = currentFrame.getLast();
		if (o == null)
			return "<last frame>";
		DebugInformation dd = o.compiled.getDebugInformation(o.prevPc);
		if (dd.lineno < 0)
			return "<system-frame>";
		return String.format("<at module %s, line %s, char %s>", dd.module,
				dd.lineno, dd.charno);
	}

	/**
	 * Executes current instruction
	 * 
	 * @param o
	 *            current frame
	 * @return execution result
	 */
	private ExecutionResult executeSingleInstruction(FrameObject o) {
		int spc = o.pc;
		o.dataStream.position(spc);
		o.prevPc = spc;
		Bytecode opcode = o.nextOpcode();
		Stack<PythonObject> stack = o.stack;

		if (o.accepts_return) {
			o.accepts_return = false;
			if (returnee == null)
				stack.push(NoneObject.NONE);
			else
				stack.push(returnee);
		}

		if (TRACE_ENABLED)
			System.err.println(CompiledBlockObject.dis(o.compiled, true, spc)
					+ " " + printStack(stack));

		switch (opcode) {
		case NOP:
			// do nothing
			break;
		case RESOLVE_CLOSURE:
			resolveClosure(o, stack);
			break;
		case OPEN_LOCALS:
			openLocals(o, stack);
			break;
		case PUSH_LOCALS:
			// retrieves locals of this call and pushes them onto stack
			pushLocals(o, stack);
			break;
		case PUSH_ENVIRONMENT:
			// pushes new environment onto environment stack.
			// also sets flag on the current frame to later pop the environment
			// when frame itself is popped
			pushEnvironment(o, stack);
			break;
		case CALL:
			// calls the runnable with arguments
			// values are gathered from the stack in reverse order, with lowest
			// being the callable itself.
			// if call(x)'s x is negative, it is vararg call, thus last argument
			// must be a tuple that will be
			// expanded to fill the arguments
			call(o, stack);
			break;
		case SETUP_LOOP:
			// Grabs object from stack. It it is something that can be iterated
			// internally, pushes
			// iterator back there. Otherwise, calls iter method.
			setupLoop(o, stack);
			break;
		case ACCEPT_ITER:
			// Replaces frame left by ECALL with returnee value.
			// If StopIteration was raised, jumps to specified bytecode
			// Any other exception is rised again
			acceptIter(o, stack);
			break;
		case GET_ITER:
			if (getIter(o, stack))
				break;
			// Note: Falls down to ECALL. This is not by mistake.
		case ECALL:
			// Leaves called method on top of stack
			// Pushes frame in which call was called
			ecall(o, stack);
			break;
		case KCALL:
		case RCALL:
			krcall(opcode, o, stack);
			break;
		case GOTO:
			gotoOperation(o, stack);
			break;
		case JUMPIFFALSE:
			jumpIfFalse(o, stack);
			break;
		case JUMPIFTRUE:
			jumpIfTrue(o, stack);
			break;
		case JUMPIFNONE:
			jumpIfNone(o, stack);
			break;
		case JUMPIFNORETURN:
			jumpIfNoReturn(o, stack);
			break;
		case MAKE_FUTURE:
			makeFuture(o, stack);
			break;
		case LOAD:
			load(o, stack);
			break;
		case LOAD_FUTURE:
			// pushes variable onto stack. If variable is future, it is not
			// resolved
			loadFuture(o, stack);
			break;
		case TEST_FUTURE:
			testFuture(o, stack);
			break;
		case LOADGLOBAL:
			loadGlobal(o, stack);
			break;
		case POP:
			pop(o, stack);
			break;
		case TRUTH_VALUE:
			truthValue(o, stack);
			break;
		case PUSH:
			push(o, stack);
			break;
		case RETURN:
			returnOperation(o, stack);
			return ExecutionResult.EOF;
		case SAVE:
			save(o, stack);
			break;
		case KWARG:
			kwarg(o, stack);
			break;
		case SAVE_LOCAL:
			saveLocal(o, stack);
			break;
		case SAVEGLOBAL:
			saveGlobal(o, stack);
			break;
		case DUP:
			dup(o, stack);
			break;
		case IMPORT:
			importOperation(o, stack);
			break;
		case SWAP_STACK:
			swapStack(o, stack);
			break;
		case MAKE_FIRST:
			makeFirst(o, stack);
			break;
		case UNPACK_SEQUENCE:
			unpackSequence(o, stack);
			break;
		case UNPACK_KWARG:
			unpackKwargs(o, stack);
			break;
		case PUSH_LOCAL_CONTEXT:
			pushLocalContext(o, stack);
			break;
		case RESOLVE_ARGS:
			resolveArgs(o, stack);
			break;
		case GETATTR:
			getAttr(o, stack);
			break;
		case DELATTR:
			delAttr(o, stack);
			break;
		case SETATTR:
			setAttr(o, stack);
			break;
		case ISINSTANCE:
			isinstance(o, stack);
			break;
		case RAISE:
			raise(o, stack);
			break;
		case RERAISE:
			reraise(o, stack);
			break;
		case PUSH_FRAME:
			pushFrame(o, stack);
			break;
		case PUSH_EXCEPTION:
			pushException(o, stack);
			break;
		case YIELD:
			return yield(o, stack);
		case LOADDYNAMIC:
			loadDynamic(o, stack);
			break;
		case SAVEDYNAMIC:
			saveDynamic(o, stack);
			break;
		case LOADBUILTIN:
			loadBuiltin(o, stack);
			break;
		case DEL:
			del(o, stack);
			break;
		default:
			if (!mathHelper.mathOperation(this, o, stack, opcode))
				throw new InterpreterError("unhandled bytecode "
						+ opcode.toString());
		}

		return ExecutionResult.OK;
	}

	private void resolveClosure(FrameObject o, Stack<PythonObject> stack) {
		UserFunctionObject fnc = (UserFunctionObject) stack.peek();
		fnc.setClosure(environment().toClosure());
	}

	private void openLocals(FrameObject o, Stack<PythonObject> stack) {
		// adds new dict to env as empty locals
		EnvironmentObject env = currentFrame.getLast().environment;
		env.pushLocals(new StringDictObject());
	}

	private void pushLocals(FrameObject o, Stack<PythonObject> stack) {
		EnvironmentObject env = currentFrame.getLast().environment;
		InternalDict locals = env.getLocals();
		stack.push((PythonObject) locals);
	}

	private void pushEnvironment(FrameObject o, Stack<PythonObject> stack) {
		o.environment = new EnvironmentObject();
		if (currentClosure != null) {
			o.environment.add(currentClosure);
			currentClosure = null;
		} else if (!PythonRuntime.runtime.buildingGlobals()) {
			o.environment.add(PythonRuntime.runtime.getGlobals());
		}
	}

	private void call(FrameObject o, Stack<PythonObject> stack) {
		int pbint = o.nextInt();
		int argl = pbint >= 0 ? pbint : -pbint;
		boolean va = false;
		if (pbint < 0)
			va = true;
		PythonObject[] args = new PythonObject[argl];

		for (int i = args.length - 1; i >= 0; i--)
			args[i] = stack.pop();
		PythonObject runnable = stack.pop();

		if (va) {
			PythonObject[] va2 = args;
			PythonObject iterable = va2[va2.length - 1];
			ListObject lo = (ListObject) PythonRuntime.LIST_TYPE.call(
					new TupleObject(true, iterable), null);

			args = new PythonObject[va2.length - 1 + lo.objects.size()];
			for (int i = 0; i < va2.length - 1; i++)
				args[i] = va2[i];
			for (int i = 0; i < lo.objects.size(); i++)
				args[i + va2.length - 1] = lo.objects.get(i);
		}

		returnee = execute(false, runnable, o.kwargs, args);
		o.kwargs = null;
		o.accepts_return = true;
	}

	private void setupLoop(FrameObject o, Stack<PythonObject> stack) {
		PythonObject runnable;
		PythonObject value = stack.pop();
		int jv = o.nextInt();
		if (value instanceof InternallyIterable) {
			// TODO: Interface or something like that
			stack.push(((InternallyIterable) value).__iter__());
			o.pc = jv;
		} else {
			runnable = environment().get("iter", true, false);
			returnee = execute(true, runnable, null, value);
			o.accepts_return = true;
		}
	}

	private void acceptIter(FrameObject o, Stack<PythonObject> stack) {
		FrameObject frame = (FrameObject) stack.pop();
		int jv = o.nextInt();
		if (frame.exception != null) {
			if (PythonRuntime.isinstance(frame.exception,
					PythonRuntime.STOP_ITERATION).truthValue()) {
				o.pc = jv;
				o.exception = frame.exception = null;
				return;
			}
			throw new PythonExecutionException(frame.exception);
		}
		if (returnee == null)
			stack.push(NoneObject.NONE);
		else
			stack.push(returnee);
	}

	private boolean getIter(FrameObject o, Stack<PythonObject> stack) {
		int jv = o.nextInt();
		PythonObject value = stack.peek();
		if (value instanceof InternalIterator) {
			value = ((InternalIterator) value).nextInternal();
			if (value == null) {
				// StopIteration is not actually thrown, only emulated
				o.pc = jv;
			} else {
				o.stack.push(value);
				o.pc += 5; // ACCEPT_ITER _always_ follows GET_ITER and this one
							// will skip it.
			}
			return true;
		}
		return false;
	}

	private void ecall(FrameObject o, Stack<PythonObject> stack) {
		checkOverflow();

		PythonObject runnable = stack.peek();
		FrameObject frame;
		try {
			returnee = execute(true, runnable, null);
			frame = currentFrame.peekLast();
			if (frame != o)
				frame.parentFrame = o;
			else
				stack.push(o);
		} catch (PythonExecutionException e) {
			frame = new FrameObject();
			frame.parentFrame = o;
			frame.exception = e.getException();
			stack.push(frame);
		}
	}

	private void krcall(Bytecode opcode, FrameObject o,
			Stack<PythonObject> stack) {
		PythonObject[] args = new PythonObject[o.nextInt()];

		for (int i = 0; i < args.length; i++)
			args[i] = stack.pop();
		PythonObject runnable = stack.pop();

		returnee = execute(false, runnable, null, args);
		o.accepts_return = (opcode == Bytecode.RCALL); // KCALL ignores returned
														// value
	}

	private void gotoOperation(FrameObject o, Stack<PythonObject> stack) {
		o.pc = o.nextInt();
	}

	private void jumpIfFalse(FrameObject o, Stack<PythonObject> stack) {
		int jv = o.nextInt();
		if (!stack.pop().truthValue())
			o.pc = jv;
	}

	private void jumpIfTrue(FrameObject o, Stack<PythonObject> stack) {
		int jv = o.nextInt();
		if (stack.pop().truthValue())
			o.pc = jv;
	}

	private void jumpIfNone(FrameObject o, Stack<PythonObject> stack) {
		int jv = o.nextInt();
		if (stack.peek() == NoneObject.NONE)
			o.pc = jv;
	}

	private void jumpIfNoReturn(FrameObject o, Stack<PythonObject> stack) {
		int jv = o.nextInt();
		FrameObject frame = (FrameObject) stack.peek();
		if (!frame.returnHappened)
			// Frame ended without return, jump to specified label and keep
			// frame on stack
			o.pc = jv;
	}

	private void makeFuture(FrameObject o, Stack<PythonObject> stack) {
		int jv = o.nextInt();
		List<String> closureCopy = new ArrayList<String>();
		for (int i = 0; i < jv; i++) {
			String name = o.compiled.getConstant(o.nextInt()).toString();
			closureCopy.add(name);
		}
		stack.push(new PythonFutureObject((UserFunctionObject) stack.pop(),
				closureCopy, environment()));
	}

	private void load(FrameObject o, Stack<PythonObject> stack) {
		String svl = ((StringObject) o.compiled.getConstant(o.nextInt())).value;
		PythonObject value = environment().get(svl, false, false);
		if (value == null)
			throw new NameError("name " + svl + " is not defined");
		if (value instanceof FutureObject)
			value = ((FutureObject) value).getValue();
		stack.push(value);
	}

	private void loadFuture(FrameObject o, Stack<PythonObject> stack) {
		String svl = ((StringObject) o.compiled.getConstant(o.nextInt())).value;
		PythonObject value = environment().get(svl, false, false);
		if (value == null)
			throw new NameError("name " + svl + " is not defined");
		if (value instanceof FutureObject)
			if (((FutureObject) value).isReady())
				value = ((FutureObject) value).getValue();
		stack.push(value);
	}

	private void testFuture(FrameObject o, Stack<PythonObject> stack) {
		String svl = ((StringObject) o.compiled.getConstant(o.nextInt())).value;
		PythonObject value = environment().get(svl, false, false);
		if (value == null)
			throw new NameError("name " + svl + " is not defined");
		if (value instanceof FutureObject)
			stack.push(BoolObject.fromBoolean(((FutureObject) value).isReady()));
		else
			stack.push(BoolObject.TRUE);
	}

	private void loadGlobal(FrameObject o, Stack<PythonObject> stack) {
		String svl = ((StringObject) o.compiled.getConstant(o.nextInt())).value;
		PythonObject value = environment().get(svl, true, false);
		if (value == null)
			throw new NameError("name " + svl + " is not defined");
		if (value instanceof FutureObject)
			value = ((FutureObject) value).getValue();
		stack.push(value);
	}

	private void pop(FrameObject o, Stack<PythonObject> stack) {
		stack.pop();
	}

	private void truthValue(FrameObject o, Stack<PythonObject> stack) {
		PythonObject value = stack.pop();
		int jv = o.nextInt();
		if (value instanceof NumberObject) {
			if (jv == 1)
				stack.push(value.truthValue() ? BoolObject.FALSE
						: BoolObject.TRUE);
			else
				stack.push(value.truthValue() ? BoolObject.TRUE
						: BoolObject.FALSE);
		} else if (value.get("__nonzero__", null) != null) {
			PythonObject runnable = value.get("__nonzero__", null);
			returnee = execute(true, runnable, null);
			o.accepts_return = true;
			if (jv == 1)
				o.pc -= 5;
		} else if (value.get("__len__", null) != null) {
			PythonObject runnable = value.get("__len__", null);
			returnee = execute(true, runnable, null);
			o.accepts_return = true;
			o.pc -= 5;
		} else {
			if (jv == 1)
				stack.push(value.truthValue() ? BoolObject.FALSE
						: BoolObject.TRUE);
			else
				stack.push(value.truthValue() ? BoolObject.TRUE
						: BoolObject.FALSE);
		}
	}

	private void push(FrameObject o, Stack<PythonObject> stack) {
		stack.push(o.compiled.getConstant(o.nextInt()));
	}

	private void returnOperation(FrameObject o, Stack<PythonObject> stack) {
		if (o.ownedGenerator != null)
			if (!o.yielding)
				throw new StopIteration();
		if (o.nextInt() == 1) {
			o.returnHappened = true;
			PythonObject retVal = stack.pop();
			returnee = retVal;
		}
		removeLastFrame();
		o.yielding = false;
	}

	private void save(FrameObject o, Stack<PythonObject> stack) {
		String ss = ((StringObject) o.compiled.getConstant(o.nextInt())).value;
		PythonObject v = stack.pop();
		environment().set(ss, v, false, false);
	}

	private void kwarg(FrameObject o, Stack<PythonObject> stack) {
		int jv = o.nextInt();
		boolean doingNew = o.kwargs == null;
		if (doingNew)
			o.kwargs = new KwArgs.HashMapKWArgs();
		for (int i = 0; i < jv; i++) {
			String key = o.compiled.getConstant(o.nextInt()).toString();
			if (!doingNew)
				if (o.kwargs.contains(key))
					throw new TypeError(
							"got multiple values for keyword argument '" + key
									+ "'");
			o.kwargs.put(key, stack.pop());
		}
	}

	private void saveLocal(FrameObject o, Stack<PythonObject> stack) {
		environment().getLocals().putVariable(
				((StringObject) o.compiled.getConstant(o.nextInt())).value,
				stack.pop());
	}

	private void saveGlobal(FrameObject o, Stack<PythonObject> stack) {
		environment().set(
				((StringObject) o.compiled.getConstant(o.nextInt())).value,
				stack.pop(), true, false);
	}

	private void dup(FrameObject o, Stack<PythonObject> stack) {
		// duplicates stack x amount of times
		int jv = o.nextInt();
		if (jv == 0)
			stack.push(stack.peek());
		else
			stack.push(stack.get(stack.size() - 1 - jv));
	}

	private void importOperation(FrameObject o, Stack<PythonObject> stack) {
		// import bytecode
		String s1 = ((StringObject) o.compiled.getConstant(o.nextInt())).value;
		String s2 = ((StringObject) o.compiled.getConstant(o.nextInt())).value;
		try {
			pythonImport(environment(), s1, s2, null);
		} catch (CastFailedException e1) {
			throw new TypeError("__all__ must be a list");
		}
	}

	private void swapStack(FrameObject o, Stack<PythonObject> stack) {
		// swaps head of the stack with value below it
		PythonObject top = stack.pop();
		PythonObject bot = stack.pop();
		stack.push(top);
		stack.push(bot);
	}

	private void makeFirst(FrameObject o, Stack<PythonObject> stack) {
		int nth = o.nextInt();
		List<PythonObject> rest = new ArrayList<PythonObject>();
		for (int i = 0; i < nth; i++)
			rest.add(stack.pop());
		PythonObject newHead = stack.pop();
		Collections.reverse(rest);
		for (PythonObject datum : rest)
			stack.push(datum);
		stack.push(newHead);
	}

	private void unpackSequence(FrameObject o, Stack<PythonObject> stack) {
		int cfc = currentFrame.size();
		PythonObject seq = stack.pop();
		int count = o.nextInt();

		ListObject lo = (ListObject) PythonRuntime.LIST_TYPE.call(
				new TupleObject(true, seq), null);
		if (lo.objects.size() > count)
			throw new TypeError("too many values to unpack");
		if (lo.objects.size() < count)
			throw new TypeError("too few values to unpack");

		Collections.reverse(lo.objects);
		for (PythonObject obj : lo.objects)
			stack.push(obj);
	}

	private void unpackKwargs(FrameObject o, Stack<PythonObject> stack) {
		PythonObject value = stack.pop();
		PythonObject keysFn = value.get("keys", null);
		PythonObject getItemFn = value.get("__getitem__", null);
		if ((keysFn == null) || (getItemFn == null))
			new TypeError("argument after ** must be a mapping, not "
					+ value.toString());
		PythonObject iterator = Utils.run("iter", execute(true, keysFn, null))
				.get(GeneratorObject.NEXT, null);
		if (o.kwargs == null)
			o.kwargs = new KwArgs.HashMapKWArgs();
		try {
			while (true) {
				PythonObject key = execute(true, iterator, null);
				returnee = execute(true, getItemFn, null, key);
				o.kwargs.put(key.toString(), returnee);
			}
		} catch (PythonExecutionException e) {
			if (PythonRuntime.isinstance(e.getException(),
					PythonRuntime.STOP_ITERATION).truthValue()) {
				; // nothing
			} else
				throw e;
		}
	}

	private void pushLocalContext(FrameObject o, Stack<PythonObject> stack) {
		// pushes value from stack into currentContex and makrs the push into
		// frame
		o.localContext = stack.pop();
	}

	private void resolveArgs(FrameObject o, Stack<PythonObject> stack) {
		// resolves args into locals
		synchronized (this.args) {
			for (String key : this.args.keySet()) {
				environment().getLocals().putVariable(key,
						this.args.getVariable(key));
			}
		}
	}

	private void getAttr(FrameObject o, Stack<PythonObject> stack) {
		try {
			PythonObject apo;
			StringObject field = (StringObject) o.compiled.getConstant(o
					.nextInt());
			PythonObject value = stack.pop(); // object to get attribute from
			if (value instanceof FutureObject)
				value = ((FutureObject) value).getValue();
			apo = value.get("__getattribute__", getLocalContext());
			if (apo != null && !(value instanceof ClassObject)) {
				// There is __getattribute__ defined, call it directly
				returnee = execute(false, apo, null, field);
				o.accepts_return = true;
				return;
			} else {
				// Try to grab argument normally...
				apo = value.get(field.value, getLocalContext());
				if (apo != null) {
					returnee = apo;
					o.accepts_return = true;
					return;
				}
				// ... and if that fails, use __getattr__ if available
				apo = value.get("__getattr__", getLocalContext());
				if (apo != null) {
					// There is __getattribute__ defined, call it directly
					returnee = execute(false, apo, null, field);
					o.accepts_return = true;
					return;
				}
				throw new AttributeError("" + value.getType()
						+ " object has no attribute '" + field + "'");
			}
		} finally {
			if (returnee instanceof PropertyObject) {
				returnee = ((PropertyObject) returnee).get();
			}
		}
	}

	private void delAttr(FrameObject o, Stack<PythonObject> stack) {
		PythonObject runnable = environment().getBuiltin("delattr");
		PythonObject[] args = new PythonObject[2];
		args[1] = o.compiled.getConstant(o.nextInt()); // attribute
		args[0] = stack.pop(); // object
		returnee = execute(false, runnable, null, args);
	}

	private void setAttr(FrameObject o, Stack<PythonObject> stack) {
		PythonObject runnable = environment().getBuiltin("setattr");
		PythonObject[] args = new PythonObject[3];
		// If argument for SETATTR is not set, attribute name is pop()ed from
		// stack
		PythonObject po = o.compiled.getConstant(o.nextInt());
		if (po == NoneObject.NONE) {
			args[1] = stack.pop(); // attribute
			args[0] = stack.pop(); // object
			args[2] = stack.pop(); // value
		} else {
			args[1] = po; // attribute
			args[0] = stack.pop(); // object
			args[2] = stack.pop(); // value
		}
		returnee = execute(false, runnable, null, args);
	}

	private void isinstance(FrameObject o, Stack<PythonObject> stack) {
		PythonObject type = stack.pop();
		PythonObject value = stack.peek();
		stack.push(BoolObject.fromBoolean(PythonRuntime.doIsInstance(value,
				type, true)));
	}

	private void raise(FrameObject o, Stack<PythonObject> stack) {
		PythonObject s = Utils.peek(stack);
		if (s == null)
			throw new TypeError(
					"no exception is being handled but raise called");
		else if (PythonRuntime.isinstance(s, PythonRuntime.ERROR).truthValue()) {
			// Throw exception normally
			throw new PythonExecutionException(s);
		} else if (PythonRuntime.isderived(s, PythonRuntime.ERROR)) {
			// Throw new exception instance
			s = ((ClassObject) s).call(TupleObject.EMPTY, KwArgs.EMPTY);
			throw new PythonExecutionException(s);
		} else
			throw new TypeError(
					"exceptions must be Error instance or class derived from Error, not "
							+ s.toString());
	}

	private void reraise(FrameObject o, Stack<PythonObject> stack) {
		PythonObject s = stack.pop();
		if (s != NoneObject.NONE) {
			PythonExecutionException pee = new PythonExecutionException(s);
			pee.noStackGeneration(true);
			throw pee;
		}
	}

	private void pushFrame(FrameObject o, Stack<PythonObject> stack) {
		checkOverflow();

		// inserts new subframe onto frame stack
		o.accepts_return = true;
		FrameObject nf = new FrameObject();
		nf.parentFrame = o;
		nf.compiled = o.compiled;
		nf.localContext = o.localContext;
		nf.environment = o.environment;
		nf.dataStream = ByteBuffer.wrap(nf.compiled.getBytedata());
		nf.pc = o.nextInt();
		currentFrame.add(nf);

		// moves x elements from original stack to new stack (used by WITH)
		int popc = o.nextInt();
		if (popc > 0) {
			List<PythonObject> po = new ArrayList<PythonObject>();
			for (int i = 0; i < popc; i++) {
				po.add(stack.pop());
			}
			Collections.reverse(po);
			for (PythonObject oo : po)
				nf.stack.push(oo);
		}
	}

	private void pushException(FrameObject o, Stack<PythonObject> stack) {
		// who has any idea what this shit does call 1-555-1337
		FrameObject frame = (FrameObject) stack.peek();
		if (frame.exception == null)
			stack.push(NoneObject.NONE);
		else
			stack.push(frame.exception);
	}

	private ExecutionResult yield(FrameObject o, Stack<PythonObject> stack) {
		String name = ((StringObject) o.compiled.getConstant(o.nextInt())).value;
		if (o.ownedGenerator == null) {
			List<FrameObject> ol = new ArrayList<FrameObject>();
			FrameObject oo = o;
			while (oo != null) {
				ol.add(oo.cloneFrame());
				oo = oo.parentFrame;
			}
			for (int i = 0; i < ol.size(); i++)
				if (i != ol.size() - 1)
					ol.get(i).parentFrame = ol.get(i + 1);
			ol.get(0).pc -= 5;
			Collections.reverse(ol);
			GeneratorObject generator = new GeneratorObject(name, ol);
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
			o.stack.push(sentValue);

			GeneratorObject generator = o.ownedGenerator;
			List<FrameObject> ol = new ArrayList<FrameObject>();
			FrameObject oo = o;
			while (oo != null) {
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
	}

	private void loadDynamic(FrameObject o, Stack<PythonObject> stack) {
		PythonObject value = null;
		boolean found = false;
		StringObject variable = (StringObject) o.compiled.getConstant(o
				.nextInt());
		for (int i = currentFrame.size() - 1; i >= 0; i--) {
			FrameObject oo = currentFrame.get(i);
			InternalDict locals = oo.environment.getLocals();
			if (locals.containsVariable(variable.value)) {
				stack.push(locals.getVariable(variable.value));
				found = true;
				break;
			}
		}
		if (!found)
			throw new NameError("dynamic variable '" + variable.value
					+ "' is undefined");
	}

	private void saveDynamic(FrameObject o, Stack<PythonObject> stack) {
		PythonObject value = stack.pop();
		StringObject variable = (StringObject) o.compiled.getConstant(o
				.nextInt());
		boolean found = false;
		for (int i = currentFrame.size() - 1; i >= 0; i--) {
			FrameObject oo = currentFrame.get(i);
			InternalDict locals = oo.environment.getLocals();
			if (locals.containsVariable(variable.value)) {
				locals.putVariable(variable.value, value);
				found = true;
				break;
			}
		}
		if (!found) {
			InternalDict locals = o.environment.getLocals();
			locals.putVariable(variable.value, value);
		}
	}

	private void loadBuiltin(FrameObject o, Stack<PythonObject> stack) {
		String vname = ((StringObject) o.compiled.getConstant(o.nextInt())).value;
		PythonObject value = environment().getBuiltin(vname);
		if (value == null)
			throw new NameError("builtin name '" + vname + "' is undefined");
		stack.push(value);
	}

	private void del(FrameObject o, Stack<PythonObject> stack) {
		StringObject vname = (StringObject) o.compiled.getConstant(o.nextInt());
		boolean isGlobal = o.nextInt() == 1;
		environment().delete(vname.value, isGlobal);
	}

	private static String printStack(Stack<PythonObject> stack) {
		StringBuilder bd = new StringBuilder();

		bd.append("[");
		Object[] arr = stack.toArray();
		for (Object o : arr) {
			String s = o.toString();
			if (s.length() > 40)
				s = s.substring(0, 37) + "...";
			bd.append(s);
			bd.append("; ");
		}
		bd.append("]");

		return bd.toString();
	}

	/**
	 * Removes last frame from frame stack
	 */
	private void removeLastFrame() {
		FrameObject o = this.currentFrame.removeLast();
		if (o.parentFrame != null) {
			o.parentFrame.returnHappened = o.returnHappened;
			o.parentFrame.yielding = o.yielding;
			o.parentFrame.sendValue = o.sendValue;
			o.yielding = false;
			o.parentFrame.stack.add(o);
		} else {
			if (currentFrame.size() == 0) {
				PythonObject e = o.exception;
				if (e != null) {
					if (e.get("__exception__", null) != null) {
						Throwable t = (Throwable) ((PointerObject) o.exception
								.get("__exception__", null)).getObject();
						if (t instanceof PythonException)
							throw (PythonException) t;
						throw new PythonExecutionException(e, t);
					} else {
						PythonException pe = PythonException.translate(e);
						if (pe != null) {

							throw pe;
						}
						throw new PythonExecutionException(e);
					}
				}
			} else {
				if (o.isSignal){
					returnee = currentFrame.peekLast().storedReturnee;
				}
				currentFrame.peekLast().exception = o.exception;
			}
		}
	}

	/**
	 * Imports based on the values on the Import bytecode
	 * 
	 * @param environment
	 * @param variable
	 * @param modulePath
	 * @param target
	 * @throws CastFailedException
	 */
	private void pythonImport(EnvironmentObject environment, String variable,
			String modulePath, PythonObject target) throws CastFailedException {
		if (modulePath == null || modulePath.equals("")) {
			if (target == null) {
				synchronized (PythonRuntime.runtime) {
					target = PythonRuntime.runtime.getModule(variable, null);
				}
			} else if (!variable.equals("*")) {
				environment.set(variable, target, true, false);
			} else {
				InternalDict dict = (InternalDict) target.getEditableFields()
						.get(ModuleObject.__DICT__).object;
				synchronized (dict) {
					Set<String> importKeys = new HashSet<String>();
					if (!dict.containsVariable("__all__"))
						importKeys.addAll(dict.keySet());
					else
						importKeys.addAll(Coerce
								.toJavaCollection(dict.getVariable("__all__"),
										List.class, String.class));
					for (String key : importKeys) {
						environment
								.set(key, dict.getVariable(key), true, false);
					}
				}
			}
		} else {
			String[] split = modulePath.split("\\.");
			String mm = split[0];
			modulePath = modulePath.replaceFirst(mm, "");
			modulePath = modulePath.replaceFirst("\\.", "");
			if (target == null) {
				target = environment.get(mm, false, false);
				if (target == null || !(target instanceof ModuleObject)) {
					ModuleObject thisModule = (ModuleObject) environment.get(
							"__thismodule__", true, false);
					target = null;
					synchronized (PythonRuntime.runtime) {
						String resolvePath = thisModule == null ? null
								: thisModule.getModuleData()
										.getPackageResolve();
						if (resolvePath == null)
							target = PythonRuntime.runtime.root.get(mm) != null ? PythonRuntime.runtime.root
									.get(mm).module : null;
						if (target == null)
							target = PythonRuntime.runtime.getModule(mm,
									resolvePath == null ? null
											: new StringObject(resolvePath,
													true));
					}
				}
			} else {
				if (target instanceof ModuleObject) {
					ModuleObject mod = (ModuleObject) target;
					PythonObject target2 = ((InternalDict) mod
							.getEditableFields().get(ModuleObject.__DICT__).object)
							.getVariable(mm);
					if (target2 != null) {
						pythonImport(environment, variable, modulePath, target2);
						return;
					}
				}
				if (target.get(mm, null) != null) {
					pythonImport(environment, variable, modulePath,
							target.get(mm, null));
					return;
				} else {
					target = PythonRuntime.runtime
							.getModule(mm, new StringObject(
									((ModuleObject) target).getModuleData()
											.getPackageResolve(), true));
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
		return "<bound python interpret for thread 0x"
				+ Long.toHexString(Thread.currentThread().getId()) + ">";
	}

	/**
	 * Sets the arguments for the next RESOLVE_ARGS call
	 * 
	 * @param a
	 */
	public void setArgs(InternalDict a) {
		args = a;
	}

	/**
	 * Returns current frame
	 * 
	 * @return
	 */
	public FrameObject frame() {
		return currentFrame.peekLast();
	}

	/**
	 * Executes bytecode until cfc equals the number of frames. Used to fully
	 * finish execution of newly pushed stack
	 * 
	 * @param cfc
	 * @return
	 */
	public PythonObject executeAll(int cfc) {
		if (cfc == currentFrame.size())
			return returnee;
		while (true) {
			ExecutionResult res = executeOnce();
			if (res == ExecutionResult.INTERRUPTED)
				return null;
			if (res == ExecutionResult.FINISHED || res == ExecutionResult.EOF)
				if (currentFrame.size() == cfc) {
					if (exception() != null) {
						PythonObject e = exception();
						currentFrame.peekLast().exception = null;
						throw new PythonExecutionException(e);
					}
					return returnee;
				}
		}
	}

	public int getAccessCount() {
		return accessCount;
	}

	public void setClosure(List<InternalDict> closure) {
		this.currentClosure = closure;
	}

	@Override
	public Set<String> getGenHandleNames() {
		return new HashSet<String>();
	}

	@Override
	protected Map<String, JavaMethodObject> getGenHandles() {
		return new HashMap<String, JavaMethodObject>();
	}

	public List<InternalDict> getCurrentClosure() {
		return currentClosure;
	}
}
