package me.enerccio.sp.compiler;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import me.enerccio.sp.parser.pythonParser.ClassdefContext;
import me.enerccio.sp.parser.pythonParser.DecoratorsContext;
import me.enerccio.sp.parser.pythonParser.Flow_stmtContext;
import me.enerccio.sp.parser.pythonParser.Raise_stmtContext;
import me.enerccio.sp.parser.pythonParser.Return_stmtContext;
import me.enerccio.sp.parser.pythonParser.Try_exceptContext;
import me.enerccio.sp.parser.pythonParser.Try_stmtContext;
import me.enerccio.sp.parser.pythonParser.*;
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
import me.enerccio.sp.types.types.ListTypeObject;
import me.enerccio.sp.types.types.ObjectTypeObject;
import me.enerccio.sp.types.types.SliceTypeObject;
import me.enerccio.sp.types.types.TupleTypeObject;
import me.enerccio.sp.utils.Utils;

public class PythonCompiler {

	private PythonBytecode cb;
	private VariableStack stack = new VariableStack();
	private LinkedList<MapObject> environments = new LinkedList<MapObject>();
	private Stack<String> compilingClass = new Stack<String>();
	
	public static ThreadLocal<String> moduleName = new ThreadLocal<String>();
	
	public List<PythonBytecode> doCompile(File_inputContext fcx, MapObject dict, ModuleObject m) {
		moduleName.set(m.fields.get(ModuleObject.__NAME__).object.toString());
		
		stack.push();
		compilingClass.push(null);
		List<PythonBytecode> bytecode = new ArrayList<PythonBytecode>();
		// create new environment
		bytecode.add(Bytecode.makeBytecode(Bytecode.PUSH_ENVIRONMENT, fcx.start));
		// context
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH, fcx.start));
		cb.value = m;
		bytecode.add(Bytecode.makeBytecode(Bytecode.PUSH_LOCAL_CONTEXT, fcx.start));
		// locals
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH_DICT, fcx.start)); 
		cb.dict = dict;
		environments.add(dict);
		
		for (StmtContext sctx : fcx.stmt())
			compileStatement(sctx, bytecode);
		
		bytecode.add(Bytecode.makeBytecode(Bytecode.POP_ENVIRONMENT, fcx.stop));
		compilingClass.pop();
		stack.pop();
		environments.removeLast();
		return bytecode;
	}

	private void compileStatement(StmtContext sctx,
			List<PythonBytecode> bytecode) {
		if (sctx.simple_stmt() != null)
			compileSimpleStatement(sctx.simple_stmt(), bytecode);
		else
			compileCompoundStatement(sctx.compound_stmt(), bytecode);
	}

	private void compileCompoundStatement(Compound_stmtContext cstmt,
			List<PythonBytecode> bytecode) {
		if (cstmt.funcdef() != null) {
			compileFunction(cstmt.funcdef(), bytecode, null);
		} else if (cstmt.classdef() != null) {
			compileClass(cstmt.classdef(), bytecode, null);
		} else if (cstmt.try_stmt() != null) {
			compileTry(cstmt.try_stmt(), bytecode, null);
		} else if (cstmt.decorated() != null) {
			DecoratedContext dc = cstmt.decorated();
			if (dc.classdef() != null)
				compileClass(dc.classdef(), bytecode, dc.decorators());
			else if (dc.funcdef() != null)
				compileFunction(dc.funcdef(), bytecode, dc.decorators());
		} else
			throw Utils.throwException("SyntaxError", "statament type not implemented");
	}
	
	
	private void compileSuite(SuiteContext ctx, List<PythonBytecode> bytecode) {
		for (StmtContext sctx : ctx.stmt())
			compileStatement(sctx, bytecode);
	}

	private void compileTry(Try_stmtContext try_stmt, List<PythonBytecode> bytecode, Object object) {
		PythonBytecode makeFrame = Bytecode.makeBytecode(Bytecode.PUSH_FRAME, try_stmt.start);
		List<PythonBytecode> exceptJumps = new ArrayList<>();
		List<PythonBytecode> finallyJumps = new ArrayList<>();
		List<PythonBytecode> endJumps = new ArrayList<>();
		bytecode.add(makeFrame);
		bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN, try_stmt.start));
		bytecode.add(Bytecode.makeBytecode(Bytecode.SWAP_STACK, try_stmt.start));
		bytecode.add(Bytecode.makeBytecode(Bytecode.PUSH_EXCEPTION, try_stmt.start));
		// TOP -> return value -> frame -> exception
		if (try_stmt.try_except().size() > 0) {
			// There is at least one except block defined  
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
			
			// Compile actual except blocks
			// Stack contains TOP -> return value -> frame -> exception if any of those is executed
			int i = 0;
			for (Try_exceptContext ex : try_stmt.try_except()) {
				if (ex.except_clause().test().size() <= 1) {
					// try ... except:
					// or
					// try ... except ErrorType:
					// -> remove exception from top of stack
					exceptJumps.get(i++).argc = bytecode.size();
					bytecode.add(Bytecode.makeBytecode(Bytecode.POP, try_stmt.start));
					compileSuite(ex.suite(), bytecode);
					bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH, try_stmt.start));
					cb.value = NoneObject.NONE;
					bytecode.add(cb = Bytecode.makeBytecode(Bytecode.GOTO, try_stmt.start));
					finallyJumps.add(cb);
				} else {
					// try ... except ErrorType, xyz:
					exceptJumps.get(i++).argc = bytecode.size();
					// Store exception from top of stack
					compileAssignment(ex.except_clause().test(1), bytecode);
					compileSuite(ex.suite(), bytecode);
					bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH, try_stmt.start));
					cb.value = NoneObject.NONE;
					bytecode.add(cb = Bytecode.makeBytecode(Bytecode.GOTO, try_stmt.start));
					finallyJumps.add(cb);
				}
			}
		}
		
		// Compile finally block (if any)
		for (PythonBytecode c : finallyJumps) c.argc = bytecode.size();
		if (try_stmt.try_finally() != null)
			compileSuite(try_stmt.try_finally().suite(), bytecode);
		// Stack contains TOP -> return value -> frame -> exception here, exception may be None
		bytecode.add(Bytecode.makeBytecode(Bytecode.RERAISE, try_stmt.start));
		// If execution reaches here, there was no exception and there is still frame on top of stack.
		// If this frame returned value, it should be returned from here as well.
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.JUMPIFNORETURN, try_stmt.start));
		endJumps.add(cb);
		// There is TOP -> return value -> frame on stack if execution reaches here
		bytecode.add(Bytecode.makeBytecode(Bytecode.POP, try_stmt.start));
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.RETURN, try_stmt.start));
		cb.argc = 1;
		
		// Compile try block
		makeFrame.argc = bytecode.size();
		compileSuite(try_stmt.suite(), bytecode);
		bytecode.add(Bytecode.makeBytecode(Bytecode.RETURN, try_stmt.start));
		
		// Very end of try...catch block. Return value and frame is still on stack
		for (PythonBytecode c : endJumps) c.argc = bytecode.size();
		bytecode.add(Bytecode.makeBytecode(Bytecode.POP, try_stmt.start));
		bytecode.add(Bytecode.makeBytecode(Bytecode.POP, try_stmt.start));
	}

	private void compileClass(ClassdefContext classdef,
			List<PythonBytecode> bytecode, DecoratorsContext dc) {
		
		String className = classdef.nname().getText();
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.LOADGLOBAL, classdef.start));
		cb.variable = "type";
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH, classdef.start));
		cb.value = new StringObject(className);
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.LOADGLOBAL, classdef.start));
		cb.variable = "tuple";
		int c = classdef.testlist() != null ? classdef.testlist().test().size() : 0;
		if (classdef.testlist() != null)
			for (TestContext tc : classdef.testlist().test())
				compile(tc, bytecode);
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, classdef.start));
		cb.argc = c;
		bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN, classdef.start));
		UserFunctionObject fnc = new UserFunctionObject();
		fnc.newObject();
		Utils.putPublic(fnc, "__name__", new StringObject("$$__classBodyFncFor" + className + "$$"));
		fnc.args = new ArrayList<String>();
		
		compilingClass.push(className);
		MapObject cdc = doCompileFunction(classdef.suite(), fnc.bytecode);
		compilingClass.pop();
		
		Utils.putPublic(fnc, "function_defaults", new MapObject());
		
		fnc.bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH, classdef.stop));
		cb.value = cdc;
		fnc.bytecode.add(cb = Bytecode.makeBytecode(Bytecode.RETURN, classdef.stop));
		cb.argc = 1;
		
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH, classdef.stop));
		cb.value = fnc;
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, classdef.stop));
		cb.argc = 0;
		bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN, classdef.stop));
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, classdef.stop));
		cb.argc = 3;
		bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN, classdef.stop));
		
		if (dc != null){
			compile(dc, bytecode);
		}
		
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.SAVE, classdef.stop));
		cb.variable = className;
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
		MapObject locals = doCompileFunction(funcdef.suite(), fnc.bytecode);
		compilingClass.pop();
		
		fnc.bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH, funcdef.stop));
		cb.value = NoneObject.NONE;
		fnc.bytecode.add(cb = Bytecode.makeBytecode(Bytecode.RETURN, funcdef.stop));
		cb.argc = 1;
		
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
				cb.argc = 2;
			}
		}
		
		bytecode.add(Bytecode.makeBytecode(Bytecode.SWAP_STACK, funcdef.stop));
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.SETATTR, funcdef.stop));
		cb.variable = "function_defaults";
		
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH, funcdef.stop));
		cb.value = locals;
		bytecode.add(Bytecode.makeBytecode(Bytecode.SWAP_STACK, funcdef.stop));
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.SETATTR, funcdef.stop));
		cb.variable = "locals";
		
		List<MapObject> ll = new ArrayList<MapObject>();
		ll.add(locals);
		for (MapObject d : Utils.reverse(environments)){
			ll.add(d);
		}
		TupleObject closure = new TupleObject(ll.toArray(new PythonObject[ll.size()]));
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH, funcdef.stop));
		cb.value = closure;
		
		bytecode.add(Bytecode.makeBytecode(Bytecode.SWAP_STACK, funcdef.stop));
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.SETATTR, funcdef.stop));
		cb.variable = "closure";
		
		if (dc != null){
			compile(dc, bytecode);
		}
		
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.SAVE, funcdef.stop));
		cb.variable = functionName;
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
				cb.argc = argc;
				bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN, d.stop));
			}
			// call
			bytecode.add(Bytecode.makeBytecode(Bytecode.SWAP_STACK, dc.stop));
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, dc.stop));
			cb.argc = 1;
			bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN, dc.stop));
		}
	}

	private String doGetLongString(String text) {
		return text.substring(3, text.length()-4);
	}

	private MapObject doCompileFunction(SuiteContext suite,
			List<PythonBytecode> bytecode) {
		
		stack.push();
		bytecode.add(Bytecode.makeBytecode(Bytecode.PUSH_ENVIRONMENT, suite.start));
		MapObject dict = new MapObject();
		environments.add(dict);
		for (MapObject d : Utils.reverse(environments)){
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH_DICT, suite.start)); 
			cb.dict = d;
		}
		bytecode.add(Bytecode.makeBytecode(Bytecode.RESOLVE_ARGS, suite.start));
		
		for (StmtContext c : suite.stmt())
			compileStatement(c, bytecode);
		

		environments.removeLast();
		stack.pop();
		
		return dict;
	}

	private void compileSimpleStatement(Simple_stmtContext sstmt,
			List<PythonBytecode> bytecode) {
		for (Small_stmtContext smstmt : sstmt.small_stmt())
			compileSmallStatement(smstmt, bytecode);
	}

	private void compileSmallStatement(Small_stmtContext smstmt, List<PythonBytecode> bytecode) {
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
		}
		
		if (smstmt.pass_stmt() != null){
			bytecode.add(Bytecode.makeBytecode(Bytecode.NOP, smstmt.start));
		}
		
		if (smstmt.expr_stmt() != null){
			compile(smstmt.expr_stmt(), bytecode);
			bytecode.add(Bytecode.makeBytecode(Bytecode.POP, smstmt.stop));
		}
			
		if (smstmt.print_stmt() != null){
			compile(smstmt.print_stmt(), bytecode);
		}
		
		if (smstmt.flow_stmt() != null){
			compile(smstmt.flow_stmt(), bytecode);
		}
		
		if (smstmt.label_stmt() != null){
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.LABEL, smstmt.start));
			cb.variable = smstmt.label_stmt().nname().getText();
		}
		
	}

	private void compile(Flow_stmtContext ctx,
			List<PythonBytecode> bytecode) {
		if (ctx.return_stmt() != null){
			compile(ctx.return_stmt(), bytecode);
		}
		
		if (ctx.raise_stmt() != null)
			compile(ctx.raise_stmt(), bytecode);
	}

	private void compile(Raise_stmtContext ctx,
			List<PythonBytecode> bytecode) {
		if (ctx.test() != null)
			compile(ctx.test(), bytecode);
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.RAISE, ctx.start));
		if (ctx.test() != null)
			cb.stackException = true;
	}

	private void compile(Return_stmtContext ctx,
			List<PythonBytecode> bytecode) {
		if (compilingClass.peek() != null)
			throw Utils.throwException("SyntaxError", "return cannot be inside class definition");
		if (ctx.testlist() != null)
			compileRightHand(ctx.testlist(), bytecode);
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.RETURN, ctx.start));
		cb.argc = 1;
	}

	private void compile(Print_stmtContext ctx,
			List<PythonBytecode> bytecode) {
		if (ctx.push() != null){
			// TODO
		} else {
			boolean eol = (ctx.endp() == null);
			for (TestContext tc : ctx.test()){
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.LOADGLOBAL, tc.start));
				cb.variable = PythonRuntime.PRINT_JAVA;
				compile(tc, bytecode);
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, tc.stop));
				cb.argc = 1;
			}
			
			if (eol){
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.LOADGLOBAL, ctx.stop));
				cb.variable = PythonRuntime.PRINT_JAVA_EOL;
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, ctx.stop));
				cb.argc = 0;
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
			cb.argc = 1;
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN, expr.stop));
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
					cb.argc = tc.test().size();
					for (int i=0; tc.test(i) != null; i++)
						compileAssignment(tc.test(i), bytecode);
				} else {
					// x = y
					compileAssignment(tc.test(0), bytecode);
				}
			}
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
			cb.variable = TupleTypeObject.TUPLE_CALL;
		}
		
		for (TestContext tc : testlist.test())
			compile(tc, bytecode);
		
		if (tlc > 1){
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, testlist.stop));
			cb.argc = tlc;
			bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN, testlist.stop));
		}
	}
	
	private void putGetAttr(String attr, List<PythonBytecode> bytecode, Token t) {
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.GETATTR, t));
		cb.variable = attr;
		bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN, t));
	}
	
	private void putNot(List<PythonBytecode> bytecode, Token t) {
		putGetAttr("__not__", bytecode, t);
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, t));
		cb.argc = 0;
		bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN, t));
	}

	private void compile(TestContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.lambdef() != null)
			compile(ctx.lambdef(), bytecode);
		if (ctx.getChildCount() > 1)
			compileTernary(ctx, bytecode);
		compile(ctx.or_test(0), bytecode);
	}

	private void compile(Or_testContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.getChildCount() > 1){
			// TODO
		} else 
			compile(ctx.and_test(0), bytecode);
	}
	
	private void compile(And_testContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.getChildCount() > 1){
			// TODO
		} else 
			compile(ctx.not_test(0), bytecode);
	}
	
	private void compile(Not_testContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.not_test() != null){
			// TODO
		} else
			compile(ctx.comparison(), bytecode);
	}
	
	private void compile(ComparisonContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.getChildCount() > 1){
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
				else if (ctx.getChild(i).getText().equals("in") || ctx.getChild(i).getText().equals("notin"))
					operation = ObjectTypeObject.__CONTAINS__;
				else if (ctx.getChild(i).getText().equals("is") || ctx.getChild(i).getText().equals("isnot")) {
					bytecode.add(cb = Bytecode.makeBytecode(Bytecode.LOADGLOBAL, ((ExprContext) ctx.getChild(i+1)).start));
					cb.variable = ObjectTypeObject.IS;
					bytecode.add(cb = Bytecode.makeBytecode(Bytecode.SWAP_STACK, ((ExprContext) ctx.getChild(i+1)).start));
					compile((ExprContext) ctx.getChild(i+1), bytecode);
					bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, ((ExprContext) ctx.getChild(i+1)).start));
					cb.argc = 2;
					bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN, ((ExprContext) ctx.getChild(i+1)).start));
					if (ctx.getChild(i).getText().equals("isnot"))
						putNot(bytecode, ((ExprContext) ctx.getChild(i+1)).start);
					return;
				} else
					throw Utils.throwException("SyntaxError", "unsupported comparison operation");
				putGetAttr(operation, bytecode, ((ExprContext) ctx.getChild(i+1)).start);
				compile((ExprContext) ctx.getChild(i+1), bytecode);
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, ((ExprContext) ctx.getChild(i+1)).start));
				cb.argc = 1;
				bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN, ((ExprContext) ctx.getChild(i+1)).start));
				if (ctx.getChild(i).getText().equals("notin"))
					putNot(bytecode, ((ExprContext) ctx.getChild(i+1)).start);
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
				cb.argc = 1;
				bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN, ctx.xor_expr(i).stop));
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
				cb.argc = 1;
				bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN, ctx.and_expr(i).stop));
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
				cb.argc = 1;
				bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN, ctx.shift_expr(i).stop));
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
				cb.argc = 1;
				bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN, ((Arith_exprContext) ctx.getChild(i+1)).stop));
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
				cb.argc = 1;
				bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN, ((TermContext)ctx.getChild(i+1)).stop));
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
				cb.argc = 1;
				bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN, ((FactorContext)ctx.getChild(i+1)).stop));
			}
		} else 
			compile(ctx.factor(0), bytecode);
	}
	
	private void compile(FactorContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.factor() != null){
			if (ctx.getText().startsWith("~")){
				putGetAttr(Arithmetics.__NOT__, bytecode, ctx.start);
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, ctx.start));
				cb.argc = 0;
				bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN, ctx.start));
				return;
			}
			
			String operation = null;
			if (ctx.getText().startsWith("+"))
				operation = Arithmetics.__ADD__;
			if (ctx.getText().startsWith("-"))
				operation = Arithmetics.__SUB__;
			
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH, ctx.factor().start));
			cb.value = new IntObject(0);
			putGetAttr(operation, bytecode, ctx.factor().start);
			compile(ctx.factor(), bytecode);
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, ctx.factor().stop));
			cb.argc = 1;
			bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN, ctx.factor().stop));
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
			cb.argc = 1;
			bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN, ctx.factor().stop));
		}
	}

	private void compile(TrailerContext tc, List<PythonBytecode> bytecode) {
		if (tc.getText().startsWith("(")){
			if (tc.arglist() == null){
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, tc.stop));
				cb.argc = 0;
			} else {
				int argc = compileArguments(tc.arglist(), bytecode);
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, tc.stop));
				cb.argc = argc;
			}
			bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN, tc.stop));
		} else if (tc.getText().startsWith("[")) {
			compileSubscript(tc.subscriptlist(), bytecode, tc.start);
		} else {			
			putGetAttr(tc.NAME().getText(), bytecode, tc.start);
		}
	}

	private void compileSubscript(ParserRuleContext arglist,
			List<PythonBytecode> bytecode, Token pt) {
		putGetAttr("__getitem__", bytecode, pt);
		if (arglist == null){
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, pt));
			cb.argc = 0;
			bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN, pt));
		} else if (arglist instanceof ArglistContext) {
			int argc = compileArguments((ArglistContext) arglist, bytecode);
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, ((ArglistContext) arglist).stop));
			cb.argc = argc;
			bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN, ((ArglistContext) arglist).stop));
		} else if (arglist instanceof ListmakerContext){
			if (((ListmakerContext) arglist).list_for() != null)
				throw Utils.throwException("SyntaxError", "list comprehension expression not allowed");
			compile(((ListmakerContext) arglist).test(0), bytecode);
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, ((ListmakerContext) arglist).test(0).stop));
			cb.argc = 1;
			bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN, ((ListmakerContext) arglist).test(0).stop));
		} else if (arglist instanceof SubscriptlistContext){
			SubscriptlistContext sc = (SubscriptlistContext) arglist;
			int tlc = sc.subscript().size();
			if (tlc > 1){
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.LOADGLOBAL, sc.start));
				cb.variable = TupleTypeObject.TUPLE_CALL;
			}
			for (SubscriptContext s : sc.subscript()){
				compile(s, bytecode);
			}
			if (tlc > 1){
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, sc.stop));
				cb.argc = tlc;
				bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN, sc.stop));
			}
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, sc.stop));
			cb.argc = 1;
			bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN, sc.stop));
		}
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
			cb.variable = SliceTypeObject.SLICE_CALL;
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
			cb.argc = 3;
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
			cb.variable = name;
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
				
				IntObject o = new IntObject(bi);
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
			compile(ctx.listmaker(), bytecode);
		} else if (ctx.getText().startsWith("{")){
			// TODO
		}
	}

	private void compile(Testlist_compContext ctx,
			List<PythonBytecode> bytecode) {
		if (ctx.comp_for() != null){
			// TODO
		} else {
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.LOADGLOBAL, ctx.start));
			cb.variable = TupleTypeObject.TUPLE_CALL;
			for (TestContext t : ctx.test())
				compile(t, bytecode);
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, ctx.stop));
			cb.argc = ctx.test().size();
			bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN, ctx.stop));
		}
	}

	private void compile(ListmakerContext listmaker,
			List<PythonBytecode> bytecode) {
		if (listmaker.list_for() != null){
			// TODO
		} else {
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.LOADGLOBAL, listmaker.start));
			cb.variable = ListTypeObject.LIST_CALL;
			for (TestContext t : listmaker.test())
				compile(t, bytecode);
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL, listmaker.stop));
			cb.argc = listmaker.test().size();
			bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN, listmaker.stop));
		}
	}

	private void compileTernary(TestContext ctx, List<PythonBytecode> bytecode) {
		// TODO Auto-generated method stub
		
	}

	private void compile(LambdefContext ctx, List<PythonBytecode> bytecode) {
		// TODO Auto-generated method stub
		
	}

	/** Generates bytecode that stores top of stack into whatever is passed as parameter */ 
	private void compileAssignment(TestContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.lambdef() != null)
			throw Utils.throwException("SyntaxError", "can't assign to lambda");
		if (ctx.or_test(1) != null)
			throw Utils.throwException("SyntaxError", "can't assign to operator");
		compileAssignment(ctx.or_test(0), bytecode);
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
					cb.argc = 2;
					bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN, s.stest().stop));
				} else {
					// xyz[a:b] = ...
					throw Utils.throwException("SyntaxError", "assignment to splice not yet implemented");
				}
			} else {
				// ... xyz.something
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH, t.start));
				cb.value = new StringObject(t.NAME().toString());
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.SETATTR, t.start));
				bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN, t.start));
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
		cb.variable = name;
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
		cb.moduleName = packageName;
		cb.variable = as;
	}

}
