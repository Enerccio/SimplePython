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

import me.enerccio.sp.errors.NativeError;
import me.enerccio.sp.interpret.CompiledBlockObject;
import me.enerccio.sp.types.pointer.PointerFactory;
import me.enerccio.sp.types.pointer.PointerObject;
import me.enerccio.sp.types.pointer.WrapPublicFactory;

public class ProxyModule extends ModuleObject {
	private static final long serialVersionUID = -8899305931463807366L;
	private static final PointerFactory pp = new WrapPublicFactory();

	private PointerObject moduleAccessor;
	private Object rawAccessor;

	public ProxyModule(Object moduleAccessor, ModuleData data) {
		super(data);
		rawAccessor = moduleAccessor;
		this.moduleAccessor = pp.doInitialize(moduleAccessor,
				moduleAccessor.getClass());
	}

	@Override
	public void injectGlobal(String key, PythonObject value) {
		// does nothing
	}

	@Override
	public PythonObject set(String key, PythonObject localContext,
			PythonObject value) {
		return moduleAccessor.set(key, localContext, value);
	}

	@Override
	public synchronized PythonObject get(String key, PythonObject localContext) {
		return moduleAccessor.get(key, localContext);
	}

	@Override
	protected String doToString() {
		return "<Module proxy of " + moduleAccessor.toString() + " at 0x"
				+ Integer.toHexString(hashCode()) + ">";
	}

	/**
	 * Initializes the module.
	 */
	@Override
	public void initModule() {
		try {
			rawAccessor.getClass().getMethod("init").invoke(rawAccessor);
		} catch (Exception e) {
			throw new NativeError(
					"failed to load java module, maybe pyj forgot to include init() method?",
					e);
		}
		isInited = true;
	}

	@Override
	public PythonObject getField(String string) {
		return moduleAccessor.get(string, null);
	}

	@Override
	public CompiledBlockObject getFrame() {
		return null;
	}
}
