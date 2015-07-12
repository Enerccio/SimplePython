package me.enerccio.sp.compiler;

import java.util.ArrayList;
import java.util.List;

import me.enerccio.sp.parser.pythonParser.And_exprContext;
import me.enerccio.sp.parser.pythonParser.And_testContext;
import me.enerccio.sp.parser.pythonParser.Arith_exprContext;
import me.enerccio.sp.parser.pythonParser.AtomContext;
import me.enerccio.sp.parser.pythonParser.ComparisonContext;
import me.enerccio.sp.parser.pythonParser.Compound_stmtContext;
import me.enerccio.sp.parser.pythonParser.Dotted_as_nameContext;
import me.enerccio.sp.parser.pythonParser.Dotted_as_namesContext;
import me.enerccio.sp.parser.pythonParser.Dotted_nameContext;
import me.enerccio.sp.parser.pythonParser.ExprContext;
import me.enerccio.sp.parser.pythonParser.Expr_stmtContext;
import me.enerccio.sp.parser.pythonParser.FactorContext;
import me.enerccio.sp.parser.pythonParser.File_inputContext;
import me.enerccio.sp.parser.pythonParser.Import_as_nameContext;
import me.enerccio.sp.parser.pythonParser.Import_fromContext;
import me.enerccio.sp.parser.pythonParser.Import_nameContext;
import me.enerccio.sp.parser.pythonParser.Import_stmtContext;
import me.enerccio.sp.parser.pythonParser.NnameContext;
import me.enerccio.sp.parser.pythonParser.Not_testContext;
import me.enerccio.sp.parser.pythonParser.Or_testContext;
import me.enerccio.sp.parser.pythonParser.PowerContext;
import me.enerccio.sp.parser.pythonParser.Shift_exprContext;
import me.enerccio.sp.parser.pythonParser.Simple_stmtContext;
import me.enerccio.sp.parser.pythonParser.Small_stmtContext;
import me.enerccio.sp.parser.pythonParser.StmtContext;
import me.enerccio.sp.parser.pythonParser.TermContext;
import me.enerccio.sp.parser.pythonParser.TestContext;
import me.enerccio.sp.parser.pythonParser.TestlistContext;
import me.enerccio.sp.parser.pythonParser.Xor_exprContext;
import me.enerccio.sp.types.mappings.MapObject;
import me.enerccio.sp.utils.Utils;

public class PythonCompiler {

	private PythonBytecode cb;
	private VariableStack stack = new VariableStack();
	
	public List<PythonBytecode> doCompile(File_inputContext fcx, MapObject dict) {
		stack.push();
		List<PythonBytecode> bytecode = new ArrayList<PythonBytecode>();
		// create new environment
		bytecode.add(Bytecode.makeBytecode(Bytecode.PUSH_ENVIRONMENT));
		// locals
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH_DICT)); 
		cb.dict = dict;
		for (StmtContext sctx : fcx.stmt())
			compileStatement(sctx, bytecode);
		
		bytecode.add(Bytecode.makeBytecode(Bytecode.POP_ENVIRONMENT));
		stack.pop();
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
		// TODO Auto-generated method stub
		
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
			
	}

	private void compile(Expr_stmtContext expr, List<PythonBytecode> bytecode) {
		TestlistContext leftHand = expr.testlist(0);
		TestlistContext rightHand = expr.testlist(1);
		if (expr.augassignexp() != null) {
			// AugAssign
			if (rightHand.test().size() > 1)
				throw Utils.throwException("SyntaxError", "illegal expression for augmented assignment");
			compileAugAssign(leftHand, rightHand);
		} else {
			compile(rightHand, bytecode);
			if (leftHand.test().size() > 1) {
				// x,y,z = ...
				bytecode.add(cb = Bytecode.makeBytecode(Bytecode.UNPACK_SEQUENCE));
				cb.argc = leftHand.test().size();
				for (int i=0; leftHand.test(i) != null; i++)
					compileAssignment(leftHand.test(i), bytecode);
			} else {
				// x = y
				compileAssignment(leftHand.test(0), bytecode);
			}
		}
	}

	private void compileAugAssign(TestlistContext leftHand, TestlistContext rightHand) {
		// TODO Auto-generated method stub
		
	}

	private void compile(TestlistContext rightHand, List<PythonBytecode> bytecode) {
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
		if (ctx.trailer().size() > 0)
			throw Utils.throwException("SyntaxError", "can't assign to function call");
		if (ctx.factor() != null)
			throw Utils.throwException("SyntaxError", "can't assign to operator");
		compileAssignment(ctx.atom(), bytecode);
	}

	private void compileAssignment(AtomContext ctx, List<PythonBytecode> bytecode) {
		if (ctx.testlist1() != null)
			throw Utils.throwException("SyntaxError", "can't assign to repr");
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
