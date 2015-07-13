package me.enerccio.sp.types.types;

import java.util.Arrays;

import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.sequences.ListObject;
import me.enerccio.sp.types.sequences.TupleObject;

public class ListTypeObject extends TypeObject {
	private static final long serialVersionUID = -4391029961115891279L;
	public static final String LIST_CALL = "list";

	@Override
	public String getTypeIdentificator() {
		return "list";
	}
	
	@Override
	public PythonObject call(TupleObject args) {
		ListObject lo = new ListObject();
		lo.newObject();
		lo.objects.addAll(Arrays.asList(args.getObjects()));
		return lo;
	}

}
