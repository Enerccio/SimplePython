package me.enerccio.sp.types.base;

import java.math.BigInteger;

import me.enerccio.sp.types.PythonObject;

public class RealObject extends NumberObject {
	private static final long serialVersionUID = 8L;
	
	public RealObject(){
		newObject();
	}

	public RealObject(double v){
		value = v;
		newObject();
	}
	
	public RealObject(float v){
		value = v;
		newObject();
	}
	
	private double value;
	
	@Override 
	public BigInteger getJavaInt() {
		return BigInteger.valueOf((long)value);
	}
	
	@Override
	public double getJavaFloat() {
		return value;
	}

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
	
	@Override
	public int hashCode(){
		return new Double(value).hashCode();
	}
	
	@Override
	public boolean equals(Object o){
		if (o instanceof RealObject)
			return value == ((RealObject)o).value;
		return false;
	}

	@Override
	protected String doToString() {
		return new Double(value).toString();
	}
}
