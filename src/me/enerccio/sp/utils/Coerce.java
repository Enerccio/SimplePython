package me.enerccio.sp.utils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import me.enerccio.sp.errors.PythonException;
import me.enerccio.sp.errors.TypeError;
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.BoolObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.base.NumberObject;
import me.enerccio.sp.types.mappings.DictObject;
import me.enerccio.sp.types.mappings.StringDictObject;
import me.enerccio.sp.types.pointer.PointerObject;
import me.enerccio.sp.types.sequences.ListObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.types.types.ListTypeObject;

public class Coerce {
	private static final Map<Class<?>, Coercion> COERCIONS = new HashMap<>();
	private static final Map<Class<?>, ContainerCoerce> COERCIONS_CONTAINERS = new HashMap<>();

	private interface Coercion {
		public Object coerce(PythonObject o, Class<?> clazz)
				throws CastFailedException;
	}

	private interface ContainerCoerce {
		public Object coerce(PythonObject o, Class<?> iClass)
				throws CastFailedException;
	}

	/** Coerces PythonObject to specified java class, if possible */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <X> X toJava(PythonObject o, Class<X> clazz)
			throws CastFailedException {
		// 0th, can't do null
		if (o == null)
			throw new CastFailedException("Can't coerce null");

		// 1st, return array if requested
		if (clazz.isArray()) {
			// specific type if array is byte[] to make it a string
			if (clazz.getComponentType() == byte.class) {
				if (o instanceof StringObject)
					return (X) ((StringObject) o).value.getBytes();
			}

			ListObject lo = ListTypeObject.make_list(o);
			X rv = (X) Array.newInstance(clazz.getComponentType(), lo.len());
			for (int i = 0; i < lo.len(); i++) {
				Array.set(rv, i,
						Coerce.toJava(lo.get(i), clazz.getComponentType()));
			}
			return rv;
		}

		// 2st, return PythonObject if requested
		if (clazz.isAssignableFrom(o.getClass()))
			if (PythonObject.class.isAssignableFrom(clazz))
				return clazz.cast(o);

		// 3nd, coerce None directly
		if (o == NoneObject.NONE) {
			if (clazz.isPrimitive())
				throw new CastFailedException("Can't convert None to "
						+ clazz.getName());
			return null;
		}

		// 4rd, try to coerce pointers directly
		if (o instanceof PointerObject) {
			Class<?> ptype = ((PointerObject) o).getObject().getClass();
			if (clazz.isAssignableFrom(ptype))
				return clazz.cast(((PointerObject) o).getObject());
		}

		if (clazz.isEnum()) {
			if (o instanceof NumberObject) {
				int ord = ((NumberObject) o).intValue();
				Object[] enums = clazz.getEnumConstants();
				if (ord < 0 || ord > enums.length)
					throw new CastFailedException(
							"enum ord less than 0 or more than " + enums.length
									+ " of enum " + clazz);
				return (X) enums[ord];
			}

			if (o instanceof StringObject) {
				String str = ((StringObject) o).value;
				try {
					return (X) Enum.valueOf((Class<? extends Enum>) clazz, str);
				} catch (IllegalArgumentException e) {
					throw new CastFailedException("'" + str
							+ "' does not correspond to any enum in " + clazz);
				}
			}
		}

		// 5th, use Coercion class
		Coercion co = COERCIONS.get(clazz);
		if (co == null) {
			// Can't coerce directly
			for (Entry<Class<?>, Coercion> coer : COERCIONS.entrySet()) {
				if (clazz.isAssignableFrom(coer.getKey())) {
					co = coer.getValue();
					break;
				}
			}
		}
		if (co == null)
			// Coercion class not found
			throw new CastFailedException("Can't convert " + o.toString()
					+ " to " + clazz.getName());

		return (X) co.coerce(o, clazz);
	}

	@SuppressWarnings("unchecked")
	public static <X> Collection<X> toJavaCollection(PythonObject o,
			Class<?> cls1, Class<X> cls2) throws CastFailedException {

		ContainerCoerce co = COERCIONS_CONTAINERS.get(cls1);
		if (co == null) {
			// Can't coerce directly
			for (Entry<Class<?>, ContainerCoerce> coer : COERCIONS_CONTAINERS
					.entrySet()) {
				if (cls1.isAssignableFrom(coer.getKey())) {
					co = coer.getValue();
					break;
				}
			}
		}

		if (co == null)
			// Coercion class not found
			throw new CastFailedException("Can't convert " + o.toString()
					+ " to " + cls1.getName());

		return (Collection<X>) co.coerce(o, cls2);
	}

	/**
	 * Works like Coerce.toJava, but throws exception appropriate for failing to
	 * convert method argument. Handles missing arguments as well.
	 * 
	 * @param t
	 *            arguments tuple
	 * @param argNumber
	 *            argument number to get
	 * @param function
	 *            function name used in exception
	 * @param clazz
	 *            requested argument type
	 * 
	 * */
	public static <X> X argument(TupleObject t, int argNumber, String function,
			Class<X> clazz) throws PythonException {
		try {
			return Coerce.toJava(t.get(argNumber), clazz);
		} catch (CastFailedException e) {
			throw new TypeError(function
					+ ": cannot convert value for argument " + argNumber, e);
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new TypeError(function + ": value for argument " + argNumber
					+ " missing", e);
		}
	}

	/**
	 * Coerces object to python type representing provided class
	 */
	public static PythonObject toPython(Object o, Class<?> cls) {
		if (cls == Byte.class || cls == byte.class)
			return NumberObject.valueOf(((Byte) o).byteValue());
		if (cls == Integer.class || cls == int.class)
			return NumberObject.valueOf(((Integer) o).longValue());
		if (cls == Long.class || cls == long.class)
			return NumberObject.valueOf(((Long) o).longValue());
		if (cls == Float.class || cls == float.class)
			return NumberObject.valueOf(((Float) o).floatValue());
		if (cls == Double.class || cls == double.class)
			return NumberObject.valueOf(((Double) o).doubleValue());
		if (cls == String.class)
			return new StringObject((String) o);
		if (cls == Void.class)
			return NoneObject.NONE;
		if (cls == Boolean.class || cls == boolean.class)
			return BoolObject.fromBoolean((Boolean) o);
		if (o instanceof Collection) {
			ListObject lo = new ListObject();
			for (Object i : (Collection<?>) o) {
				lo.objects.add(i == null ? NoneObject.NONE : toPython(i,
						i.getClass()));
			}
			return lo;
		}
		if (cls.isArray() && cls.getComponentType() == byte.class) {
			return new StringObject(new String((byte[]) o));
		}
		if (o instanceof Map) {
			Map<?, ?> map = (Map<?, ?>) o;
			DictObject dict = new DictObject();
			for (Map.Entry<?, ?> e : map.entrySet()) {
				dict.backingMap.put(
						e.getKey() == null ? NoneObject.NONE : toPython(
								e.getKey(), e.getKey().getClass()),
						e.getValue() == null ? NoneObject.NONE : toPython(
								e.getValue(), e.getValue().getClass()));
			}
			return dict;
		}
		if (o instanceof PythonObject) {
			if (o instanceof StringObject)
				return new StringObject(((StringObject) o).value);
			if (o instanceof TupleObject)
				return new TupleObject(((TupleObject) o).getObjects());
			return (PythonObject) o;
		}
		if (o == null)
			return NoneObject.NONE;
		return PythonRuntime.runtime.getJavaClass(false, o.getClass()
				.getCanonicalName(), o, null);
	}

	/** Coerces object to nearest applicable python type */
	public static PythonObject toPython(Object o) {
		return PythonRuntime.runtime.getJavaClass(false,
				o.getClass().getName(), o, null);
	}

	/** Coerces object to nearest applicable python type */
	public static PythonObject toPython(int i) {
		return NumberObject.valueOf(i);
	}

	static {

		COERCIONS.put(Integer.class, new Coercion() {
			/** Coerces Integer */
			@Override
			public Object coerce(PythonObject o, Class<?> clazz)
					throws CastFailedException {
				if (o instanceof NumberObject)
					return ((NumberObject) o).intValue();

				throw new CastFailedException("Can't convert " + o.toString()
						+ " to Integer");
			}
		});

		COERCIONS.put(int.class, new Coercion() {
			/** Coerces int */
			@Override
			public Object coerce(PythonObject o, Class<?> clazz)
					throws CastFailedException {
				if (o instanceof NumberObject)
					return ((NumberObject) o).intValue();

				throw new CastFailedException("Can't convert " + o.toString()
						+ " to int");
			}
		});

		COERCIONS.put(Long.class, new Coercion() {
			/** Coerces Long */
			@Override
			public Object coerce(PythonObject o, Class<?> clazz)
					throws CastFailedException {
				if (o instanceof NumberObject)
					return ((NumberObject) o).longValue();

				throw new CastFailedException("Can't convert " + o.toString()
						+ " to Long");
			}
		});

		COERCIONS.put(long.class, new Coercion() {
			/** Coerces long */
			@Override
			public Object coerce(PythonObject o, Class<?> clazz)
					throws CastFailedException {
				if (o instanceof NumberObject)
					return ((NumberObject) o).longValue();

				throw new CastFailedException("Can't convert " + o.toString()
						+ " to long");
			}
		});

		COERCIONS.put(Float.class, new Coercion() {
			/** Coerces Float */
			@Override
			public Object coerce(PythonObject o, Class<?> clazz)
					throws CastFailedException {
				if (o instanceof NumberObject)
					return ((NumberObject) o).floatValue();

				throw new CastFailedException("Can't convert " + o.toString()
						+ " to Float");
			}
		});

		COERCIONS.put(float.class, new Coercion() {
			/** Coerces float */
			@Override
			public Object coerce(PythonObject o, Class<?> clazz)
					throws CastFailedException {
				if (o instanceof NumberObject)
					return ((NumberObject) o).floatValue();

				throw new CastFailedException("Can't convert " + o.toString()
						+ " to float");
			}
		});

		COERCIONS.put(Double.class, new Coercion() {
			/** Coerces Double */
			@Override
			public Object coerce(PythonObject o, Class<?> clazz)
					throws CastFailedException {
				if (o instanceof NumberObject)
					return ((NumberObject) o).doubleValue();

				throw new CastFailedException("Can't convert " + o.toString()
						+ " to Double");
			}
		});

		COERCIONS.put(double.class, new Coercion() {
			/** Coerces Double */
			@Override
			public Object coerce(PythonObject o, Class<?> clazz)
					throws CastFailedException {
				if (o instanceof NumberObject)
					return ((NumberObject) o).doubleValue();

				throw new CastFailedException("Can't convert " + o.toString()
						+ " to double");
			}
		});

		COERCIONS.put(Boolean.class, new Coercion() {
			/** Coerces Boolean. Everything can be coerced to boolean */
			@Override
			public Object coerce(PythonObject o, Class<?> clazz) {
				return o.truthValue() ? Boolean.TRUE : Boolean.FALSE;
			}
		});

		COERCIONS.put(boolean.class, new Coercion() {
			/** Coerces boolean. Everything can be coerced to boolean */
			@Override
			public Object coerce(PythonObject o, Class<?> clazz) {
				return o.truthValue();
			}
		});

		COERCIONS.put(String.class, new Coercion() {
			/**
			 * Coerces String. Only StringObject coerced to String
			 * 
			 * @throws CastFailedException
			 */
			@Override
			public Object coerce(PythonObject o, Class<?> clazz)
					throws CastFailedException {
				if (o instanceof StringObject)
					return ((StringObject) o).getString();

				throw new CastFailedException("Can't convert " + o.toString()
						+ " to string");
			}
		});

		COERCIONS.put(Map.class, new Coercion() {
			/** Coerces boolean. Everything can be coerced to boolean */
			@Override
			public Object coerce(PythonObject o, Class<?> clazz)
					throws CastFailedException {
				if (o instanceof DictObject)
					return ((DictObject) o).asRegularDict();
				if (o instanceof StringDictObject)
					return ((StringDictObject) o).asRegularDict();

				throw new CastFailedException("Can't convert " + o.toString()
						+ " to Map");
			}
		});

		COERCIONS.put(Object.class, new Coercion() {
			/**
			 * Coerces to object. Everything is an object, but PointerObject is
			 * dereferenced first
			 */
			@Override
			public Object coerce(PythonObject o, Class<?> clazz)
					throws CastFailedException {
				if (o instanceof PointerObject)
					return ((PointerObject) o).getObject();
				else if (o instanceof NumberObject) {
					NumberObject n = (NumberObject) o;
					switch (n.getNumberType()) {
					case BOOL:
						return n.truthValue() ? Boolean.TRUE : Boolean.FALSE;
					case COMPLEX:
						return n;
					case FLOAT:
						if (PythonRuntime.USE_DOUBLE_FLOAT)
							return n.doubleValue();
						else
							return n.floatValue();
					case INT:
						return n.intValue();
					case LONG:
						if (PythonRuntime.USE_INT_ONLY)
							return n.intValue();
						else
							return n.longValue();
					}
				} else if (o instanceof StringObject) {
					return o.toString();
				}
				return o;
			}
		});

		COERCIONS_CONTAINERS.put(List.class, new ContainerCoerce() {

			@Override
			public Object coerce(PythonObject o, Class<?> iClass)
					throws CastFailedException {

				ListObject lo = (ListObject) PythonRuntime.LIST_TYPE.call(
						new TupleObject(o), null);
				List<Object> l = new ArrayList<Object>();
				synchronized (lo.objects) {
					for (PythonObject po : lo.objects)
						l.add(toJava(po, iClass));
				}

				return l;
			}
		});
	}

	public static class CannotCoerceException extends RuntimeException {
		private static final long serialVersionUID = 8306084748185702275L;

		public CannotCoerceException(PythonObject type) {
			super("Cannot coerce " + type.toString() + " to java");
		}
	}
}
