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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import me.enerccio.sp.compiler.PythonBytecode.Pop;
import me.enerccio.sp.compiler.VariableStack.VariableType;
import me.enerccio.sp.errors.SyntaxError;
import me.enerccio.sp.interpret.CompiledBlockObject;
import me.enerccio.sp.interpret.InternalDict;
import me.enerccio.sp.interpret.ModuleResolver;
import me.enerccio.sp.parser.pythonParser.And_exprContext;
import me.enerccio.sp.parser.pythonParser.And_testContext;
import me.enerccio.sp.parser.pythonParser.ArglistContext;
import me.enerccio.sp.parser.pythonParser.ArgumentContext;
import me.enerccio.sp.parser.pythonParser.Arith_exprContext;
import me.enerccio.sp.parser.pythonParser.AtomContext;
import me.enerccio.sp.parser.pythonParser.Bracket_atomContext;
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
import me.enerccio.sp.parser.pythonParser.Del_stmtContext;
import me.enerccio.sp.parser.pythonParser.DictentryContext;
import me.enerccio.sp.parser.pythonParser.DictorsetmakerContext;
import me.enerccio.sp.parser.pythonParser.Dotted_as_nameContext;
import me.enerccio.sp.parser.pythonParser.Dotted_as_namesContext;
import me.enerccio.sp.parser.pythonParser.Dotted_nameContext;
import me.enerccio.sp.parser.pythonParser.Dynamic_stmtContext;
import me.enerccio.sp.parser.pythonParser.Eval_inputContext;
import me.enerccio.sp.parser.pythonParser.Exec_stmtContext;
import me.enerccio.sp.parser.pythonParser.ExprContext;
import me.enerccio.sp.parser.pythonParser.Expr_stmtContext;
import me.enerccio.sp.parser.pythonParser.ExprlistContext;
import me.enerccio.sp.parser.pythonParser.FactorContext;
import me.enerccio.sp.parser.pythonParser.FargContext;
import me.enerccio.sp.parser.pythonParser.File_inputContext;
import me.enerccio.sp.parser.pythonParser.Flow_stmtContext;
import me.enerccio.sp.parser.pythonParser.For_stmtContext;
import me.enerccio.sp.parser.pythonParser.FuncdefContext;
import me.enerccio.sp.parser.pythonParser.FutureContext;
import me.enerccio.sp.parser.pythonParser.Future_opContext;
import me.enerccio.sp.parser.pythonParser.Future_stmtContext;
import me.enerccio.sp.parser.pythonParser.Global_stmtContext;
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
import me.enerccio.sp.parser.pythonParser.Ready_opContext;
import me.enerccio.sp.parser.pythonParser.Return_stmtContext;
import me.enerccio.sp.parser.pythonParser.Shift_exprContext;
import me.enerccio.sp.parser.pythonParser.Simple_stmtContext;
import me.enerccio.sp.parser.pythonParser.Small_stmtContext;
import me.enerccio.sp.parser.pythonParser.StmtContext;
import me.enerccio.sp.parser.pythonParser.StringContext;
import me.enerccio.sp.parser.pythonParser.StringLiterarContext;
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
import me.enerccio.sp.parser.pythonParser.VarargContext;
import me.enerccio.sp.parser.pythonParser.While_stmtContext;
import me.enerccio.sp.parser.pythonParser.With_itemContext;
import me.enerccio.sp.parser.pythonParser.With_stmtContext;
import me.enerccio.sp.parser.pythonParser.Xor_exprContext;
import me.enerccio.sp.parser.pythonParser.Yield_exprContext;
import me.enerccio.sp.parser.pythonParser.Yield_or_exprContext;
import me.enerccio.sp.parser.pythonParser.Yield_stmtContext;
import me.enerccio.sp.types.ModuleObject.ModuleData;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.ComplexObject;
import me.enerccio.sp.types.base.EllipsisObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.base.NumberObject;
import me.enerccio.sp.types.callables.UserFunctionObject;
import me.enerccio.sp.types.mappings.DictObject;
import me.enerccio.sp.types.mappings.StringDictObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.types.DictTypeObject;
import me.enerccio.sp.types.types.ListTypeObject;
import me.enerccio.sp.types.types.ObjectTypeObject;
import me.enerccio.sp.types.types.SliceTypeObject;
import me.enerccio.sp.types.types.TupleTypeObject;
import me.enerccio.sp.utils.Utils;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Compiles source into Python Bytecode
 * 
 * @author Enerccio
 *
 */
public class PythonCompiler {
	public static volatile long genFunc = 0;
	private static final ModuleData GENERATED_FUNCTIONS = new ModuleData() {
		@Override
		public ModuleResolver getResolver() {
			return null;
		}

		@Override
		public String getName() {
			return "<generated-functions>";
		};

		@Override
		public String getFileName() {
			return getName();
		}

		@Override
		public String getPackageResolve() {
			return "";
		}

		@Override
		public boolean isPackage() {
			return false;
		}
	};
	private static final ModuleData EVAL_FUNCTIONS = new ModuleData() {
		@Override
		public ModuleResolver getResolver() {
			return null;
		}

		@Override
		public String getName() {
			return "<eval>";
		};

		@Override
		public String getFileName() {
			return getName();
		}

		@Override
		public String getPackageResolve() {
			return "";
		}

		@Override
		public boolean isPackage() {
			return false;
		}
	};

	private PythonBytecode cb;
	private VariableStack stack = new VariableStack();
	private Stack<String> compilingClass = new Stack<String>();
	private Stack<String> compilingFunction = new Stack<String>();
	private Stack<String> docstring = new Stack<String>();

	private Set<Futures> futures = new HashSet<Futures>();

	private ModuleData module = null;

	/**
	 * Compiles source into single UserFunctionObject. Used by function()
	 * function
	 * 
	 * @param sctx
	 *            String context
	 * @param globals
	 *            list of globals for the environment
	 * @param args
	 *            string arguments that function requires
	 * @param vargarg
	 *            null or name of the vararg argument
	 * @param kwararg
	 * @param defaults
	 *            function defaults
	 * @param locals
	 *            map of locals for the function
	 * @return completed object
	 */
	public UserFunctionObject doCompile(String_inputContext sctx,
			List<InternalDict> globals, List<String> args, String vargarg,
			String kwararg, InternalDict defaults, InternalDict locals) {
		module = GENERATED_FUNCTIONS;
		stack.push();
		compilingFunction.push("generated-function");
		UserFunctionObject fnc = new UserFunctionObject();

		String functionName = "generated-function-" + (++genFunc);
		Utils.putPublic(fnc, "__name__", new StringObject(functionName));

		fnc.args = args;
		if (vargarg != null) {
			fnc.isVararg = true;
			fnc.vararg = vargarg;
		}

		if (kwararg != null) {
			fnc.isKvararg = true;
			fnc.kvararg = kwararg;
		}

		List<PythonBytecode> fncb = new ArrayList<PythonBytecode>();
		compilingClass.push(null);
		doCompileFunction(sctx, fncb, sctx.start);
		compilingClass.pop();

		cb = addBytecode(fncb, Bytecode.PUSH, sctx.stop);
		cb.value = NoneObject.NONE;
		cb = addBytecode(fncb, Bytecode.RETURN, sctx.stop);
		cb.intValue = 1;

		fnc.block = new CompiledBlockObject(fncb);

		globals.add(locals);
		Utils.putPublic(fnc, "function_defaults", (PythonObject) defaults);
		Utils.putPublic(fnc, "__doc__", getDocstring());

		List<InternalDict> iDictList = new ArrayList<InternalDict>();
		for (InternalDict d : globals)
			iDictList.add(d);
		fnc.setClosure(iDictList);

		compilingFunction.pop();
		return fnc;
	}

	public CompiledBlockObject doCompile(File_inputContext fcx,
			final String filename) {
		ModuleData m = module = new ModuleData() {
			@Override
			public ModuleResolver getResolver() {
				return null;
			}

			@Override
			public String getName() {
				return filename;
			};

			@Override
			public String getFileName() {
				return getName();
			}

			@Override
			public String getPackageResolve() {
				return "";
			}

			@Override
			public boolean isPackage() {
				return false;
			}
		};
		stack.push();
		compilingFunction.push(null);
		compilingClass.push(null);

		ArrayList<PythonBytecode> bytecode = new ArrayList<PythonBytecode>();

		addBytecode(bytecode, Bytecode.PUSH_ENVIRONMENT, fcx.start);
		cb = addBytecode(bytecode, Bytecode.PUSH, fcx.start);
		cb.value = NoneObject.NONE;
		addBytecode(bytecode, Bytecode.PUSH_LOCAL_CONTEXT, fcx.start);

		compilingClass.push(null);
		for (Label_or_stmtContext ls : fcx.label_or_stmt()) {
			compile(ls, bytecode, null, m);
		}
		compilingClass.pop();

		CompiledBlockObject block = new CompiledBlockObject(bytecode);

		stack.pop();
		compilingClass.pop();
		compilingFunction.pop();

		return block;
	}

	public CompiledBlockObject doCompileEval(Eval_inputContext ecx) {
		module = EVAL_FUNCTIONS;
		stack.push();
		compilingFunction.push("eval-function");
		compilingClass.push(null);

		ArrayList<PythonBytecode> bytecode = new ArrayList<PythonBytecode>();

		addBytecode(bytecode, Bytecode.PUSH_ENVIRONMENT, ecx.start);
		cb = addBytecode(bytecode, Bytecode.PUSH, ecx.start);
		cb.value = NoneObject.NONE;
		addBytecode(bytecode, Bytecode.PUSH_LOCAL_CONTEXT, ecx.start);

		compilingClass.push(null);
		int i = 0;
		int total = ecx.testlist().test().size();
		for (TestContext tc : ecx.testlist().test()) {
			compile(tc, bytecode);
			if (i != total - 1)
				addBytecode(bytecode, Bytecode.POP, tc.stop);
		}
		cb = addBytecode(bytecode, Bytecode.RETURN, ecx.stop);
		cb.intValue = 0;

		compilingClass.pop();

		CompiledBlockObject block = new CompiledBlockObject(bytecode);

		stack.pop();
		compilingClass.pop();
		compilingFunction.pop();

		return block;
	}

	/**
	 * Compiles file input into List of bytecode for the module
	 * 
	 * @param fcx
	 *            file input
	 * @param dict
	 *            module's __dict__
	 * @param m
	 *            module
	 * @return
	 */
	public CompiledBlockObject doCompile(File_inputContext fcx, ModuleData m,
			StringDictObject builtins) {
		this.module = m;
		compilingFunction.push(null);

		stack.push();
		compilingClass.push(null);
		List<PythonBytecode> bytecode = new ArrayList<PythonBytecode>();
		// create new environment
		addBytecode(bytecode, Bytecode.PUSH_ENVIRONMENT, fcx.start);
		if (builtins != null) {
			addBytecode(bytecode, Bytecode.OPEN_LOCALS, fcx.start);
			addBytecode(bytecode, Bytecode.RESOLVE_ARGS, fcx.start);
		}
		// context
		cb = addBytecode(bytecode, Bytecode.PUSH, fcx.start);
		cb.value = NoneObject.NONE;
		addBytecode(bytecode, Bytecode.PUSH_LOCAL_CONTEXT, fcx.start);
		if (builtins != null) {
			cb = addBytecode(bytecode, Bytecode.PUSH, fcx.start);
			cb.value = builtins;
			cb = addBytecode(bytecode, Bytecode.SAVE_LOCAL, fcx.start);
			cb.stringValue = "__builtin__";
		}

		compile(fcx, bytecode, m);

		cb = addBytecode(bytecode, Bytecode.PUSH, fcx.start);
		cb.value = getDocstring();
		cb = addBytecode(bytecode, Bytecode.SAVE_LOCAL, fcx.start);
		cb.stringValue = "__doc__";

		compilingClass.pop();
		stack.pop();

		CompiledBlockObject cob = new CompiledBlockObject(bytecode);

		compilingFunction.pop();
		return cob;
	}

	private void compile(File_inputContext fcx, List<PythonBytecode> bytecode,
			ModuleData m) {
		boolean first = true;
		for (Label_or_stmtContext ls : fcx.label_or_stmt()) {
			if (first) {
				first = false;
				String docString = getDocstring(ls);
				docstring.add(docString);
				if (docString != null)
					continue;
			}
			compile(ls, bytecode, null, m);
		}
	}

	private String getDocstring(Label_or_stmtContext ls) {
		if (ls.stmt() != null)
			return getDocstring(ls.stmt());
		return null;
	}

	private void compileStatement(StmtContext sctx,
			List<PythonBytecode> bytecode, ControllStack cs) {
		if (sctx.future_stmt() != null)
			compileFuture(sctx.future_stmt(), bytecode);
		else if (sctx.simple_stmt() != null)
			compileSimpleStatement(sctx.simple_stmt(), bytecode, cs);
		else
			compileCompoundStatement(sctx.compound_stmt(), bytecode, cs);
	}

	private void compileCompoundStatement(Compound_stmtContext cstmt,
			List<PythonBytecode> bytecode, ControllStack cs) {
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
		} else if (cstmt.with_stmt() != null) {
			compileWith(cstmt.with_stmt(), bytecode, cs);
		} else
			throw new SyntaxError("statament type not implemented");
	}

	private void compileSuite(SuiteContext ctx, List<PythonBytecode> bytecode,
			ControllStack cs) {
		if (ctx.simple_stmt() != null)
			compileSimpleStatement(ctx.simple_stmt(), bytecode, cs);
		for (StmtContext sctx : ctx.stmt())
			compileStatement(sctx, bytecode, cs);
	}

	private void compileWith(With_stmtContext with_stmt,
			List<PythonBytecode> bytecode, ControllStack cs) {
		compileWith(0, with_stmt.with_item(), with_stmt.suite(), bytecode, cs);
	}

	private void compileWith(int i, List<With_itemContext> items,
			SuiteContext suite, List<PythonBytecode> bytecode, ControllStack cs) {
		if (i == items.size()) {
			compileWithBody(suite, bytecode, cs);
		} else {
			compileWithProtocol(i + 1, items, items.get(i), suite, bytecode, cs);
		}
	}

	private class WithControllStackItem implements ControllStackItem {

		@Override
		public void outputContinue(Continue_stmtContext ctx,
				List<PythonBytecode> bytecode, ControllStack cs) {
			cb = addBytecode(bytecode, Bytecode.LOADBUILTIN, ctx.start);
			cb.stringValue = "LoopContinue";
			cb = addBytecode(bytecode, Bytecode.CALL, ctx.start);
			cb.intValue = 0;
			addBytecode(bytecode, Bytecode.RAISE, ctx.start);
		}

		@Override
		public void outputBreak(Break_stmtContext ctx,
				List<PythonBytecode> bytecode, ControllStack cs) {
			cb = addBytecode(bytecode, Bytecode.LOADBUILTIN, ctx.start);
			cb.stringValue = "LoopBreak";
			cb = addBytecode(bytecode, Bytecode.CALL, ctx.start);
			cb.intValue = 0;
			addBytecode(bytecode, Bytecode.RAISE, ctx.start);
		}

		@Override
		public void outputFinallyBreakBlock(Try_stmtContext ctx,
				List<PythonBytecode> bytecode, ControllStack cs) {
			// TODO Auto-generated method stub

		}

		@Override
		public void outputFinallyContinueBlock(Try_stmtContext ctx,
				List<PythonBytecode> bytecode, ControllStack cs) {
			// TODO Auto-generated method stub

		}

	}

	private void compileWithProtocol(int i, List<With_itemContext> items,
			With_itemContext wi, SuiteContext suite,
			List<PythonBytecode> bytecode, ControllStack cs) {
		WithControllStackItem withCSI = new WithControllStackItem();
		cs = ControllStack.push(cs, withCSI);
		PythonBytecode makeFrame = Bytecode.makeBytecode(Bytecode.PUSH_FRAME,
				wi.start, getFunction(), module);
		PythonBytecode excTestJump = Bytecode.makeBytecode(Bytecode.GOTO,
				wi.start, getFunction(), module);

		compileWithEntryProtocol(wi, bytecode, cs);
		bytecode.add(makeFrame);
		addBytecode(bytecode, Bytecode.SWAP_STACK, wi.start);
		addBytecode(bytecode, Bytecode.PUSH_EXCEPTION, wi.start);
		bytecode.add(excTestJump);
		makeFrame.intValue = bytecode.size();
		makeFrame.object = 1;

		// assignment is done within subframe
		if (wi.expr() != null) {
			compileAssignment(wi.expr(), bytecode);
		} else {
			addBytecode(bytecode, Bytecode.POP, wi.stop);
		}
		compileWith(i, items, suite, bytecode, cs);
		addBytecode(bytecode, Bytecode.RETURN, wi.stop);
		excTestJump.intValue = bytecode.size();
		// Stack contains TOP -> with item -> return value -> frame -> exception
		addBytecode(bytecode, Bytecode.DUP, wi.stop);
		cb = addBytecode(bytecode, Bytecode.MAKE_FIRST, wi.stop);
		cb.intValue = 4;
		// Stack contains TOP -> return value -> frame -> exception #1 ->
		// exception #1 -> with item
		putGetAttr("__exit__", bytecode, wi.stop);
		addBytecode(bytecode, Bytecode.SWAP_STACK, wi.stop);
		// Stack contains TOP -> return value -> frame -> exception #1 -> with
		// item.__exit__ -> exception #1
		addBytecode(bytecode, Bytecode.DUP, wi.stop);
		// Stack contains TOP -> return value -> frame -> exception #1 -> with
		// item.__exit__ -> exception #1 -> exception #1
		PythonBytecode jumpIfNoException = addBytecode(bytecode,
				Bytecode.JUMPIFNONE, wi.stop);
		// Stack contains TOP -> return value -> frame -> exception #1 -> with
		// item.__exit__ -> exception #1 -> exception #1
		cb = addBytecode(bytecode, Bytecode.LOADBUILTIN, wi.stop);
		cb.stringValue = "type";
		addBytecode(bytecode, Bytecode.SWAP_STACK, wi.stop);
		cb = addBytecode(bytecode, Bytecode.CALL, wi.stop);
		cb.intValue = 1;
		// Stack contains TOP -> return value -> frame -> exception #1 -> with
		// item.__exit__ -> exception #1 -> exception #1.__class__
		addBytecode(bytecode, Bytecode.SWAP_STACK, wi.stop);
		// Stack contains TOP -> return value -> frame -> exception #1 -> with
		// item.__exit__ -> exception #1.__class__ -> exception #1

		jumpIfNoException.intValue = bytecode.size();
		// Stack contains TOP -> return value -> frame -> exception #1 -> with
		// item.__exit__ -> exception #1.__class__/None -> exception #1/None
		cb = addBytecode(bytecode, Bytecode.CALL, wi.stop);
		cb.intValue = 2;
		// Stack contains TOP -> return value -> frame -> exception #1 ->
		// result(with item.__exit__)
		PythonBytecode exceptionNotSuppressed = addBytecode(bytecode,
				Bytecode.JUMPIFFALSE, wi.stop);
		// exception is suppressed, just pop it and push None
		addBytecode(bytecode, Bytecode.POP, wi.stop);
		cb = addBytecode(bytecode, Bytecode.PUSH, wi.stop);
		cb.value = NoneObject.NONE;
		exceptionNotSuppressed.intValue = bytecode.size();
		// raises if exception is on top of the stack, or not if None

		// test for LoopBreak and LoopContinue if in loop
		cs.pop();
		ControllStackItem it = cs.peek();
		if (it != null) {
			cb = addBytecode(bytecode, Bytecode.LOADBUILTIN, wi.stop);
			cb.stringValue = "LoopContinue";
			addBytecode(bytecode, Bytecode.ISINSTANCE, wi.stop);
			PythonBytecode skipOver1 = Bytecode.makeBytecode(
					Bytecode.JUMPIFFALSE, wi.stop, getFunction(), module);
			bytecode.add(skipOver1);

			if (it instanceof TryFinallyItem
					|| it instanceof WithControllStackItem) {
				addBytecode(bytecode, Bytecode.SWAP_STACK, wi.stop);
				addBytecode(bytecode, Bytecode.POP, wi.stop); // frame
				addBytecode(bytecode, Bytecode.SWAP_STACK, wi.stop);
				addBytecode(bytecode, Bytecode.POP, wi.stop); // return code
																// (should be
																// None)
				addBytecode(bytecode, Bytecode.RERAISE, wi.stop);
				if (it instanceof TryFinallyItem) {
					((TryFinallyItem) it).needsContinueBlock = true;
				}
			} else {
				addBytecode(bytecode, Bytecode.POP, wi.stop);
				addBytecode(bytecode, Bytecode.POP, wi.stop);
				addBytecode(bytecode, Bytecode.POP, wi.stop);
				cb = addBytecode(bytecode, Bytecode.GOTO, wi.stop);
				cb.intValue = ((LoopStackItem) it).start;
			}

			skipOver1.intValue = bytecode.size();

			cb = addBytecode(bytecode, Bytecode.LOADBUILTIN, wi.stop);
			cb.stringValue = "LoopBreak";
			addBytecode(bytecode, Bytecode.ISINSTANCE, wi.stop);
			PythonBytecode skipOver2 = Bytecode.makeBytecode(
					Bytecode.JUMPIFFALSE, wi.stop, getFunction(), module);
			bytecode.add(skipOver2);

			if (it instanceof TryFinallyItem
					|| it instanceof WithControllStackItem) {
				addBytecode(bytecode, Bytecode.SWAP_STACK, wi.stop);
				addBytecode(bytecode, Bytecode.POP, wi.stop); // frame
				addBytecode(bytecode, Bytecode.SWAP_STACK, wi.stop);
				addBytecode(bytecode, Bytecode.POP, wi.stop); // return code
																// (should be
																// None)
				addBytecode(bytecode, Bytecode.RERAISE, wi.stop);
				if (it instanceof TryFinallyItem) {
					((TryFinallyItem) it).needsBreakBlock = true;
				}
			} else {
				addBytecode(bytecode, Bytecode.POP, wi.stop);
				addBytecode(bytecode, Bytecode.POP, wi.stop);
				addBytecode(bytecode, Bytecode.POP, wi.stop);
				PythonBytecode c = Bytecode.makeBytecode(Bytecode.GOTO,
						wi.stop, getFunction(), module);
				bytecode.add(c);
				((LoopStackItem) it).addJump(c);
			}

			skipOver2.intValue = bytecode.size();
		}

		addBytecode(bytecode, Bytecode.RERAISE, wi.stop);
		// If execution reaches here, there was no exception and there is still
		// frame on top of stack.
		// If this frame returned value, it should be returned from here as
		// well.
		PythonBytecode noReturnJump = addBytecode(bytecode,
				Bytecode.JUMPIFNORETURN, wi.stop);
		// There is TOP -> return value -> frame on stack if execution reaches
		// here
		addBytecode(bytecode, Bytecode.POP, wi.stop);
		cb = addBytecode(bytecode, Bytecode.RETURN, wi.stop);
		cb.intValue = 1;
		noReturnJump.intValue = bytecode.size();
		addBytecode(bytecode, Bytecode.POP, wi.stop);
		addBytecode(bytecode, Bytecode.POP, wi.stop);
	}

	private void compileWithEntryProtocol(With_itemContext wi,
			List<PythonBytecode> bytecode, ControllStack cs) {
		compile(wi.test(), bytecode);
		addBytecode(bytecode, Bytecode.DUP, wi.stop);
		putGetAttr("__enter__", bytecode, wi.stop);
		cb = addBytecode(bytecode, Bytecode.CALL, wi.stop);
		cb.intValue = 0;
	}

	private void compileWithBody(SuiteContext suite,
			List<PythonBytecode> bytecode, ControllStack cs) {
		compileSuite(suite, bytecode, cs);
		addBytecode(bytecode, Bytecode.RETURN, suite.stop);
	}

	private void compileTry(Try_stmtContext try_stmt,
			List<PythonBytecode> bytecode, ControllStack cs) {
		TryFinallyItem tfi = new TryFinallyItem(try_stmt);
		cs = ControllStack.push(cs, tfi);
		PythonBytecode makeFrame = Bytecode.makeBytecode(Bytecode.PUSH_FRAME,
				try_stmt.start, getFunction(), module);
		PythonBytecode excTestJump = Bytecode.makeBytecode(Bytecode.GOTO,
				try_stmt.start, getFunction(), module);
		PythonBytecode elseJump = null;
		List<PythonBytecode> exceptJumps = new ArrayList<>();
		List<PythonBytecode> finallyJumps = new ArrayList<>();
		List<PythonBytecode> endJumps = new ArrayList<>();
		bytecode.add(makeFrame);
		addBytecode(bytecode, Bytecode.SWAP_STACK, try_stmt.start);
		addBytecode(bytecode, Bytecode.PUSH_EXCEPTION, try_stmt.start);
		bytecode.add(excTestJump);
		// Compile try block
		makeFrame.intValue = bytecode.size();
		compileSuite(try_stmt.suite(), bytecode, cs);
		addBytecode(bytecode, Bytecode.RETURN, try_stmt.start);
		excTestJump.intValue = bytecode.size();
		// Compile exception tests.
		// Stack contains TOP -> return value -> frame -> exception
		if (tfi.needsBreakBlock) {
			// Special break block is needed
			cb = addBytecode(bytecode, Bytecode.LOADBUILTIN, try_stmt.start);
			cb.stringValue = "LoopBreak";
			addBytecode(bytecode, Bytecode.ISINSTANCE, try_stmt.start);
			PythonBytecode skipOver = Bytecode
					.makeBytecode(Bytecode.JUMPIFFALSE, try_stmt.start,
							getFunction(), module);
			bytecode.add(skipOver);

			tfi.outputFinallyBreakBlock(try_stmt, bytecode, cs);
			skipOver.intValue = bytecode.size();
		}
		if (tfi.needsContinueBlock) {
			// Special continue block is needed
			cb = addBytecode(bytecode, Bytecode.LOADBUILTIN, try_stmt.start);
			cb.stringValue = "LoopContinue";
			addBytecode(bytecode, Bytecode.ISINSTANCE, try_stmt.start);
			PythonBytecode skipOver = Bytecode
					.makeBytecode(Bytecode.JUMPIFFALSE, try_stmt.start,
							getFunction(), module);
			bytecode.add(skipOver);

			tfi.outputFinallyContinueBlock(try_stmt, bytecode, cs);
			skipOver.intValue = bytecode.size();
		}
		if (try_stmt.try_except().size() > 0) {
			// There is at least one except block defined
			if (try_stmt.else_block() != null)
				// ... and else on top of that
				elseJump = addBytecode(bytecode, Bytecode.JUMPIFNONE,
						try_stmt.start);
			boolean alwaysHandled = false;
			// Stack contains TOP -> return value -> frame -> exception when
			// execution reaches here
			for (Try_exceptContext ex : try_stmt.try_except()) {
				if (ex.except_clause().test().size() == 0) {
					// try ... except:
					cb = addBytecode(bytecode, Bytecode.GOTO, try_stmt.start);
					exceptJumps.add(cb);
					alwaysHandled = true;
				} else {
					// try ... except ErrorType, xyz:
					// or
					// try ... except ErrorType:
					compile(ex.except_clause().test(0), bytecode);
					addBytecode(bytecode, Bytecode.ISINSTANCE, try_stmt.start);
					cb = addBytecode(bytecode, Bytecode.JUMPIFTRUE,
							try_stmt.start);
					exceptJumps.add(cb);
				}
			}
			if (!alwaysHandled) {
				cb = addBytecode(bytecode, Bytecode.GOTO, try_stmt.start);
				finallyJumps.add(cb);
			}

			// Compile else block, if any
			if (elseJump != null) {
				elseJump.intValue = bytecode.size();
				addBytecode(bytecode, Bytecode.POP, try_stmt.start);
				cb = addBytecode(bytecode, Bytecode.JUMPIFNORETURN,
						try_stmt.start);
				cb.intValue = bytecode.size() + 2;
				cb = addBytecode(bytecode, Bytecode.PUSH, try_stmt.start);
				cb.value = NoneObject.NONE;
				cb = addBytecode(bytecode, Bytecode.GOTO, try_stmt.start);
				finallyJumps.add(cb);
				compileSuite(try_stmt.else_block().suite(), bytecode, cs);
				cb = addBytecode(bytecode, Bytecode.PUSH, try_stmt.start);
				cb.value = NoneObject.NONE;
				cb = addBytecode(bytecode, Bytecode.GOTO, try_stmt.start);
				finallyJumps.add(cb);
			}

			// Compile actual except blocks
			// Stack contains TOP -> return value -> frame -> exception if any
			// of those is executed
			int i = 0;
			for (Try_exceptContext ex : try_stmt.try_except()) {
				if (ex.except_clause().test().size() <= 1) {
					// try ... except:
					// or
					// try ... except ErrorType:
					// -> remove exception from top of stack
					exceptJumps.get(i++).intValue = bytecode.size();
					addBytecode(bytecode, Bytecode.POP, try_stmt.start);
					compileSuite(ex.suite(), bytecode, cs);
					cb = addBytecode(bytecode, Bytecode.PUSH, try_stmt.start);
					cb.value = NoneObject.NONE;
					cb = addBytecode(bytecode, Bytecode.GOTO, try_stmt.start);
					finallyJumps.add(cb);
				} else {
					// try ... except ErrorType, xyz:
					exceptJumps.get(i++).intValue = bytecode.size();
					// Store exception from top of stack
					compileAssignment(ex.except_clause().test(1), bytecode);
					compileSuite(ex.suite(), bytecode, cs);
					cb = addBytecode(bytecode, Bytecode.PUSH, try_stmt.start);
					cb.value = NoneObject.NONE;
					cb = addBytecode(bytecode, Bytecode.GOTO, try_stmt.start);
					finallyJumps.add(cb);
				}
			}
		}

		// Compile finally block (if any)
		for (PythonBytecode c : finallyJumps)
			c.intValue = bytecode.size();
		if (try_stmt.try_finally() != null)
			compileSuite(try_stmt.try_finally().suite(), bytecode, cs);
		// Stack contains TOP -> return value -> frame -> exception here,
		// exception may be None
		addBytecode(bytecode, Bytecode.RERAISE, try_stmt.start);
		// If execution reaches here, there was no exception and there is still
		// frame on top of stack.
		// If this frame returned value, it should be returned from here as
		// well.
		cb = addBytecode(bytecode, Bytecode.JUMPIFNORETURN, try_stmt.start);
		endJumps.add(cb);
		// There is TOP -> return value -> frame on stack if execution reaches
		// here
		addBytecode(bytecode, Bytecode.POP, try_stmt.start);
		cb = addBytecode(bytecode, Bytecode.RETURN, try_stmt.start);
		cb.intValue = 1;

		cs.pop();

		// Very end of try...catch block. Return value and frame is still on
		// stack
		for (PythonBytecode c : endJumps)
			c.intValue = bytecode.size();
		addBytecode(bytecode, Bytecode.POP, try_stmt.start);
		addBytecode(bytecode, Bytecode.POP, try_stmt.start);
	}

	private void compile(Label_or_stmtContext ls,
			List<PythonBytecode> bytecode, ControllStack cs, ModuleData m) {
		if (ls.stmt() != null)
			compileStatement(ls.stmt(), bytecode, cs);
	}

	private void compileWhile(While_stmtContext ctx,
			List<PythonBytecode> bytecode, ControllStack cs) {
		LoopStackItem lsi = new LoopStackItem(bytecode.size());
		cs = ControllStack.push(cs, lsi);
		// Compile condition
		compile(ctx.test(), bytecode);
		addBytecode(bytecode, Bytecode.TRUTH_VALUE, ctx.start);
		PythonBytecode jump = Bytecode.makeBytecode(Bytecode.JUMPIFFALSE,
				ctx.start, getFunction(), module);
		bytecode.add(jump);
		// Compile body
		compileSuite(ctx.suite(0), bytecode, cs);
		// Jump back
		lsi.addContinue(ctx, bytecode);
		// Compile else block, if needed
		jump.intValue = bytecode.size();
		;
		cs.pop();
		if (ctx.suite(1) != null)
			compileSuite(ctx.suite(1), bytecode, cs);
		lsi.finalize(bytecode.size());
	}

	private void compileFor(For_stmtContext ctx, List<PythonBytecode> bytecode,
			ControllStack cs) {
		PythonBytecode getIter;
		PythonBytecode acceptIter;
		PythonBytecode setupLoop;
		// Compile SETUP_LOOP that grabs iterator on top of stack. This opcode
		// jumps if grabbed iterator is
		// optimized, java-based thing.
		compileRightHand(ctx.testlist(), bytecode);
		setupLoop = addBytecode(bytecode, Bytecode.SETUP_LOOP, ctx.start);
		putGetAttr("next", bytecode, ctx.start);
		setupLoop.intValue = bytecode.size();
		// Compile loop
		LoopStackItem lsi = new LoopStackItem(bytecode.size());
		cs = ControllStack.push(cs, lsi);
		getIter = addBytecode(bytecode, Bytecode.GET_ITER, ctx.start);
		acceptIter = addBytecode(bytecode, Bytecode.ACCEPT_ITER, ctx.start);
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
		addBytecode(bytecode, Bytecode.POP, ctx.start);
	}

	private void compileIf(If_stmtContext ctx, List<PythonBytecode> bytecode,
			ControllStack cs) {
		List<PythonBytecode> endJumps = new ArrayList<>();
		PythonBytecode jump = null;
		// If and elif blocks
		for (int i = 0; i < ctx.test().size(); i++) {
			compile(ctx.test(i), bytecode);
			addBytecode(bytecode, Bytecode.TRUTH_VALUE, ctx.start);
			jump = addBytecode(bytecode, Bytecode.JUMPIFFALSE, ctx.start);
			compileSuite(ctx.suite(i), bytecode, cs);
			cb = addBytecode(bytecode, Bytecode.GOTO, ctx.start);
			endJumps.add(cb);
			jump.intValue = bytecode.size();
		}
		// Else block
		if (ctx.else_block() != null)
			compileSuite(ctx.else_block().suite(), bytecode, cs);
		// End if..else block
		for (PythonBytecode c : endJumps)
			c.intValue = bytecode.size();
	}

	private void compileSwitch(Switch_stmtContext ctx,
			List<PythonBytecode> bytecode, ControllStack cs) {
		List<PythonBytecode> jumps = new ArrayList<>();
		List<PythonBytecode> endJumps = new ArrayList<>();
		// Compile value to compare with
		compile(ctx.test(), bytecode);
		// Compile comparisons
		for (int i = 0; i < ctx.case_block().size(); i++) {
			addBytecode(bytecode, Bytecode.DUP, ctx.start);
			putGetAttr(PythonObject.__EQ__, bytecode,
					ctx.case_block(i).test().start);
			compile(ctx.case_block(i).test(), bytecode);
			cb = addBytecode(bytecode, Bytecode.RCALL, ctx.start);
			cb.intValue = 1;
			cb = addBytecode(bytecode, Bytecode.JUMPIFTRUE, ctx.start);
			jumps.add(cb);
		}
		// If there is else block, compile it here
		if (ctx.else_block() != null) {
			addBytecode(bytecode, Bytecode.POP, ctx.start);
			compileSuite(ctx.else_block().suite(), bytecode, cs);
			cb = addBytecode(bytecode, Bytecode.GOTO, ctx.start);
			endJumps.add(cb);
		}
		// Compile actual case blocks
		for (int i = 0; i < ctx.case_block().size(); i++) {
			jumps.get(i).intValue = bytecode.size();
			addBytecode(bytecode, Bytecode.POP, ctx.start);
			compileSuite(ctx.case_block(i).suite(), bytecode, cs);
			// Unlike traditional case, this one jumps to end of switch block
			// automatically
			cb = addBytecode(bytecode, Bytecode.GOTO, ctx.start);
			endJumps.add(cb);
		}
		for (PythonBytecode c : endJumps)
			c.intValue = bytecode.size();
	}

	private void compileClass(ClassdefContext classdef,
			List<PythonBytecode> bytecode, DecoratorsContext dc) {
		String className = classdef.nname().getText();
		compilingFunction.push(null);
		compilingClass.push(className);
		cb = addBytecode(bytecode, Bytecode.LOADBUILTIN, classdef.start);
		cb.stringValue = "type";
		cb = addBytecode(bytecode, Bytecode.PUSH, classdef.start);
		cb.value = new StringObject(className);
		cb = addBytecode(bytecode, Bytecode.LOADBUILTIN, classdef.start);
		cb.stringValue = "make_tuple";
		int c = classdef.testlist() != null ? classdef.testlist().test().size()
				: 0;
		if (classdef.testlist() != null)
			for (TestContext tc : classdef.testlist().test())
				compile(tc, bytecode);
		else {
			cb = addBytecode(bytecode, Bytecode.LOADBUILTIN, classdef.start);
			cb.stringValue = "object";
			c = 1;
		}
		cb = addBytecode(bytecode, Bytecode.CALL, classdef.start);
		cb.intValue = c;
		UserFunctionObject fnc = new UserFunctionObject();
		Utils.putPublic(fnc, "__name__", new StringObject("$$__classBodyFncFor"
				+ className + "$$"));
		fnc.args = new ArrayList<String>();

		List<PythonBytecode> fncb = new ArrayList<PythonBytecode>();
		doCompileFunction(classdef.suite(), fncb, classdef.suite().start);

		Utils.putPublic(fnc, "function_defaults", new StringDictObject());

		addBytecode(fncb, Bytecode.PUSH_LOCALS, classdef.stop);
		cb = addBytecode(fncb, Bytecode.RETURN, classdef.stop);
		cb.intValue = 1;

		fnc.block = new CompiledBlockObject(fncb);

		cb = addBytecode(bytecode, Bytecode.PUSH, classdef.stop);
		cb.value = fnc;
		addBytecode(bytecode, Bytecode.RESOLVE_CLOSURE, classdef.stop);
		cb = addBytecode(bytecode, Bytecode.CALL, classdef.stop);
		cb.intValue = 0;
		cb = addBytecode(bytecode, Bytecode.CALL, classdef.stop);
		cb.intValue = 3;

		addBytecode(bytecode, Bytecode.DUP, classdef.stop);
		cb = addBytecode(bytecode, Bytecode.PUSH, classdef.stop);
		cb.value = getDocstring();
		addBytecode(bytecode, Bytecode.SWAP_STACK, classdef.stop);
		cb = addBytecode(bytecode, Bytecode.SETATTR, classdef.stop);
		cb.stringValue = "__doc__";

		if (dc != null) {
			compile(dc, bytecode);
		}

		cb = addBytecode(bytecode, Bytecode.SAVE_LOCAL, classdef.stop);
		cb.stringValue = className;
		compilingClass.pop();
		compilingFunction.pop();
	}

	private PythonObject getDocstring() {
		String doc = Utils.peek(docstring);
		if (docstring.size() > 0)
			docstring.pop();
		if (doc == null)
			return NoneObject.NONE;
		return new StringObject(doc.trim());
	}

	private void compileFunction(FuncdefContext funcdef,
			List<PythonBytecode> bytecode, DecoratorsContext dc) {
		UserFunctionObject fnc = new UserFunctionObject();

		String functionName = funcdef.nname().getText();
		Utils.putPublic(fnc, "__name__",
				new StringObject(compilingClass.peek() == null ? functionName
						: compilingClass.peek() + "." + functionName));
		Utils.putPublic(fnc, "__location__", new StringObject(module.getName()
				+ " line " + funcdef.start.getLine() + ", char "
				+ funcdef.start.getCharPositionInLine()));

		compilingFunction.push(functionName);

		List<String> arguments = new ArrayList<String>();
		for (int i = 0; i < funcdef.farg().size(); i++) {
			arguments.add(funcdef.farg(i).nname().getText());
		}

		fnc.args = arguments;
		if (funcdef.vararg() != null) {
			VarargContext vc = funcdef.vararg();
			if (vc.svararg() != null) {
				fnc.isVararg = true;
				fnc.vararg = vc.svararg().nname().getText();
			}
			if (vc.kvararg() != null) {
				fnc.isKvararg = true;
				fnc.kvararg = vc.kvararg().nname().getText();
			}
		}

		compilingClass.push(null);
		List<PythonBytecode> fncb = new ArrayList<PythonBytecode>();
		doCompileFunction(funcdef.suite(), fncb, funcdef.suite().start);
		compilingClass.pop();

		Utils.putPublic(fnc, "__doc__", getDocstring());

		cb = addBytecode(fncb, Bytecode.PUSH, funcdef.stop);
		cb.value = NoneObject.NONE;
		cb = addBytecode(fncb, Bytecode.RETURN, funcdef.stop);
		cb.intValue = 1;

		fnc.block = new CompiledBlockObject(fncb);

		cb = addBytecode(bytecode, Bytecode.PUSH, funcdef.stop);
		cb.value = fnc;

		addBytecode(bytecode, Bytecode.RESOLVE_CLOSURE, funcdef.stop);
		addBytecode(bytecode, Bytecode.DUP, funcdef.stop); // function_defaults

		cb = addBytecode(bytecode, Bytecode.PUSH, funcdef.stop);
		cb.value = new StringDictObject();

		for (int i = 0; i < funcdef.farg().size(); i++) {
			FargContext ctx = funcdef.farg(i);
			if (ctx.test() != null) {
				addBytecode(bytecode, Bytecode.DUP, ctx.start);
				putGetAttr("__setitem__", bytecode, ctx.start);
				cb = addBytecode(bytecode, Bytecode.PUSH, ctx.start);
				cb.value = new StringObject(ctx.nname().getText());
				compile(ctx.test(), bytecode);
				cb = addBytecode(bytecode, Bytecode.CALL, ctx.stop);
				cb.intValue = 2;
				addBytecode(bytecode, Bytecode.POP, ctx.stop);
			}
		}

		addBytecode(bytecode, Bytecode.SWAP_STACK, funcdef.stop);
		cb = addBytecode(bytecode, Bytecode.SETATTR, funcdef.stop);
		cb.stringValue = "function_defaults";

		if (dc != null) {
			compile(dc, bytecode);
		}

		cb = addBytecode(bytecode, Bytecode.SAVE_LOCAL, funcdef.stop);
		cb.stringValue = functionName;

		compilingFunction.pop();
	}

	private void compile(DecoratorsContext dc, List<PythonBytecode> bytecode) {
		// function on stack
		for (int i = dc.decorator().size() - 1; i >= 0; i--) {
			DecoratorContext d = dc.decorator(i);
			// compile decorator
			compile(d.test(), bytecode);
			if (d.arglist() != null) {
				CallArgsData args = compileArguments(d.arglist(), bytecode);
				cb = addBytecode(bytecode, Bytecode.CALL, d.stop);
				cb.intValue = args.normalArgCount;
				if (args.hasArgExpansion)
					cb.intValue = -1 * (cb.intValue + 1);
			}
			// call
			addBytecode(bytecode, Bytecode.SWAP_STACK, dc.stop);
			cb = addBytecode(bytecode, Bytecode.CALL, dc.stop);
			cb.intValue = 1;
		}
	}

	private void doCompileFunction(ParseTree suite,
			List<PythonBytecode> bytecode, Token t) {

		stack.push();
		addBytecode(bytecode, Bytecode.PUSH_ENVIRONMENT, t);
		addBytecode(bytecode, Bytecode.OPEN_LOCALS, t);
		addBytecode(bytecode, Bytecode.RESOLVE_ARGS, t);

		if (suite instanceof SuiteContext) {
			boolean first = true;
			for (StmtContext c : ((SuiteContext) suite).stmt()) {
				if (first) {
					first = false;
					String docString = getDocstring(c);
					docstring.add(docString);
					if (docString != null)
						continue;
				}

				compileStatement(c, bytecode, null);
			}
		} else if (suite instanceof TestContext) {
			compile((TestContext) suite, bytecode);
		} else
			for (StmtContext c : ((String_inputContext) suite).stmt())
				compileStatement(c, bytecode, null);

		stack.pop();
	}

	private String getDocstring(StmtContext c) {
		if (c.simple_stmt() != null)
			return getDocstring(c.simple_stmt());
		return null;
	}

	private String getDocstring(Simple_stmtContext simple_stmt) {
		if (simple_stmt.small_stmt().size() == 1)
			return getDocstring(simple_stmt.small_stmt(0));
		return null;
	}

	private String getDocstring(Small_stmtContext small_stmt) {
		if (small_stmt.expr_stmt() != null)
			return getDocstring(small_stmt.expr_stmt());
		return null;
	}

	private String getDocstring(Expr_stmtContext expr_stmt) {
		if (expr_stmt.testlist() != null)
			return getDocstring(expr_stmt.testlist());
		return null;
	}

	private String getDocstring(TestlistContext testlist) {
		if (testlist.test().size() == 1)
			return getDocstring(testlist.test(0));
		return null;
	}

	private String getDocstring(TestContext test) {
		if (test.or_test().size() == 1)
			return getDocstring(test.or_test(0));
		return null;
	}

	private String getDocstring(Or_testContext or_test) {
		if (or_test.and_test().size() == 1)
			return getDocstring(or_test.and_test(0));
		return null;
	}

	private String getDocstring(And_testContext and_test) {
		if (and_test.not_test().size() == 1)
			return getDocstring(and_test.not_test(0));
		return null;
	}

	private String getDocstring(Not_testContext not_test) {
		if (not_test.comparison() != null)
			return getDocstring(not_test.comparison());
		return null;
	}

	private String getDocstring(ComparisonContext comparison) {
		if (comparison.expr().size() == 1)
			return getDocstring(comparison.expr(0));
		return null;
	}

	private String getDocstring(ExprContext expr) {
		if (expr.xor_expr().size() == 1)
			return getDocstring(expr.xor_expr(0));
		return null;
	}

	private String getDocstring(Xor_exprContext xor_expr) {
		if (xor_expr.and_expr().size() == 1)
			return getDocstring(xor_expr.and_expr(0));
		return null;
	}

	private String getDocstring(And_exprContext and_expr) {
		if (and_expr.shift_expr().size() == 1)
			return getDocstring(and_expr.shift_expr(0));
		return null;
	}

	private String getDocstring(Shift_exprContext shift_expr) {
		if (shift_expr.arith_expr().size() == 1)
			return getDocstring(shift_expr.arith_expr(0));
		return null;
	}

	private String getDocstring(Arith_exprContext arith_expr) {
		if (arith_expr.term().size() == 1)
			return getDocstring(arith_expr.term(0));
		return null;
	}

	private String getDocstring(TermContext term) {
		if (term.factor().size() == 1)
			return getDocstring(term.factor(0));
		return null;
	}

	private String getDocstring(FactorContext factor) {
		if (factor.power() != null)
			return getDocstring(factor.power());
		return null;
	}

	private String getDocstring(PowerContext power) {
		if (power.atom() != null)
			return getDocstring(power.atom());
		return null;
	}

	private String getDocstring(AtomContext atom) {
		if (atom.string().size() == 1)
			return getDocstring(atom.string(0));
		return null;
	}

	private String getDocstring(StringContext string) {
		if (string.stringLiterar() != null)
			return getDocstring(string.stringLiterar());
		return null;
	}

	private String getDocstring(StringLiterarContext s) {
		if (s.SHORT_STRING() != null) {
			String ss = s.getText();
			ss = ss.substring(1, ss.length() - 1);
			return ss;
		} else {
			String ss = s.getText();
			ss = ss.substring(3, ss.length() - 4);
			return ss;
		}
	}

	private void compileSimpleStatement(Simple_stmtContext sstmt,
			List<PythonBytecode> bytecode, ControllStack cs) {
		for (Small_stmtContext smstmt : sstmt.small_stmt())
			compileSmallStatement(smstmt, bytecode, cs);
	}

	private void compileSmallStatement(Small_stmtContext smstmt,
			List<PythonBytecode> bytecode, ControllStack cs) {
		// Import Statement
		if (smstmt.import_stmt() != null) {
			Import_stmtContext imps = smstmt.import_stmt();
			if (imps.import_name() != null) {
				Import_nameContext in = imps.import_name();
				Dotted_as_namesContext das = in.dotted_as_names();
				for (Dotted_as_nameContext dans : das.dotted_as_name())
					compileImport(dans, bytecode);
			} else {
				Import_fromContext imf = imps.import_from();
				String packageName = "";
				int i = 0;
				Dotted_nameContext d = imf.dotted_name();
				for (NnameContext name : d.nname()) {
					packageName += name.getText();
					if (++i != d.nname().size())
						packageName += ".";
				}
				if (imf.star() != null) {
					addImport(packageName, "*", bytecode, imf.star().start);
					return;
				}
				if (imf.import_as_names() != null)
					for (Import_as_nameContext asname : imf.import_as_names()
							.import_as_name()) {
						compileImport2(asname, bytecode, packageName);
					}
			}
			// } else if (smstmt.parenthesesless_call() != null) {
			// compileParentheseslessCall(smstmt.parenthesesless_call(),
			// bytecode);
		} else if (smstmt.pass_stmt() != null) {
			addBytecode(bytecode, Bytecode.NOP, smstmt.start);
		} else if (smstmt.expr_stmt() != null) {
			compile(smstmt.expr_stmt(), bytecode);
		} else if (smstmt.print_stmt() != null) {
			compile(smstmt.print_stmt(), bytecode);
		} else if (smstmt.flow_stmt() != null) {
			compile(smstmt.flow_stmt(), bytecode, cs);
		} else if (smstmt.global_stmt() != null) {
			compile(smstmt.global_stmt(), bytecode);
		} else if (smstmt.dynamic_stmt() != null) {
			compile(smstmt.dynamic_stmt(), bytecode);
		} else if (smstmt.del_stmt() != null) {
			compile(smstmt.del_stmt(), bytecode);
		} else if (smstmt.exec_stmt() != null) {
			compile(smstmt.exec_stmt(), bytecode);
		}
	}

	private void compileFuture(Future_stmtContext future_stmt,
			List<PythonBytecode> bytecode) {
		for (NnameContext future : future_stmt.nname())
			compileFuture(future.getText(), bytecode);
	}

	private void compileFuture(String fidx, List<PythonBytecode> bytecode) {
		switch (fidx) {
		case "print_function":
			futures.add(Futures.PRINT_FUNCTION);
			break;
		default:
			throw new SyntaxError("unknown future: " + fidx);
		}
	}

	private void compile(Exec_stmtContext ctx, List<PythonBytecode> bytecode) {
		cb = addBytecode(bytecode, Bytecode.LOADBUILTIN, ctx.start);
		cb.stringValue = "exec_function";
		compile(ctx.expr(), bytecode);
		if (ctx.test().size() == 2) {
			compile(ctx.test(0), bytecode);
			compile(ctx.test(1), bytecode);
		} else if (ctx.test().size() == 1) {
			compile(ctx.test(0), bytecode);
			cb = addBytecode(bytecode, Bytecode.PUSH, ctx.start);
			cb.value = NoneObject.NONE;
		} else {
			cb = addBytecode(bytecode, Bytecode.PUSH, ctx.start);
			cb.value = NoneObject.NONE;
			cb = addBytecode(bytecode, Bytecode.PUSH, ctx.start);
			cb.value = NoneObject.NONE;
		}
		cb = addBytecode(bytecode, Bytecode.CALL, ctx.start);
		cb.intValue = 3;
	}

	private void compile(Del_stmtContext ctx, List<PythonBytecode> bytecode) {
		compileDel(ctx.exprlist(), bytecode);
	}

	private void compileDel(ExprlistContext exprlist,
			List<PythonBytecode> bytecode) {
		for (ExprContext ctx : exprlist.expr())
			compileDel(ctx, bytecode, false);
	}

	private void compileDel(ExprContext ctx, List<PythonBytecode> bytecode,
			boolean delkeyOnly) {
		if (ctx.xor_expr().size() > 1)
			throw new SyntaxError("can't use xor in del statemenet");
		compileDel(ctx.xor_expr(0), bytecode, delkeyOnly);
	}

	private void compileDel(Xor_exprContext ctx, List<PythonBytecode> bytecode,
			boolean delkeyOnly) {
		if (ctx.and_expr().size() > 1)
			throw new SyntaxError("can't use and in del statemenet");
		compileDel(ctx.and_expr(0), bytecode, delkeyOnly);
	}

	private void compileDel(And_exprContext ctx, List<PythonBytecode> bytecode,
			boolean delkeyOnly) {
		if (ctx.shift_expr().size() > 1)
			throw new SyntaxError("can't use shift in del statemenet");
		compileDel(ctx.shift_expr(0), bytecode, delkeyOnly);
	}

	private void compileDel(Shift_exprContext ctx,
			List<PythonBytecode> bytecode, boolean delkeyOnly) {
		if (ctx.arith_expr().size() > 1)
			throw new SyntaxError("can't use arithmetics in del statemenet");
		compileDel(ctx.arith_expr(0), bytecode, delkeyOnly);
	}

	private void compileDel(Arith_exprContext ctx,
			List<PythonBytecode> bytecode, boolean delkeyOnly) {
		if (ctx.term().size() > 1)
			throw new SyntaxError("can't use arithmetics in del statemenet");
		compileDel(ctx.term(0), bytecode, delkeyOnly);
	}

	private void compileDel(TermContext ctx, List<PythonBytecode> bytecode,
			boolean delkeyOnly) {
		if (ctx.factor().size() > 1)
			throw new SyntaxError("can't use arithmetics in del statemenet");
		compileDel(ctx.factor(0), bytecode, delkeyOnly);
	}

	private void compileDel(FactorContext ctx, List<PythonBytecode> bytecode,
			boolean delkeyOnly) {
		if (ctx.factor() != null)
			throw new SyntaxError("can't use arithmetics in del statemenet");
		compileDel(ctx.power(), bytecode, delkeyOnly);
	}

	private void compileDel(PowerContext ctx, List<PythonBytecode> bytecode,
			boolean delkeyOnly) {
		if (ctx.factor() != null)
			throw new SyntaxError("can't use arithmetics in del statemenet");
		if (ctx.trailer().size() > 0) {
			compile(ctx.atom(), bytecode);
			// atom on stack
			for (int i = 0; i < ctx.trailer().size(); i++) {
				if (i == ctx.trailer().size() - 1) {
					// some value is on stack, depending on last trailer, either
					// delkeys or delattr, or error
					TrailerContext tc = ctx.trailer(i);
					String text = tc.getText().trim();
					if (text.startsWith("("))
						throw new SyntaxError("can't use del in function call");
					if (tc.NAME() != null) {
						String fname = tc.NAME().getText();
						cb = addBytecode(bytecode, Bytecode.DELATTR, tc.start);
						cb.stringValue = fname;
						return;
					} else {
						putGetAttr("__delkey__", bytecode, tc.start);
						compileSubscript(tc.subscriptlist(), bytecode);
					}
				} else {
					compile(ctx.trailer(i), bytecode);
				}
			}
		} else {
			compileDel(ctx.atom(), bytecode, false);
		}
	}

	private void compileDel(AtomContext atom, List<PythonBytecode> bytecode,
			boolean delkeyOnly) {
		if (!delkeyOnly) {
			if (atom.nname() != null) {
				String vname = atom.nname().getText();
				if (stack.typeOfVariable(vname) == VariableType.DYNAMIC)
					throw new SyntaxError(
							"can't use del statemenet on dynamic variables");
				cb = addBytecode(bytecode, Bytecode.DEL, atom.nname().start);
				cb.stringValue = atom.nname().getText();
				cb.booleanValue = stack.typeOfVariable(vname) == VariableType.GLOBAL;
				return;
			}
			throw new SyntaxError("can't use atoms in del statemenet");
		}
	}

	private void compile(Global_stmtContext ctx, List<PythonBytecode> bytecode) {
		for (NnameContext nc : ctx.nname())
			stack.addGlobal(nc.getText());
	}

	private void compile(Dynamic_stmtContext ctx, List<PythonBytecode> bytecode) {
		for (NnameContext nc : ctx.nname())
			stack.addDynamic(nc.getText());
	}

	private void compile(Flow_stmtContext ctx, List<PythonBytecode> bytecode,
			ControllStack cs) {
		if (ctx.return_stmt() != null)
			compile(ctx.return_stmt(), bytecode);
		else if (ctx.break_stmt() != null)
			compile(ctx.break_stmt(), bytecode, cs);
		else if (ctx.continue_stmt() != null)
			compile(ctx.continue_stmt(), bytecode, cs);
		else if (ctx.raise_stmt() != null)
			compile(ctx.raise_stmt(), bytecode);
		else if (ctx.yield_stmt() != null)
			compile(ctx.yield_stmt(), bytecode);
	}

	private void compile(Yield_stmtContext ctx, List<PythonBytecode> bytecode) {
		compile(ctx.yield_expr(), bytecode);
		addBytecode(bytecode, Bytecode.POP, ctx.start);
	}

	private void compile(Yield_exprContext ctx, List<PythonBytecode> bytecode) {
		String name = compilingFunction.peek();
		if (name == null)
			throw new SyntaxError("yield outside function body");

		compileRightHand(ctx.testlist(), bytecode);
		cb = addBytecode(bytecode, Bytecode.YIELD, ctx.stop);
		cb.stringValue = name;
	}

	private void compile(Break_stmtContext break_stmt,
			List<PythonBytecode> bytecode, ControllStack cs) {
		if ((cs == null) || (cs.size() == 0))
			throw new SyntaxError("'break' outside loop");
		cs.peek().outputBreak(break_stmt, bytecode, cs);
	}

	private void compile(Continue_stmtContext continue_stmt,
			List<PythonBytecode> bytecode, ControllStack cs) {
		if ((cs == null) || (cs.size() == 0))
			throw new SyntaxError("'continue' outside loop");
		cs.peek().outputContinue(continue_stmt, bytecode, cs);
	}

	private void compile(Raise_stmtContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.test().size() > 1) {
			// raise Error, "argument"
			compile(ctx.test(0), bytecode); // Exception class
			compile(ctx.test(1), bytecode); // Argument
			cb = addBytecode(bytecode, Bytecode.CALL, ctx.start);
			cb.intValue = 1;
			cb = addBytecode(bytecode, Bytecode.RAISE, ctx.start);
			cb.booleanValue = true;
		} else if (ctx.test().size() == 1) {
			// raise Error("argument") or raise Error
			compile(ctx.test(0), bytecode);
			cb = addBytecode(bytecode, Bytecode.RAISE, ctx.start);
			cb.booleanValue = true;
		} else {
			// raise # and nothing else
			cb = addBytecode(bytecode, Bytecode.RAISE, ctx.start);
			cb.booleanValue = false;
		}
	}

	private void compile(Return_stmtContext ctx, List<PythonBytecode> bytecode) {
		if (compilingClass.peek() != null)
			throw new SyntaxError("return cannot be inside class definition");
		if (ctx.testlist() != null)
			compileRightHand(ctx.testlist(), bytecode);
		cb = addBytecode(bytecode, Bytecode.RETURN, ctx.start);
		cb.intValue = 1;
	}

	private void compile(Print_stmtContext ctx, List<PythonBytecode> bytecode) {

		if (futures.contains(Futures.PRINT_FUNCTION)) {
			compilePrintFunction(ctx, bytecode);
			return;
		}

		int st = ctx.push() == null ? 0 : 1;
		cb = addBytecode(bytecode, Bytecode.LOADBUILTIN, ctx.start);
		cb.stringValue = "print_function";

		cb = addBytecode(bytecode, Bytecode.LOADBUILTIN, ctx.start);
		cb.stringValue = TupleTypeObject.MAKE_TUPLE_CALL;

		if (ctx.arglist() != null) {
			throw new SyntaxError(
					"print is statement, did you forget to import from futures?");
		}

		for (int i = st; i < ctx.test().size(); i++) {
			compile(ctx.test(i), bytecode);
		}

		cb = addBytecode(bytecode, Bytecode.CALL, ctx.stop);
		cb.intValue = ctx.test().size() - st;

		cb = addBytecode(bytecode, Bytecode.PUSH, ctx.stop);
		cb.value = new StringObject(" ");

		cb = addBytecode(bytecode, Bytecode.PUSH, ctx.stop);
		if (ctx.endp() == null)
			cb.value = new StringObject("\n");
		else
			cb.value = new StringObject("");

		if (st == 1) {
			compile(ctx.test(0), bytecode);
		} else {
			cb = addBytecode(bytecode, Bytecode.PUSH, ctx.stop);
			cb.value = NoneObject.NONE;
		}

		cb = addBytecode(bytecode, Bytecode.CALL, ctx.stop);
		cb.intValue = 4;

		addBytecode(bytecode, Bytecode.POP, ctx.stop);
	}

	private void compilePrintFunction(Print_stmtContext ctx,
			List<PythonBytecode> bytecode) {
		if (ctx.arglist() == null)
			throw new SyntaxError(
					"print is now a function, did you forget not to import from futures?");

		cb = addBytecode(bytecode, Bytecode.LOADBUILTIN, ctx.start);
		cb.stringValue = "print_function_fnc";

		if (ctx.arglist() == null) {
			cb = addBytecode(bytecode, Bytecode.CALL, ctx.stop);
			cb.intValue = 0;
		} else {
			CallArgsData args = compileArguments(ctx.arglist(), bytecode);
			cb = addBytecode(bytecode, Bytecode.CALL, ctx.stop);
			cb.intValue = args.normalArgCount;
			if (args.hasArgExpansion)
				cb.intValue = -1 * (cb.intValue + 1);
		}

		addBytecode(bytecode, Bytecode.POP, ctx.stop);
	}

	private void compile(Expr_stmtContext expr, List<PythonBytecode> bytecode) {
		if (expr.augassignexp() != null) {
			// AugAssign
			TestlistContext leftHand = expr.testlist();
			if (leftHand.test().size() > 1)
				throw new SyntaxError(
						"illegal expression for augmented assignment");
			// TODO
			// Bytecode.makeBytecode(
			compileRightHand(leftHand, bytecode);
			compileRightHand(expr.augassignexp().yield_or_expr(), bytecode);
			if (expr.augassignexp().augassign().getText().equals("+="))
				addBytecode(bytecode, Bytecode.ADD, expr.start);
			else if (expr.augassignexp().augassign().getText().equals("-="))
				addBytecode(bytecode, Bytecode.SUB, expr.start);
			else if (expr.augassignexp().augassign().getText().equals("*="))
				addBytecode(bytecode, Bytecode.MUL, expr.start);
			else if (expr.augassignexp().augassign().getText().equals("/="))
				addBytecode(bytecode, Bytecode.DIV, expr.start);
			else if (expr.augassignexp().augassign().getText().equals("%="))
				addBytecode(bytecode, Bytecode.MOD, expr.start);
			else
				throw new SyntaxError("illegal augmented assignment");
			compileAssignment(leftHand.test(0), bytecode);
		} else {
			if (expr.yield_or_expr().size() == 0) {
				compileRightHand(expr.testlist(), bytecode);
				addBytecode(bytecode, Bytecode.POP, expr.stop);
				return;
			}

			Yield_or_exprContext rightHand = expr.yield_or_expr(expr
					.yield_or_expr().size() - 1);
			List<ParseTree> rest = new ArrayList<ParseTree>();
			rest.add(expr.testlist());
			for (int i = 0; i < expr.yield_or_expr().size() - 1; i++)
				rest.add(expr.yield_or_expr(i));

			Collections.reverse(rest);
			compileRightHand(rightHand, bytecode);

			for (ParseTree pt : rest) {
				Yield_or_exprContext yt = null;
				TestlistContext tc = null;
				if (pt instanceof Yield_or_exprContext) {
					yt = (Yield_or_exprContext) pt;
				} else {
					tc = (TestlistContext) pt;
				}
				addBytecode(bytecode, Bytecode.DUP, tc == null ? yt.start
						: tc.start);
				if (yt != null && yt.yield_expr() != null) {

				} else {
					if (yt != null)
						tc = yt.testlist();
					if (tc.test().size() > 1) {
						// x,y,z = ...
						cb = addBytecode(bytecode, Bytecode.UNPACK_SEQUENCE,
								tc.start);
						cb.intValue = tc.test().size();
						for (int i = 0; tc.test(i) != null; i++)
							compileAssignment(tc.test(i), bytecode);
					} else {
						// x = y
						compileAssignment(tc.test(0), bytecode);
					}
				}
			}
			addBytecode(bytecode, Bytecode.POP, expr.stop);
		}
	}

	private void compileRightHand(Yield_or_exprContext ctx,
			List<PythonBytecode> bytecode) {
		if (ctx.testlist() != null)
			compileRightHand(ctx.testlist(), bytecode);
		else
			compile(ctx.yield_expr(), bytecode);
	}

	/**
	 * Compiles right side of assignment, leaving value of right hand on top of
	 * stack Offset can be used to skip left-side test.
	 */
	private void compileRightHand(TestlistContext testlist,
			List<PythonBytecode> bytecode) {
		int tlc = testlist.test().size();
		if (tlc > 1) {
			cb = addBytecode(bytecode, Bytecode.LOADBUILTIN, testlist.start);
			cb.stringValue = TupleTypeObject.MAKE_TUPLE_CALL;
		}

		for (TestContext tc : testlist.test())
			compilePossibleFuture(tc, bytecode);

		if (tlc > 1) {
			cb = addBytecode(bytecode, Bytecode.CALL, testlist.stop);
			cb.intValue = tlc;
		}
	}

	private void putGetAttr(String attr, List<PythonBytecode> bytecode, Token t) {
		cb = addBytecode(bytecode, Bytecode.GETATTR, t);
		cb.stringValue = attr;
	}

	private void putNot(List<PythonBytecode> bytecode, Token t) {
		cb = addBytecode(bytecode, Bytecode.TRUTH_VALUE, t);
		cb.intValue = 1;
	}

	private void compilePossibleFuture(TestContext ctx,
			List<PythonBytecode> bytecode) {
		if (findFuture(ctx)) {
			compileFuture(ctx, bytecode);
			return;
		}
		compile(ctx, bytecode);
	}

	private void compile(TestContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.lambdef() != null) {
			compile(ctx.lambdef(), bytecode);
			return;
		} else if (ctx.future() != null) {
			compile(ctx.future().test(), bytecode);
			return;
		} else if (ctx.getChildCount() > 1) {
			compileTernary(ctx, bytecode);
			return;
		}
		compile(ctx.or_test(0), bytecode);
	}

	private void compileFuture(TestContext ctx, List<PythonBytecode> bytecode) {
		UserFunctionObject fnc = new UserFunctionObject();

		String functionName = "future";
		Utils.putPublic(fnc, "__name__",
				new StringObject(compilingClass.peek() == null ? functionName
						: compilingClass.peek() + "." + functionName));

		List<PythonBytecode> fncb = new ArrayList<PythonBytecode>();
		compilingClass.push(null);
		doCompileFunction(ctx, fncb, ctx.start);
		compilingClass.pop();

		cb = addBytecode(fncb, Bytecode.RETURN, ctx.stop);
		cb.intValue = 1;

		fnc.block = new CompiledBlockObject(fncb);
		Utils.putPublic(fnc, "function_defaults", new StringDictObject());
		fnc.args = new ArrayList<String>();

		cb = addBytecode(bytecode, Bytecode.PUSH, ctx.stop);
		cb.value = fnc;
		addBytecode(bytecode, Bytecode.RESOLVE_CLOSURE, ctx.stop);

		cb = addBytecode(bytecode, Bytecode.MAKE_FUTURE, ctx.stop);
		cb.object = inspectVariables(ctx).toArray(new String[] {});
	}

	private Set<String> inspectVariables(TestContext testlist) {
		Set<String> vars = new HashSet<>();
		for (ParseTree x : testlist.children)
			inspectVariables(x, vars);
		return vars;
	}

	private void inspectVariables(ParseTree t, Set<String> vars) {
		if (t instanceof NnameContext)
			vars.add(t.getText());
		for (int i = 0; i < t.getChildCount(); i++)
			inspectVariables(t.getChild(i), vars);
	}

	private boolean findFuture(TestContext testlist) {
		Boolean rv = null;
		for (ParseTree x : testlist.children) {
			rv = findFuture(x);
			if (rv != null)
				return rv;
		}
		return false;
	}

	private Boolean findFuture(ParseTree t) {
		Boolean rv = null;
		if (t instanceof FutureContext)
			return true;
		if (t instanceof LambdefContext)
			return false;
		for (int i = 0; i < t.getChildCount(); i++) {
			rv = findFuture(t.getChild(i));
			if (rv != null)
				return rv;
		}
		return rv;
	}

	private void compile(Or_testContext ctx, List<PythonBytecode> bytecode) {
		List<PythonBytecode> jumps = new LinkedList<>();
		int last = ctx.and_test().size() - 1;
		for (int i = 0; i < last; i++) {
			compile(ctx.and_test(i), bytecode);
			addBytecode(bytecode, Bytecode.DUP, ctx.start);
			addBytecode(bytecode, Bytecode.TRUTH_VALUE, ctx.start);
			cb = addBytecode(bytecode, Bytecode.JUMPIFTRUE, ctx.start);
			addBytecode(bytecode, Bytecode.POP, ctx.start);
			jumps.add(cb);
		}
		compile(ctx.and_test(last), bytecode);
		for (PythonBytecode c : jumps)
			c.intValue = bytecode.size();
	}

	private void compile(And_testContext ctx, List<PythonBytecode> bytecode) {
		List<PythonBytecode> jumps = new LinkedList<>();
		int last = ctx.not_test().size() - 1;
		for (int i = 0; i < last; i++) {
			compile(ctx.not_test(i), bytecode);
			addBytecode(bytecode, Bytecode.DUP, ctx.start);
			addBytecode(bytecode, Bytecode.TRUTH_VALUE, ctx.start);
			cb = addBytecode(bytecode, Bytecode.JUMPIFFALSE, ctx.start);
			addBytecode(bytecode, Bytecode.POP, ctx.start);
			jumps.add(cb);
		}
		compile(ctx.not_test(last), bytecode);
		for (PythonBytecode c : jumps)
			c.intValue = bytecode.size();
	}

	private void compile(Not_testContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.not_test() != null) {
			int amount = countNots(ctx, bytecode);
			cb = addBytecode(bytecode, Bytecode.TRUTH_VALUE, ctx.start);
			cb.intValue = amount % 2;
		} else if (ctx.ready_op() != null)
			compile(ctx.ready_op(), bytecode);
		else if (ctx.future_op() != null)
			compile(ctx.future_op(), bytecode);
		else
			compile(ctx.comparison(), bytecode);
	}

	private void compile(Ready_opContext ctx, List<PythonBytecode> bytecode) {
		cb = addBytecode(bytecode, Bytecode.TEST_FUTURE, ctx.start);
		cb.stringValue = ctx.nname().getText();
	}

	private void compile(Future_opContext ctx, List<PythonBytecode> bytecode) {
		cb = addBytecode(bytecode, Bytecode.LOAD_FUTURE, ctx.start);
		cb.stringValue = ctx.nname().getText();
	}

	private int countNots(Not_testContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.not_test() != null)
			return 1 + countNots(ctx.not_test(), bytecode);
		else if (ctx.ready_op() != null)
			compile(ctx.ready_op(), bytecode);
		else if (ctx.future_op() != null)
			compile(ctx.future_op(), bytecode);
		else
			compile(ctx.comparison(), bytecode);
		return 0;
	}

	private void compile(ComparisonContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.getChildCount() > 1) {
			if (ctx.getChild(1).getText().equals("in")
					|| ctx.getChild(1).getText().equals("notin")) {
				String operation = ObjectTypeObject.__CONTAINS__;
				compile(ctx.expr(1), bytecode);
				putGetAttr(operation, bytecode,
						((ExprContext) ctx.getChild(2)).start);
				compile((ExprContext) ctx.getChild(0), bytecode);
				cb = addBytecode(bytecode, Bytecode.CALL,
						((ExprContext) ctx.getChild(2)).start);
				cb.intValue = 1;
				if (ctx.getChild(1).getText().equals("notin"))
					putNot(bytecode, ((ExprContext) ctx.getChild(2)).start);
				return;
			}

			compile(ctx.expr(0), bytecode);
			for (int i = 1; i < ctx.getChildCount(); i += 2) {
				Bytecode operation = null;
				if (ctx.getChild(i).getText().equals("<"))
					operation = Bytecode.LT;
				else if (ctx.getChild(i).getText().equals(">"))
					operation = Bytecode.GT;
				else if (ctx.getChild(i).getText().equals("=="))
					operation = Bytecode.EQ;
				else if (ctx.getChild(i).getText().equals(">="))
					operation = Bytecode.GE;
				else if (ctx.getChild(i).getText().equals("<="))
					operation = Bytecode.LE;
				else if (ctx.getChild(i).getText().equals("<>"))
					operation = Bytecode.NE;
				else if (ctx.getChild(i).getText().equals("!="))
					operation = Bytecode.NE;
				else if (ctx.getChild(i).getText().equals("?"))
					operation = Bytecode.QM;
				else if (ctx.getChild(i).getText().equals("is")
						|| ctx.getChild(i).getText().equals("isnot")) {
					cb = addBytecode(bytecode, Bytecode.LOADBUILTIN,
							((ExprContext) ctx.getChild(i + 1)).start);
					cb.stringValue = ObjectTypeObject.IS;
					cb = addBytecode(bytecode, Bytecode.SWAP_STACK,
							((ExprContext) ctx.getChild(i + 1)).start);
					compile((ExprContext) ctx.getChild(i + 1), bytecode);
					cb = addBytecode(bytecode, Bytecode.CALL,
							((ExprContext) ctx.getChild(i + 1)).start);
					cb.intValue = 2;
					if (ctx.getChild(i).getText().equals("isnot"))
						putNot(bytecode,
								((ExprContext) ctx.getChild(i + 1)).start);
					return;
				} else
					throw new SyntaxError("unsupported comparison operation");
				compile((ExprContext) ctx.getChild(i + 1), bytecode);
				addBytecode(bytecode, operation,
						((ExprContext) ctx.getChild(i + 1)).start);
			}
		} else
			compile(ctx.expr(0), bytecode);
	}

	private void compile(ExprContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.getChildCount() > 1) {
			compile(ctx.xor_expr(0), bytecode);
			for (int i = 1; i < ctx.xor_expr().size(); i++) {
				compile(ctx.xor_expr(i), bytecode);
				addBytecode(bytecode, Bytecode.OR, ctx.xor_expr(i).stop);
			}
		} else
			compile(ctx.xor_expr(0), bytecode);
	}

	private void compile(Xor_exprContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.getChildCount() > 1) {
			compile(ctx.and_expr(0), bytecode);
			for (int i = 1; i < ctx.and_expr().size(); i++) {
				compile(ctx.and_expr(i), bytecode);
				addBytecode(bytecode, Bytecode.XOR, ctx.and_expr(i).stop);
			}
		} else
			compile(ctx.and_expr(0), bytecode);
	}

	private void compile(And_exprContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.getChildCount() > 1) {
			compile(ctx.shift_expr(0), bytecode);
			for (int i = 1; i < ctx.shift_expr().size(); i++) {
				compile(ctx.shift_expr(i), bytecode);
				addBytecode(bytecode, Bytecode.AND, ctx.shift_expr(i).stop);
			}
		} else
			compile(ctx.shift_expr(0), bytecode);
	}

	private void compile(Shift_exprContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.getChildCount() > 1) {
			compile(ctx.arith_expr(0), bytecode);
			for (int i = 1; i < ctx.getChildCount(); i += 2) {
				String op = ctx.getChild(i).getText();
				Bytecode operation = null;
				switch (op) {
				case "<<":
					operation = Bytecode.LSHIFT;
					break;
				case ">>":
					operation = Bytecode.RSHIFT;
					break;
				case "::":
					operation = Bytecode.DCOLON;
					break;
				case "->":
					operation = Bytecode.RARROW;
					break;
				}
				compile((Arith_exprContext) ctx.getChild(i + 1), bytecode);
				addBytecode(bytecode, operation,
						((Arith_exprContext) ctx.getChild(i + 1)).stop);
			}
		} else
			compile(ctx.arith_expr(0), bytecode);
	}

	private void compile(Arith_exprContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.getChildCount() > 1) {
			compile(ctx.term(0), bytecode);
			for (int i = 1; i < ctx.getChildCount(); i += 2) {
				compile((TermContext) ctx.getChild(i + 1), bytecode);

				Bytecode operation = ctx.getChild(i).getText().equals("+") ? Bytecode.ADD
						: Bytecode.SUB;
				addBytecode(bytecode, operation,
						((TermContext) ctx.getChild(i + 1)).stop);
			}
		} else
			compile(ctx.term(0), bytecode);
	}

	private void compile(TermContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.getChildCount() > 1) {
			compile(ctx.factor(0), bytecode);
			for (int i = 1; i < ctx.getChildCount(); i += 2) {
				String operation = ctx.getChild(i).getText();
				Bytecode op = null;
				if (operation.equals("*"))
					op = Bytecode.MUL;
				if (operation.equals("/"))
					op = Bytecode.DIV;
				if (operation.equals("%"))
					op = Bytecode.MOD;
				compile((FactorContext) ctx.getChild(i + 1), bytecode);
				addBytecode(bytecode, op,
						((FactorContext) ctx.getChild(i + 1)).stop);
			}
		} else
			compile(ctx.factor(0), bytecode);
	}

	private void compile(FactorContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.factor() != null) {
			if (ctx.getText().startsWith("~")) {
				putGetAttr(NumberObject.__NEG__, bytecode, ctx.start);
				cb = addBytecode(bytecode, Bytecode.CALL, ctx.start);
				cb.intValue = 0;
				return;
			}

			Bytecode operation = null;
			if (ctx.getText().startsWith("+"))
				operation = Bytecode.ADD;
			if (ctx.getText().startsWith("-"))
				operation = Bytecode.SUB;

			cb = addBytecode(bytecode, Bytecode.PUSH, ctx.factor().start);
			cb.value = NumberObject.valueOf(0);
			compile(ctx.factor(), bytecode);
			addBytecode(bytecode, operation, ctx.factor().stop);
		} else
			compile(ctx.power(), bytecode);
	}

	private void compile(PowerContext ctx, List<PythonBytecode> bytecode) {
		compile(ctx.atom(), bytecode);
		if (ctx.trailer().size() > 0) {
			for (TrailerContext tc : ctx.trailer()) {
				compile(tc, bytecode);
			}
		}
		if (ctx.factor() != null) {
			compile(ctx.factor(), bytecode);
			addBytecode(bytecode, Bytecode.POW, ctx.factor().stop);
		}
	}

	/** Compiles calls - something() - and indexes - something[x] */
	private void compile(TrailerContext tc, List<PythonBytecode> bytecode) {
		if (tc.getText().startsWith("(")) {
			if (tc.arglist() == null) {
				cb = addBytecode(bytecode, Bytecode.CALL, tc.stop);
				cb.intValue = 0;
			} else {
				CallArgsData args = compileArguments(tc.arglist(), bytecode);
				cb = addBytecode(bytecode, Bytecode.CALL, tc.stop);
				cb.intValue = args.normalArgCount;
				if (args.hasArgExpansion)
					cb.intValue = -1 * (cb.intValue + 1);
			}
		} else if (tc.getText().startsWith("[")) {
			putGetAttr("__getitem__", bytecode, tc.start);
			compileSubscript(tc.subscriptlist(), bytecode);
		} else {
			putGetAttr(tc.NAME().getText(), bytecode, tc.start);
		}
	}

	private void compileSubscript(SubscriptlistContext sc,
			List<PythonBytecode> bytecode) {
		int tlc = sc.subscript().size();
		if (tlc > 1) {
			cb = addBytecode(bytecode, Bytecode.LOADBUILTIN, sc.start);
			cb.stringValue = TupleTypeObject.MAKE_TUPLE_CALL;
		}
		for (SubscriptContext s : sc.subscript()) {
			compile(s, bytecode);
		}
		if (tlc > 1) {
			cb = addBytecode(bytecode, Bytecode.CALL, sc.stop);
			cb.intValue = tlc;
		}
		cb = addBytecode(bytecode, Bytecode.CALL, sc.stop);
		cb.intValue = 1;
	}

	private void compile(SubscriptContext s, List<PythonBytecode> bytecode) {
		if (s.ellipsis() != null) {
			cb = addBytecode(bytecode, Bytecode.PUSH, s.start);
			cb.value = EllipsisObject.ELLIPSIS;
			return;
		}

		if (s.stest() != null) {
			compile(s.stest().test(), bytecode);
		} else {
			cb = addBytecode(bytecode, Bytecode.LOADBUILTIN, s.start);
			cb.stringValue = SliceTypeObject.SLICE_CALL;
			if (s.test().size() == 0) {
				for (int i = 0; i < 3; i++) {
					cb = addBytecode(bytecode, Bytecode.PUSH, s.start);
					cb.value = NoneObject.NONE;
				}
			} else if (s.test().size() == 2) {
				if (s.sliceop() == null) {
					compile(s.test(0), bytecode);
					compile(s.test(1), bytecode);
					cb = addBytecode(bytecode, Bytecode.PUSH, s.stop);
					cb.value = NoneObject.NONE;
				} else {
					compile(s.test(0), bytecode);
					compile(s.test(1), bytecode);
					compile(s.sliceop().test(), bytecode);
				}
			} else if (s.test().size() == 1) {
				if (s.getText().startsWith(":")) {
					cb = addBytecode(bytecode, Bytecode.PUSH, s.start);
					cb.value = NoneObject.NONE;
					compile(s.test(0), bytecode);
					cb = addBytecode(bytecode, Bytecode.PUSH, s.stop);
					cb.value = NoneObject.NONE;
				} else {
					compile(s.test(0), bytecode);
					cb = addBytecode(bytecode, Bytecode.PUSH, s.stop);
					cb.value = NoneObject.NONE;
					cb = addBytecode(bytecode, Bytecode.PUSH, s.stop);
					cb.value = NoneObject.NONE;
				}
			}

			cb = addBytecode(bytecode, Bytecode.CALL, s.stop);
			cb.intValue = 3;
		}
	}

	/** Compiles function call arguments */
	private CallArgsData compileArguments(ArglistContext arglist,
			List<PythonBytecode> bytecode) {
		CallArgsData rv = new CallArgsData();
		List<String> kws = new ArrayList<>();
		for (ArgumentContext ac : arglist.argument()) {
			if (ac.kwarg() != null) {
				rv.kwArgCount++;
				compile(ac.kwarg().test(), bytecode);
				kws.add(0, ac.kwarg().nname().getText());
			} else {
				rv.normalArgCount++;
				if (rv.kwArgCount > 0)
					throw new SyntaxError("non-keyword arg after keyword arg");
				compile(ac.test(), bytecode);
			}
		}
		if (arglist.arg_kwexpand() != null) {
			// call(**b)
			compile(arglist.arg_kwexpand().test(), bytecode);
			cb = addBytecode(bytecode, Bytecode.UNPACK_KWARG, arglist.start);
			rv.hasKwExpansion = true;
		}
		if (kws.size() > 0) {
			cb = addBytecode(bytecode, Bytecode.KWARG, arglist.start);
			cb.object = kws.toArray(new String[] {});
		}
		if (arglist.test() != null) {
			// call(*a)
			compile(arglist.test(), bytecode);
			rv.hasArgExpansion = true;
		}
		return rv;
	}

	/**
	 * Dummy container as I can't return multiple stuff from compileArguments
	 * method
	 */
	private static class CallArgsData {
		public int kwArgCount = 0;
		public int normalArgCount = 0;
		public boolean hasArgExpansion = false;
		@SuppressWarnings("unused")
		public boolean hasKwExpansion = false;
	}

	private void compile(AtomContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.nname() != null) {
			String name = ctx.nname().getText();
			if (stack.typeOfVariable(name) == VariableType.GLOBAL)
				cb = addBytecode(bytecode, Bytecode.LOADGLOBAL, ctx.start);
			else if (stack.typeOfVariable(name) == VariableType.DYNAMIC)
				cb = addBytecode(bytecode, Bytecode.LOADDYNAMIC, ctx.start);
			else
				cb = addBytecode(bytecode, Bytecode.LOAD, ctx.start);
			cb.stringValue = name;
		} else if (ctx.number() != null) {
			NumberContext nb = ctx.number();
			if (nb.integer() != null) {
				IntegerContext ic = nb.integer();
				String numberValue = nb.integer().getText();
				BigInteger bi = null;

				if (ic.BIN_INTEGER() != null)
					bi = new BigInteger(
							numberValue.startsWith("x") ? numberValue.substring(1)
									: numberValue.substring(2), 2);
				if (ic.OCT_INTEGER() != null)
					bi = new BigInteger(
							numberValue.startsWith("x") ? numberValue.substring(1)
									: numberValue.substring(2), 8);
				if (ic.DECIMAL_INTEGER() != null)
					bi = new BigInteger(numberValue, 10);
				if (ic.HEX_INTEGER() != null)
					bi = new BigInteger(
							numberValue.startsWith("x") ? numberValue.substring(1)
									: numberValue.substring(2), 16);

				NumberObject o = NumberObject.valueOf(bi.longValue());
				cb = addBytecode(bytecode, Bytecode.PUSH, ctx.start);
				cb.value = o;
			} else if (nb.FLOAT_NUMBER() != null) {
				String numberValue = nb.FLOAT_NUMBER().getText();
				NumberObject r = NumberObject.valueOf(Double
						.parseDouble(numberValue));
				cb = addBytecode(bytecode, Bytecode.PUSH, ctx.start);
				cb.value = r;
			} else {
				String n = nb.IMAG_NUMBER().getText();
				ComplexObject c = new ComplexObject(0, Double.parseDouble(n
						.substring(0, n.length() - 1)));
				cb = addBytecode(bytecode, Bytecode.PUSH, ctx.start);
				cb.value = c;
			}
		} else if (ctx.string().size() != 0) {
			StringBuilder bd = new StringBuilder();
			for (StringContext s : ctx.string()) {
				if (s.stringLiterar().SHORT_STRING() != null) {
					String ss = s.getText();
					ss = ss.substring(1, ss.length() - 1);
					bd.append(ss);
				} else {
					String ss = s.getText();
					ss = ss.substring(3, ss.length() - 4);
					bd.append(ss);
				}
			}
			cb = addBytecode(bytecode, Bytecode.PUSH, ctx.start);
			cb.value = new StringObject(recompactSpecials(bd.toString()));
		} else if (ctx.getText().startsWith("(")) {
			compile(ctx.bracket_atom(), bytecode, ctx.start);
		} else if (ctx.getText().startsWith("[")) {
			compile(ctx.listmaker(), bytecode, ctx.getStart());
		} else if (ctx.getText().startsWith("{")) {
			compile(ctx.dictorsetmaker(), bytecode, ctx.getStart());
		} else if (ctx.getText().equals("print")) {
			if (futures.contains(Futures.PRINT_FUNCTION)) {
				cb = addBytecode(bytecode, Bytecode.LOADBUILTIN, ctx.start);
				cb.stringValue = "print_function_fnc";
			} else {
				throw new SyntaxError(
						"invalid syntax print not a function (did you forget future import?)");
			}
		}
	}

	private String recompactSpecials(String string) {
		StringBuilder bd = new StringBuilder();

		for (int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);

			if (c == '\\') {
				char next = string.charAt(++i);
				switch (next) {
				case '\\':
					bd.append("\\");
					break;
				case '\'':
					bd.append("'");
					break;
				case '"':
					bd.append("\"");
					break;
				case 'b':
					bd.append("\b");
					break;
				case 't':
					bd.append("\t");
					break;
				case 'n':
					bd.append("\n");
					break;
				case 'f':
					bd.append("\f");
					break;
				case 'r':
					bd.append("\r");
					break;
				default:
					bd.append(c);
					bd.append(next);
				}
			} else {
				bd.append(c);
			}
		}

		return bd.toString();
	}

	private void compile(Bracket_atomContext ctx,
			List<PythonBytecode> bytecode, Token t) {
		if (ctx == null) {
			cb = addBytecode(bytecode, Bytecode.LOADBUILTIN, t);
			cb.stringValue = TupleTypeObject.MAKE_TUPLE_CALL;
			cb = addBytecode(bytecode, Bytecode.CALL, t);
			cb.intValue = 0;
			return;
		}
		if (ctx.testlist_comp() != null)
			compile(ctx.testlist_comp(), bytecode, t);
		else
			compile(ctx.yield_expr(), bytecode);
	}

	private void compile(DictentryContext dcx, List<PythonBytecode> bytecode) {
		addBytecode(bytecode, Bytecode.DUP, dcx.start);
		compile(dcx.test(0), bytecode);
		compile(dcx.test(1), bytecode);
		cb = addBytecode(bytecode, Bytecode.CALL, dcx.start);
		cb.intValue = 2;
		addBytecode(bytecode, Bytecode.POP, dcx.stop);
	}

	private void compile(Testlist_compContext ctx,
			List<PythonBytecode> bytecode, Token t) {
		if (ctx.comp_for() != null) {
			// TODO
		} else {
			if ((ctx.test().size() > 1)
					|| ctx.children.get(ctx.children.size() - 1).getText()
							.equals(",")) {
				cb = addBytecode(bytecode, Bytecode.LOADBUILTIN, ctx.start);
				cb.stringValue = TupleTypeObject.MAKE_TUPLE_CALL;
				for (TestContext i : ctx.test())
					compile(i, bytecode);
				cb = addBytecode(bytecode, Bytecode.CALL, ctx.stop);
				cb.intValue = ctx.test().size();
			} else {
				compile(ctx.test(0), bytecode);
			}
		}
	}

	private void compile(ListmakerContext listmaker,
			List<PythonBytecode> bytecode, Token token) {
		if (listmaker == null) {
			// [ ]
			cb = addBytecode(bytecode, Bytecode.LOADBUILTIN, token);
			cb.stringValue = ListTypeObject.MAKE_LIST_CALL;
			cb = addBytecode(bytecode, Bytecode.CALL, token);
			cb.intValue = 0;
			return;
		}
		if (listmaker.list_for() != null) {
			// [ x for x in somethingiterable ]
			List_forContext fCtx = listmaker.list_for();
			// Generate empty list
			cb = addBytecode(bytecode, Bytecode.LOADBUILTIN, listmaker.start);
			cb.stringValue = ListTypeObject.LIST_CALL;
			cb = addBytecode(bytecode, Bytecode.CALL, listmaker.stop);
			cb.intValue = 0;
			/** Stack: TOP -> list */
			addBytecode(bytecode, Bytecode.DUP, listmaker.stop);
			// Get append method
			putGetAttr("append", bytecode, fCtx.start);
			/** Stack: TOP -> list -> list.append */
			compileListFor(listmaker.test(0), fCtx, bytecode);
			addBytecode(bytecode, Bytecode.POP, fCtx.start);
			/** Stack: TOP -> list */
		} else {
			cb = addBytecode(bytecode, Bytecode.LOADBUILTIN, listmaker.start);
			cb.stringValue = ListTypeObject.MAKE_LIST_CALL;
			for (TestContext t : listmaker.test())
				compile(t, bytecode);
			cb = addBytecode(bytecode, Bytecode.CALL, listmaker.stop);
			cb.intValue = listmaker.test().size();
		}
	}

	/**
	 * Compiles list_for part of list comprehension. Note: When this block is
	 * compiled, TOP -> list -> list.append is required to be prepared on stack.
	 * Same thing is left after block is executed
	 */
	private void compileListFor(TestContext expression, List_forContext fCtx,
			List<PythonBytecode> bytecode) {
		// Compile somethingiterable & get iterator
		cb = addBytecode(bytecode, Bytecode.LOADBUILTIN, fCtx.start);
		cb.stringValue = "iter";
		compileRightHand(fCtx.testlist(), bytecode);
		cb = addBytecode(bytecode, Bytecode.RCALL, fCtx.testlist().start);
		cb.intValue = 1;
		putGetAttr("next", bytecode, fCtx.start);
		// Compile getting item from iterator
		/** Stack: TOP -> list -> list.append -> iterator.next */
		int loopStart = bytecode.size();
		PythonBytecode acceptIter;
		addBytecode(bytecode, Bytecode.ECALL, fCtx.start);
		acceptIter = addBytecode(bytecode, Bytecode.ACCEPT_ITER, fCtx.start);
		// Compile assigning to variable
		compileAssignment(fCtx.exprlist(), bytecode);
		/** Stack: TOP -> list -> list.append -> iterator.next */
		// Compile optional list_iter
		boolean compileStoring = true;
		if (fCtx.list_iter() != null) {
			List_iterContext lIter = fCtx.list_iter();
			if (lIter.list_if() != null) {
				compile(lIter.list_if().test(), bytecode);
				addBytecode(bytecode, Bytecode.TRUTH_VALUE, fCtx.start);
				cb = addBytecode(bytecode, Bytecode.JUMPIFFALSE, fCtx.start);
				cb.intValue = loopStart;
			} else if (lIter.list_for() != null) {
				compileStoring = false;
				/** Stack: TOP -> list -> list.append -> iterator.next */
				cb = addBytecode(bytecode, Bytecode.DUP, fCtx.start);
				cb.intValue = 1;
				/**
				 * Stack: TOP -> list -> list.append -> iterator.next ->
				 * list.append
				 */
				compileListFor(expression, lIter.list_for(), bytecode);
				addBytecode(bytecode, Bytecode.POP, fCtx.start);
			}
		}
		if (compileStoring) {
			// Compile expression
			compile(expression, bytecode);
			// Compile storing to list
			/** Stack: TOP -> list -> list.append -> iterator.next -> expression */
			cb = addBytecode(bytecode, Bytecode.DUP, fCtx.start);
			cb.intValue = 2;
			/**
			 * Stack: TOP -> list -> list.append -> iterator.next -> expression
			 * -> list.append
			 */
			addBytecode(bytecode, Bytecode.SWAP_STACK, fCtx.start);
			/**
			 * Stack: TOP -> list -> list.append -> iterator.next -> list.append
			 * -> expression
			 */
			cb = addBytecode(bytecode, Bytecode.RCALL, fCtx.start);
			cb.intValue = 1;
			addBytecode(bytecode, Bytecode.POP, fCtx.start);
		}
		/** Stack: TOP -> list -> list.append -> iterator.next */
		cb = addBytecode(bytecode, Bytecode.GOTO, fCtx.start);
		cb.intValue = loopStart;
		acceptIter.intValue = bytecode.size();
		/** Stack: TOP -> list -> list.append -> iterator.next */
		addBytecode(bytecode, Bytecode.POP, fCtx.start);
	}

	private void compile(DictorsetmakerContext dictorsetmaker,
			List<PythonBytecode> bytecode, Token t) {
		if ((dictorsetmaker == null)
				|| ((dictorsetmaker.dictentry(0) != null) && (dictorsetmaker
						.comp_for() == null))) {
			// { } or { x:y, a:b, h:i }
			cb = addBytecode(bytecode, Bytecode.LOADBUILTIN, t);
			cb.stringValue = DictTypeObject.DICT_CALL;
			cb = addBytecode(bytecode, Bytecode.CALL, t);
			cb.intValue = 0;
			if (dictorsetmaker != null) {
				// { x:y, a:b, h:i }
				addBytecode(bytecode, Bytecode.DUP, dictorsetmaker.start);
				putGetAttr(DictObject.__SETITEM__, bytecode,
						dictorsetmaker.start);
				if (dictorsetmaker.comp_for() != null) {
					// TODO
					throw new NotImplementedException();
				} else
					for (DictentryContext dcx : dictorsetmaker.dictentry())
						compile(dcx, bytecode);
				addBytecode(bytecode, Bytecode.POP, t);
			}
		} else {
			// { x : y for x in somethingiterable } or { x for x in
			// somethingiterable }
			Comp_forContext cCtx = dictorsetmaker.comp_for();
			List_forContext fCtx = dictorsetmaker.list_for();
			if (dictorsetmaker.dictentry(0) == null) {
				// { x for x in somethingiterable }
				// Generate empty list
				cb = addBytecode(bytecode, Bytecode.LOADBUILTIN,
						dictorsetmaker.start);
				cb.stringValue = ListTypeObject.LIST_CALL;
				cb = addBytecode(bytecode, Bytecode.CALL, dictorsetmaker.stop);
				cb.intValue = 0;
			} else {
				// { x : y for x in somethingiterable }
				// Generate empty dict
				cb = addBytecode(bytecode, Bytecode.LOADBUILTIN,
						dictorsetmaker.start);
				cb.stringValue = DictTypeObject.DICT_CALL;
				cb = addBytecode(bytecode, Bytecode.CALL, dictorsetmaker.stop);
				cb.intValue = 0;
			}
			/** Stack: TOP -> dict */
			addBytecode(bytecode, Bytecode.DUP, dictorsetmaker.stop);
			/** Stack: TOP -> dict -> dict.put */
			if (dictorsetmaker.dictentry(0) == null) {
				// set - Get append method for list
				putGetAttr("append", bytecode, fCtx.start);
				compileListFor(dictorsetmaker.test(0), fCtx, bytecode);
				/** Stack: TOP -> list -> list.append */
				addBytecode(bytecode, Bytecode.POP, dictorsetmaker.start);
				cb = addBytecode(bytecode, Bytecode.LOADBUILTIN,
						dictorsetmaker.stop);
				cb.stringValue = "set";
				addBytecode(bytecode, Bytecode.SWAP_STACK, dictorsetmaker.stop);
				/** Stack: TOP -> set -> list */
				cb = addBytecode(bytecode, Bytecode.RCALL, dictorsetmaker.stop);
				cb.intValue = 1;
				/** Stack: TOP -> set(list) */
			} else {
				// dict - Get setitem method
				putGetAttr(DictObject.__SETITEM__, bytecode, cCtx.start);
				compileCompFor(dictorsetmaker.dictentry(0), cCtx, bytecode);
				/** Stack: TOP -> dict -> dict.put */
				addBytecode(bytecode, Bytecode.POP, dictorsetmaker.start);
				/** Stack: TOP -> dict */
			}
		}
	}

	/**
	 * Compiles list_for part of list comprehension. Note: When this block is
	 * compiled, TOP -> list -> list.append is required to be prepared on stack.
	 * Same thing is left after block is executed
	 */
	private void compileCompFor(DictentryContext dictentryContext,
			Comp_forContext cCtx, List<PythonBytecode> bytecode) {
		// Compile somethingiterable & get iterator
		cb = addBytecode(bytecode, Bytecode.LOADBUILTIN, cCtx.start);
		cb.stringValue = "iter";
		compile(cCtx.or_test(), bytecode);
		cb = addBytecode(bytecode, Bytecode.RCALL, cCtx.or_test().start);
		cb.intValue = 1;
		putGetAttr("next", bytecode, cCtx.start);
		// Compile getting item from iterator
		/** Stack: TOP -> dict -> dict.put -> iterator.next */
		int loopStart = bytecode.size();
		PythonBytecode acceptIter;
		addBytecode(bytecode, Bytecode.ECALL, cCtx.start);
		acceptIter = addBytecode(bytecode, Bytecode.ACCEPT_ITER, cCtx.start);
		// Compile assigning to variable
		compileAssignment(cCtx.exprlist(), bytecode);
		/** Stack: TOP -> dict -> dict.put -> iterator.next */
		// Compile optional list_iter
		boolean compileStoring = true;
		if (cCtx.comp_iter() != null) {
			Comp_iterContext lIter = cCtx.comp_iter();
			if (lIter.comp_if() != null) {
				compile(lIter.comp_if().test(), bytecode);
				addBytecode(bytecode, Bytecode.TRUTH_VALUE, cCtx.start);
				cb = addBytecode(bytecode, Bytecode.JUMPIFFALSE, cCtx.start);
				cb.intValue = loopStart;
			} else if (lIter.comp_for() != null) {
				compileStoring = false;
				/** Stack: TOP -> dict -> dict.put -> iterator.next */
				cb = addBytecode(bytecode, Bytecode.DUP, cCtx.start);
				cb.intValue = 1;
				/** Stack: TOP -> dict -> dict.put -> iterator.next -> dict.put */
				compileCompFor(dictentryContext, lIter.comp_for(), bytecode);
				addBytecode(bytecode, Bytecode.POP, cCtx.start);
			}
		}
		if (compileStoring) {
			/** Stack: TOP -> dict -> dict.put -> iterator.next */
			// Grap dict.put
			cb = addBytecode(bytecode, Bytecode.DUP, cCtx.start);
			cb.intValue = 1;
			/** Stack: TOP -> dict -> dict.put -> iterator.next -> dict.put */
			// Compile expression
			compile(dictentryContext.test(1), bytecode);
			/**
			 * Stack: TOP -> dict -> dict.put -> iterator.next -> dict.put ->
			 * value
			 */
			compile(dictentryContext.test(0), bytecode);
			// Compile storing to list
			/**
			 * Stack: TOP -> dict -> dict.put -> iterator.next -> dict.put ->
			 * value -> key
			 */
			cb = addBytecode(bytecode, Bytecode.RCALL, cCtx.start);
			cb.intValue = 2;
			addBytecode(bytecode, Bytecode.POP, cCtx.start);
		}
		/** Stack: TOP -> dict -> dict.put -> iterator.next */
		cb = addBytecode(bytecode, Bytecode.GOTO, cCtx.start);
		cb.intValue = loopStart;
		acceptIter.intValue = bytecode.size();
		/** Stack: TOP -> dict -> dict.put -> iterator.next */
		addBytecode(bytecode, Bytecode.POP, cCtx.start);
	}

	private void compileTernary(TestContext ctx, List<PythonBytecode> bytecode) {
		// TODO Auto-generated method stub

	}

	private void compile(LambdefContext ctx, List<PythonBytecode> bytecode) {
		UserFunctionObject fnc = new UserFunctionObject();

		String functionName = "lambda";
		Utils.putPublic(fnc, "__name__",
				new StringObject(compilingClass.peek() == null ? functionName
						: compilingClass.peek() + "." + functionName));

		List<String> arguments = new ArrayList<String>();
		for (int i = 0; i < ctx.farg().size(); i++) {
			arguments.add(ctx.farg(i).nname().getText());
		}

		fnc.args = arguments;
		if (ctx.vararg() != null) {
			VarargContext vc = ctx.vararg();
			if (vc.svararg() != null) {
				fnc.isVararg = true;
				fnc.vararg = vc.svararg().nname().getText();
			}
			if (vc.kvararg() != null) {
				fnc.isKvararg = true;
				fnc.kvararg = vc.kvararg().nname().getText();
			}
		}

		List<PythonBytecode> fncb = new ArrayList<PythonBytecode>();
		compilingClass.push(null);
		doCompileFunction(ctx.suite(), fncb, ctx.suite().start);
		compilingClass.pop();

		if (fncb.get(fncb.size() - 1) instanceof Pop) {
			fncb.remove(fncb.size() - 1);
			cb = addBytecode(fncb, Bytecode.RETURN, ctx.stop);
			cb.intValue = 1;
		} else {
			cb = addBytecode(fncb, Bytecode.PUSH, ctx.stop);
			cb.value = NoneObject.NONE;
			cb = addBytecode(fncb, Bytecode.RETURN, ctx.stop);
			cb.intValue = 1;
		}

		fnc.block = new CompiledBlockObject(fncb);

		cb = addBytecode(bytecode, Bytecode.PUSH, ctx.stop);
		cb.value = fnc;

		addBytecode(bytecode, Bytecode.DUP, ctx.stop); // function_defaults

		cb = addBytecode(bytecode, Bytecode.PUSH, ctx.stop);
		cb.value = new StringDictObject();

		for (int i = 0; i < ctx.farg().size(); i++) {
			FargContext fctx = ctx.farg(i);
			if (fctx.test() != null) {
				addBytecode(bytecode, Bytecode.DUP, fctx.start);
				putGetAttr(DictObject.__SETITEM__, bytecode, fctx.start);
				cb = addBytecode(bytecode, Bytecode.PUSH, fctx.start);
				cb.value = new StringObject(fctx.nname().getText());
				compile(fctx.test(), bytecode);
				cb = addBytecode(bytecode, Bytecode.CALL, fctx.stop);
				cb.intValue = 2;
				addBytecode(bytecode, Bytecode.POP, fctx.stop);
			}
		}

		addBytecode(bytecode, Bytecode.SWAP_STACK, ctx.stop);
		cb = addBytecode(bytecode, Bytecode.SETATTR, ctx.stop);
		cb.stringValue = "function_defaults";
	}

	/**
	 * Generates bytecode that stores top of stack into whatever is passed as
	 * parameter
	 */
	@SuppressWarnings("unused")
	private void compileAssignment(TestlistContext tc,
			List<PythonBytecode> bytecode) {
		if (tc.test().size() > 1) {
			// x,y,z = ...
			cb = addBytecode(bytecode, Bytecode.UNPACK_SEQUENCE, tc.start);
			cb.intValue = tc.test().size();
			for (int i = 0; tc.test(i) != null; i++)
				compileAssignment(tc.test(i), bytecode);
		} else {
			// x = y
			compileAssignment(tc.test(0), bytecode);
		}
	}

	private void compileAssignment(TestContext ctx,
			List<PythonBytecode> bytecode) {
		if (ctx.lambdef() != null)
			throw new SyntaxError("can't assign to lambda");
		if (ctx.or_test(1) != null)
			throw new SyntaxError("can't assign to operator");
		compileAssignment(ctx.or_test(0), bytecode);
	}

	/**
	 * Generates bytecode that stores top of stack into whatever is passed as
	 * parameter
	 */
	private void compileAssignment(ExprlistContext ctx,
			List<PythonBytecode> bytecode) {
		if (ctx.expr().size() > 1) {
			throw new SyntaxError("can't assign to tuple");
		}
		compileAssignment(ctx.expr(0), bytecode);
	}

	private void compileAssignment(Or_testContext ctx,
			List<PythonBytecode> bytecode) {
		if (ctx.and_test(1) != null)
			throw new SyntaxError("can't assign to operator");
		compileAssignment(ctx.and_test(0), bytecode);
	}

	private void compileAssignment(And_testContext ctx,
			List<PythonBytecode> bytecode) {
		if (ctx.not_test(1) != null)
			throw new SyntaxError("can't assign to operator");
		compileAssignment(ctx.not_test(0), bytecode);
	}

	private void compileAssignment(Not_testContext ctx,
			List<PythonBytecode> bytecode) {
		if (ctx.not_test() != null)
			throw new SyntaxError("can't assign to operator");
		compileAssignment(ctx.comparison(), bytecode);
	}

	private void compileAssignment(ComparisonContext ctx,
			List<PythonBytecode> bytecode) {
		if (ctx.expr(1) != null)
			throw new SyntaxError("can't assign to comparison");
		compileAssignment(ctx.expr(0), bytecode);
	}

	private void compileAssignment(ExprContext ctx,
			List<PythonBytecode> bytecode) {
		if (ctx.xor_expr(1) != null)
			throw new SyntaxError("can't assign to operator");
		compileAssignment(ctx.xor_expr(0), bytecode);
	}

	private void compileAssignment(Xor_exprContext ctx,
			List<PythonBytecode> bytecode) {
		if (ctx.and_expr(1) != null)
			throw new SyntaxError("can't assign to operator");
		compileAssignment(ctx.and_expr(0), bytecode);
	}

	private void compileAssignment(And_exprContext ctx,
			List<PythonBytecode> bytecode) {
		if (ctx.shift_expr(1) != null)
			throw new SyntaxError("can't assign to operator");
		compileAssignment(ctx.shift_expr(0), bytecode);
	}

	private void compileAssignment(Shift_exprContext ctx,
			List<PythonBytecode> bytecode) {
		if (ctx.arith_expr(1) != null)
			throw new SyntaxError("can't assign to operator");
		compileAssignment(ctx.arith_expr(0), bytecode);
	}

	private void compileAssignment(Arith_exprContext ctx,
			List<PythonBytecode> bytecode) {
		if (ctx.term(1) != null)
			throw new SyntaxError("can't assign to operator");
		compileAssignment(ctx.term(0), bytecode);
	}

	private void compileAssignment(TermContext ctx,
			List<PythonBytecode> bytecode) {
		if (ctx.factor(1) != null)
			throw new SyntaxError("can't assign to operator");
		compileAssignment(ctx.factor(0), bytecode);
	}

	private void compileAssignment(FactorContext ctx,
			List<PythonBytecode> bytecode) {
		if (ctx.factor() != null)
			throw new SyntaxError("can't assign to operator");
		compileAssignment(ctx.power(), bytecode);
	}

	private void compileAssignment(PowerContext ctx,
			List<PythonBytecode> bytecode) {
		if (ctx.factor() != null)
			throw new SyntaxError("can't assign to operator");
		if (ctx.trailer().size() > 0)
			compileTrailers(ctx.atom(), ctx.trailer(), 0, bytecode);
		else
			compileAssignment(ctx.atom(), bytecode);
	}

	private void compileTrailers(AtomContext atom,
			List<TrailerContext> trailers, int offset,
			List<PythonBytecode> bytecode) {
		if (offset == trailers.size() - 1) {
			// Last trailer - set item
			if (trailers.size() == 1) {
				// Last trailer is also first one
				compile(atom, bytecode);
			}
			TrailerContext t = trailers.get(offset);
			if (t.arglist() != null) {
				// ... xyz(something)
				throw new SyntaxError("can't assign to function call");
			} else if (t.subscriptlist() != null) {
				// ... xyz[something]
				if (t.subscriptlist().subscript().size() > 1)
					throw new SyntaxError(
							"list indices must be integers, not tuple");
				SubscriptContext s = t.subscriptlist().subscript(0);
				if (s.stest() != null) {
					// xyz[a] = value
					// // stack -> value -> xyz
					putGetAttr(DictObject.__SETITEM__, bytecode,
							s.stest().start); // stack -> value ->
												// xyz.__SETITEM__
					addBytecode(bytecode, Bytecode.SWAP_STACK, s.stest().stop); // stack
																				// ->
																				// xyz.__SETITEM__
																				// ->
																				// value
					compile(s.stest().test(), bytecode); // stack ->
															// xyz.__SETITEM__
															// -> value -> a
					cb = addBytecode(bytecode, Bytecode.KCALL, s.stest().stop);
					cb.intValue = 2;
				} else {
					// xyz[a:b] = ...
					throw new SyntaxError(
							"assignment to splice not yet implemented");
				}
			} else {
				// ... xyz.something
				cb = addBytecode(bytecode, Bytecode.PUSH, t.start);
				cb.value = new StringObject(t.NAME().toString());
				cb = addBytecode(bytecode, Bytecode.SETATTR, t.start);
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

	private void compileAssignment(AtomContext ctx,
			List<PythonBytecode> bytecode) {
		if (ctx.listmaker() != null)
			throw new SyntaxError("can't assign to generator expression");
		if (ctx.dictorsetmaker() != null)
			throw new SyntaxError("can't assign to generator literal");
		if ((ctx.number() != null) || (ctx.string().size() > 0))
			throw new SyntaxError("can't assign to literal");
		if (ctx.bracket_atom() != null)
			throw new SyntaxError("can't assign to generator expression");
		compileAssignment(ctx.nname(), bytecode);
	}

	private void compileAssignment(NnameContext nname,
			List<PythonBytecode> bytecode) {
		String name = nname.getText();
		if (stack.typeOfVariable(name) == VariableType.GLOBAL)
			cb = addBytecode(bytecode, Bytecode.SAVEGLOBAL, nname.start);
		else if (stack.typeOfVariable(name) == VariableType.DYNAMIC)
			cb = addBytecode(bytecode, Bytecode.SAVEDYNAMIC, nname.start);
		else
			cb = addBytecode(bytecode, Bytecode.SAVE, nname.start);
		cb.stringValue = name;
	}

	private void compileImport2(Import_as_nameContext asname,
			List<PythonBytecode> bytecode, String packageNameBase) {
		String as;
		if (asname.children.size() == 3) {
			as = asname.nname().get(1).getText();
		} else {
			as = asname.nname().get(0).getText();
		}

		addImport(packageNameBase + "." + asname.nname().get(0).getText(), as,
				bytecode, asname.start);
	}

	private void compileImport(Dotted_as_nameContext dans,
			List<PythonBytecode> bytecode) {
		String as = null;
		NnameContext t = dans.nname();
		if (t != null) {
			as = t.getText();
		}

		String packageName = "";
		int i = 0;
		Dotted_nameContext d = dans.dotted_name();
		for (NnameContext name : d.nname()) {
			packageName += name.getText();
			if (++i != d.nname().size()) {
				packageName += ".";
			} else if (as == null)
				as = name.getText();
		}

		addImport(packageName, as, bytecode, dans.start);
	}

	private void addImport(String packageName, String as,
			List<PythonBytecode> bytecode, Token t) {
		cb = addBytecode(bytecode, Bytecode.IMPORT, t);
		cb.object = packageName;
		cb.stringValue = as;
	}

	private interface ControllStackItem {
		public void outputBreak(Break_stmtContext ctx,
				List<PythonBytecode> bytecode, ControllStack cs);

		public void outputContinue(Continue_stmtContext ctx,
				List<PythonBytecode> bytecode, ControllStack cs);

		public void outputFinallyBreakBlock(Try_stmtContext ctx,
				List<PythonBytecode> bytecode, ControllStack cs);

		public void outputFinallyContinueBlock(Try_stmtContext ctx,
				List<PythonBytecode> bytecode, ControllStack cs);
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

	private PythonBytecode addBytecode(List<PythonBytecode> bytecode,
			Bytecode bctype, Token token) {
		PythonBytecode rv;
		rv = Bytecode.makeBytecode(bctype, token, getFunction(), module);
		bytecode.add(rv);
		return rv;
	}

	private class TryFinallyItem implements ControllStackItem {
		private Try_stmtContext finallyCtx;
		boolean needsBreakBlock = false;
		boolean needsContinueBlock = false;

		TryFinallyItem(Try_stmtContext ctx) {
			this.finallyCtx = ctx;
		}

		@Override
		public void outputContinue(Continue_stmtContext ctx,
				List<PythonBytecode> bytecode, ControllStack cs) {
			needsContinueBlock = true;
			cb = addBytecode(bytecode, Bytecode.LOADBUILTIN, ctx.start);
			cb.stringValue = "LoopContinue";
			cb = addBytecode(bytecode, Bytecode.CALL, ctx.start);
			cb.intValue = 0;
			addBytecode(bytecode, Bytecode.RAISE, ctx.start);
		}

		@Override
		public void outputBreak(Break_stmtContext ctx,
				List<PythonBytecode> bytecode, ControllStack cs) {
			needsBreakBlock = true;
			cb = addBytecode(bytecode, Bytecode.LOADBUILTIN, ctx.start);
			cb.stringValue = "LoopBreak";
			cb = addBytecode(bytecode, Bytecode.CALL, ctx.start);
			cb.intValue = 0;
			addBytecode(bytecode, Bytecode.RAISE, ctx.start);
		}

		@Override
		public void outputFinallyBreakBlock(Try_stmtContext ctx,
				List<PythonBytecode> bytecode, ControllStack cs) {
			cs.pop(); // Pop this
			compileSuite(finallyCtx.try_finally().suite(), bytecode, cs);

			ControllStackItem overMe = cs.peek();
			if (overMe instanceof TryFinallyItem) {
				((TryFinallyItem) overMe).needsBreakBlock = true;
				addBytecode(bytecode, Bytecode.SWAP_STACK, ctx.start);
				addBytecode(bytecode, Bytecode.POP, ctx.start); // frame
				addBytecode(bytecode, Bytecode.SWAP_STACK, ctx.start);
				addBytecode(bytecode, Bytecode.POP, ctx.start); // return code
																// (should be
																// None)
				addBytecode(bytecode, Bytecode.RERAISE, ctx.start);
			} else if (overMe instanceof WithControllStackItem) {
				addBytecode(bytecode, Bytecode.SWAP_STACK, ctx.start);
				addBytecode(bytecode, Bytecode.POP, ctx.start); // frame
				addBytecode(bytecode, Bytecode.SWAP_STACK, ctx.start);
				addBytecode(bytecode, Bytecode.POP, ctx.start); // return code
																// (should be
																// None)
				addBytecode(bytecode, Bytecode.RERAISE, ctx.start);
			} else {
				addBytecode(bytecode, Bytecode.POP, ctx.start); // exception
				addBytecode(bytecode, Bytecode.POP, ctx.start); // frame
				addBytecode(bytecode, Bytecode.POP, ctx.start); // return code
																// (should be
																// None)
				cs.peek().outputFinallyBreakBlock(ctx, bytecode, cs);
			}
			ControllStack.push(cs, this); // Return this back to stack
		}

		@Override
		public void outputFinallyContinueBlock(Try_stmtContext ctx,
				List<PythonBytecode> bytecode, ControllStack cs) {
			cs.pop(); // Pop this
			compileSuite(finallyCtx.try_finally().suite(), bytecode, cs);

			ControllStackItem overMe = cs.peek();
			if (overMe instanceof TryFinallyItem) {
				((TryFinallyItem) overMe).needsContinueBlock = true;
				addBytecode(bytecode, Bytecode.SWAP_STACK, ctx.start);
				addBytecode(bytecode, Bytecode.POP, ctx.start); // frame
				addBytecode(bytecode, Bytecode.SWAP_STACK, ctx.start);
				addBytecode(bytecode, Bytecode.POP, ctx.start); // return code
																// (should be
																// None)
				addBytecode(bytecode, Bytecode.RERAISE, ctx.start);
			} else if (overMe instanceof WithControllStackItem) {
				addBytecode(bytecode, Bytecode.SWAP_STACK, ctx.start);
				addBytecode(bytecode, Bytecode.POP, ctx.start); // frame
				addBytecode(bytecode, Bytecode.SWAP_STACK, ctx.start);
				addBytecode(bytecode, Bytecode.POP, ctx.start); // return code
																// (should be
																// None)
				addBytecode(bytecode, Bytecode.RERAISE, ctx.start);
			} else {
				cb = addBytecode(bytecode, Bytecode.POP, ctx.start); // exception
				cb.intValue = 1;
				addBytecode(bytecode, Bytecode.POP, ctx.start); // frame
				addBytecode(bytecode, Bytecode.POP, ctx.start); // return code
																// (should be
																// None)
				cs.peek().outputFinallyContinueBlock(ctx, bytecode, cs);
			}
			ControllStack.push(cs, this); // Return this back to stack
		}
	}

	private String getFunction() {
		if (compilingFunction.size() <= 1)
			return Bytecode.NO_FUNCTION;
		String fnName = compilingFunction.peek();
		if (fnName == null) {
			if (compilingClass.size() <= 1)
				return Bytecode.NO_FUNCTION;
			if (compilingClass.peek() == null)
				return String.format("<class %s>", compilingClass.peek());
			return String.format("<class %s>", compilingClass.peek());
		}
		if ((compilingClass.size() > 1) && (compilingClass.peek() != null))
			return String.format("%s.%s", compilingClass.peek(), fnName);
		return fnName;
	}

	private class LoopStackItem implements ControllStackItem {
		private List<PythonBytecode> bcs = new LinkedList<>();
		public int start;

		LoopStackItem(int startAddress) {
			this.start = startAddress;
		}

		@Override
		public void outputContinue(Continue_stmtContext ctx,
				List<PythonBytecode> bytecode, ControllStack cs) {
			PythonBytecode c = Bytecode.makeBytecode(Bytecode.GOTO, ctx.start,
					getFunction(), module);
			c.intValue = start;
			bytecode.add(c);
		}

		@Override
		public void outputBreak(Break_stmtContext ctx,
				List<PythonBytecode> bytecode, ControllStack cs) {
			PythonBytecode c = Bytecode.makeBytecode(Bytecode.GOTO, ctx.start,
					getFunction(), module);
			bytecode.add(c);
			addJump(c);
		}

		@Override
		public void outputFinallyBreakBlock(Try_stmtContext ctx,
				List<PythonBytecode> bytecode, ControllStack cs) {
			PythonBytecode c = Bytecode.makeBytecode(Bytecode.GOTO, ctx.start,
					getFunction(), module);
			bytecode.add(c);
			addJump(c);
		}

		@Override
		public void outputFinallyContinueBlock(Try_stmtContext ctx,
				List<PythonBytecode> bytecode, ControllStack cs) {
			PythonBytecode c = Bytecode.makeBytecode(Bytecode.GOTO, ctx.start,
					getFunction(), module);
			c.intValue = start;
			bytecode.add(c);
		}

		public void addJump(PythonBytecode c) {
			bcs.add(c);
		}

		public void addContinue(ParserRuleContext ctx,
				List<PythonBytecode> bytecode) {
			cb = addBytecode(bytecode, Bytecode.GOTO, ctx.start);
			cb.intValue = start;
		}

		public void finalize(int endOfLoopAddress) {
			for (PythonBytecode c : bcs)
				c.intValue = endOfLoopAddress;
		}
	}
}
