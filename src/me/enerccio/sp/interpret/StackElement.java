package me.enerccio.sp.interpret;

import java.util.Map;
import java.util.Set;

import me.enerccio.sp.serialization.PySerializer;
import me.enerccio.sp.types.ModuleObject.ModuleData;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.Tags;
import me.enerccio.sp.types.callables.JavaMethodObject;

/** Container for stack data */
public class StackElement extends PythonObject {
	private static final long serialVersionUID = -3288411350030175582L;
	public static final StackElement SYSTEM_FRAME = new StackElement();
	public static final StackElement LAST_FRAME = new StackElement();

	@Override
	public byte getTag() {
		return Tags.STACK_EL;
	}

	public final ModuleData module;
	public final int line;
	public final int character;
	public final String function;

	@Override
	public Set<String> getGenHandleNames() {
		return PythonObject.sfields.keySet();
	}

	@Override
	protected Map<String, JavaMethodObject> getGenHandles() {
		return PythonObject.sfields;
	}

	private StackElement() {
		super(false);
		line = character = -1;
		module = null;
		function = "<module>";
	}

	public StackElement(ModuleData module, String function, int line,
			int character) {
		super(false);
		this.module = module;
		this.line = line;
		this.character = character;
		this.function = function;
	}

	@Override
	public boolean truthValue() {
		return true;
	}

	@Override
	protected String doToString() {
		return "<" + module.getName() + " at line " + line + " " + character
				+ ">";
	}

	@Override
	protected void serializeDirectState(PySerializer pySerializer) {
		pySerializer.serializeJava(module);
		pySerializer.serialize(line);
		pySerializer.serialize(character);
		pySerializer.serialize(function);
	}
}