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

import me.enerccio.sp.interpret.PythonInterpreter;
import me.enerccio.sp.interpret.KwArgs;
import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.AugumentedPythonObject;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.ClassInstanceObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.mappings.DictObject;
import me.enerccio.sp.types.mappings.PythonProxy;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.types.system.ClassMethodObject;
import me.enerccio.sp.types.system.StaticMethodObject;
import me.enerccio.sp.utils.Utils;

/**
 * Class Object. Represents custom class types.
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
	
	public ClassObject(){
		
	}
	
	@Override
	public void newObject() {
		super.newObject();
		
		try {
			Utils.putPublic(this, __CALL__, new JavaMethodObject(this, "call"));
			Utils.putPublic(this, __GETATTR__, new JavaMethodObject(this, this.getClass().getMethod("getAttr", 
					new Class<?>[]{StringObject.class}), false));
			Utils.putPublic(this, __SETATTR__, new JavaMethodObject(this, this.getClass().getMethod("setAttr", 
					new Class<?>[]{StringObject.class, PythonObject.class}), false));
		} catch (NoSuchMethodException e){
			e.printStackTrace();
		}
	};
	
	public PythonObject getAttr(StringObject o){
		if (fields.containsKey(o.value))
			return fields.get(o.value).object;
		try {
			return ((DictObject)fields.get(__DICT__).object).doGet(o);
		} catch (NullPointerException e){
			throw Utils.throwException("AttributeError", String.format("%s object has no attribute '%s'", Utils.run("type", this), o.value));
		}
	}
	
	public PythonObject setAttr(StringObject o, PythonObject v){
		try {
			if (v == null)
				((DictObject)fields.get(__DICT__).object).backingMap.remove(o);
			else {
				((DictObject)fields.get(__DICT__).object).put(o.value, v);
			}
			return NoneObject.NONE;
		} catch (NullPointerException e){
			throw Utils.throwException("AttributeError", String.format("%s object has no attribute '%s'", Utils.run("type", this), o.value));
		}
	}


	@Override
	public PythonObject call(TupleObject args, KwArgs kwargs) {
		return newObject(args, kwargs);
	}

	/**
	 * Creates new instance from this type.
	 * @param args
	 * @return
	 */
	private PythonObject newObject(TupleObject args, KwArgs kwargs) {
		ClassInstanceObject instance = new ClassInstanceObject();
		Utils.putPublic(instance, __CLASS__, this);
		
		List<ClassObject> bbases = Utils.resolveDiamonds(this);
		
		for (ClassObject o : bbases){
			addToInstance(o.fields.get(__DICT__).object, instance, o);
		}
		
		int cfc = PythonInterpreter.interpret.get().currentFrame.size();

		PythonInterpreter.interpret.get().invoke(instance.get(ClassInstanceObject.__INIT__, instance), kwargs, args);
		PythonObject o = PythonInterpreter.interpret.get().executeAll(cfc);
		if (o == NoneObject.NONE)
			return instance;
		return o;
	}

	/**
	 * Adds the dict of the type to the instance
	 * @param s
	 * @param instance
	 * @param clazz
	 */
	private void addToInstance(PythonObject s, ClassInstanceObject instance, ClassObject clazz) {
		DictObject dict = (DictObject)s;
		synchronized (dict.backingMap){
			for (PythonProxy pkey : dict.backingMap.keySet()){
				PythonObject key = pkey.o;
				String kkey = ((StringObject)key).getString();
				PythonObject value = dict.backingMap.get(key);
				if (value instanceof ClassMethodObject){
					PythonObject data = value.fields.get(ClassMethodObject.__FUNC__).object;
					value = new UserMethodObject();
					value.newObject();
					Utils.putPublic(value, UserMethodObject.SELF, this);
					Utils.putPublic(value, UserMethodObject.FUNC, data);
				} else if (value instanceof StaticMethodObject){
					value = value.fields.get(StaticMethodObject.__FUNC__).object;
				} else if (value instanceof UserFunctionObject || value instanceof BoundHandleObject){
					PythonObject data;
					if (value instanceof UserFunctionObject)
						data = value;
					else
						data = value.fields.get(BoundHandleObject.FUNC).object;
					value = new UserMethodObject();
					value.newObject();
					Utils.putPublic(value, UserMethodObject.SELF, instance);
					Utils.putPublic(value, UserMethodObject.FUNC, data);
					Utils.putPublic(value, UserMethodObject.ACCESSOR, clazz);
				} else if ((value instanceof JavaFunctionObject) && ((JavaFunctionObject)value).isWrappedMethod()){
					PythonObject data = value;
					value = new UserMethodObject();
					value.newObject();
					Utils.putPublic(value, UserMethodObject.SELF, instance);
					Utils.putPublic(value, UserMethodObject.FUNC, data);
				}
				
				AccessRestrictions ar = AccessRestrictions.PUBLIC;
				if (kkey.startsWith("__") && !kkey.endsWith("__"))
					ar = AccessRestrictions.PRIVATE;
				
				instance.fields.put(kkey, new AugumentedPythonObject(value, ar, clazz));
			}
		}
	}

	@Override
	protected String doToString() {
		return "<class " + get(__NAME__, this).toString() + ">";
	}

}
