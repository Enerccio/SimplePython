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
package me.enerccio.sp.sandbox;

public enum SecureAction {
	/**
	 * checked when script wants to open file
	 */
	OPEN_FILE,
	/**
	 * checked when new java instance is created via python
	 */
	JAVA_INSTANCE_CREATION,
	/**
	 * checked when new thread is created
	 */
	NEW_THREAD,
	/**
	 * checked when python wants to terminate java
	 */
	TERMINATE_JAVA,
	/**
	 * checked when python wants to disassemble python code
	 */
	DISASSEMBLY,
	/**
	 * checked when eval is called in python
	 */
	RUNTIME_EVAL,
	/**
	 * checked when compile is called in python
	 */
	RUNTIME_COMPILE,
	/**
	 * when webbrowser is requested by python
	 */
	WEBBROWSER,
	/**
	 * When socket networking is requested by python
	 */
	SOCKET,
	/**
	 * When socket server networking is requested (also SOCKET is checked first)
	 */
	SOCKET_SERVER, 
	/**
	 * When importing .pyj files
	 */
	JAVA_MODULE,
}