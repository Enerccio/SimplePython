package me.enerccio.sp.types.base;

import java.math.BigInteger;

import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.Arithmetics;
import me.enerccio.sp.types.AugumentedPythonObject;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.callables.JavaMethodObject;

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
	
	@Override
	public void newObject(){
		super.newObject();
		try {
			fields.put(Arithmetics.__ADD__, new AugumentedPythonObject(
					new JavaMethodObject(this, this.getClass().getMethod("add", 
							new Class<?>[]{PythonObject.class}), false), AccessRestrictions.PUBLIC));
			fields.put(Arithmetics.__SUB__, new AugumentedPythonObject(
					new JavaMethodObject(this, this.getClass().getMethod("sub", 
							new Class<?>[]{PythonObject.class}), false), AccessRestrictions.PUBLIC));
			fields.put(Arithmetics.__MUL__, new AugumentedPythonObject(
					new JavaMethodObject(this, this.getClass().getMethod("mul", 
							new Class<?>[]{PythonObject.class}), false), AccessRestrictions.PUBLIC));
			fields.put(Arithmetics.__DIV__, new AugumentedPythonObject(
					new JavaMethodObject(this, this.getClass().getMethod("div", 
							new Class<?>[]{PythonObject.class}), false), AccessRestrictions.PUBLIC));
			fields.put(Arithmetics.__MOD__, new AugumentedPythonObject(
					new JavaMethodObject(this, this.getClass().getMethod("mod", 
							new Class<?>[]{PythonObject.class}), false), AccessRestrictions.PUBLIC));
			fields.put(Arithmetics.__AND__, new AugumentedPythonObject(
					new JavaMethodObject(this, this.getClass().getMethod("and", 
							new Class<?>[]{PythonObject.class}), false), AccessRestrictions.PUBLIC));
			fields.put(Arithmetics.__OR__, new AugumentedPythonObject(
					new JavaMethodObject(this, this.getClass().getMethod("or", 
							new Class<?>[]{PythonObject.class}), false), AccessRestrictions.PUBLIC));
			fields.put(Arithmetics.__NOT__, new AugumentedPythonObject(
					new JavaMethodObject(this, this.getClass().getMethod("not", 
							new Class<?>[]{}), false), AccessRestrictions.PUBLIC));
			fields.put(Arithmetics.__XOR__, new AugumentedPythonObject(
					new JavaMethodObject(this, this.getClass().getMethod("xor", 
							new Class<?>[]{PythonObject.class}), false), AccessRestrictions.PUBLIC));
			fields.put(Arithmetics.__POW__, new AugumentedPythonObject(
					new JavaMethodObject(this, this.getClass().getMethod("pow", 
							new Class<?>[]{PythonObject.class}), false), AccessRestrictions.PUBLIC));
			fields.put(Arithmetics.__RSHIFT__, new AugumentedPythonObject(
					new JavaMethodObject(this, this.getClass().getMethod("rs", 
							new Class<?>[]{PythonObject.class}), false), AccessRestrictions.PUBLIC));
			fields.put(Arithmetics.__LSHIFT__, new AugumentedPythonObject(
					new JavaMethodObject(this, this.getClass().getMethod("ls", 
							new Class<?>[]{PythonObject.class}), false), AccessRestrictions.PUBLIC));
		} catch (Exception e) {
			
		}
	}
	
	public PythonObject add(PythonObject arg){
		return Arithmetics.doOperator(this, arg, Arithmetics.__ADD__);
	}
	
	public PythonObject sub(PythonObject arg){
		return Arithmetics.doOperator(this, arg, Arithmetics.__SUB__);
	}
	
	public PythonObject mul(PythonObject arg){
		return Arithmetics.doOperator(this, arg, Arithmetics.__MUL__);
	}
	
	public PythonObject div(PythonObject arg){
		return Arithmetics.doOperator(this, arg, Arithmetics.__DIV__);
	}
	
	public PythonObject mod(PythonObject arg){
		return Arithmetics.doOperator(this, arg, Arithmetics.__MOD__);
	}
	
	public PythonObject and(PythonObject arg){
		return Arithmetics.doOperator(this, arg, Arithmetics.__AND__);
	}
	
	public PythonObject or(PythonObject arg){
		return Arithmetics.doOperator(this, arg, Arithmetics.__OR__);
	}
	
	public PythonObject xor(PythonObject arg){
		return Arithmetics.doOperator(this, arg, Arithmetics.__XOR__);
	}
	
	public PythonObject not(){
		return Arithmetics.doOperator(this, null, Arithmetics.__NOT__);
	}
	
	public PythonObject pow(PythonObject arg){
		return Arithmetics.doOperator(this, arg, Arithmetics.__POW__);
	}
	
	public PythonObject ls(PythonObject arg){
		return Arithmetics.doOperator(this, arg, Arithmetics.__LSHIFT__);
	}
	
	public PythonObject rs(PythonObject arg){
		return Arithmetics.doOperator(this, arg, Arithmetics.__RSHIFT__);
	}
}
