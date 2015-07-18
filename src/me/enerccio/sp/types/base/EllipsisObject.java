package me.enerccio.sp.types.base;

import me.enerccio.sp.types.PythonObject;

public class EllipsisObject extends PythonObject {
	private static final long serialVersionUID = 70L;
	
	public static final EllipsisObject ELLIPSIS = new EllipsisObject();

	private EllipsisObject(){
		
	}
	
	@Override
	public boolean truthValue() {
		return true;
	}
	
	@Override
	protected String doToString() {
		return "...";
	}

}
