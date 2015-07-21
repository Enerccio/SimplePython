package me.enerccio.sp.types.types;

import me.enerccio.sp.types.callables.ClassObject;
import me.enerccio.sp.types.mappings.MapObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

public class ObjectTypeObject extends ClassObject {
	private static final long serialVersionUID = 4583318830595686027L;
	public static final String OBJECT_CALL = "object";
	public static final String __CONTAINS__ = "__contains__";
	public static final String IS = "is";
	
	public ObjectTypeObject inst = new ObjectTypeObject();
	
	@Override
	public void newObject() {
		super.newObject();
		Utils.putPublic(this, "__name__", new StringObject("object"));
		Utils.putPublic(this, "__bases__", new TupleObject());
		Utils.putPublic(this, "__dict__", new MapObject());
	}
	
	@Override
	protected String doToString() {
		return "<type object>";
	}

}
