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

import java.util.Stack;

import me.enerccio.sp.compiler.Bytecode;
import me.enerccio.sp.errors.InterpreterError;
import me.enerccio.sp.types.PythonObject;

/**
 * PythonInterpret. Interprets bytecode. One per thread and gets automatically constructed the moment something wants to access it in a thread.
 * @author Enerccio
 *
 */
public class JavaPythonInterpreter extends AbstractPythonInterpreter {

	/**
	 * Executes current instruction
	 * 
	 * @param o
	 *            current frame
	 * @return execution result
	 */
	@Override
	protected ExecutionResult doExecuteSingleInstruction(FrameObject o,
			Stack<PythonObject> stack, Bytecode opcode) {

		switch (opcode) {
		case NOP:
			// do nothing
			break;
		case D_RETURN:
		case D_STARTFUNC:
			o.pc += 4;
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
}
