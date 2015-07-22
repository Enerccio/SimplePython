package me.enerccio.sp.types;

import java.util.List;

import me.enerccio.sp.compiler.PythonBytecode;
import me.enerccio.sp.compiler.PythonCompiler;
import me.enerccio.sp.interpret.ExecutionResult;
import me.enerccio.sp.interpret.PythonExecutionException;
import me.enerccio.sp.interpret.PythonInterpret;
import me.enerccio.sp.parser.pythonParser;
import me.enerccio.sp.parser.pythonParser.File_inputContext;
import me.enerccio.sp.runtime.ModuleProvider;
import me.enerccio.sp.types.mappings.MapObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.utils.Utils;

public class ModuleObject extends PythonObject {
	private static final long serialVersionUID = -2347220852204272570L;
	public static final String __NAME__ = "__name__";
	public static final String __DICT__ = "__dict__";
	public static final String __THISMODULE__ = "__thismodule__";

	public ModuleObject(MapObject globals, ModuleProvider provider) {
		this.provider = provider;

		Utils.putPublic(this, __NAME__, new StringObject(provider.getModuleName()));
		Utils.putPublic(this, __DICT__, globals);
		
		try {
			pythonParser p = Utils.parse(this.provider);
			File_inputContext fcx = p.file_input();
			if (fcx != null){
				frame = new PythonCompiler().doCompile(fcx, globals, this);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw Utils.throwException("SyntaxError", "failed to parse source code of " + provider);
		}
		globals.backingMap.put(new StringObject(__THISMODULE__), this);
		globals.backingMap.put(new StringObject(__NAME__), new StringObject(provider.getModuleName()));
	}
	
	public final ModuleProvider provider;
	private List<PythonBytecode> frame;
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

	public void initModule() {
		doInitModule();
		isInited = true;
	}

	private void doInitModule() {
		int cfc = PythonInterpret.interpret.get().currentFrame.size();
		PythonInterpret.interpret.get().executeBytecode(frame);
		while (true){
			ExecutionResult res = PythonInterpret.interpret.get().executeOnce();
			if (res == ExecutionResult.INTERRUPTED)
				return;
			if (res == ExecutionResult.FINISHED || res == ExecutionResult.EOF)
				if (PythonInterpret.interpret.get().currentFrame.size() == cfc){
					if (PythonInterpret.interpret.get().exception() != null){
						PythonInterpret.interpret.get().currentFrame.peekLast().exception = null;
						throw new PythonExecutionException(PythonInterpret.interpret.get().exception());
					}
					return;
				}
		}
	}
}
