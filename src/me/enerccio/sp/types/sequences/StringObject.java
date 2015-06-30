package me.enerccio.sp.types.sequences;

import me.enerccio.sp.types.base.IntObject;

public class StringObject extends ImmutableSequenceObject {
	private static final long serialVersionUID = 11L;
	
	public StringObject(String v){
		value = v;
	}
	
	public String value;
	
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
	
	@Override
	public int hashCode(){
		return value.hashCode();
	}

	@Override
	public boolean equals(Object o){
		if (o instanceof StringObject)
			return value.equals(((StringObject)o).value);
		return false;
	}
	
	@Override
	protected String doToString() {
		return value;
	}
}
