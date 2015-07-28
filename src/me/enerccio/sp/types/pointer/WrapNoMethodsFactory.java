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
package me.enerccio.sp.types.pointer;

/**
 * Will wrap no methods at all.
 * @author Enerccio
 *
 */
public class WrapNoMethodsFactory implements PointerFactory {
	private static final long serialVersionUID = -4372859400649832530L;

	@Override
	public PointerObject doInitialize(Object instance) {
		return new PointerObject(instance);
	}

}
