package me.enerccio.sp.types.mappings;

import java.io.Serializable;

import me.enerccio.sp.types.PythonObject;

public class PythonProxy implements Serializable {
	private static final long serialVersionUID = 5305512089616516954L;
	public PythonObject o;
	public PythonProxy(PythonObject key) {
		o = key;
	}
	@Override
	public int hashCode(){
		return o.getId();
	}
	@Override
	public boolean equals(Object p){
		return ((PythonProxy)p).o.equals(o);
	}
	@Override
	public String toString(){
		return o.toString();
	}
}