package me.enerccio.sp.compiler;

import java.util.ArrayList;
import java.util.List;

import me.enerccio.sp.parser.pythonParser.Compound_stmtContext;
import me.enerccio.sp.parser.pythonParser.File_inputContext;
import me.enerccio.sp.parser.pythonParser.Simple_stmtContext;
import me.enerccio.sp.parser.pythonParser.StmtContext;
import me.enerccio.sp.types.mappings.MapObject;

public class PythonCompiler {

	private PythonBytecode cb;
	
	public List<PythonBytecode> doCompile(File_inputContext fcx) {
		List<PythonBytecode> bytecode = new ArrayList<PythonBytecode>();
		// create new environment
		bytecode.add(Bytecode.makeBytecode(Bytecode.PUSH_ENVIRONMENT));
		// locals
		bytecode.add(cb = Bytecode.makeBytecode(Bytecode.PUSH_DICT)); 
		cb.dict = new MapObject();
		for (StmtContext sctx : fcx.stmt())
			compileStatement(sctx, bytecode);
		return bytecode;
	}

	private void compileStatement(StmtContext sctx,
			List<PythonBytecode> bytecode) {
		if (sctx.simple_stmt() != null)
			compileSimpleStatement(sctx.simple_stmt(), bytecode);
		else
			compileCompoundStatement(sctx.compound_stmt(), bytecode);
	}

	private void compileCompoundStatement(Compound_stmtContext compound_stmt,
			List<PythonBytecode> bytecode) {
		// TODO Auto-generated method stub
		
	}

	private void compileSimpleStatement(Simple_stmtContext simple_stmt,
			List<PythonBytecode> bytecode) {
		// TODO Auto-generated method stub
		
	}

}
