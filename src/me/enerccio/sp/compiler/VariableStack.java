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
package me.enerccio.sp.compiler;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Stack that contains variable's scope
 * 
 * @author Enerccio
 *
 */
public class VariableStack {

	public enum VariableType {
		LOCAL, GLOBAL, DYNAMIC
	}

	private static class VariableStackElement {
		private Map<String, VariableType> variables = new HashMap<String, VariableType>();
	}

	private Stack<VariableStackElement> stack = new Stack<VariableStackElement>();

	/**
	 * Inserts new layer to the stack
	 */
	public void push() {
		stack.add(new VariableStackElement());
	}

	/**
	 * Removes layer from the stack
	 */
	public void pop() {
		stack.pop();
	}

	/**
	 * Sets variable as global in current scope
	 * 
	 * @param variable
	 */
	public void addGlobal(String variable) {
		stack.peek().variables.put(variable, VariableType.GLOBAL);
	}

	public void addDynamic(String variable) {
		stack.peek().variables.put(variable, VariableType.DYNAMIC);
	}

	public VariableType typeOfVariable(String variable) {
		VariableStackElement e = stack.peek();
		if (e.variables.containsKey(variable))
			return e.variables.get(variable);
		return VariableType.LOCAL;
	}

}
