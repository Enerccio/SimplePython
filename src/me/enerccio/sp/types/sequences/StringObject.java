package me.enerccio.sp.types.sequences;

import me.enerccio.sp.types.base.IntObject;

public class StringObject extends ImmutableSequenceObject {
	private static final long serialVersionUID = 11L;
	
	public StringObject(String v){
		value = v;
	}
	
	private String value;
	
	@Override
	public IntObject size() {
		return new IntObject(value.length());
	}
	
	@Override
	public IntObject getId(){
		return new IntObject(value.hashCode());
	}

	public String getString() {
		return value;
	}

}
