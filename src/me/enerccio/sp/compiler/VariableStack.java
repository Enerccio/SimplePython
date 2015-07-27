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

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class VariableStack {
	
	private static class VariableStackElement {
		private Set<String> globals = new HashSet<String>();
	}

	private Stack<VariableStackElement> stack = new Stack<VariableStackElement>();
	
	public void push(){
		stack.add(new VariableStackElement());
	}
	
	public void pop(){
		stack.pop();
	}
	
	public void addGlobal(String variable){
		stack.peek().globals.add(variable);
	}
	
	public boolean isGlobalVariable(String variable){
		return stack.peek().globals.contains(variable);
	}
}
