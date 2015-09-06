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
package me.enerccio.sp.interpret;

import java.util.Stack;

import me.enerccio.sp.compiler.Bytecode;
import me.enerccio.sp.errors.InterpreterError;
import me.enerccio.sp.types.PythonObject;

/**
 * PythonInterpret. Interprets bytecode. One per thread and gets automatically constructed the moment something wants to access it in a thread.
 * Empty class, because abstract one already does all the work, but kept to have clear distinction between native and pure java
 * @author Enerccio
 */
public class JavaPythonInterpreter extends AbstractPythonInterpreter {

}
