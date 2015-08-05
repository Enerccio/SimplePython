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
package me.enerccio.sp.types;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.types.base.BoolObject;
import me.enerccio.sp.types.base.ClassInstanceObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.callables.ClassObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.types.properties.MethodPropertyObject;
import me.enerccio.sp.utils.Utils;

/**
 * Represents every single python object. Root of the hierarchy.
 * @author Enerccio
 *
 */
public abstract class PythonObject implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final String __CLASS__ = "__class__";
	
	public PythonObject(){
		
	}
	
	private static Map<String, JavaMethodObject> sfields = new HashMap<String, JavaMethodObject>();
	private static MethodPropertyObject mpo;
	static {
		try {
			sfields.put(Arithmetics.__EQ__,  new JavaMethodObject(PythonObject.class, "eq", PythonObject.class));
			sfields.put(Arithmetics.__NE__,  new JavaMethodObject(PythonObject.class, "ne", PythonObject.class));
			sfields.put(Arithmetics.__NE__,  new JavaMethodObject(PythonObject.class, "ne", PythonObject.class));
			
			mpo = new MethodPropertyObject(__CLASS__, JavaMethodObject.noArgMethod(PythonObject.class, "getType"));
		} catch (Exception e){
			e.printStackTrace();
		} 
	}
	
	protected void bindMethod(String name, JavaMethodObject m) {
		fields.put(name, new AugumentedPythonObject(m.cloneWithThis(this), AccessRestrictions.PUBLIC));
	}
	
	/**
	 * Should be called only once to initialize methods of the object
	 */
	public void newObject(){
		registerObject();
		bindMethods(sfields);
		Utils.putPublic(this, __CLASS__, mpo.bindTo(this));
	}

	protected void bindMethods(Map<String, JavaMethodObject> map) {
		for (Entry<String, JavaMethodObject> e : map.entrySet())
			bindMethod(e.getKey(), e.getValue());
	}

	/**
	 * Initializes basic method of all objects, EQ, NE and NOT
	 */
	protected void registerObject(){
		PythonRuntime.runtime.newInstanceInitialization(this);
	}
	
	public PythonObject eq(PythonObject other){
		return BoolObject.fromBoolean(other == this);
	}
	
	public PythonObject ne(PythonObject other){
		return BoolObject.fromBoolean(other != this);
	}

	/**
	 * Returns type of this object
	 * @return
	 */
	public PythonObject getType(){
		return Utils.run("type", this);
	}
	
	/**
	 * Returns id of this object
	 * @return
	 */
	public int getId(){
		return hashCode();
	}
	
	/**
	 * Returns truth value of this object
	 * @return
	 */
	public abstract boolean truthValue();
	
	/**
	 * Fields of this object are stored here.
	 */
	public Map<String, AugumentedPythonObject> fields = Collections.synchronizedMap(new HashMap<String, AugumentedPythonObject>());
	
	/**
	 * Returns the field value for the key and local context
	 * @param key
	 * @param localContext
	 * @return
	 */
	public synchronized PythonObject get(String key, PythonObject localContext) {
		if (!fields.containsKey(key))
			return null;
		AugumentedPythonObject field = fields.get(key);
		if (field.restrictions == AccessRestrictions.PRIVATE && !isPrivate(localContext, field))
			throw Utils.throwException("AttributeError", "access to field '" + key + "' is restricted for type '" + 
					Utils.run("str", Utils.run("typename", this)) + "'");
		return field.object;
	}

	/**
	 * Sets the value of the field into value with local context
	 * @param key
	 * @param localContext
	 * @param value
	 * @return
	 */
	public synchronized PythonObject set(String key, PythonObject localContext,
			PythonObject value){
		if (!fields.containsKey(key))
			throw Utils.throwException("AttributeError", "'" + 
					Utils.run("str", Utils.run("typename", this)) + "' object has no attribute '" + key + "'");
		AugumentedPythonObject field = fields.get(key);
		if (field.restrictions == AccessRestrictions.PRIVATE && !isPrivate(localContext, field))
			throw Utils.throwException("AttributeError", "access to field '" + key + "' is restricted for type '" + 
					Utils.run("str", Utils.run("typename", this)) + "'");
		field.object = value;
		if (value == null)
			fields.remove(key);
		return NoneObject.NONE;
	}

	/**
	 * Checks if field is private
	 * @param localContext
	 * @param p
	 * @return
	 */
	private boolean isPrivate(PythonObject localContext, AugumentedPythonObject p) {
		if (localContext == null)
			return false;
		
		if (this instanceof ClassInstanceObject){
			if (localContext instanceof ClassObject){
				return p.owner == null ? true : (localContext == p.owner);
			}
			return false;
		}
		
		return true;
	}

	/**
	 * Creates new empty field with restrictions and context
	 * @param key
	 * @param restrictions
	 * @param currentContext
	 */
	public synchronized void create(String key, AccessRestrictions restrictions, PythonObject currentContext){
		if (fields.containsKey(key))
			throw Utils.throwException("AttributeError", "'" + 
					Utils.run("str", Utils.run("typename", this)) + "' object already has a attribute '" + key + "'");
		AugumentedPythonObject field = new AugumentedPythonObject(NoneObject.NONE, restrictions, currentContext);
		fields.put(key, field);
	}
	
	@Override
	public boolean equals(Object o){
		return o == this;
	}
	
	@Override
	public final String toString(){
		return doToString();
	}

	/**
	 * Returns string representation of the object
	 * @return
	 */
	protected abstract String doToString();
	
	public volatile boolean mark = false;
	public long linkName;
}
