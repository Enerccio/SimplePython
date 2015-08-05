package me.enerccio.sp.utils;

import me.enerccio.sp.interpret.KwArgs;
import me.enerccio.sp.types.sequences.TupleObject;

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
public class ArgumentConsumer {

	/**
	 * Consumes the argument, either from tuple or from kwargs, if either are provided, or the default value. 
	 * @param function name of the function calling this, for exception purposes
	 * @param to tuple object, must be not null
	 * @param kw kwargs, can be null
	 * @param ord ord in the tuple
	 * @param arg name of the argument
	 * @param clazz class of the java coercion
	 * @param defaultValue default value
	 * @return value or default value
	 */
	public static <X> X consumeArgument(String function, TupleObject to, KwArgs kw, int ord, String arg, Class<X> clazz, X defaultValue){
		X value = defaultValue;
		if (to.len() > ord){
			value = Coerce.argument(to, ord, function, clazz);
			if (kw != null)
				if (kw.contains(arg))
					throw Utils.throwException("TypeError", function+"(): duplicate argument '" + arg + "'");
		} else {
			if (kw != null){
				if (kw.contains(arg))
					value = kw.consume(arg, clazz);
			}
		}
		return value;
	}
	
	/**
	 * Consumes the argument, either from tuple or from kwargs, if either are provided, or raise python exception
	 * @param function name of the function calling this, for exception purposes
	 * @param to tuple object, must be not null
	 * @param kw kwargs, can be null
	 * @param ord ord in the tuple
	 * @param arg name of the argument
	 * @param clazz class of the java coercion
	 * @return value
	 */
	public static <X> X consumeArgumentNoDefault(String function, TupleObject to, KwArgs kw, int ord, String arg, Class<X> clazz){
		if (to.len() > ord){
			X value = Coerce.argument(to, ord, function, clazz);
			if (kw != null)
				if (kw.contains(arg))
					throw Utils.throwException("TypeError", function+"(): duplicate argument '" + arg + "'");
			return value;
		} else {
			if (kw != null){
				if (kw.contains(arg))
					return kw.consume(arg, clazz);
			}
		}
		throw Utils.throwException(function + "(): argument at position " + ord + " not provided");
	}
}
