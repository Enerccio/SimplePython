/*
 * SimplePython - embeddable python interpret in java
 * Copyright (c) Peter Vanusanik, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package me.enerccio.sp.types.callables;

import java.util.List;
import java.util.Map;
import java.util.Set;

import me.enerccio.sp.errors.AttributeError;
import me.enerccio.sp.interpret.InternalDict;
import me.enerccio.sp.interpret.KwArgs;
import me.enerccio.sp.interpret.PythonInterpreter;
import me.enerccio.sp.serialization.PySerializer;
import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.AugumentedPythonObject;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.ClassInstanceObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.types.system.ClassMethodObject;
import me.enerccio.sp.types.system.StaticMethodObject;
import me.enerccio.sp.utils.StaticTools.DiamondResolver;
import me.enerccio.sp.utils.Utils;

/**
 * Class Object. Represents custom class types.
 * 
 * @author Enerccio
 *
 */
public class ClassObject extends CallableObject {
	private static final long serialVersionUID = -4002910687424344724L;
	public static final String __NAME__ = "__name__";
	public static final String __BASES__ = "__bases__";
	public static final String __DICT__ = "__dict__";
	public static final String __NEW__ = "__new__";
	public static final String __CLASS__ = "__class__";
	public static final String __GETATTR__ = "__getattr__";
	public static final String __SETATTR__ = "__setattr__";

	public ClassObject() {

	}

	@Override
	public Set<String> getGenHandleNames() {
		return PythonObject.sfields.keySet();
	}

	@Override
	protected Map<String, JavaMethodObject> getGenHandles() {
		return PythonObject.sfields;
	}

	@Override
	public void newObject() {
		super.newObject();

		try {
			Utils.putPublic(this, __CALL__, new JavaMethodObject(this, "call"));
			Utils.putPublic(this, __GETATTR__, new JavaMethodObject(this,
					"getAttr", StringObject.class));
			Utils.putPublic(this, __SETATTR__, new JavaMethodObject(this,
					"setAttr", StringObject.class, PythonObject.class));
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	};

	public PythonObject getAttr(StringObject o) {
		if (fields.containsKey(o.value))
			return fields.get(o.value).object;
		try {
			return ((InternalDict) fields.get(__DICT__).object)
					.getVariable(o.value);
		} catch (NullPointerException e) {
			throw new AttributeError(String.format(
					"%s object has no attribute '%s'",
					Utils.run("typename", this), o.value));
		}
	}

	public PythonObject setAttr(StringObject o, PythonObject v) {
		try {
			if (v == null)
				((InternalDict) fields.get(__DICT__).object).remove(o.value);
			else {
				((InternalDict) fields.get(__DICT__).object).putVariable(
						o.value, v);
			}
			return NoneObject.NONE;
		} catch (NullPointerException e) {
			throw new AttributeError(String.format(
					"%s object has no attribute '%s'",
					Utils.run("typename", this), o.value));
		}
	}

	@Override
	public PythonObject call(TupleObject args, KwArgs kwargs) {
		return newObject(args, kwargs);
	}

	/**
	 * Creates new instance from this type.
	 * 
	 * @param args
	 * @return
	 */
	private PythonObject newObject(TupleObject args, KwArgs kwargs) {
		ClassInstanceObject instance = new ClassInstanceObject();
		Utils.putPublic(instance, __CLASS__, this);

		List<ClassObject> bbases = DiamondResolver.resolveDiamonds(this);

		for (ClassObject o : bbases) {
			addToInstance(o.fields.get(__DICT__).object, instance, o);
		}

		int cfc = PythonInterpreter.interpreter.get().currentFrame.size();

		PythonInterpreter.interpreter.get().invoke(
				instance.get(ClassInstanceObject.__INIT__, instance), kwargs,
				args);
		PythonObject o = PythonInterpreter.interpreter.get().executeAll(cfc);
		if (o == NoneObject.NONE)
			return instance;
		return o;
	}

	/**
	 * Adds the dict of the type to the instance
	 * 
	 * @param s
	 * @param instance
	 * @param clazz
	 */
	private void addToInstance(PythonObject s, ClassInstanceObject instance,
			ClassObject clazz) {
		InternalDict dict = (InternalDict) s;
		synchronized (dict) {
			for (String kkey : dict.keySet()) {
				PythonObject value = dict.getVariable(kkey);
				if (value instanceof ClassMethodObject) {
					PythonObject data = value.getEditableFields().get(
							ClassMethodObject.__FUNC__).object;
					value = new UserMethodObject();
					Utils.putPublic(value, UserMethodObject.SELF, this);
					Utils.putPublic(value, UserMethodObject.FUNC, data);
				} else if (value instanceof StaticMethodObject) {
					value = value.getEditableFields().get(
							StaticMethodObject.__FUNC__).object;
				} else if (value instanceof UserFunctionObject
						|| value instanceof BoundHandleObject) {
					PythonObject data;
					if (value instanceof UserFunctionObject)
						data = value;
					else
						data = value.getEditableFields().get(
								BoundHandleObject.FUNC).object;
					value = new UserMethodObject();
					Utils.putPublic(value, UserMethodObject.SELF, instance);
					Utils.putPublic(value, UserMethodObject.FUNC, data);
					Utils.putPublic(value, UserMethodObject.ACCESSOR, clazz);
				} else if ((value instanceof JavaFunctionObject)
						&& ((JavaFunctionObject) value).isWrappedMethod()) {
					PythonObject data = value;
					value = new UserMethodObject();
					Utils.putPublic(value, UserMethodObject.SELF, instance);
					Utils.putPublic(value, UserMethodObject.FUNC, data);
				}

				AccessRestrictions ar = AccessRestrictions.PUBLIC;
				if (kkey.startsWith("__") && !kkey.endsWith("__"))
					ar = AccessRestrictions.PRIVATE;

				instance.getEditableFields().put(kkey,
						new AugumentedPythonObject(value, ar, clazz));
			}
		}
	}

	@Override
	protected String doToString() {
		return "<class " + get(__NAME__, this).toString() + ">";
	}

	@Override
	public PythonObject set(String key, PythonObject localContext,
			PythonObject value) {
		if (key.equals(__NAME__) || key.equals(__DICT__))
			throw new AttributeError("'"
					+ Utils.run("str", Utils.run("typename", this))
					+ "' object attribute '" + key + "' is read only");
		if (fields.containsKey(key))
			return super.set(key, localContext, value);
		else {
			((InternalDict) fields.get(__DICT__).object)
					.putVariable(key, value);
		}
		return NoneObject.NONE;
	}

	@Override
	public void create(String key, AccessRestrictions restrictions,
			PythonObject currentContext) {

	}

	@Override
	public synchronized PythonObject get(String key, PythonObject localContext) {
		PythonObject o = super.get(key, localContext);
		if (o == null && fields.containsKey(__DICT__))
			o = ((InternalDict) fields.get(__DICT__).object).getVariable(key);
		return o;
	}
	
	@Override
	protected void serializeDirectState(PySerializer pySerializer) {
		
	}
}
