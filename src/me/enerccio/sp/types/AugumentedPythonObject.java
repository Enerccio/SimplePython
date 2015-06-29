package me.enerccio.sp.types;

public class AugumentedPythonObject {
	
	public AugumentedPythonObject(PythonObject object,
			AccessRestrictions restrictions) {
		this.object = object;
		this.restrictions = restrictions;
	}
	
	public volatile PythonObject object;
	public final AccessRestrictions restrictions;
	
	@Override
	public String toString() {
		return "AugumentedPythonObject [object=" + object + ", restrictions="
				+ restrictions + "]";
	}
}
