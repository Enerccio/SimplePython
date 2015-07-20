package me.enerccio.sp.types.base;

import java.math.BigInteger;

import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.AugumentedPythonObject;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.utils.Utils;

public class ComplexObject extends NumberObject {
	private static final long serialVersionUID = 9L;
	private static final String REAL_ACCESSOR = "real";
	private static final String IMAG_ACCESSOR = "imag";
	
	public ComplexObject(){
		newObject();
	}
	
	public ComplexObject(double r, double i){
		this(new RealObject(r), new RealObject(i));
	}
	
	public ComplexObject(RealObject r, RealObject i) {
		fields.put(REAL_ACCESSOR, new AugumentedPythonObject(r, AccessRestrictions.PUBLIC));
		fields.put(IMAG_ACCESSOR, new AugumentedPythonObject(i, AccessRestrictions.PUBLIC));
		newObject();
	}
	
	@Override 
	public BigInteger getJavaInt() {
		throw Utils.throwException("TypeError", "can't convert complex to int");
	}
	
	@Override
	public double getJavaFloat() {
		throw Utils.throwException("TypeError", "can't convert complex to float");
	}

	@Override
	public boolean truthValue() {
		return Utils.get(this, REAL_ACCESSOR).truthValue();
	}
	
	public double getRealPart(){
		return ((RealObject) Utils.get(this, REAL_ACCESSOR)).doubleValue();
	}
	
	public double getImagPart(){
		return ((RealObject) Utils.get(this, IMAG_ACCESSOR)).doubleValue();
	}

	@Override
	protected PythonObject getIntValue() {
		return Utils.get(this, REAL_ACCESSOR);
	}
	
	@Override
	public int hashCode(){
		return fields.get(REAL_ACCESSOR).object.hashCode() ^ fields.get(IMAG_ACCESSOR).object.hashCode();
	}
	
	@Override
	public boolean equals(Object o){
		if (o instanceof ComplexObject){
			return ((ComplexObject)o).fields.get(REAL_ACCESSOR).object.equals(fields.get(REAL_ACCESSOR).object)
					&& ((ComplexObject)o).fields.get(IMAG_ACCESSOR).object.equals(fields.get(IMAG_ACCESSOR).object);
		}
		return false;
	}

	@Override
	protected String doToString() {
		return "(" + fields.get(REAL_ACCESSOR).object.toString() + "+" + fields.get(IMAG_ACCESSOR).object.toString() + "j)";
	}
}
