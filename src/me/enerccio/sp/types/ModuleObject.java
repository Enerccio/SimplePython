package me.enerccio.sp.types;

import java.util.ArrayList;
import java.util.List;

import me.enerccio.sp.interpret.PythonInterpret;
import me.enerccio.sp.parser.pythonParser;
import me.enerccio.sp.parser.pythonParser.StmtContext;
import me.enerccio.sp.runtime.ModuleProvider;
import me.enerccio.sp.types.mappings.MapObject;
import me.enerccio.sp.utils.Utils;

public class ModuleObject extends PythonObject {
	private static final long serialVersionUID = -2347220852204272570L;
	private static final String __NAME__ = "__name__";
	private static final String __DICT__ = "__dict__";

	public ModuleObject(MapObject globals, ModuleProvider provider) {
		this.provider = provider;
		Utils.putPublic(this, __DICT__, globals);
		
		try {
			pythonParser p = Utils.parse(this.provider);
			if (p.file_input() != null){
				for (StmtContext ctx : p.file_input().stmt())
					statements.add(ctx);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw Utils.throwException("ParseException", "Failed to parse source code of " + provider);
		}
	}
	
	private ModuleProvider provider;
	private List<StmtContext> statements = new ArrayList<StmtContext>();
	public volatile boolean isInited = false;

	@Override
	public boolean truthValue() {
		return true;
	}
	
	@Override
	public PythonObject set(String key, PythonObject localContext,
			PythonObject value) {
		if (key.equals(__NAME__) || key.equals(__DICT__))
			throw Utils.throwException("AttributeError", "'" + 
					Utils.run("str", Utils.run("type", this)) + "' object attribute '" + key + "' is read only");
		return super.set(key, localContext, value);
	}

	@Override
	protected String doToString() {
		return "<Module " + get(__NAME__, this).toString() + " at 0x" + Integer.toHexString(hashCode()) + ">";
	}

	public String getFullPath() {
		StringBuilder bd = new StringBuilder();
		for (String pathComponent : provider.getPackages())
			bd.append(pathComponent + ".");
		return bd.append(provider.getModuleName()).toString();
	}

	public void initModule() {
		doInitModule();
		isInited = true;
	}

	private void doInitModule() {
		PythonInterpret i = PythonInterpret.interpret.get();
		i.pushEnvironment((MapObject)get(__DICT__, this));
		i.currentContext.push(this);
		
		i.runAst(statements);
		
		i.currentContext.pop();
		i.currentEnvironment.pop();
	}
}
