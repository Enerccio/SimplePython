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
package me.enerccio.sp.types.types;

import java.util.ArrayList;
import java.util.List;

import me.enerccio.sp.compiler.PythonCompiler;
import me.enerccio.sp.errors.TypeError;
import me.enerccio.sp.interpret.InternalDict;
import me.enerccio.sp.interpret.KwArgs;
import me.enerccio.sp.runtime.PythonRuntime;
import me.enerccio.sp.sandbox.PythonSecurityManager.SecureAction;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.mappings.StringDictObject;
import me.enerccio.sp.types.sequences.ListObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.StaticTools.ParserGenerator;

/**
 * function()
 * @author Enerccio
 *
 */
public class FunctionTypeObject extends TypeObject {
	private static final long serialVersionUID = 4621194768946693335L;
	public static final String FUNCTION_CALL = "function";

	@Override
	public String getTypeIdentificator() {
		return "function";
	}

	// function(string, locals, tuple_of_maps, list_of_anames, vararg_name, kwargs_name, dict)
	@Override
	public PythonObject call(TupleObject args, KwArgs kwargs){
		if (kwargs != null)
			kwargs.notExpectingKWArgs();	// Throws exception if there is kwarg defined 
		if (args.len() != 7)
			throw new TypeError(" function(): incorrect number of parameters, requires 7, got " + args.len());
		
		PythonRuntime.runtime.checkSandboxAction("function", SecureAction.RUNTIME_COMPILE);
		
		String src = null;
		InternalDict dict = null;
		List<InternalDict> maps = new ArrayList<InternalDict>();
		List<String> aas = new ArrayList<String>();
		String vararg = null;
		String kwararg = null;
		InternalDict defaults = null;
		
		try {
			PythonObject arg = args.getObjects()[0];
			src = ((StringObject)arg).value;
			
			dict = (InternalDict)args.getObjects()[1];
			
			TupleObject to = (TupleObject)args.getObjects()[2];
			for (PythonObject o : to.getObjects())
				maps.add(((InternalDict)o));
			
			ListObject o = (ListObject)args.getObjects()[3];
			for (PythonObject oo : o.objects)
				aas.add(((StringObject)oo).value);
			
			arg = args.getObjects()[4];
			if (arg != NoneObject.NONE)
				vararg = ((StringObject)arg).value;
			
			arg = args.getObjects()[4];
			if (arg != NoneObject.NONE)
				kwararg = ((StringObject)arg).value;
			
			arg = args.getObjects()[6];
			if (arg != NoneObject.NONE)
				defaults = (InternalDict)arg;
			else
				defaults = new StringDictObject();
			
		} catch (ClassCastException e){
			throw new TypeError(" function(): wrong types of arguments");
		}
		
		PythonCompiler c = new PythonCompiler();
		
		return c.doCompile(ParserGenerator.parseStringInput(src).string_input(), maps, aas, vararg, kwararg, defaults, dict);
	}

}
