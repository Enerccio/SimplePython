package me.enerccio.sp.compiler;

import java.util.ArrayList;
import java.util.List;

import me.enerccio.sp.parser.pythonParser.Compound_stmtContext;
import me.enerccio.sp.parser.pythonParser.Dotted_as_nameContext;
import me.enerccio.sp.parser.pythonParser.Dotted_as_namesContext;
import me.enerccio.sp.parser.pythonParser.Dotted_nameContext;
import me.enerccio.sp.parser.pythonParser.Expr_stmtContext;
import me.enerccio.sp.parser.pythonParser.File_inputContext;
import me.enerccio.sp.parser.pythonParser.Import_as_nameContext;
import me.enerccio.sp.parser.pythonParser.Import_fromContext;
import me.enerccio.sp.parser.pythonParser.Import_nameContext;
import me.enerccio.sp.parser.pythonParser.Import_stmtContext;
import me.enerccio.sp.parser.pythonParser.NnameContext;
import me.enerccio.sp.parser.pythonParser.Simple_stmtContext;
import me.enerccio.sp.parser.pythonParser.Small_stmtContext;
import me.enerccio.sp.parser.pythonParser.StmtContext;
import me.enerccio.sp.types.mappings.MapObject;

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

	private void compile(Expr_stmtContext expr,
			List<PythonBytecode> bytecode) {
		
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
