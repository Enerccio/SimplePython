package me.enerccio.sp.types.sequences;

import java.io.UnsupportedEncodingException;

import me.enerccio.sp.types.base.IntObject;

public class BytesObject extends ImmutableSequenceObject {
	private static final long serialVersionUID = 13L;
	
	public BytesObject(StringObject o, StringObject encoding){
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

}
