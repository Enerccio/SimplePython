package me.enerccio.sp.types.types;

import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.SliceObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

public class SliceTypeObject extends TypeObject {
	private static final long serialVersionUID = 1174044496063617044L;
	public static final String SLICE_CALL = "slice";

	@Override
	public String getTypeIdentificator() {
		return "splice";
	}

	@Override
	public PythonObject call(TupleObject args) {
		if (args.size().intValue() != 3)
			throw Utils.throwException("TypeError", "slice(): incorrect number of parameters, must be 3");
		
		return new SliceObject(args.valueAt(0), args.valueAt(1), args.valueAt(2));
	}
}
