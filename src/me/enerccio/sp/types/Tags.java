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

public abstract class Tags {
	public static final byte PYTHON_BYTECODE = 0;
	public static final byte CBO = 1;
	public static final byte EO = 2;
	public static final byte FO = 3;
	public static final byte PYTHON_INTERPRETER = 4;
	public static final byte STACK_EL = 5;
	public static final byte MODULE = 6;
	public static final byte BOOL = 7;
	public static final byte INSTANCE = 8;
	public static final byte COMPLEX = 9;
	public static final byte ELLIPSIS = 10;
	public static final byte NONE = 11;
	public static final byte INT = 12;
	public static final byte LONG = 13;
	public static final byte FLOAT = 14;
	public static final byte DOUBLE = 15;
	public static final byte SLICE = 16;
	public static final byte BH = 17;
	public static final byte CLASS = 18;
	public static final byte JCONGRUENT = 19;
	public static final byte JFUNC = 20;
	public static final byte JMETH = 21;
	public static final byte UFUNC = 22;
	public static final byte UMETH = 23;
	public static final byte GEN = 24;
	public static final byte OSI = 25;
	public static final byte XRI = 26;
	public static final byte DICT = 27;
	public static final byte SDICT = 28;
	public static final byte POINTER = 29;
	public static final byte FP = 30;
	public static final byte MP = 31;
	public static final byte LIST = 32;
	public static final byte STRING = 33;
	public static final byte TUPLE = 34;
	public static final byte XRANGE = 35;
	public static final byte CLASSM = 36;
	public static final byte JFUTURE = 37;
	public static final byte PFUTURE = 38;
	public static final byte STATICM = 39;
	public static final byte FUTUREQ = 40;
	
	public static final byte BOOL_TYPE = -1;
	public static final byte BF_TYPE = -2;
	public static final byte BC_TYPE = -3;
	public static final byte CM_TYPE = -4;
	public static final byte CB_TYPE = -5;
	public static final byte COMPLEX_TYPE = -6;
	public static final byte DICT_TYPE = -7;
	public static final byte ENV_TYPE = -8;
	public static final byte FLOAT_TYPE = -9;
	public static final byte FRAME_TYPE = -10;
	public static final byte FUNCTION_TYPE = -11;
	public static final byte FUTURE_TYPE = -12;
	public static final byte INT_TYPE = -13;
	public static final byte JCALL_TYPE = -14;
	public static final byte JINST_TYPE = -15;
	public static final byte LIST_TYPE = -16;
	public static final byte LONG_TYPE = -17;
	public static final byte METH_TYPE = -18;
	public static final byte NONE_TYPE = -19;
	public static final byte SLICE_TYPE = -20;
	public static final byte SMETH_TYPE = -21;
	public static final byte STRING_TYPE = -22;
	public static final byte TUPLE_TYPE = -23;
	public static final byte TYPE_TYPE = -24;
	public static final byte TYPE_TYPE_TYPE = -25;
	public static final byte XRANGE_TYPE = -26;

	private Tags(){
		
	}
	
}
