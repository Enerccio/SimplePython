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
