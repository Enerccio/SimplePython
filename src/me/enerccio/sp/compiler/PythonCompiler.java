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
package me.enerccio.sp.compiler;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import me.enerccio.sp.compiler.PythonBytecode.Pop;
import me.enerccio.sp.interpret.CompiledBlockObject;
import me.enerccio.sp.parser.pythonParser.And_exprContext;
import me.enerccio.sp.parser.pythonParser.And_testContext;
import me.enerccio.sp.parser.pythonParser.ArglistContext;
import me.enerccio.sp.parser.pythonParser.ArgumentContext;
import me.enerccio.sp.parser.pythonParser.Arith_exprContext;
import me.enerccio.sp.parser.pythonParser.AtomContext;
import me.enerccio.sp.parser.pythonParser.Break_stmtContext;
import me.enerccio.sp.parser.pythonParser.ClassdefContext;
import me.enerccio.sp.parser.pythonParser.Comp_forContext;
import me.enerccio.sp.parser.pythonParser.Comp_iterContext;
import me.enerccio.sp.parser.pythonParser.ComparisonContext;
import me.enerccio.sp.parser.pythonParser.Compound_stmtContext;
import me.enerccio.sp.parser.pythonParser.Continue_stmtContext;
import me.enerccio.sp.parser.pythonParser.DecoratedContext;
import me.enerccio.sp.parser.pythonParser.DecoratorContext;
import me.enerccio.sp.parser.pythonParser.DecoratorsContext;
import me.enerccio.sp.parser.pythonParser.DictentryContext;
import me.enerccio.sp.parser.pythonParser.DictorsetmakerContext;
import me.enerccio.sp.parser.pythonParser.Dotted_as_nameContext;
import me.enerccio.sp.parser.pythonParser.Dotted_as_namesContext;
import me.enerccio.sp.parser.pythonParser.Dotted_nameContext;
import me.enerccio.sp.parser.pythonParser.ExprContext;
import me.enerccio.sp.parser.pythonParser.Expr_stmtContext;
import me.enerccio.sp.parser.pythonParser.ExprlistContext;
import me.enerccio.sp.parser.pythonParser.FactorContext;
import me.enerccio.sp.parser.pythonParser.FargContext;
import me.enerccio.sp.parser.pythonParser.File_inputContext;
import me.enerccio.sp.parser.pythonParser.Flow_stmtContext;
import me.enerccio.sp.parser.pythonParser.For_stmtContext;
import me.enerccio.sp.parser.pythonParser.FuncdefContext;
import me.enerccio.sp.parser.pythonParser.If_stmtContext;
import me.enerccio.sp.parser.pythonParser.Import_as_nameContext;
import me.enerccio.sp.parser.pythonParser.Import_fromContext;
import me.enerccio.sp.parser.pythonParser.Import_nameContext;
import me.enerccio.sp.parser.pythonParser.Import_stmtContext;
import me.enerccio.sp.parser.pythonParser.IntegerContext;
import me.enerccio.sp.parser.pythonParser.Label_or_stmtContext;
import me.enerccio.sp.parser.pythonParser.LambdefContext;
import me.enerccio.sp.parser.pythonParser.List_forContext;
import me.enerccio.sp.parser.pythonParser.List_iterContext;
import me.enerccio.sp.parser.pythonParser.ListmakerContext;
import me.enerccio.sp.parser.pythonParser.NnameContext;
import me.enerccio.sp.parser.pythonParser.Not_testContext;
import me.enerccio.sp.parser.pythonParser.NumberContext;
import me.enerccio.sp.parser.pythonParser.Or_testContext;
import me.enerccio.sp.parser.pythonParser.PowerContext;
import me.enerccio.sp.parser.pythonParser.Print_stmtContext;
import me.enerccio.sp.parser.pythonParser.Raise_stmtContext;
import me.enerccio.sp.parser.pythonParser.Return_stmtContext;
import me.enerccio.sp.parser.pythonParser.Shift_exprContext;
import me.enerccio.sp.parser.pythonParser.Simple_stmtContext;
import me.enerccio.sp.parser.pythonParser.Small_stmtContext;
import me.enerccio.sp.parser.pythonParser.StmtContext;
import me.enerccio.sp.parser.pythonParser.StringContext;
import me.enerccio.sp.parser.pythonParser.String_inputContext;
import me.enerccio.sp.parser.pythonParser.SubscriptContext;
import me.enerccio.sp.parser.pythonParser.SubscriptlistContext;
import me.enerccio.sp.parser.pythonParser.SuiteContext;
import me.enerccio.sp.parser.pythonParser.Switch_stmtContext;
import me.enerccio.sp.parser.pythonParser.TermContext;
import me.enerccio.sp.parser.pythonParser.TestContext;
import me.enerccio.sp.parser.pythonParser.TestlistContext;
import me.enerccio.sp.parser.pythonParser.Testlist_compContext;
import me.enerccio.sp.parser.pythonParser.TrailerContext;
import me.enerccio.sp.parser.pythonParser.Try_exceptContext;
import me.enerccio.sp.parser.pythonParser.Try_stmtContext;
import me.enerccio.sp.parser.pythonParser.While_stmtContext;
import me.enerccio.sp.parser.pythonParser.Xor_exprContext;
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.types.Arithmetics;
import me.enerccio.sp.types.ModuleObject;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.ComplexObject;
import me.enerccio.sp.types.base.EllipsisObject;
import me.enerccio.sp.types.base.IntObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.base.RealObject;
import me.enerccio.sp.types.callables.UserFunctionObject;
import me.enerccio.sp.types.mappings.MapObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.types.types.DictTypeObject;
import me.enerccio.sp.types.types.ListTypeObject;
import me.enerccio.sp.types.types.ObjectTypeObject;
import me.enerccio.sp.types.types.SliceTypeObject;
import me.enerccio.sp.types.types.TupleTypeObject;
import me.enerccio.sp.utils.Utils;

import org.antlr.v4.runtime.ParserRuleContext;

/**
 * Compiles source into Python Bytecode
 * @author Enerccio
 *
 */
public class PythonCompiler {
	private static volatile int genFunc = 0;

	private PythonBytecode cb;
	private VariableStack stack = new VariableStack();
	private LinkedList<MapObject> environments = new LinkedList<MapObject>();
	private Stack<String> compilingClass = new Stack<String>();
	
	public static ThreadLocal<String> moduleName = new ThreadLocal<String>();
	
	/**
	 * Compiles source into single UserFunctionObject. Used by function() function
	 * @param sctx String context 
	 * @param globals list of globals for the environment
	 * @param args string arguments that function requires
	 * @param vargarg null or name of the vararg argument
	 * @param defaults function defaults
	 * @param locals map of locals for the function
	 * @return completed object
	 */
	public UserFunctionObject doCompile(String_inputContext sctx, List<MapObject> globals, 
			List<String> args, String vargarg, MapObject defaults, MapObject locals) {
		moduleName.set("generated-functions");
		stack.push();
		
		for (MapObject global : globals)
			environments.add(global);
		
		UserFunctionObject fnc = new UserFunctionObject();
		fnc.newObject();
		
		String functionName = "generated-function-" + (++genFunc);
		Utils.putPublic(fnc, "__name__", new StringObject(functionName));
		
		
		fnc.args = args;
		if (vargarg != null){
			fnc.isVararg = true;
			fnc.vararg = vargarg;
		}
		
		List<PythonBytecode> fncb = new ArrayList<PythonBytecode>();
		compilingClass.push(null);
		doCompileFunction(sctx, fncb, sctx.start, locals);
		compilingClass.pop();
		
		fncb.add(cb = Bytecode.makeBytecode(Bytecode.PUSH, sctx.stop));
		cb.value = NoneObject.NONE;
		fncb.add(cb = Bytecode.makeBytecode(Bytecode.RETURN, sctx.stop));
		cb.intValue = 1;
		
		fnc.block = new CompiledBlockObject(fncb);
		fnc.block.newObject();
		
		globals.add(locals);
		Collections.reverse(globals);
		
		TupleObject to = new TupleObject(globals.toArray(new PythonObject[globals.size()]));
		to.newObject();
		Utils.putPublic(fnc, "closure", to);
		Utils.putPublic(fnc, "locals", locals);
		Utils.putPublic(fnc, "function_defaults", defaults);
		
		return fnc;
	}
	
	/**
	 * Compiles file input into List of bytecode for the module
	 * @param fcx file input
	 * @param dict module's __dict__
	 * @param m module
	 * @return
	 */
	public CompiledBlockObject doCompile(File_inputContext fcx, MapObject dict, ModuleObject m) {
		return doCompile(fcx, dict, m.fields.get(ModuleObject.__NAME__).object.toString(), m);
	}
	
	public CompiledBlockObject doCompile(File_inputContext fcx, MapObject dict, String mn, PythonObject locals) {
		moduleName.set(mn);
		
		stack.push();
		compilingClass.push(null);
		List<PythonBytecode> bytecode = new ArrayList<PythonBytecode>();
		// create new environment
		bytecode.add(Bytecode.makeBytecode(Bytecode.PUSH_ENVIRONMENT, fcx.start));
		// context
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH, fcx.start));
		cb.value = NoneObject.NONE;
		bytecode.add(Bytecode.makeBytecode(Bytecode.PUSH_LOCAL_CONTEXT, fcx.start));
		// locals
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH_DICT, fcx.start)); 
		cb.mapValue = dict;
		environments.add(dict);
		
		for (Label_or_stmtContext ls : fcx.label_or_stmt())
			compile(ls, bytecode, null);
		
		compilingClass.pop();
		stack.pop();
		environments.removeLast();
		
		CompiledBlockObject cob = new CompiledBlockObject(bytecode);
		cob.newObject();
		
		return cob;
	}

	private void compileStatement(StmtContext sctx, List<PythonBytecode> bytecode, ControllStack cs) {
		if (sctx.simple_stmt() != null)
			compileSimpleStatement(sctx.simple_stmt(), bytecode, cs);
		else
			compileCompoundStatement(sctx.compound_stmt(), bytecode, cs);
	}

	private void compileCompoundStatement(Compound_stmtContext cstmt, List<PythonBytecode> bytecode, ControllStack cs) {
		if (cstmt.funcdef() != null) {
			compileFunction(cstmt.funcdef(), bytecode, null);
		} else if (cstmt.classdef() != null) {
			compileClass(cstmt.classdef(), bytecode, null);
		} else if (cstmt.try_stmt() != null) {
			compileTry(cstmt.try_stmt(), bytecode, cs);
		} else if (cstmt.while_stmt() != null) {
			compileWhile(cstmt.while_stmt(), bytecode, cs);
		} else if (cstmt.for_stmt() != null) {
			compileFor(cstmt.for_stmt(), bytecode, cs);
		} else if (cstmt.if_stmt() != null) {
			compileIf(cstmt.if_stmt(), bytecode, cs);
		} else if (cstmt.switch_stmt() != null) {
			compileSwitch(cstmt.switch_stmt(), bytecode, cs);
		} else if (cstmt.decorated() != null) {
			DecoratedContext dc = cstmt.decorated();
			if (dc.classdef() != null)
				compileClass(dc.classdef(), bytecode, dc.decorators());
			else if (dc.funcdef() != null)
				compileFunction(dc.funcdef(), bytecode, dc.decorators());
		} else
			throw Utils.throwException("SyntaxError", "statament type not implemented");
	}
	
	private void compileSuite(SuiteContext ctx, List<PythonBytecode> bytecode, ControllStack cs) {
		if (ctx.simple_stmt() != null)
			compileSimpleStatement(ctx.simple_stmt(), bytecode, cs);
		for (StmtContext sctx : ctx.stmt())
			compileStatement(sctx, bytecode, cs);
	}

	private void compileTry(Try_stmtContext try_stmt, List<PythonBytecode> bytecode, ControllStack cs) {
		TryFinallyItem tfi = new TryFinallyItem(try_stmt);
		cs = ControllStack.push(cs, tfi);
		PythonBytecode makeFrame = Bytecode.makeBytecode(Bytecode.PUSH_FRAME, try_stmt.start);
		PythonBytecode excTestJump = Bytecode.makeBytecode(Bytecode.GOTO, try_stmt.start);
		PythonBytecode elseJump = null;
		List<PythonBytecode> exceptJumps = new ArrayList<>();
		List<PythonBytecode> finallyJumps = new ArrayList<>();
		List<PythonBytecode> endJumps = new ArrayList<>();
		bytecode.add(makeFrame);
		bytecode.add(Bytecode.makeBytecode(Bytecode.SWAP_STACK, try_stmt.start));
		bytecode.add(Bytecode.makeBytecode(Bytecode.PUSH_EXCEPTION, try_stmt.start));
		bytecode.add(excTestJump);
		// Compile try block
		makeFrame.intValue = bytecode.size();
		compileSuite(try_stmt.suite(), bytecode, cs);
		bytecode.add(Bytecode.makeBytecode(Bytecode.RETURN, try_stmt.start));
		excTestJump.intValue = bytecode.size();
		// Compile exception tests.
		// Stack contains TOP -> return value -> frame -> exception
		if (tfi.needsBreakBlock) {
			// Special break block is needed
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.LOADGLOBAL, try_stmt.start));
			cb.stringValue = "LoopBreak"; 
			bytecode.add(Bytecode.makeBytecode(Bytecode.ISINSTANCE, try_stmt.start));
			PythonBytecode skipOver = Bytecode.makeBytecode(Bytecode.JUMPIFFALSE, try_stmt.start);
			bytecode.add(skipOver);
			
			tfi.outputFinallyBreakBlock(try_stmt, bytecode, cs);
			skipOver.intValue = bytecode.size();
		}
		if (tfi.needsContinueBlock) {
			// Special continue block is needed
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.LOADGLOBAL, try_stmt.start));
			cb.stringValue = "LoopContinue"; 
			bytecode.add(Bytecode.makeBytecode(Bytecode.ISINSTANCE, try_stmt.start));
			PythonBytecode skipOver = Bytecode.makeBytecode(Bytecode.JUMPIFFALSE, try_stmt.start);
			bytecode.add(skipOver);
			
			tfi.outputFinallyContinueBlock(try_stmt, bytecode, cs);
			skipOver.intValue = bytecode.size();
		}
		if (try_stmt.try_except().size() > 0) {
			// There is at least one except block defined
			if (try_stmt.else_block() != null)
				// ... and else on top of that
				bytecode.add(elseJump = Bytecode.makeBytecode(Bytecode.JUMPIFNONE, try_stmt.start));
			boolean alwaysHandled = false;
			// Stack contains TOP -> return value -> frame -> exception when execution reaches here
			for (Try_exceptContext ex : try_stmt.try_except()) {
				if (ex.except_clause().test().size() == 0) {
					// try ... except:
					bytecode.add(cb = Bytecode.makeBytecode(Bytecode.GOTO, try_stmt.start));
					exceptJumps.add(cb);
					alwaysHandled = true;
				} else {
					// try ... except ErrorType, xyz:
					// or 
					// try ... except ErrorType:
					compile(ex.except_clause().test(0), bytecode); 
					bytecode.add(Bytecode.makeBytecode(Bytecode.ISINSTANCE, try_stmt.start));
					bytecode.add(cb = Bytecode.makeBytecode(Bytecode.JUMPIFTRUE, try_stmt.start));
					exceptJumps.add(cb);
				}
			}
			if (!alwaysHandled) {
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.GOTO, try_stmt.start));
				finallyJumps.add(cb);
			}
			
			// Compile else block, if any
			if (elseJump != null) {
				elseJump.intValue = bytecode.size();
				bytecode.add(Bytecode.makeBytecode(Bytecode.POP, try_stmt.start));
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.JUMPIFNORETURN, try_stmt.start));
				cb.intValue = bytecode.size() + 2;
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH, try_stmt.start));
				cb.value = NoneObject.NONE;
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.GOTO, try_stmt.start));
				finallyJumps.add(cb);
				compileSuite(try_stmt.else_block().suite(), bytecode, cs);
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH, try_stmt.start));
				cb.value = NoneObject.NONE;
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.GOTO, try_stmt.start));
				finallyJumps.add(cb);
			}
			
			// Compile actual except blocks
			// Stack contains TOP -> return value -> frame -> exception if any of those is executed
			int i = 0;
			for (Try_exceptContext ex : try_stmt.try_except()) {
				if (ex.except_clause().test().size() <= 1) {
					// try ... except:
					// or
					// try ... except ErrorType:
					// -> remove exception from top of stack
					exceptJumps.get(i++).intValue = bytecode.size();
					bytecode.add(Bytecode.makeBytecode(Bytecode.POP, try_stmt.start));
					compileSuite(ex.suite(), bytecode, cs);
					bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH, try_stmt.start));
					cb.value = NoneObject.NONE;
					bytecode.add(cb = Bytecode.makeBytecode(Bytecode.GOTO, try_stmt.start));
					finallyJumps.add(cb);
				} else {
					// try ... except ErrorType, xyz:
					exceptJumps.get(i++).intValue = bytecode.size();
					// Store exception from top of stack
					compileAssignment(ex.except_clause().test(1), bytecode);
					compileSuite(ex.suite(), bytecode, cs);
					bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH, try_stmt.start));
					cb.value = NoneObject.NONE;
					bytecode.add(cb = Bytecode.makeBytecode(Bytecode.GOTO, try_stmt.start));
					finallyJumps.add(cb);
				}
			}
		}
		
		// Compile finally block (if any)
		for (PythonBytecode c : finallyJumps) c.intValue = bytecode.size();
		if (try_stmt.try_finally() != null)
			compileSuite(try_stmt.try_finally().suite(), bytecode, cs);
		// Stack contains TOP -> return value -> frame -> exception here, exception may be None
		bytecode.add(Bytecode.makeBytecode(Bytecode.RERAISE, try_stmt.start));
		// If execution reaches here, there was no exception and there is still frame on top of stack.
		// If this frame returned value, it should be returned from here as well.
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.JUMPIFNORETURN, try_stmt.start));
		endJumps.add(cb);
		// There is TOP -> return value -> frame on stack if execution reaches here
		bytecode.add(Bytecode.makeBytecode(Bytecode.POP, try_stmt.start));
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.RETURN, try_stmt.start));
		cb.intValue = 1;

		
		cs.pop();
		
		// Very end of try...catch block. Return value and frame is still on stack
		for (PythonBytecode c : endJumps) c.intValue = bytecode.size();
		bytecode.add(Bytecode.makeBytecode(Bytecode.POP, try_stmt.start));
		bytecode.add(Bytecode.makeBytecode(Bytecode.POP, try_stmt.start));
	}

	private void compile(Label_or_stmtContext ls, List<PythonBytecode> bytecode, ControllStack cs) {
			compileStatement(ls.stmt(), bytecode, cs);
	}


	private void compileWhile(While_stmtContext ctx, List<PythonBytecode> bytecode, ControllStack cs) {
		LoopStackItem lsi = new LoopStackItem(bytecode.size());
		cs = ControllStack.push(cs, lsi);
		// Compile condition
		compile(ctx.test(), bytecode);
		bytecode.add(Bytecode.makeBytecode(Bytecode.TRUTH_VALUE, ctx.start));
		PythonBytecode jump = Bytecode.makeBytecode(Bytecode.JUMPIFFALSE, ctx.start);
		bytecode.add(jump);
		// Compile body
		compileSuite(ctx.suite(0), bytecode, cs); 
		// Jump back
		lsi.addContinue(ctx, bytecode);
		// Compile else block, if needed
		jump.intValue = bytecode.size();;
		cs.pop();
		if (ctx.suite(1) != null)
			compileSuite(ctx.suite(1), bytecode, cs);
		lsi.finalize(bytecode.size());
	}
	
	private void compileFor(For_stmtContext ctx, List<PythonBytecode> bytecode, ControllStack cs) {
		PythonBytecode getIter;
		PythonBytecode acceptIter;
		PythonBytecode setupLoop;
		// Compile SETUP_LOOP that grabs iterator on top of stack. This opcode jumps if grabbed iterator is
		// optimized, java-based thing.
		compileRightHand(ctx.testlist(), bytecode);
		bytecode.add(setupLoop = Bytecode.makeBytecode(Bytecode.SETUP_LOOP, ctx.start));
		putGetAttr("next", bytecode, ctx.start);
		setupLoop.intValue = bytecode.size();
		// Compile loop
		LoopStackItem lsi = new LoopStackItem(bytecode.size());
		cs = ControllStack.push(cs, lsi);
		bytecode.add(getIter = Bytecode.makeBytecode(Bytecode.GET_ITER, ctx.start));
		bytecode.add(acceptIter = Bytecode.makeBytecode(Bytecode.ACCEPT_ITER, ctx.start));
		// Assign to iteration variable
		compileAssignment(ctx.exprlist(), bytecode);
		// Compile block
		compileSuite(ctx.suite(0), bytecode, cs);
		lsi.addContinue(ctx, bytecode);
		// Compile else block (if any)
		getIter.intValue = acceptIter.intValue = bytecode.size();
		if (ctx.suite(1) != null)
			compileSuite(ctx.suite(1), bytecode, cs);
		// TODO: Else somewhere here
		lsi.finalize(bytecode.size());
		// Pop iter.next
		bytecode.add(Bytecode.makeBytecode(Bytecode.POP, ctx.start));
	}

	private void compileIf(If_stmtContext ctx, List<PythonBytecode> bytecode, ControllStack cs) {
		List<PythonBytecode> endJumps = new ArrayList<>();
		PythonBytecode jump = null;
		// If and elif blocks
		for (int i=0; i<ctx.test().size(); i++) {
			compile(ctx.test(i), bytecode);
			bytecode.add(Bytecode.makeBytecode(Bytecode.TRUTH_VALUE, ctx.start));
			bytecode.add(jump = Bytecode.makeBytecode(Bytecode.JUMPIFFALSE, ctx.start));
			compileSuite(ctx.suite(i), bytecode, cs);
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.GOTO, ctx.start));
			endJumps.add(cb);
			jump.intValue = bytecode.size();
		}
		// Else block
		if (ctx.else_block() != null)
			compileSuite(ctx.else_block().suite(), bytecode, cs);
		// End if..else block
		for (PythonBytecode c : endJumps) c.intValue = bytecode.size();
	}
	

	private void compileSwitch(Switch_stmtContext ctx, List<PythonBytecode> bytecode, ControllStack cs) {
		List<PythonBytecode> jumps = new ArrayList<>();
		List<PythonBytecode> endJumps = new ArrayList<>();
		// Compile value to compare with
		compile(ctx.test(), bytecode);
		// Compile comparisons
		for (int i=0; i<ctx.case_block().size(); i++) {
			bytecode.add(Bytecode.makeBytecode(Bytecode.DUP, ctx.start));
			putGetAttr(Arithmetics.__EQ__, bytecode, ctx.case_block(i).test().start);
			compile(ctx.case_block(i).test(), bytecode);
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.RCALL, ctx.start));
			cb.intValue = 1;
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.JUMPIFTRUE, ctx.start));
			jumps.add(cb);
		}
		// If there is else block, compile it here
		if (ctx.else_block() != null) {
			bytecode.add(Bytecode.makeBytecode(Bytecode.POP, ctx.start));
			compileSuite(ctx.else_block().suite(), bytecode, cs);
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.GOTO, ctx.start));
			endJumps.add(cb);
		}
		// Compile actual case blocks
		for (int i=0; i<ctx.case_block().size(); i++) {
			jumps.get(i).intValue = bytecode.size();
			bytecode.add(Bytecode.makeBytecode(Bytecode.POP, ctx.start));
			compileSuite(ctx.case_block(i).suite(), bytecode, cs);
			// Unlike traditional case, this one jumps to end of switch block automatically 
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.GOTO, ctx.start));
			endJumps.add(cb);
		}
		for (PythonBytecode c : endJumps) c.intValue = bytecode.size();
	}
	
	private void compileClass(ClassdefContext classdef, List<PythonBytecode> bytecode, DecoratorsContext dc) {
		
		String className = classdef.nname().getText();
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.LOADGLOBAL, classdef.start));
		cb.stringValue = "type";
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH, classdef.start));
		cb.value = new StringObject(className);
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.LOADGLOBAL, classdef.start));
		cb.stringValue = "tuple";
		int c = classdef.testlist() != null ? classdef.testlist().test().size() : 0;
		if (classdef.testlist() != null)
			for (TestContext tc : classdef.testlist().test())
				compile(tc, bytecode);
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, classdef.start));
		cb.intValue = c;
		UserFunctionObject fnc = new UserFunctionObject();
		fnc.newObject();
		Utils.putPublic(fnc, "__name__", new StringObject("$$__classBodyFncFor" + className + "$$"));
		fnc.args = new ArrayList<String>();
		
		List<PythonBytecode> fncb = new ArrayList<PythonBytecode>();
		compilingClass.push(className);
		MapObject cdc = doCompileFunction(classdef.suite(), fncb, classdef.suite().start, null);
		compilingClass.pop();
		
		Utils.putPublic(fnc, "function_defaults", new MapObject());
		
		fncb.add(cb = Bytecode.makeBytecode(Bytecode.PUSH, classdef.stop));
		cb.value = cdc;
		fncb.add(cb = Bytecode.makeBytecode(Bytecode.RETURN, classdef.stop));
		cb.intValue = 1;
		
		fnc.block = new CompiledBlockObject(fncb);
		fnc.block.newObject();
		
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH, classdef.stop));
		cb.value = fnc;
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, classdef.stop));
		cb.intValue = 0;
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, classdef.stop));
		cb.intValue = 3;
		
		if (dc != null){
			compile(dc, bytecode);
		}
		
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.SAVE_LOCAL, classdef.stop));
		cb.stringValue = className;
	}

	private void compileFunction(FuncdefContext funcdef,
			List<PythonBytecode> bytecode, DecoratorsContext dc) {
		UserFunctionObject fnc = new UserFunctionObject();
		fnc.newObject();
		
		if (funcdef.docstring() != null)
			Utils.putPublic(fnc, "__doc__", new StringObject(doGetLongString(funcdef.docstring().LONG_STRING().getText())));
		String functionName = funcdef.nname().getText();
		Utils.putPublic(fnc, "__name__", new StringObject(compilingClass.peek() == null ? functionName : compilingClass.peek() + "." + functionName));
		
		List<String> arguments = new ArrayList<String>();
		for (int i=0; i<funcdef.farg().size(); i++){
			arguments.add(funcdef.farg(i).nname().getText());
		}
		
		fnc.args = arguments;
		if (funcdef.vararg() != null){
			fnc.isVararg = true;
			fnc.vararg = funcdef.vararg().nname().getText();
		}
		
		compilingClass.push(null);
		List<PythonBytecode> fncb = new ArrayList<PythonBytecode>();
		MapObject locals = doCompileFunction(funcdef.suite(), fncb, funcdef.suite().start, null);
		compilingClass.pop();
		
		fncb.add(cb = Bytecode.makeBytecode(Bytecode.PUSH, funcdef.stop));
		cb.value = NoneObject.NONE;
		fncb.add(cb = Bytecode.makeBytecode(Bytecode.RETURN, funcdef.stop));
		cb.intValue = 1;
		
		fnc.block = new CompiledBlockObject(fncb);
		fnc.block.newObject();
		
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH, funcdef.stop));
		cb.value = fnc;
		
		bytecode.add(Bytecode.makeBytecode(Bytecode.DUP, funcdef.stop)); // function_defaults
		bytecode.add(Bytecode.makeBytecode(Bytecode.DUP, funcdef.stop)); // locals
		bytecode.add(Bytecode.makeBytecode(Bytecode.DUP, funcdef.stop)); // closure
		
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH, funcdef.stop));
		cb.value = new MapObject();

		for (int i=0; i<funcdef.farg().size(); i++){
			FargContext ctx = funcdef.farg(i);
			if (ctx.test() != null){
				bytecode.add(Bytecode.makeBytecode(Bytecode.DUP, ctx.start));
				putGetAttr("__setitem__", bytecode, ctx.start);
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH, ctx.start));
				cb.value = new StringObject(ctx.nname().getText());
				compile(ctx.test(), bytecode);
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, ctx.stop));
				cb.intValue = 2;
				bytecode.add(Bytecode.makeBytecode(Bytecode.POP, ctx.stop)); 
			}
		}
		
		bytecode.add(Bytecode.makeBytecode(Bytecode.SWAP_STACK, funcdef.stop));
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.SETATTR, funcdef.stop));
		cb.stringValue = "function_defaults";
		
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH, funcdef.stop));
		cb.value = locals;
		bytecode.add(Bytecode.makeBytecode(Bytecode.SWAP_STACK, funcdef.stop));
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.SETATTR, funcdef.stop));
		cb.stringValue = "locals";
		
		List<MapObject> ll = new ArrayList<MapObject>();
		ll.add(locals);
		for (MapObject d : Utils.reverse(environments)){
			ll.add(d);
		}
		TupleObject closure = new TupleObject(ll.toArray(new PythonObject[ll.size()]));
		closure.newObject();
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH, funcdef.stop));
		cb.value = closure;
		
		bytecode.add(Bytecode.makeBytecode(Bytecode.SWAP_STACK, funcdef.stop));
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.SETATTR, funcdef.stop));
		cb.stringValue = "closure";
		
		if (dc != null){
			compile(dc, bytecode);
		}
		
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.SAVE_LOCAL, funcdef.stop));
		cb.stringValue = functionName;
	}

	private void compile(DecoratorsContext dc, List<PythonBytecode> bytecode) {
		// function on stack
		for (int i=dc.decorator().size()-1; i>=0; i--){
			DecoratorContext d = dc.decorator(i);
			// compile decorator
			compile(d.test(), bytecode);
			if (d.arglist() != null){
				int argc = compileArguments(d.arglist(), bytecode);
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, d.stop));
				cb.intValue = argc;
			}
			// call
			bytecode.add(Bytecode.makeBytecode(Bytecode.SWAP_STACK, dc.stop));
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, dc.stop));
			cb.intValue = 1;
		}
	}

	private String doGetLongString(String text) {
		return text.substring(3, text.length()-4);
	}

	private MapObject doCompileFunction(ParseTree suite,
			List<PythonBytecode> bytecode, Token t, MapObject dict) {

		stack.push();
		bytecode.add(Bytecode.makeBytecode(Bytecode.PUSH_ENVIRONMENT, t));
		if (dict == null)
			dict = new MapObject();
		environments.add(dict);
		for (MapObject d : Utils.reverse(environments)){
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH_DICT, t)); 
			cb.mapValue = d;
		}
		bytecode.add(Bytecode.makeBytecode(Bytecode.RESOLVE_ARGS, t));
		
		if (suite instanceof SuiteContext)
			for (StmtContext c : ((SuiteContext)suite).stmt())
				compileStatement(c, bytecode, null);
		else
			for (StmtContext c : ((String_inputContext) suite).stmt())
				compileStatement(c, bytecode, null);
		

		environments.removeLast();
		stack.pop();
		
		return dict;
	}

	private void compileSimpleStatement(Simple_stmtContext sstmt, List<PythonBytecode> bytecode, ControllStack cs) {
		for (Small_stmtContext smstmt : sstmt.small_stmt())
			compileSmallStatement(smstmt, bytecode, cs);
	}

	private void compileSmallStatement(Small_stmtContext smstmt, List<PythonBytecode> bytecode, ControllStack cs) {
		// Import Statement
		if (smstmt.import_stmt() != null){
			Import_stmtContext imps = smstmt.import_stmt();
			if (imps.import_name() != null){
				Import_nameContext in = imps.import_name();
				Dotted_as_namesContext das = in.dotted_as_names();
				for (Dotted_as_nameContext dans : das.dotted_as_name())
					compileImport(dans, bytecode);
			} else {
				Import_fromContext imf = imps.import_from();
				String packageName = "";
				int i=0;
				Dotted_nameContext d = imf.dotted_name();
				for (NnameContext name : d.nname()){
					packageName += name.getText();
					if (++i != d.nname().size())
						packageName += ".";
				}
				if (imf.star() != null){
					addImport(packageName, "*", bytecode, imf.star().start);
					return;
				}
				if (imf.import_as_names() != null)
					for (Import_as_nameContext asname : imf.import_as_names().import_as_name()){
						compileImport2(asname, bytecode, packageName);
					}
			}
//		} else if (smstmt.parenthesesless_call() != null) {
//			compileParentheseslessCall(smstmt.parenthesesless_call(), bytecode);
		} else if (smstmt.pass_stmt() != null){
			bytecode.add(Bytecode.makeBytecode(Bytecode.NOP, smstmt.start));
		} else if (smstmt.expr_stmt() != null){
			compile(smstmt.expr_stmt(), bytecode);
		} else if (smstmt.print_stmt() != null){
			compile(smstmt.print_stmt(), bytecode);
		} else if (smstmt.flow_stmt() != null){
			compile(smstmt.flow_stmt(), bytecode, cs);
		}
	}

	private void compile(Flow_stmtContext ctx, List<PythonBytecode> bytecode, ControllStack cs) {
		if (ctx.return_stmt() != null)
			compile(ctx.return_stmt(), bytecode);
		else if (ctx.break_stmt() != null)
			compile(ctx.break_stmt(), bytecode, cs);
		else if (ctx.continue_stmt() != null)
			compile(ctx.continue_stmt(), bytecode, cs);
		else if (ctx.raise_stmt() != null)
			compile(ctx.raise_stmt(), bytecode);
	}

	private void compile(Break_stmtContext break_stmt, List<PythonBytecode> bytecode, ControllStack cs) {
		if ((cs == null) || (cs.size() == 0))
			throw Utils.throwException("SyntaxError", "'break' outside loop");
		cs.peek().outputBreak(break_stmt, bytecode, cs);
	}

	private void compile(Continue_stmtContext continue_stmt, List<PythonBytecode> bytecode, ControllStack cs) {
		if ((cs == null) || (cs.size() == 0))
			throw Utils.throwException("SyntaxError", "'continue' outside loop");
		cs.peek().outputContinue(continue_stmt, bytecode, cs);
	}

	private void compile(Raise_stmtContext ctx,
			List<PythonBytecode> bytecode) {
		if (ctx.test() != null)
			compile(ctx.test(), bytecode);
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.RAISE, ctx.start));
		if (ctx.test() != null)
			cb.booleanValue = true;
	}

	private void compile(Return_stmtContext ctx, List<PythonBytecode> bytecode) {
		if (compilingClass.peek() != null)
			throw Utils.throwException("SyntaxError", "return cannot be inside class definition");
		if (ctx.testlist() != null)
			compileRightHand(ctx.testlist(), bytecode);
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.RETURN, ctx.start));
		cb.intValue = 1;
	}

	private void compile(Print_stmtContext ctx,
			List<PythonBytecode> bytecode) {
		if (ctx.push() != null){
			// TODO
		} else {
			boolean eol = (ctx.endp() == null);
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.LOADGLOBAL, ctx.start));
			cb.stringValue = PythonRuntime.PRINT_JAVA;
			int tlc = ctx.test().size();
			if (tlc > 1){
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.LOADGLOBAL, ctx.start));
				cb.stringValue = TupleTypeObject.TUPLE_CALL;
			}
			
			for (TestContext tc : ctx.test())
				compile(tc, bytecode);
			
			if (tlc > 1){
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, ctx.stop));
				cb.intValue = tlc;
			}
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, ctx.stop));
			bytecode.add(Bytecode.makeBytecode(Bytecode.POP, ctx.stop));
			cb.intValue = 1;
			
			if (eol){
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.LOADGLOBAL, ctx.stop));
				cb.stringValue = PythonRuntime.PRINT_JAVA_EOL;
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, ctx.stop));
				cb.intValue = 0;
				bytecode.add(Bytecode.makeBytecode(Bytecode.POP, ctx.stop));
			}
		}
	}

	private void compile(Expr_stmtContext expr, List<PythonBytecode> bytecode) {
		if (expr.augassignexp() != null) {
			// AugAssign
			TestlistContext leftHand = expr.testlist(0);
			if (leftHand.test().size() > 1)
				throw Utils.throwException("SyntaxError", "illegal expression for augmented assignment");
			// TODO
			// Bytecode.makeBytecode(
			compileRightHand(leftHand, bytecode);
			if (expr.augassignexp().augassign().getText().equals("+="))
				putGetAttr(Arithmetics.__ADD__, bytecode, expr.start);
			else if (expr.augassignexp().augassign().getText().equals("-="))
				putGetAttr(Arithmetics.__SUB__, bytecode, expr.start);
			else if (expr.augassignexp().augassign().getText().equals("*="))
				putGetAttr(Arithmetics.__MUL__, bytecode, expr.start);
			else if (expr.augassignexp().augassign().getText().equals("/="))
				putGetAttr(Arithmetics.__DIV__, bytecode, expr.start);
			else if (expr.augassignexp().augassign().getText().equals("%="))
				putGetAttr(Arithmetics.__MOD__, bytecode, expr.start);
			else
				throw Utils.throwException("SyntaxError", "illegal augmented assignment");
			compileRightHand(expr.augassignexp().testlist(), bytecode);
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, expr.stop));
			cb.intValue = 1;
			compileAssignment(leftHand.test(0), bytecode);			
		} else {
			TestlistContext rightHand = expr.testlist(expr.testlist().size()-1);
			List<TestlistContext> rest = new ArrayList<TestlistContext>();
			for (int i=0; i<expr.testlist().size()-1; i++)
				rest.add(expr.testlist(i));
			
			Collections.reverse(rest);
			compileRightHand(rightHand, bytecode);
			
			for (TestlistContext tc : rest){
				bytecode.add(Bytecode.makeBytecode(Bytecode.DUP, tc.start));
				if (tc.test().size() > 1) {
					// x,y,z = ...
					bytecode.add(cb = Bytecode.makeBytecode(Bytecode.UNPACK_SEQUENCE, tc.start));
					cb.intValue = tc.test().size();
					for (int i=0; tc.test(i) != null; i++)
						compileAssignment(tc.test(i), bytecode);
				} else {
					// x = y
					compileAssignment(tc.test(0), bytecode);
				}
			}
			bytecode.add(Bytecode.makeBytecode(Bytecode.POP, expr.stop));
		}
	}
	
	/** 
	 * Compiles right side of assignment, leaving value of right hand on top of stack
	 * Offset can be used to skip left-side test.
	 */
	private void compileRightHand(TestlistContext testlist, List<PythonBytecode> bytecode) {
		int tlc = testlist.test().size();
		if (tlc > 1){
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.LOADGLOBAL, testlist.start));
			cb.stringValue = TupleTypeObject.TUPLE_CALL;
		}
		
		for (TestContext tc : testlist.test())
			compile(tc, bytecode);
		
		if (tlc > 1){
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, testlist.stop));
			cb.intValue = tlc;
		}
	}
	
	private void putGetAttr(String attr, List<PythonBytecode> bytecode, Token t) {
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.GETATTR, t));
		cb.stringValue = attr;
	}
	
	private void putNot(List<PythonBytecode> bytecode, Token t) {
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.TRUTH_VALUE, t));
		cb.intValue = 1;
	}

	private void compile(TestContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.lambdef() != null){
			compile(ctx.lambdef(), bytecode);
			return;
		}
		if (ctx.getChildCount() > 1){
			compileTernary(ctx, bytecode);
			return;
		}
		compile(ctx.or_test(0), bytecode);
	}

	private void compile(Or_testContext ctx, List<PythonBytecode> bytecode) {
		List<PythonBytecode> jumps = new LinkedList<>();
		int last = ctx.and_test().size() - 1;
		for (int i=0; i<last; i++) {
			compile(ctx.and_test(i), bytecode);
			bytecode.add(Bytecode.makeBytecode(Bytecode.DUP, ctx.start));
			bytecode.add(Bytecode.makeBytecode(Bytecode.TRUTH_VALUE, ctx.start));
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.JUMPIFTRUE, ctx.start));
			bytecode.add(Bytecode.makeBytecode(Bytecode.POP, ctx.start));
			jumps.add(cb);
		}
		compile(ctx.and_test(last), bytecode);
		for (PythonBytecode c : jumps)
			c.intValue = bytecode.size();
	}
	
	private void compile(And_testContext ctx, List<PythonBytecode> bytecode) {
		List<PythonBytecode> jumps = new LinkedList<>();
		int last = ctx.not_test().size() - 1;
		for (int i=0; i<last; i++) {
			compile(ctx.not_test(i), bytecode);
			bytecode.add(Bytecode.makeBytecode(Bytecode.DUP, ctx.start));
			bytecode.add(Bytecode.makeBytecode(Bytecode.TRUTH_VALUE, ctx.start));
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.JUMPIFFALSE, ctx.start));
			bytecode.add(Bytecode.makeBytecode(Bytecode.POP, ctx.start));
			jumps.add(cb);
		}
		compile(ctx.not_test(last), bytecode);
		for (PythonBytecode c : jumps)
			c.intValue = bytecode.size();
	}
	
	private void compile(Not_testContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.not_test() == null) {
			compile(ctx.comparison(), bytecode);
			return;
		}
		int amount = countNots(ctx, bytecode);
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.TRUTH_VALUE, ctx.start));
		cb.intValue = amount % 2;
	}
	
	private int countNots(Not_testContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.not_test() != null)
			return 1 + countNots(ctx.not_test(), bytecode);
		compile(ctx.comparison(), bytecode);
		return 0;
	}
	
	private void compile(ComparisonContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.getChildCount() > 1){
			if (ctx.getChild(1).getText().equals("in") || ctx.getChild(1).getText().equals("notin")){
				String operation = ObjectTypeObject.__CONTAINS__;
				compile(ctx.expr(1), bytecode);
				putGetAttr(operation, bytecode, ((ExprContext) ctx.getChild(2)).start);
				compile((ExprContext) ctx.getChild(0), bytecode);
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, ((ExprContext) ctx.getChild(2)).start));
				cb.intValue = 1;
				if (ctx.getChild(1).getText().equals("notin"))
					putNot(bytecode, ((ExprContext) ctx.getChild(2)).start);
				return;
			}
			
			compile(ctx.expr(0), bytecode);
			for (int i=1; i<ctx.getChildCount(); i+=2){
				String operation = null;
				if (ctx.getChild(i).getText().equals("<"))
					operation = Arithmetics.__LT__;
				else if (ctx.getChild(i).getText().equals(">"))
					operation = Arithmetics.__GT__;
				else if (ctx.getChild(i).getText().equals("=="))
					operation = Arithmetics.__EQ__;
				else if (ctx.getChild(i).getText().equals(">="))
					operation = Arithmetics.__GE__;
				else if (ctx.getChild(i).getText().equals("<="))
					operation = Arithmetics.__LE__;
				else if (ctx.getChild(i).getText().equals("<>"))
					operation = Arithmetics.__NE__;
				else if (ctx.getChild(i).getText().equals("!="))
					operation = Arithmetics.__NE__;
				else if (ctx.getChild(i).getText().equals("is") || ctx.getChild(i).getText().equals("isnot")) {
					bytecode.add(cb = Bytecode.makeBytecode(Bytecode.LOADGLOBAL, ((ExprContext) ctx.getChild(i+1)).start));
					cb.stringValue = ObjectTypeObject.IS;
					bytecode.add(cb = Bytecode.makeBytecode(Bytecode.SWAP_STACK, ((ExprContext) ctx.getChild(i+1)).start));
					compile((ExprContext) ctx.getChild(i+1), bytecode);
					bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, ((ExprContext) ctx.getChild(i+1)).start));
					cb.intValue = 2;
					if (ctx.getChild(i).getText().equals("isnot"))
						putNot(bytecode, ((ExprContext) ctx.getChild(i+1)).start);
					return;
				} else
					throw Utils.throwException("SyntaxError", "unsupported comparison operation");
				putGetAttr(operation, bytecode, ((ExprContext) ctx.getChild(i+1)).start);
				compile((ExprContext) ctx.getChild(i+1), bytecode);
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, ((ExprContext) ctx.getChild(i+1)).start));
				cb.intValue = 1;
				
			}
		} else 
			compile(ctx.expr(0), bytecode);
	}
	
	private void compile(ExprContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.getChildCount() > 1){
			compile(ctx.xor_expr(0), bytecode);
			String operation = Arithmetics.__OR__;
			for (int i=1; i<ctx.xor_expr().size(); i++){
				putGetAttr(operation, bytecode, ctx.xor_expr(i).start);
				compile(ctx.xor_expr(i), bytecode);
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, ctx.xor_expr(i).stop));
				cb.intValue = 1;
			}
		} else 
			compile(ctx.xor_expr(0), bytecode);
	}
	
	private void compile(Xor_exprContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.getChildCount() > 1){
			compile(ctx.and_expr(0), bytecode);
			String operation = Arithmetics.__XOR__;
			for (int i=1; i<ctx.and_expr().size(); i++){
				putGetAttr(operation, bytecode, ctx.and_expr(i).start);
				compile(ctx.and_expr(i), bytecode);
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, ctx.and_expr(i).stop));
				cb.intValue = 1;
			}
		} else 
			compile(ctx.and_expr(0), bytecode);
	}


	private void compile(And_exprContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.getChildCount() > 1){
			compile(ctx.shift_expr(0), bytecode);
			String operation = Arithmetics.__AND__;
			for (int i=1; i<ctx.shift_expr().size(); i++){
				putGetAttr(operation, bytecode, ctx.shift_expr(i).start);
				compile(ctx.shift_expr(i), bytecode);
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, ctx.shift_expr(i).stop));
				cb.intValue = 1;
			}
		} else 
			compile(ctx.shift_expr(0), bytecode);
	}
	
	private void compile(Shift_exprContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.getChildCount() > 1){
			compile(ctx.arith_expr(0), bytecode);
			for (int i=1; i<ctx.getChildCount(); i+=2){
				String operation = ctx.getChild(i).getText().equals("<<") ? Arithmetics.__LSHIFT__ : Arithmetics.__RSHIFT__;
				putGetAttr(operation, bytecode, ((Arith_exprContext) ctx.getChild(i+1)).start);
				compile((Arith_exprContext) ctx.getChild(i+1), bytecode);
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, ((Arith_exprContext) ctx.getChild(i+1)).stop));
				cb.intValue = 1;
			}
		} else 
			compile(ctx.arith_expr(0), bytecode);
	}
	
	private void compile(Arith_exprContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.getChildCount() > 1){
			compile(ctx.term(0), bytecode);
			for (int i=1; i<ctx.getChildCount(); i+=2){
				String operation = ctx.getChild(i).getText().equals("+") ? Arithmetics.__ADD__ : Arithmetics.__SUB__;
				putGetAttr(operation, bytecode, ((TermContext)ctx.getChild(i+1)).start);
				compile((TermContext)ctx.getChild(i+1), bytecode);
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, ((TermContext)ctx.getChild(i+1)).stop));
				cb.intValue = 1;
			}
		} else 
			compile(ctx.term(0), bytecode);
	}
	
	private void compile(TermContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.getChildCount() > 1){
			compile(ctx.factor(0), bytecode);
			for (int i=1; i<ctx.getChildCount(); i+=2){
				String operation = ctx.getChild(i).getText();
				if (operation.equals("*"))
					operation = Arithmetics.__MUL__;
				if (operation.equals("/"))
					operation = Arithmetics.__DIV__;
				if (operation.equals("%"))
					operation = Arithmetics.__MOD__;
				putGetAttr(operation, bytecode, ((FactorContext)ctx.getChild(i+1)).start);
				compile((FactorContext)ctx.getChild(i+1), bytecode);
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, ((FactorContext)ctx.getChild(i+1)).stop));
				cb.intValue = 1;
			}
		} else 
			compile(ctx.factor(0), bytecode);
	}
	
	private void compile(FactorContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.factor() != null){
			if (ctx.getText().startsWith("~")){
				putGetAttr(Arithmetics.__NOT__, bytecode, ctx.start);
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, ctx.start));
				cb.intValue = 0;
				return;
			}
			
			String operation = null;
			if (ctx.getText().startsWith("+"))
				operation = Arithmetics.__ADD__;
			if (ctx.getText().startsWith("-"))
				operation = Arithmetics.__SUB__;
			
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH, ctx.factor().start));
			cb.value = IntObject.valueOf(0);
			putGetAttr(operation, bytecode, ctx.factor().start);
			compile(ctx.factor(), bytecode);
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, ctx.factor().stop));
			cb.intValue = 1;
		} else 
			compile(ctx.power(), bytecode);
	}
	
	private void compile(PowerContext ctx, List<PythonBytecode> bytecode) {
		compile(ctx.atom(), bytecode);
		if (ctx.trailer().size() > 0){
			for (TrailerContext tc : ctx.trailer()){
				compile(tc, bytecode);
			}
		}
		if (ctx.factor() != null){
			String operation = Arithmetics.__POW__;
			putGetAttr(operation, bytecode, ctx.factor().start);
			compile(ctx.factor(), bytecode);
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, ctx.factor().stop));
			cb.intValue = 1;
		}
	}

	private void compile(TrailerContext tc, List<PythonBytecode> bytecode) {
		if (tc.getText().startsWith("(")){
			if (tc.arglist() == null){
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, tc.stop));
				cb.intValue = 0;
			} else {
				int argc = compileArguments(tc.arglist(), bytecode);
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, tc.stop));
				cb.intValue = argc;
			}
		} else if (tc.getText().startsWith("[")) {
			compileSubscript(tc.subscriptlist(), bytecode, tc.start);
		} else {			
			putGetAttr(tc.NAME().getText(), bytecode, tc.start);
		}
	}

	private void compileSubscript(SubscriptlistContext sc,
			List<PythonBytecode> bytecode, Token pt) {
		putGetAttr("__getitem__", bytecode, pt);
		int tlc = sc.subscript().size();
		if (tlc > 1){
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.LOADGLOBAL, sc.start));
			cb.stringValue = TupleTypeObject.TUPLE_CALL;
		}
		for (SubscriptContext s : sc.subscript()){
			compile(s, bytecode);
		}
		if (tlc > 1){
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, sc.stop));
			cb.intValue = tlc;
		}
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, sc.stop));
		cb.intValue = 1;
	}

	private void compile(SubscriptContext s, List<PythonBytecode> bytecode) {
		if (s.ellipsis() != null){
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH, s.start));
			cb.value = EllipsisObject.ELLIPSIS;
			return;
		}
		
		if (s.stest() != null){
			compile(s.stest().test(), bytecode);
		} else {
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.LOADGLOBAL, s.start));
			cb.stringValue = SliceTypeObject.SLICE_CALL;
			if (s.test().size() == 0){
				for (int i=0; i<3; i++){
					bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH, s.start));
					cb.value = NoneObject.NONE;
				}
			} else if (s.test().size() == 2){
				if (s.sliceop() == null){
					compile(s.test(0), bytecode);
					compile(s.test(1), bytecode);
					bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH, s.stop));
					cb.value = NoneObject.NONE;
				} else {
					compile(s.test(0), bytecode);
					compile(s.test(1), bytecode);
					compile(s.sliceop().test(), bytecode);
				}
			} else if (s.test().size() == 1){
				if (s.getText().startsWith(":")){
					bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH, s.start));
					cb.value = NoneObject.NONE;
					compile(s.test(0), bytecode);
					bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH, s.stop));
					cb.value = NoneObject.NONE;
				} else {
					compile(s.test(0), bytecode);
					bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH, s.stop));
					cb.value = NoneObject.NONE;
					bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH, s.stop));
					cb.value = NoneObject.NONE;
				}
			}
			
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, s.stop));
			cb.intValue = 3;
		}
	}

	private int compileArguments(ArglistContext arglist,
			List<PythonBytecode> bytecode) {
		for (ArgumentContext ac : arglist.argument())
			compile(ac, bytecode);
		if (arglist.test() != null){
			compile(arglist.test(), bytecode);
			return -(arglist.argument().size() + 1);
		}
		return arglist.argument().size();
	}

	private void compile(ArgumentContext ac, List<PythonBytecode> bytecode) {
		if (ac.test().size() == 1){
			if (ac.comp_for() == null){
				compile(ac.test(0), bytecode);
			} else {
				// TODO
			}
		} else {
			// TODO
		}
	}

	private void compile(AtomContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.nname() != null){
			String name = ctx.nname().getText();
			if (stack.isGlobalVariable(name))
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.LOADGLOBAL, ctx.start));
			else
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.LOAD, ctx.start));
			cb.stringValue = name;
		} else if (ctx.number() != null) {
			NumberContext nb = ctx.number();
			if (nb.integer() != null){
				IntegerContext ic = nb.integer();
				String numberValue = nb.integer().getText();
				BigInteger bi = null;
				
				if (ic.BIN_INTEGER() != null)
					bi = new BigInteger(numberValue, 2);
				if (ic.OCT_INTEGER() != null)
					bi = new BigInteger(numberValue, 8);
				if (ic.DECIMAL_INTEGER() != null)
					bi = new BigInteger(numberValue, 10);
				if (ic.HEX_INTEGER() != null)
					bi = new BigInteger(numberValue, 16);
				
				IntObject o = IntObject.valueOf(bi.longValue());
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH, ctx.start));
				cb.value = o;
			} else if (nb.FLOAT_NUMBER() != null){
				String numberValue = nb.FLOAT_NUMBER().getText();
				RealObject r = new RealObject(Double.parseDouble(numberValue));
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH, ctx.start));
				cb.value = r;
			} else {
				String n = nb.IMAG_NUMBER().getText();
				ComplexObject c = new ComplexObject(0, Double.parseDouble(n.substring(0, n.length()-1)));
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH, ctx.start));
				cb.value = c;
			}			
		} else if (ctx.string().size() != 0){
			StringBuilder bd = new StringBuilder();
			for (StringContext s : ctx.string()){
				if (s.stringLiterar().SHORT_STRING() != null){
					String ss = s.getText();
					ss = ss.substring(1, ss.length()-1);
					bd.append(ss);
				} else {
					String ss = s.getText();
					ss = ss.substring(3, ss.length()-4);
					bd.append(ss);
				}
			}
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH, ctx.start));
			cb.value = new StringObject(bd.toString());
		} else if (ctx.getText().startsWith("(")){
			compile(ctx.testlist_comp(), bytecode);
		} else if (ctx.getText().startsWith("[")){
			compile(ctx.listmaker(), bytecode, ctx.getStart());
		} else if (ctx.getText().startsWith("{")){
			compile(ctx.dictorsetmaker(), bytecode, ctx.getStart());
		}
	}
	
	private void compile(DictentryContext dcx, List<PythonBytecode> bytecode) {
		bytecode.add(Bytecode.makeBytecode(Bytecode.DUP, dcx.start));
		compile(dcx.test(0), bytecode);
		compile(dcx.test(1), bytecode);
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, dcx.start));
		cb.intValue = 2;
		bytecode.add(Bytecode.makeBytecode(Bytecode.POP, dcx.stop));
	}

	private void compile(Testlist_compContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.comp_for() != null){
			// TODO
		} else {
			if ((ctx.test().size() > 1) || ctx.children.get(ctx.children.size() - 1).getText().equals(",")) {
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.LOADGLOBAL, ctx.start));
				cb.stringValue = TupleTypeObject.TUPLE_CALL;
				for (TestContext i : ctx.test())
					compile(i, bytecode);
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, ctx.stop));
				cb.intValue = ctx.test().size();
			} else {
				compile(ctx.test(0), bytecode);
			}
		}
	}

	private void compile(ListmakerContext listmaker, List<PythonBytecode> bytecode, Token token) {
		if (listmaker == null) {
			// [ ]
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.LOADGLOBAL, token));
			cb.stringValue = ListTypeObject.LIST_CALL;
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, token));
			cb.intValue = 0;
			return;
		}
		if (listmaker.list_for() != null) {
			// [ x for x in somethingiterable ]
			List_forContext fCtx = listmaker.list_for();
			// Generate empty list
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.LOADGLOBAL, listmaker.start));
			cb.stringValue = ListTypeObject.LIST_CALL;
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, listmaker.stop));
			cb.intValue = 0;
			/** Stack: TOP -> list */
			bytecode.add(Bytecode.makeBytecode(Bytecode.DUP, listmaker.stop));
			// Get append method
			putGetAttr("append", bytecode, fCtx.start);
			/** Stack: TOP -> list -> list.append */
			compileListFor(listmaker.test(0), fCtx, bytecode);
			bytecode.add(Bytecode.makeBytecode(Bytecode.POP, fCtx.start));
			/** Stack: TOP -> list */			
		} else {
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.LOADGLOBAL, listmaker.start));
			cb.stringValue = ListTypeObject.LIST_CALL;
			for (TestContext t : listmaker.test())
				compile(t, bytecode);
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, listmaker.stop));
			cb.intValue = listmaker.test().size();
		}
	}
	
	/** 
	 * Compiles list_for part of list comprehension.
	 * Note: When this block is compiled, TOP -> list -> list.append is required to be
	 * prepared on stack. Same thing is left after block is executed
	 */
	private void compileListFor(TestContext expression, List_forContext fCtx, List<PythonBytecode> bytecode) {
		// Compile somethingiterable & get iterator
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.LOADGLOBAL, fCtx.start));
		cb.stringValue = "iter";
		compileRightHand(fCtx.testlist(), bytecode);
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.RCALL, fCtx.testlist().start));
		cb.intValue = 1;
		putGetAttr("next", bytecode, fCtx.start);
		// Compile getting item from iterator
		/** Stack: TOP -> list -> list.append -> iterator.next */
		int loopStart = bytecode.size();
		PythonBytecode acceptIter;
		bytecode.add(Bytecode.makeBytecode(Bytecode.ECALL, fCtx.start));
		bytecode.add(acceptIter = Bytecode.makeBytecode(Bytecode.ACCEPT_ITER, fCtx.start));
		// Compile assigning to variable
		compileAssignment(fCtx.exprlist(), bytecode);
		/** Stack: TOP -> list -> list.append -> iterator.next */
		// Compile optional list_iter
		boolean compileStoring = true;
		if (fCtx.list_iter() != null) {
			List_iterContext lIter = fCtx.list_iter();
			if (lIter.list_if() != null) {
				compile(lIter.list_if().test(), bytecode);
				bytecode.add(Bytecode.makeBytecode(Bytecode.TRUTH_VALUE, fCtx.start));
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.JUMPIFFALSE, fCtx.start));
				cb.intValue = loopStart;
			} else if (lIter.list_for() != null) {
				compileStoring = false;
				/** Stack: TOP -> list -> list.append -> iterator.next */
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.DUP, fCtx.start));
				cb.intValue = 1;
				/** Stack: TOP -> list -> list.append -> iterator.next -> list.append */
				compileListFor(expression, lIter.list_for(), bytecode); 
				bytecode.add(Bytecode.makeBytecode(Bytecode.POP, fCtx.start));
			}
		}
		if (compileStoring) {
			// Compile expression
			compile(expression, bytecode);
			// Compile storing to list
			/** Stack: TOP -> list -> list.append -> iterator.next -> expression */
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.DUP, fCtx.start));
			cb.intValue = 2;
			/** Stack: TOP -> list -> list.append -> iterator.next -> expression -> list.append */
			bytecode.add(Bytecode.makeBytecode(Bytecode.SWAP_STACK, fCtx.start));
			/** Stack: TOP -> list -> list.append -> iterator.next -> list.append -> expression */
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.RCALL, fCtx.start));
			cb.intValue = 1;
			bytecode.add(Bytecode.makeBytecode(Bytecode.POP, fCtx.start));
		}
		/** Stack: TOP -> list -> list.append -> iterator.next  */
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.GOTO, fCtx.start));
		cb.intValue = loopStart;
		acceptIter.intValue = bytecode.size();
		/** Stack: TOP -> list -> list.append -> iterator.next */
		bytecode.add(Bytecode.makeBytecode(Bytecode.POP, fCtx.start));
	}

	private void compile(DictorsetmakerContext dictorsetmaker, List<PythonBytecode> bytecode, Token t) {
		if ((dictorsetmaker == null) || ((dictorsetmaker.dictentry(0) != null) && (dictorsetmaker.comp_for() == null))) {
			// { } or { x:y, a:b, h:i }
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.LOADGLOBAL, t));
			cb.stringValue = DictTypeObject.DICT_CALL;
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, t));
			cb.intValue = 0;
			if ( dictorsetmaker != null) {
				// { x:y, a:b, h:i }
				bytecode.add(Bytecode.makeBytecode(Bytecode.DUP, dictorsetmaker.start));
				putGetAttr(MapObject.__SETITEM__, bytecode, dictorsetmaker.start);
				if (dictorsetmaker.comp_for() != null){
					// TODO
					throw new NotImplementedException();
				} else 
					for (DictentryContext dcx : dictorsetmaker.dictentry())
						compile(dcx, bytecode);
				bytecode.add(Bytecode.makeBytecode(Bytecode.POP, t));
			}
		} else {
			// { x : y for x in somethingiterable } or { x for x in somethingiterable }
			Comp_forContext cCtx = dictorsetmaker.comp_for();
			List_forContext fCtx = dictorsetmaker.list_for();
			if (dictorsetmaker.dictentry(0) == null) {
				// { x for x in somethingiterable }
				// Generate empty list
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.LOADGLOBAL, dictorsetmaker.start));
				cb.stringValue = ListTypeObject.LIST_CALL;
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, dictorsetmaker.stop));
				cb.intValue = 0;
			} else {
				// { x : y for x in somethingiterable }
				// Generate empty dict
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.LOADGLOBAL, dictorsetmaker.start));
				cb.stringValue = DictTypeObject.DICT_CALL;
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, dictorsetmaker.stop));
				cb.intValue = 0;
			}
			/** Stack: TOP -> dict */
			bytecode.add(Bytecode.makeBytecode(Bytecode.DUP, dictorsetmaker.stop));
			/** Stack: TOP -> dict -> dict.put */
			if (dictorsetmaker.dictentry(0) == null) {
				// set - Get append method for list
				putGetAttr("append", bytecode, fCtx.start);
				compileListFor(dictorsetmaker.test(0), fCtx, bytecode);
				/** Stack: TOP -> list -> list.append */
				bytecode.add(Bytecode.makeBytecode(Bytecode.POP, dictorsetmaker.start));
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.LOADGLOBAL, dictorsetmaker.stop));
				cb.stringValue = "set";
				bytecode.add(Bytecode.makeBytecode(Bytecode.SWAP_STACK, dictorsetmaker.stop));
				/** Stack: TOP -> set -> list */
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.RCALL, dictorsetmaker.stop));
				cb.intValue = 1;
				/** Stack: TOP -> set(list) */
			} else {
				// dict - Get setitem method
				putGetAttr("__setitem__", bytecode, cCtx.start);
				compileCompFor(dictorsetmaker.dictentry(0), cCtx, bytecode);
				/** Stack: TOP -> dict -> dict.put */
				bytecode.add(Bytecode.makeBytecode(Bytecode.POP, dictorsetmaker.start));
				/** Stack: TOP -> dict */			
			}
		}
	}
	
	/** 
	 * Compiles list_for part of list comprehension.
	 * Note: When this block is compiled, TOP -> list -> list.append is required to be
	 * prepared on stack. Same thing is left after block is executed
	 */
	private void compileCompFor(DictentryContext dictentryContext, Comp_forContext cCtx, List<PythonBytecode> bytecode) {
		// Compile somethingiterable & get iterator
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.LOADGLOBAL, cCtx.start));
		cb.stringValue = "iter";
		compile(cCtx.or_test(), bytecode);
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.RCALL, cCtx.or_test().start));
		cb.intValue = 1;
		putGetAttr("next", bytecode, cCtx.start);
		// Compile getting item from iterator
		/** Stack: TOP -> dict -> dict.put -> iterator.next */
		int loopStart = bytecode.size();
		PythonBytecode acceptIter;
		bytecode.add(Bytecode.makeBytecode(Bytecode.ECALL, cCtx.start));
		bytecode.add(acceptIter = Bytecode.makeBytecode(Bytecode.ACCEPT_ITER, cCtx.start));
		// Compile assigning to variable
		compileAssignment(cCtx.exprlist(), bytecode);
		/** Stack: TOP -> dict -> dict.put -> iterator.next */
		// Compile optional list_iter
		boolean compileStoring = true;
		if (cCtx.comp_iter() != null) {
			Comp_iterContext lIter = cCtx.comp_iter();
			if (lIter.comp_if() != null) {
				compile(lIter.comp_if().test(), bytecode);
				bytecode.add(Bytecode.makeBytecode(Bytecode.TRUTH_VALUE, cCtx.start));
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.JUMPIFFALSE, cCtx.start));
				cb.intValue = loopStart;
			} else if (lIter.comp_for() != null) {
				compileStoring = false;
				/** Stack: TOP -> dict -> dict.put -> iterator.next */
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.DUP, cCtx.start));
				cb.intValue = 1;
				/** Stack: TOP -> dict -> dict.put -> iterator.next -> dict.put */
				compileCompFor(dictentryContext, lIter.comp_for(), bytecode); 
				bytecode.add(Bytecode.makeBytecode(Bytecode.POP, cCtx.start));
			}
		}
		if (compileStoring) {
			/** Stack: TOP -> dict -> dict.put -> iterator.next */
			// Grap dict.put
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.DUP, cCtx.start));
			cb.intValue = 1;
			/** Stack: TOP -> dict -> dict.put -> iterator.next -> dict.put */
			// Compile expression
			compile(dictentryContext.test(1), bytecode);
			/** Stack: TOP -> dict -> dict.put -> iterator.next -> dict.put -> value */
			compile(dictentryContext.test(0), bytecode);
			// Compile storing to list
			/** Stack: TOP -> dict -> dict.put -> iterator.next -> dict.put -> value -> key */
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.RCALL, cCtx.start));
			cb.intValue = 2;
			bytecode.add(Bytecode.makeBytecode(Bytecode.POP, cCtx.start));
		}
		/** Stack: TOP -> dict -> dict.put -> iterator.next  */
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.GOTO, cCtx.start));
		cb.intValue = loopStart;
		acceptIter.intValue = bytecode.size();
		/** Stack: TOP -> dict -> dict.put -> iterator.next */
		bytecode.add(Bytecode.makeBytecode(Bytecode.POP, cCtx.start));
	}

	private void compileTernary(TestContext ctx, List<PythonBytecode> bytecode) {
		// TODO Auto-generated method stub
		
	}

	private void compile(LambdefContext ctx, List<PythonBytecode> bytecode) {
		UserFunctionObject fnc = new UserFunctionObject();
		fnc.newObject();
		
		String functionName = "lambda";
		Utils.putPublic(fnc, "__name__", new StringObject(compilingClass.peek() == null ? functionName : compilingClass.peek() + "." + functionName));
		
		List<String> arguments = new ArrayList<String>();
		for (int i=0; i<ctx.farg().size(); i++){
			arguments.add(ctx.farg(i).nname().getText());
		}
		
		fnc.args = arguments;
		if (ctx.vararg() != null){
			fnc.isVararg = true;
			fnc.vararg = ctx.vararg().nname().getText();
		}
		
		List<PythonBytecode> fncb = new ArrayList<PythonBytecode>();
		compilingClass.push(null);
		MapObject locals = doCompileFunction(ctx.suite(), fncb, ctx.suite().start, null);
		compilingClass.pop();
		
		if (fncb.get(fncb.size()-1) instanceof Pop){
			fncb.remove(fncb.size()-1);
			fncb.add(cb = Bytecode.makeBytecode(Bytecode.RETURN, ctx.stop));
			cb.intValue = 1;	
		} else {
			fncb.add(cb = Bytecode.makeBytecode(Bytecode.PUSH, ctx.stop));
			cb.value = NoneObject.NONE;
			fncb.add(cb = Bytecode.makeBytecode(Bytecode.RETURN, ctx.stop));
			cb.intValue = 1;	
		}
		
		fnc.block = new CompiledBlockObject(fncb);
		fnc.block.newObject();
		
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH, ctx.stop));
		cb.value = fnc;
		
		bytecode.add(Bytecode.makeBytecode(Bytecode.DUP, ctx.stop)); // function_defaults
		bytecode.add(Bytecode.makeBytecode(Bytecode.DUP, ctx.stop)); // locals
		bytecode.add(Bytecode.makeBytecode(Bytecode.DUP, ctx.stop)); // closure
		
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH, ctx.stop));
		cb.value = new MapObject();

		for (int i=0; i<ctx.farg().size(); i++){
			FargContext fctx = ctx.farg(i);
			if (fctx.test() != null){
				bytecode.add(Bytecode.makeBytecode(Bytecode.DUP, fctx.start));
				putGetAttr("__setitem__", bytecode, fctx.start);
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH, fctx.start));
				cb.value = new StringObject(fctx.nname().getText());
				compile(fctx.test(), bytecode);
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, fctx.stop));
				cb.intValue = 2;
				bytecode.add(Bytecode.makeBytecode(Bytecode.POP, fctx.stop));
			}
		}
		
		bytecode.add(Bytecode.makeBytecode(Bytecode.SWAP_STACK, ctx.stop));
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.SETATTR, ctx.stop));
		cb.stringValue = "function_defaults";
		
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH, ctx.stop));
		cb.value = locals;
		bytecode.add(Bytecode.makeBytecode(Bytecode.SWAP_STACK, ctx.stop));
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.SETATTR, ctx.stop));
		cb.stringValue = "locals";
		
		List<MapObject> ll = new ArrayList<MapObject>();
		ll.add(locals);
		for (MapObject d : Utils.reverse(environments)){
			ll.add(d);
		}
		TupleObject closure = new TupleObject(ll.toArray(new PythonObject[ll.size()]));
		closure.newObject();
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH, ctx.stop));
		cb.value = closure;
		
		bytecode.add(Bytecode.makeBytecode(Bytecode.SWAP_STACK, ctx.stop));
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.SETATTR, ctx.stop));
		cb.stringValue = "closure";
	}

	/** Generates bytecode that stores top of stack into whatever is passed as parameter */
	private void compileAssignment(TestlistContext tc, List<PythonBytecode> bytecode) {
		if (tc.test().size() > 1) {
			// x,y,z = ...
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.UNPACK_SEQUENCE));
			cb.intValue = tc.test().size();
			for (int i=0; tc.test(i) != null; i++)
				compileAssignment(tc.test(i), bytecode);
		} else {
			// x = y
			compileAssignment(tc.test(0), bytecode);
		}
	}

	private void compileAssignment(TestContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.lambdef() != null)
			throw Utils.throwException("SyntaxError", "can't assign to lambda");
		if (ctx.or_test(1) != null)
			throw Utils.throwException("SyntaxError", "can't assign to operator");
		compileAssignment(ctx.or_test(0), bytecode);
	}

	/** Generates bytecode that stores top of stack into whatever is passed as parameter */ 
	private void compileAssignment(ExprlistContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.expr().size() > 1) {
			throw Utils.throwException("SyntaxError", "can't assign to tuple");
		}
		compileAssignment(ctx.expr(0), bytecode);
	}
	
	private void compileAssignment(Or_testContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.and_test(1) != null)
			throw Utils.throwException("SyntaxError", "can't assign to operator");
		compileAssignment(ctx.and_test(0), bytecode);
	}

	private void compileAssignment(And_testContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.not_test(1) != null)
			throw Utils.throwException("SyntaxError", "can't assign to operator");
		compileAssignment(ctx.not_test(0), bytecode);		
	}

	private void compileAssignment(Not_testContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.not_test() != null)
			throw Utils.throwException("SyntaxError", "can't assign to operator");
		compileAssignment(ctx.comparison(), bytecode);		
	}

	private void compileAssignment(ComparisonContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.expr(1) != null)
			throw Utils.throwException("SyntaxError", "can't assign to comparison");
		compileAssignment(ctx.expr(0), bytecode);		
	}

	private void compileAssignment(ExprContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.xor_expr(1) != null)
			throw Utils.throwException("SyntaxError", "can't assign to operator");
		compileAssignment(ctx.xor_expr(0), bytecode);
	}

	private void compileAssignment(Xor_exprContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.and_expr(1) != null)
			throw Utils.throwException("SyntaxError", "can't assign to operator");
		compileAssignment(ctx.and_expr(0), bytecode);
	}

	private void compileAssignment(And_exprContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.shift_expr(1) != null)
			throw Utils.throwException("SyntaxError", "can't assign to operator");
		compileAssignment(ctx.shift_expr(0), bytecode);
	}

	private void compileAssignment(Shift_exprContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.arith_expr(1) != null)
			throw Utils.throwException("SyntaxError", "can't assign to operator");
		compileAssignment(ctx.arith_expr(0), bytecode);
	}

	private void compileAssignment(Arith_exprContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.term(1) != null)
			throw Utils.throwException("SyntaxError", "can't assign to operator");
		compileAssignment(ctx.term(0), bytecode);
	}
	
	private void compileAssignment(TermContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.factor(1) != null)
			throw Utils.throwException("SyntaxError", "can't assign to operator");
		compileAssignment(ctx.factor(0), bytecode);
	}
	
	private void compileAssignment(FactorContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.factor() != null)
			throw Utils.throwException("SyntaxError", "can't assign to operator");
		compileAssignment(ctx.power(), bytecode);
	}

	private void compileAssignment(PowerContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.factor() != null)
			throw Utils.throwException("SyntaxError", "can't assign to operator");
		if (ctx.trailer().size() > 0)
			compileTrailers(ctx.atom(), ctx.trailer(), 0, bytecode);
		else
			compileAssignment(ctx.atom(), bytecode);
	}

	private void compileTrailers(AtomContext atom, List<TrailerContext> trailers, int offset, List<PythonBytecode> bytecode) {
		if (offset == trailers.size() - 1) {
			// Last trailer - set item
			if (trailers.size() == 1) {
				// Last trailer is also first one
				compile(atom, bytecode);
			}
			TrailerContext t = trailers.get(offset);
			if (t.arglist() != null) {
				// ... xyz(something)
				throw Utils.throwException("SyntaxError", "can't assign to function call");
			} else if (t.subscriptlist() != null) {
				// ... xyz[something]
				if (t.subscriptlist().subscript().size() > 1)
					throw Utils.throwException("SyntaxError", "list indices must be integers, not tuple");
				SubscriptContext s = t.subscriptlist().subscript(0);
				if (s.stest() != null) {
					// xyz[a] = ...
					putGetAttr("__setitem__", bytecode, s.stest().start);
					bytecode.add(Bytecode.makeBytecode(Bytecode.SWAP_STACK, s.stest().start));
					compile(s.stest().test(), bytecode);
					bytecode.add(Bytecode.makeBytecode(Bytecode.SWAP_STACK, s.stest().stop));
					bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, s.stest().stop));
					cb.intValue = 2;
				} else {
					// xyz[a:b] = ...
					throw Utils.throwException("SyntaxError", "assignment to splice not yet implemented");
				}
			} else {
				// ... xyz.something
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH, t.start));
				cb.value = new StringObject(t.NAME().toString());
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.SETATTR, t.start));
			}
		} else if (offset == 0) {
			// First trailer - push atom on stack
			compile(atom, bytecode);
			compile(trailers.get(0), bytecode);
			compileTrailers(atom, trailers, offset + 1, bytecode);
		} else {
			// Not last - still get item
			compile(trailers.get(offset), bytecode);
			compileTrailers(atom, trailers, offset + 1, bytecode);
		}
	}
	
	private void compileAssignment(AtomContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.listmaker() != null)
			throw Utils.throwException("SyntaxError", "can't assign to generator expression");
		if (ctx.dictorsetmaker() != null)
			throw Utils.throwException("SyntaxError", "can't assign to generator literal");
		if ( (ctx.number() != null) || (ctx.string().size() > 0) )
			throw Utils.throwException("SyntaxError", "can't assign to literal");
		if (ctx.testlist_comp() != null)
			throw Utils.throwException("SyntaxError", "can't assign to generator expression");
		compileAssignment(ctx.nname(), bytecode);
	}

	private void compileAssignment(NnameContext nname, List<PythonBytecode> bytecode) {
		String name = nname.getText();
		if (stack.isGlobalVariable(name))
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.SAVEGLOBAL, nname.start));
		else
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.SAVE, nname.start));
		cb.stringValue = name;
	}

	private void compileImport2(Import_as_nameContext asname,
			List<PythonBytecode> bytecode, String packageNameBase) {
		String as;
		if (asname.children.size() == 3){
			as = asname.nname().get(1).getText();
		} else {
			as = asname.nname().get(0).getText();
		}
		
		addImport(packageNameBase + "." + asname.nname().get(0).getText(), as, bytecode, asname.start);
	}

	private void compileImport(Dotted_as_nameContext dans, List<PythonBytecode> bytecode) {
		String as = null;
		NnameContext t = dans.nname();
		if (t != null){
			as = t.getText();
		}
		
		String packageName = "";
		int i=0;
		Dotted_nameContext d = dans.dotted_name();
		for (NnameContext name : d.nname()){
			packageName += name.getText();
			if (++i != d.nname().size()){
				packageName += ".";
			} else if (as == null)
				as = name.getText();
		}
		
		addImport(packageName, as, bytecode, dans.start);
	}

	private void addImport(String packageName, String as,
			List<PythonBytecode> bytecode, Token t) {
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.IMPORT, t));
		cb.stringValue2 = packageName;
		cb.stringValue = as;
	}
	
	private interface ControllStackItem {
		public void outputBreak(Break_stmtContext ctx, List<PythonBytecode> bytecode, ControllStack cs);
		public void outputContinue(Continue_stmtContext ctx, List<PythonBytecode> bytecode, ControllStack cs);
		public void outputFinallyBreakBlock(Try_stmtContext ctx, List<PythonBytecode> bytecode, ControllStack cs);
		public void outputFinallyContinueBlock(Try_stmtContext ctx, List<PythonBytecode> bytecode, ControllStack cs);
	}
	
	private static class ControllStack {
		LinkedList<ControllStackItem> items = new LinkedList<>();
		public ControllStack(ControllStackItem... lsi) {
			for (ControllStackItem i : lsi)
				items.push(i);
		}
		
		public ControllStackItem peek() {
			return items.peekFirst();
		}
		
		public int size() {
			return items.size();
		}
		
		public ControllStackItem pop() {
			return items.pop();
		}
		
		static ControllStack push(ControllStack cs, ControllStackItem i) {
			if (cs == null)
				return new ControllStack(i);
			cs.items.push(i);
			return cs;
		}
	};
	
	private class TryFinallyItem implements ControllStackItem {
		private List<PythonBytecode> bcs = new LinkedList<>();
		private Try_stmtContext finallyCtx;
		boolean needsBreakBlock = false;
		boolean needsContinueBlock = false;

		TryFinallyItem(Try_stmtContext ctx) {
			this.finallyCtx = ctx;
		}
		
		@Override
		public void outputContinue(Continue_stmtContext ctx, List<PythonBytecode> bytecode, ControllStack cs) {
			needsContinueBlock = true;
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.LOADGLOBAL, ctx.start));
			cb.stringValue = "LoopContinue";
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, ctx.start));
			cb.intValue = 0;
			bytecode.add(Bytecode.makeBytecode(Bytecode.RAISE, ctx.start));
		}

		
		@Override
		public void outputBreak(Break_stmtContext ctx, List<PythonBytecode> bytecode, ControllStack cs) {
			needsBreakBlock = true;
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.LOADGLOBAL, ctx.start));
			cb.stringValue = "LoopBreak";
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, ctx.start));
			cb.intValue = 0;
			bytecode.add(Bytecode.makeBytecode(Bytecode.RAISE, ctx.start));
		}

		@Override
		public void outputFinallyBreakBlock(Try_stmtContext ctx, List<PythonBytecode> bytecode, ControllStack cs) {
			cs.pop(); // Pop this
			compileSuite(finallyCtx.try_finally().suite(), bytecode, cs);
			
			ControllStackItem overMe = cs.peek();
			if (overMe instanceof TryFinallyItem) {
				((TryFinallyItem)overMe).needsBreakBlock = true;
				bytecode.add(Bytecode.makeBytecode(Bytecode.SWAP_STACK, ctx.start));
				bytecode.add(Bytecode.makeBytecode(Bytecode.POP, ctx.start));	// frame
				bytecode.add(Bytecode.makeBytecode(Bytecode.SWAP_STACK, ctx.start));
				bytecode.add(Bytecode.makeBytecode(Bytecode.POP, ctx.start));	// return code (should be None)
				bytecode.add(Bytecode.makeBytecode(Bytecode.RERAISE, ctx.start));
			} else {
				bytecode.add(Bytecode.makeBytecode(Bytecode.POP, ctx.start));	// exception
				bytecode.add(Bytecode.makeBytecode(Bytecode.POP, ctx.start));	// frame
				bytecode.add(Bytecode.makeBytecode(Bytecode.POP, ctx.start));	// return code (should be None)
				cs.peek().outputFinallyBreakBlock(ctx, bytecode, cs);
			}
			ControllStack.push(cs, this); // Return this back to stack
		}
		
		@Override
		public void outputFinallyContinueBlock(Try_stmtContext ctx, List<PythonBytecode> bytecode, ControllStack cs) {
			cs.pop(); // Pop this
			compileSuite(finallyCtx.try_finally().suite(), bytecode, cs);
			
			ControllStackItem overMe = cs.peek();
			if (overMe instanceof TryFinallyItem) {
				((TryFinallyItem)overMe).needsContinueBlock = true;
				bytecode.add(Bytecode.makeBytecode(Bytecode.SWAP_STACK, ctx.start));
				bytecode.add(Bytecode.makeBytecode(Bytecode.POP, ctx.start));	// frame
				bytecode.add(Bytecode.makeBytecode(Bytecode.SWAP_STACK, ctx.start));
				bytecode.add(Bytecode.makeBytecode(Bytecode.POP, ctx.start));	// return code (should be None)
				bytecode.add(Bytecode.makeBytecode(Bytecode.RERAISE, ctx.start));
			} else {
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.POP, ctx.start));	// exception
				cb.intValue = 1;
				bytecode.add(Bytecode.makeBytecode(Bytecode.POP, ctx.start));	// frame
				bytecode.add(Bytecode.makeBytecode(Bytecode.POP, ctx.start));	// return code (should be None)
				cs.peek().outputFinallyContinueBlock(ctx, bytecode, cs);
			}
			ControllStack.push(cs, this); // Return this back to stack
		}		
	}
	
	private class LoopStackItem implements ControllStackItem {
		private List<PythonBytecode> bcs = new LinkedList<>();
		private int start;

		LoopStackItem(int startAddress) {
			this.start = startAddress;
		}

		@Override
		public void outputContinue(Continue_stmtContext ctx, List<PythonBytecode> bytecode, ControllStack cs) {
			PythonBytecode c = Bytecode.makeBytecode(Bytecode.GOTO, ctx.start);
			c.intValue = start;
			bytecode.add(c); 
		}

		@Override
		public void outputBreak(Break_stmtContext ctx, List<PythonBytecode> bytecode, ControllStack cs) {
			PythonBytecode c = Bytecode.makeBytecode(Bytecode.GOTO, ctx.start); 
			bytecode.add(c); 
			addJump(c);
		}
		
		@Override
		public void outputFinallyBreakBlock(Try_stmtContext ctx, List<PythonBytecode> bytecode, ControllStack cs) {
			PythonBytecode c = Bytecode.makeBytecode(Bytecode.GOTO, ctx.start); 
			bytecode.add(c); 
			addJump(c);
		}

		@Override
		public void outputFinallyContinueBlock(Try_stmtContext ctx, List<PythonBytecode> bytecode, ControllStack cs) {
			PythonBytecode c = Bytecode.makeBytecode(Bytecode.GOTO, ctx.start);
			c.intValue = start;
			bytecode.add(c); 
		}

		public void addJump(PythonBytecode c) {
			bcs.add(c);
		}

		public void addContinue(ParserRuleContext ctx, List<PythonBytecode> bytecode) {
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.GOTO, ctx.start));
			cb.intValue = start;
		}

		public void finalize(int endOfLoopAddress) {
			for (PythonBytecode c : bcs)
				c.intValue = endOfLoopAddress;
		}
	}
}
