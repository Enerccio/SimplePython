package me.enerccio.sp.types.base;

import me.enerccio.sp.types.PythonObject;

public class RealObject extends NumberObject {
	private static final long serialVersionUID = 8L;

	public RealObject(double v){
		value = v;
	}
	
	public RealObject(float v){
		value = v;
	}
	
	private double value;
	
	@Override
	public boolean truthValue() {
		return value != 0;
	}

	@Override
	public IntObject getId(){
		return new IntObject(Double.valueOf(value).hashCode());
	}

	public float floatValue() {
		return (float)doubleValue();
	}
	
	public double doubleValue(){
		return value;
	}

	@Override
	protected PythonObject getIntValue() {
		return new IntObject(new Double(value).longValue());
	}
}
