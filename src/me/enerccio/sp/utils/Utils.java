package me.enerccio.sp.utils;

import java.lang.reflect.Array;

import me.enerccio.sp.interpret.PythonInterpret;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.BoolObject;
import me.enerccio.sp.types.base.IntObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.base.RealObject;
import me.enerccio.sp.types.pointer.PointerObject;
import me.enerccio.sp.types.sequences.StringObject;

public class Utils {

	
	/**
	 * Adds object to the left of the array, ie x + [a, b] => [x, a, b]
	 * 
	 * @param data
	 *            what to be pushed
	 * @param array
	 *            array to be pushed in
	 * @return new array with object pushed in left
	 */
	public static <T> T[] pushLeft(T data, T[] array) {
		@SuppressWarnings("unchecked")
		T[] pushed = (T[]) Array.newInstance(array.getClass()
				.getComponentType(), array.length + 1);
		pushed[0] = data;
		System.arraycopy(array, 0, pushed, 1, array.length);
		return pushed;
	}
	
	public static PythonObject cast(Object ret, Class<?> retType) {
		if (retType == Byte.class || retType == byte.class)
			return new IntObject(((Byte) ret).byteValue());
		if (retType == Integer.class || retType == int.class)
			return new IntObject(((Integer) ret).longValue());
		if (retType == Long.class || retType == long.class)
			return new IntObject(((Long) ret).longValue());
		if (retType == Float.class || retType == float.class)
			return new RealObject(((Float) ret).floatValue());
		if (retType == Double.class || retType == double.class)
			return new RealObject(((Double) ret).doubleValue());
		if (retType == String.class)
			return new StringObject((String) ret);
		if (retType == Void.class)
			return NoneObject.NONE;
		if (retType == Boolean.class || retType == boolean.class)
			return BoolObject.fromBoolean((Boolean) ret);
		return new PointerObject(ret);
	}
	
	public static Object asJavaObject(Class<?> aType, PythonObject datum)
			throws PointerMethodIncompatibleException {
		if (aType == Integer.class || aType == int.class) {
			if (datum instanceof IntObject)
				return ((IntObject)datum).intValue();
			else
				return asJavaObject(aType, PythonInterpret.interpret.get().executeCall("int", datum));
		}

		if (aType == Long.class || aType == long.class) {
			if (datum instanceof IntObject)
				return ((IntObject)datum).longValue();
			else
				return asJavaObject(aType, PythonInterpret.interpret.get().executeCall("int", datum));
		}

		if (aType == Float.class || aType == float.class) {
			if (datum instanceof RealObject)
				return ((RealObject)datum).floatValue();
			else
				return asJavaObject(aType, PythonInterpret.interpret.get().executeCall("float", datum));
		}

		if (aType == Double.class || aType == double.class) {
			if (datum instanceof RealObject)
				return ((RealObject)datum).doubleValue();
			else
				return asJavaObject(aType, PythonInterpret.interpret.get().executeCall("float", datum));
		}

		if (aType == Boolean.class || aType == boolean.class) {
			return new Boolean(datum.truthValue());
		}

		if (aType == String.class) {
			if (datum instanceof StringObject)
				return ((StringObject) datum).getString();
		}

		if (datum instanceof PointerObject) {
			Class<?> ptype = ((PointerObject) datum).getObject().getClass();
			if (aType.isAssignableFrom(ptype))
				return ((PointerObject) datum).getObject();
		}

		throw new PointerMethodIncompatibleException();
	}
	
}
