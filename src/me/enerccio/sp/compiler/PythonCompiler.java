package me.enerccio.sp.compiler;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.antlr.v4.runtime.ParserRuleContext;

import me.enerccio.sp.parser.pythonParser.ClassdefContext;
import me.enerccio.sp.parser.pythonParser.Flow_stmtContext;
import me.enerccio.sp.parser.pythonParser.Return_stmtContext;
import me.enerccio.sp.parser.pythonParser.ArgumentContext;
import me.enerccio.sp.parser.pythonParser.Parenthesesless_callContext;
import me.enerccio.sp.parser.pythonParser.*;
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.types.Arithmetics;
import me.enerccio.sp.types.ModuleObject;
import me.enerccio.sp.types.base.ComplexObject;
import me.enerccio.sp.types.base.EllipsisObject;
import me.enerccio.sp.types.base.IntObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.base.RealObject;
import me.enerccio.sp.types.callables.UserFunctionObject;
import me.enerccio.sp.types.mappings.MapObject;
import me.enerccio.sp.types.sequences.StringObject;
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
	
	public List<PythonBytecode> doCompile(File_inputContext fcx, MapObject dict, ModuleObject m) {
		stack.push();
		compilingClass.push(null);
		List<PythonBytecode> bytecode = new ArrayList<PythonBytecode>();
		// create new environment
		bytecode.add(Bytecode.makeBytecode(Bytecode.PUSH_ENVIRONMENT));
		// context
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH));
		cb.value = m;
		bytecode.add(Bytecode.makeBytecode(Bytecode.PUSH_LOCAL_CONTEXT));
		// locals
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH_DICT)); 
		cb.dict = dict;
		environments.add(dict);
		
		for (StmtContext sctx : fcx.stmt())
			compileStatement(sctx, bytecode);
		
		bytecode.add(Bytecode.makeBytecode(Bytecode.POP_ENVIRONMENT));
		compilingClass.pop();
		stack.pop();
		environments.removeLast();
		return bytecode;
	}

	private void compileStatement(StmtContext sctx,
			List<PythonBytecode> bytecode) {
		if (sctx.simple_stmt() != null)
			compileSimpleStatement(sctx.simple_stmt(), bytecode);
		else if (sctx.parenthesesless_call() != null)
			compileParentheseslessCall(sctx.parenthesesless_call(), bytecode);
		else
			compileCompoundStatement(sctx.compound_stmt(), bytecode);
	}

	private void compileCompoundStatement(Compound_stmtContext cstmt,
			List<PythonBytecode> bytecode) {
		if (cstmt.funcdef() != null){
			compileFunction(cstmt.funcdef(), bytecode);
		}
		if (cstmt.classdef() != null)
			compileClass(cstmt.classdef(), bytecode);
		// TODO
	}

	private void compileClass(ClassdefContext classdef,
			List<PythonBytecode> bytecode) {
		String className = classdef.nname().getText();
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.LOADGLOBAL));
		cb.variable = "type";
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH));
		cb.value = new StringObject(className);
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.LOADGLOBAL));
		cb.variable = "tuple";
		int c = classdef.testlist() != null ? classdef.testlist().test().size() : 0;
		if (classdef.testlist() != null)
			for (TestContext tc : classdef.testlist().test())
				compile(tc, bytecode);
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL));
		cb.argc = c;
		bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN));
		UserFunctionObject fnc = new UserFunctionObject();
		fnc.newObject();
		Utils.putPublic(fnc, "__name__", new StringObject("$$__classBodyFncFor" + className + "$$"));
		fnc.args = new ArrayList<String>();
		
		compilingClass.push(className);
		MapObject cdc = doCompileFunction(classdef.suite(), fnc.bytecode);
		compilingClass.pop();
		
		fnc.bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH));
		cb.value = cdc;
		fnc.bytecode.add(Bytecode.makeBytecode(Bytecode.RETURN));
		
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH));
		cb.value = fnc;
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL));
		cb.argc = 0;
		bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN));
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL));
		cb.argc = 3;
		bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN));
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.SAVE));
		cb.variable = className;
	}

	private void compileFunction(FuncdefContext funcdef,
			List<PythonBytecode> bytecode) {
		UserFunctionObject fnc = new UserFunctionObject();
		fnc.newObject();
		
		if (funcdef.docstring() != null)
			Utils.putPublic(fnc, "__doc__", new StringObject(doGetLongString(funcdef.docstring().LONG_STRING().getText())));
		String functionName = funcdef.nname(0).getText();
		Utils.putPublic(fnc, "__name__", new StringObject(compilingClass.peek() == null ? functionName : compilingClass.peek() + "." + functionName));
		
		List<String> arguments = new ArrayList<String>();
		for (int i=1; i<funcdef.nname().size(); i++){
			arguments.add(funcdef.nname(i).getText());
		}
		
		fnc.args = arguments;
		if (funcdef.vararg() != null){
			fnc.isVararg = true;
			fnc.vararg = funcdef.vararg().nname().getText();
		}
		
		compilingClass.push(null);
		doCompileFunction(funcdef.suite(), fnc.bytecode);
		compilingClass.pop();
		
		fnc.bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH));
		cb.value = NoneObject.NONE;
		fnc.bytecode.add(Bytecode.makeBytecode(Bytecode.RETURN));
		
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH));
		cb.value = fnc;
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.SAVE));
		cb.variable = functionName;
		
	}

	private String doGetLongString(String text) {
		return text.substring(3, text.length()-4);
	}

	private MapObject doCompileFunction(SuiteContext suite,
			List<PythonBytecode> bytecode) {
		
		stack.push();
		bytecode.add(Bytecode.makeBytecode(Bytecode.PUSH_ENVIRONMENT));
		MapObject dict = new MapObject();
		environments.add(dict);
		for (MapObject d : Utils.reverse(environments)){
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH_DICT)); 
			cb.dict = d;
		}
		bytecode.add(Bytecode.makeBytecode(Bytecode.RESOLVE_ARGS));
		
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

	private void compileParentheseslessCall(Parenthesesless_callContext ctx, List<PythonBytecode> bytecode) {
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.LOAD));
		cb.variable = ctx.nname().getText();
		if (ctx.arglist() == null) {
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL));
			cb.argc = 0;
		} else {
			int argc = compileArguments(ctx.arglist(), bytecode);
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL));
			cb.argc = argc;
		}
		if (ctx.testlist() != null) {
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN));
			compileAssignment(ctx.testlist(), bytecode);
		}
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
					addImport(packageName, "*", bytecode);
					return;
				}
				if (imf.import_as_names() != null)
					for (Import_as_nameContext asname : imf.import_as_names().import_as_name()){
						compileImport2(asname, bytecode, packageName);
					}
			}
		}
		
		if (smstmt.pass_stmt() != null){
			bytecode.add(Bytecode.makeBytecode(Bytecode.NOP));
		}
		
		if (smstmt.expr_stmt() != null){
			compile(smstmt.expr_stmt(), bytecode);
			bytecode.add(Bytecode.makeBytecode(Bytecode.POP));
		}
			
		if (smstmt.print_stmt() != null){
			compile(smstmt.print_stmt(), bytecode);
		}
		
		if (smstmt.flow_stmt() != null){
			compile(smstmt.flow_stmt(), bytecode);
		}
	}

	private void compile(Flow_stmtContext ctx,
			List<PythonBytecode> bytecode) {
		if (ctx.return_stmt() != null){
			compile(ctx.return_stmt(), bytecode);
		}
	}

	private void compile(Return_stmtContext ctx,
			List<PythonBytecode> bytecode) {
		if (compilingClass.peek() != null)
			throw Utils.throwException("SyntaxError", "return cannot be inside class definition");
		if (ctx.testlist() != null)
			compileRightHand(ctx.testlist(), bytecode);
		bytecode.add(Bytecode.makeBytecode(Bytecode.RETURN));
	}

	private void compile(Print_stmtContext ctx,
			List<PythonBytecode> bytecode) {
		if (ctx.push() != null){
			// TODO
		} else {
			boolean eol = (ctx.endp() == null);
			for (TestContext tc : ctx.test()){
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.LOADGLOBAL));
				cb.variable = PythonRuntime.PRINT_JAVA;
				compile(tc, bytecode);
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL));
				cb.argc = 1;
			}
			
			if (eol){
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.LOADGLOBAL));
				cb.variable = PythonRuntime.PRINT_JAVA_EOL;
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL));
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
				putGetAttr(Arithmetics.__ADD__, bytecode);
			else if (expr.augassignexp().augassign().getText().equals("-="))
				putGetAttr(Arithmetics.__SUB__, bytecode);
			else if (expr.augassignexp().augassign().getText().equals("*="))
				putGetAttr(Arithmetics.__MUL__, bytecode);
			else if (expr.augassignexp().augassign().getText().equals("/="))
				putGetAttr(Arithmetics.__DIV__, bytecode);
			else if (expr.augassignexp().augassign().getText().equals("%="))
				putGetAttr(Arithmetics.__MOD__, bytecode);
			else
				throw Utils.throwException("SyntaxError", "illegal augmented assignment");
			compileRightHand(expr.augassignexp().testlist(), bytecode);
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL));
			cb.argc = 1;
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN));
			compileAssignment(leftHand.test(0), bytecode);			
		} else {
			TestlistContext rightHand = expr.testlist(expr.testlist().size()-1);
			List<TestlistContext> rest = new ArrayList<TestlistContext>();
			for (int i=0; i<expr.testlist().size()-1; i++)
				rest.add(expr.testlist(i));
			
			Collections.reverse(rest);
			compileRightHand(rightHand, bytecode);
			
			for (TestlistContext tc : rest){
				bytecode.add(Bytecode.makeBytecode(Bytecode.DUP));
				compileAssignment(tc, bytecode);
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
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.LOADGLOBAL));
			cb.variable = TupleTypeObject.TUPLE_CALL;
		}
		
		for (TestContext tc : testlist.test())
			compile(tc, bytecode);
		
		if (tlc > 1){
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL));
			cb.argc = tlc;
			bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN));
		}
	}
	
	private void putGetAttr(String attr, List<PythonBytecode> bytecode) {
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.GETATTR));
		cb.variable = attr;
	}
	
	private void putNot(List<PythonBytecode> bytecode) {
		putGetAttr("__not__", bytecode);
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL));
		cb.argc = 0;
		bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN));
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
					bytecode.add(cb = Bytecode.makeBytecode(Bytecode.LOADGLOBAL));
					cb.variable = ObjectTypeObject.IS;
					bytecode.add(cb = Bytecode.makeBytecode(Bytecode.SWAP_STACK));
					compile((ExprContext) ctx.getChild(i+1), bytecode);
					bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL));
					cb.argc = 2;
					bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN));
					if (ctx.getChild(i).getText().equals("isnot"))
						putNot(bytecode);
					return;
				} else
					throw Utils.throwException("SyntaxError", "unsupported comparison operation");
				putGetAttr(operation, bytecode);
				compile((ExprContext) ctx.getChild(i+1), bytecode);
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL));
				cb.argc = 1;
				bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN));
				if (ctx.getChild(i).getText().equals("notin"))
					putNot(bytecode);
			}
		} else 
			compile(ctx.expr(0), bytecode);
	}
	
	private void compile(ExprContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.getChildCount() > 1){
			compile(ctx.xor_expr(0), bytecode);
			String operation = Arithmetics.__OR__;
			for (int i=1; i<ctx.xor_expr().size(); i++){
				putGetAttr(operation, bytecode);
				compile(ctx.xor_expr(i), bytecode);
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL));
				cb.argc = 1;
				bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN));
			}
		} else 
			compile(ctx.xor_expr(0), bytecode);
	}
	
	private void compile(Xor_exprContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.getChildCount() > 1){
			compile(ctx.and_expr(0), bytecode);
			String operation = Arithmetics.__XOR__;
			for (int i=1; i<ctx.and_expr().size(); i++){
				putGetAttr(operation, bytecode);
				compile(ctx.and_expr(i), bytecode);
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL));
				cb.argc = 1;
				bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN));
			}
		} else 
			compile(ctx.and_expr(0), bytecode);
	}


	private void compile(And_exprContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.getChildCount() > 1){
			compile(ctx.shift_expr(0), bytecode);
			String operation = Arithmetics.__AND__;
			for (int i=1; i<ctx.shift_expr().size(); i++){
				putGetAttr(operation, bytecode);
				compile(ctx.shift_expr(i), bytecode);
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL));
				cb.argc = 1;
				bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN));
			}
		} else 
			compile(ctx.shift_expr(0), bytecode);
	}
	
	private void compile(Shift_exprContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.getChildCount() > 1){
			compile(ctx.arith_expr(0), bytecode);
			for (int i=1; i<ctx.getChildCount(); i+=2){
				String operation = ctx.getChild(i).getText().equals("<<") ? Arithmetics.__LSHIFT__ : Arithmetics.__RSHIFT__;
				putGetAttr(operation, bytecode);
				compile((Arith_exprContext) ctx.getChild(i+1), bytecode);
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL));
				cb.argc = 1;
				bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN));
			}
		} else 
			compile(ctx.arith_expr(0), bytecode);
	}
	
	private void compile(Arith_exprContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.getChildCount() > 1){
			compile(ctx.term(0), bytecode);
			for (int i=1; i<ctx.getChildCount(); i+=2){
				String operation = ctx.getChild(i).getText().equals("+") ? Arithmetics.__ADD__ : Arithmetics.__SUB__;
				putGetAttr(operation, bytecode);
				compile((TermContext)ctx.getChild(i+1), bytecode);
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL));
				cb.argc = 1;
				bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN));
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
				putGetAttr(operation, bytecode);
				compile((FactorContext)ctx.getChild(i+1), bytecode);
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL));
				cb.argc = 1;
				bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN));
			}
		} else 
			compile(ctx.factor(0), bytecode);
	}
	
	private void compile(FactorContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.factor() != null){
			if (ctx.getText().startsWith("~")){
				putGetAttr(Arithmetics.__NOT__, bytecode);
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL));
				cb.argc = 0;
				bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN));
				return;
			}
			
			String operation = null;
			if (ctx.getText().startsWith("+"))
				operation = Arithmetics.__ADD__;
			if (ctx.getText().startsWith("-"))
				operation = Arithmetics.__SUB__;
			
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH));
			cb.value = new IntObject(0);
			putGetAttr(operation, bytecode);
			compile(ctx.factor(), bytecode);
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL));
			cb.argc = 1;
			bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN));
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
			putGetAttr(operation, bytecode);
			compile(ctx.factor(), bytecode);
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL));
			cb.argc = 1;
			bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN));
		}
	}

	private void compile(TrailerContext tc, List<PythonBytecode> bytecode) {
		if (tc.getText().startsWith("(")){
			if (tc.arglist() == null){
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL));
				cb.argc = 0;
			} else {
				int argc = compileArguments(tc.arglist(), bytecode);
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL));
				cb.argc = argc;
			}
			bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN));
		} else if (tc.getText().startsWith("[")) {
			compileSubscript(tc.subscriptlist(), bytecode);
		} else {			
			putGetAttr(tc.NAME().getText(), bytecode);
		}
	}

	private void compileSubscript(ParserRuleContext arglist,
			List<PythonBytecode> bytecode) {
		putGetAttr("__getitem__", bytecode);
		if (arglist == null){
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL));
			cb.argc = 0;
			bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN));
		} else if (arglist instanceof ArglistContext) {
			int argc = compileArguments((ArglistContext) arglist, bytecode);
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL));
			cb.argc = argc;
			bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN));
		} else if (arglist instanceof ListmakerContext){
			if (((ListmakerContext) arglist).list_for() != null)
				throw Utils.throwException("SyntaxError", "list comprehension expression not allowed");
			compile(((ListmakerContext) arglist).test(0), bytecode);
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL));
			cb.argc = 1;
			bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN));
		} else if (arglist instanceof SubscriptlistContext){
			SubscriptlistContext sc = (SubscriptlistContext) arglist;
			int tlc = sc.subscript().size();
			if (tlc > 1){
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.LOADGLOBAL));
				cb.variable = TupleTypeObject.TUPLE_CALL;
			}
			for (SubscriptContext s : sc.subscript()){
				compile(s, bytecode);
			}
			if (tlc > 1){
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL));
				cb.argc = tlc;
				bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN));
			}
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL));
			cb.argc = 1;
			bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN));
		}
	}

	private void compile(SubscriptContext s, List<PythonBytecode> bytecode) {
		if (s.ellipsis() != null){
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH));
			cb.value = EllipsisObject.ELLIPSIS;
			return;
		}
		
		if (s.stest() != null){
			compile(s.stest().test(), bytecode);
		} else {
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.LOADGLOBAL));
			cb.variable = SliceTypeObject.SLICE_CALL;
			if (s.test().size() == 0){
				for (int i=0; i<3; i++){
					bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH));
					cb.value = NoneObject.NONE;
				}
			} else if (s.test().size() == 2){
				if (s.sliceop() == null){
					compile(s.test(0), bytecode);
					compile(s.test(1), bytecode);
					bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH));
					cb.value = NoneObject.NONE;
				} else {
					compile(s.test(0), bytecode);
					compile(s.test(1), bytecode);
					compile(s.sliceop().test(), bytecode);
				}
			} else if (s.test().size() == 1){
				if (s.getText().startsWith(":")){
					bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH));
					cb.value = NoneObject.NONE;
					compile(s.test(0), bytecode);
					bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH));
					cb.value = NoneObject.NONE;
				} else {
					compile(s.test(0), bytecode);
					bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH));
					cb.value = NoneObject.NONE;
					bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH));
					cb.value = NoneObject.NONE;
				}
			}
			
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL));
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
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.LOADGLOBAL));
			else
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.LOAD));
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
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH));
				cb.value = o;
			} else if (nb.FLOAT_NUMBER() != null){
				String numberValue = nb.FLOAT_NUMBER().getText();
				RealObject r = new RealObject(Double.parseDouble(numberValue));
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH));
				cb.value = r;
			} else {
				String n = nb.IMAG_NUMBER().getText();
				ComplexObject c = new ComplexObject(0, Double.parseDouble(n.substring(0, n.length()-1)));
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH));
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
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH));
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
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.LOADGLOBAL));
			cb.variable = TupleTypeObject.TUPLE_CALL;
			for (TestContext t : ctx.test())
				compile(t, bytecode);
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL));
			cb.argc = ctx.test().size();
			bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN));
		}
	}

	private void compile(ListmakerContext listmaker,
			List<PythonBytecode> bytecode) {
		if (listmaker.list_for() != null){
			// TODO
		} else {
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.LOADGLOBAL));
			cb.variable = ListTypeObject.LIST_CALL;
			for (TestContext t : listmaker.test())
				compile(t, bytecode);
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL));
			cb.argc = listmaker.test().size();
			bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN));
		}
	}

	private void compileTernary(TestContext ctx, List<PythonBytecode> bytecode) {
		// TODO Auto-generated method stub
		
	}

	private void compile(LambdefContext ctx, List<PythonBytecode> bytecode) {
		// TODO Auto-generated method stub
		
	}

	/** Generates bytecode that stores top of stack into whatever is passed as parameter */
	private void compileAssignment(TestlistContext tc, List<PythonBytecode> bytecode) {
		if (tc.test().size() > 1) {
			// x,y,z = ...
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.UNPACK_SEQUENCE));
			cb.argc = tc.test().size();
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
					putGetAttr("__setitem__", bytecode);
					bytecode.add(Bytecode.makeBytecode(Bytecode.SWAP_STACK));
					compile(s.stest().test(), bytecode);
					bytecode.add(Bytecode.makeBytecode(Bytecode.SWAP_STACK));
					bytecode.add(cb = Bytecode.makeBytecode(Bytecode.CALL));
					cb.argc = 2;
					bytecode.add(Bytecode.makeBytecode(Bytecode.ACCEPT_RETURN));
				} else {
					// xyz[a:b] = ...
					throw Utils.throwException("SyntaxError", "assignment to splice not yet implemented");
				}
			} else {
				// ... xyz.something
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH));
				cb.value = new StringObject(t.NAME().toString());
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.SETATTR));
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
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.SAVEGLOBAL));
		else
			bytecode.add(cb = Bytecode.makeBytecode(Bytecode.SAVE));
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
		
		addImport(packageNameBase + "." + asname.nname().get(0).getText(), as, bytecode);
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
		
		addImport(packageName, as, bytecode);
	}

	private void addImport(String packageName, String as,
			List<PythonBytecode> bytecode) {
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.IMPORT));
		cb.moduleName = packageName;
		cb.variable = as;
	}

}
