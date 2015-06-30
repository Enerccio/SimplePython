package me.enerccio.sp.types.sequences;

import java.io.UnsupportedEncodingException;

import me.enerccio.sp.types.base.IntObject;

public class ByteArrayObject extends MutableSequenceObject {
	private static final long serialVersionUID = 17L;

	public ByteArrayObject(StringObject o, StringObject encoding){
		try {
			bytes = o.getString().getBytes(encoding.getString());
		} catch (UnsupportedEncodingException e) {
			// TODO
		}
	}
	
	private byte[] bytes;
	
	@Override
	public IntObject size() {
		return new IntObject(bytes.length);
	}

	@Override
	protected String doToString() {
		return bytes.toString();
	}

	
}
