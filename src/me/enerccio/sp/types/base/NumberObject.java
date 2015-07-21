package me.enerccio.sp.types.base;

import java.math.BigInteger;

import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.Arithmetics;
import me.enerccio.sp.types.AugumentedPythonObject;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

public abstract class NumberObject extends PythonObject {
	private static final long serialVersionUID = 8168239961379175666L;
	public static final String __INT__ = "__int__";

	public NumberObject(){
		
	}
	
	@Override
	protected void registerObject(){
		
	}

	@Override
	public void newObject() {	
		super.newObject();
		try {
			fields.put(__INT__, new AugumentedPythonObject(
					new JavaMethodObject(this, this.getClass().getMethod("intValue", 
							new Class<?>[]{TupleObject.class}), true), AccessRestrictions.PUBLIC));
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
			
			
			fields.put(Arithmetics.__LT__, new AugumentedPythonObject(
					new JavaMethodObject(this, this.getClass().getMethod("lt", 
							new Class<?>[]{PythonObject.class}), false), AccessRestrictions.PUBLIC));
			fields.put(Arithmetics.__LE__, new AugumentedPythonObject(
					new JavaMethodObject(this, this.getClass().getMethod("le", 
							new Class<?>[]{PythonObject.class}), false), AccessRestrictions.PUBLIC));
			fields.put(Arithmetics.__EQ__, new AugumentedPythonObject(
					new JavaMethodObject(this, this.getClass().getMethod("eq", 
							new Class<?>[]{PythonObject.class}), false), AccessRestrictions.PUBLIC));
			fields.put(Arithmetics.__NE__, new AugumentedPythonObject(
					new JavaMethodObject(this, this.getClass().getMethod("ne", 
							new Class<?>[]{PythonObject.class}), false), AccessRestrictions.PUBLIC));
			fields.put(Arithmetics.__GE__, new AugumentedPythonObject(
					new JavaMethodObject(this, this.getClass().getMethod("ge", 
							new Class<?>[]{PythonObject.class}), false), AccessRestrictions.PUBLIC));
			fields.put(Arithmetics.__GT__, new AugumentedPythonObject(
					new JavaMethodObject(this, this.getClass().getMethod("gt", 
							new Class<?>[]{PythonObject.class}), false), AccessRestrictions.PUBLIC));

		} catch (Exception e) {
			
		}
	};
	
	protected abstract PythonObject getIntValue();
	
	/** Converts number to BigInteger */ 
	public abstract BigInteger getJavaInt();
	/** Converts number to float */
	public abstract double getJavaFloat();
	
	public PythonObject intValue(TupleObject args){
		if (args.len() != 0)
			throw Utils.throwException("TypeError", "__int__ requires zero parameters");
		return getIntValue();
	}
	
	@Override
	public PythonObject set(String key, PythonObject localContext,
			PythonObject value) {
		if (!fields.containsKey(key))
			throw Utils.throwException("AttributeError", "'" + 
					Utils.run("str", Utils.run("type", this)) + "' object has no attribute '" + key + "'");
		throw Utils.throwException("AttributeError", "'" + 
				Utils.run("str", Utils.run("type", this)) + "' object attribute '" + key + "' is read only");
	}

	@Override
	public void create(String key, AccessRestrictions restrictions) {
		
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
	
	public PythonObject lt(PythonObject arg){
		return Arithmetics.doOperator(this, arg, Arithmetics.__LT__);
	}
	
	public PythonObject le(PythonObject arg){
		return Arithmetics.doOperator(this, arg, Arithmetics.__LE__);
	}
	
	public PythonObject eq(PythonObject arg){
		return Arithmetics.doOperator(this, arg, Arithmetics.__EQ__);
	}
	
	public PythonObject ne(PythonObject arg){
		return Arithmetics.doOperator(this, arg, Arithmetics.__NE__);
	}
	
	public PythonObject gt(PythonObject arg){
		return Arithmetics.doOperator(this, arg, Arithmetics.__GT__);
	}
	
	public PythonObject ge(PythonObject arg){
		return Arithmetics.doOperator(this, arg, Arithmetics.__GE__);
	}
}
