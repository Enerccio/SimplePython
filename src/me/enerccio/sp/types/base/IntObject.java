package me.enerccio.sp.types.base;

import java.math.BigInteger;

import me.enerccio.sp.types.PythonObject;

public class IntObject extends NumberObject {
	private static final long serialVersionUID = 6L;
	
	public IntObject(){
		newObject();
	}
	
	public IntObject(int v){
		value = BigInteger.valueOf(v);
		newObject();
	}
	
	public IntObject(long v){
		value = BigInteger.valueOf(v);
		newObject();
	}
	
	public IntObject(BigInteger v){
		value = v;
		newObject();
	}
	
	private BigInteger value;

	@Override
	public boolean truthValue() {
		return !value.equals(0);
	}
	
	@Override 
	public BigInteger getJavaInt() {
		return value;
	}
	
	@Override
	public double getJavaFloat() {
		return value.doubleValue();
	}
	
	@Override
	public IntObject getId(){
		return new IntObject(value.hashCode());
	}

	public int intValue() {
		return (int)longValue();
	}

	public long longValue() {
		return value.longValue();
	}

	@Override
	protected PythonObject getIntValue() {
		return this;
	}
	
	@Override
	public int hashCode(){
		return value.hashCode();
	}
	
	@Override
	public boolean equals(Object o){
		if (o instanceof IntObject)
			return value.equals(((IntObject)o).value);
		return false;
	}

	@Override
	protected String doToString() {
		return value.toString();
	}
}
