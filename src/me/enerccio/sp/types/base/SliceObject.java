package me.enerccio.sp.types.base;

import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.AugumentedPythonObject;
import me.enerccio.sp.types.PythonObject;

public class SliceObject extends PythonObject {
	private static final long serialVersionUID = 5763751093225639862L;
	public static final String START_ACCESSOR = "start";
	public static final String STOP_ACCESSOR = "stop";
	public static final String STEP_ACCESSOR = "step";
	
	public SliceObject(PythonObject start, PythonObject end, PythonObject step){
		newObject();
		fields.put(START_ACCESSOR, new AugumentedPythonObject(start, AccessRestrictions.PUBLIC));
		fields.put(STOP_ACCESSOR, new AugumentedPythonObject(end, AccessRestrictions.PUBLIC));
		fields.put(STEP_ACCESSOR, new AugumentedPythonObject(step, AccessRestrictions.PUBLIC));
	}

	@Override
	public boolean truthValue() {
		return true;
	}

	@Override
	protected String doToString() {
		return "<slice at 0x" + Integer.toHexString(hashCode()) + ">";
	}

}
