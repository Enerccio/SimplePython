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
package me.enerccio.sp.interpret.jit;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import me.enerccio.sp.compiler.Bytecode;
import me.enerccio.sp.interpret.CompiledBlockObject;
import me.enerccio.sp.interpret.ExecutionResult;
import me.enerccio.sp.interpret.PythonInterpreter;
import me.enerccio.sp.types.sequences.StringObject;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;

public class JJITCompiler extends Thread {

	private JJITCompiler(){
		setName("jjit-compiler-thread");
		setDaemon(true);
		start();
	}
	
	private static boolean enabled = false;
	private static JJITCompiler compiler;
	
	public static boolean isEnabled(){
		return enabled;
	}
	
	public static void setEnable(boolean enabled){
		JJITCompiler.enabled = enabled;
		if (compiler == null){
			synchronized (JJITCompiler.class){
				if (compiler == null){
					compiler = new JJITCompiler();
				}
			}
		}
	}
	
	@Override
	public void run(){
		compileJITRequests();
	}
	
	public static void enqueue(JitRequest request){
		compiler.doEnqueue(request);
	}
	
	private ConcurrentLinkedQueue<JitRequest> requests = new ConcurrentLinkedQueue<JitRequest>();
	
	private void doEnqueue(JitRequest request){
		requests.add(request);
	}

	private void compileJITRequests() {
		while (true){
			if (interrupted())
				return;
			
			JitRequest rq = requests.poll();
			
			if (rq == null){
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
					return;
				}
			} else {
				try {
					compileRequest(rq);
				} catch (Exception e) {
					e.printStackTrace();
					rq.hasResult = true;
				}
			}
		}
	}
	
	private Map<ClassLoader, ClassPool> pools = new HashMap<ClassLoader, ClassPool>();

	private void compileRequest(JitRequest rq) throws Exception {
		ClassLoader cl = rq.getLoader();
		ClassPool cp = getClassPool(cl);
		
		CtClass ci = cp.get("me.enerccio.sp.interpret.jit.CompiledPython");
		CtClass object = cp.get("java.lang.Object");
		
		CtClass jitted = cp.makeClass("jit-class&" + Integer.toHexString(rq.getBlock().hashCode()) + "&" + rq.getPcFrom() + "&" + rq.getDebug());
		jitted.setSuperclass(object);
		jitted.addInterface(ci);
		
		CtConstructor ct = CtNewConstructor.defaultConstructor(jitted);
		jitted.addConstructor(ct);
		
		String builtMethod = buildMethod(rq.getBlock(), rq.getPcFrom(), rq.getDebug(), jitted);
		CtMethod method = CtNewMethod.make(builtMethod, jitted);
		jitted.addMethod(method);
		
		jitted.debugWriteFile();
		
		Class<?> cls = cp.toClass(jitted, cl, null);
		rq.result = (CompiledPython) cls.newInstance();
		rq.hasResult = true;
	}

	private ClassPool getClassPool(ClassLoader cl) throws Exception {
		if (!pools.containsKey(cl)){
			ClassPool cp = new ClassPool();
			cp.appendSystemPath();
			
			cp.importPackage("java.util");
			cp.importPackage("me.enerccio.sp.errors");
			cp.importPackage("me.enerccio.sp.interpret");
			cp.importPackage("me.enerccio.sp.interpret.debug");
			cp.importPackage("me.enerccio.sp.interpret.jit");
			cp.importPackage("me.enerccio.sp.runtime");
			cp.importPackage("me.enerccio.sp.compiler");
			cp.importPackage("me.enerccio.sp.types");
			cp.importPackage("me.enerccio.sp.types.base");
			cp.importPackage("me.enerccio.sp.types.callables");
			cp.importPackage("me.enerccio.sp.types.iterators");
			cp.importPackage("me.enerccio.sp.types.mappings");
			cp.importPackage("me.enerccio.sp.types.pointer");
			cp.importPackage("me.enerccio.sp.types.propertries");
			cp.importPackage("me.enerccio.sp.types.sequences");
			cp.importPackage("me.enerccio.sp.types.system");
			cp.importPackage("me.enerccio.sp.types.types");
			cp.importPackage("me.enerccio.sp.utils");
			
			pools.put(cl, cp);
		}
		return pools.get(cl);
	}
	
	private static final String header
		= "public ExecutionException execute(PythonInterpreter i, FrameObject o, Stack<PythonObject> stack, CompiledBlockObject cb, Debugger debugger){\n";
	
	private static final String footer
		= "}\n";
	
	private static final String header_ord
		= "public ExecutionException execute_%s(PythonInterpreter i, FrameObject o, Stack<PythonObject> stack, CompiledBlockObject cb, Debugger debugger){\n";

	
	private String buildMethod(CompiledBlockObject block, int pcFrom,
			boolean debug, CtClass jitted) throws Exception {
		return header + buildMethodBody(block, pcFrom, debug, jitted, -1) + footer;
	}
	
	private String buildMethod(CompiledBlockObject block, int pcFrom,
			boolean debug, CtClass jitted, int ord) throws Exception{
		return String.format(header_ord, ord) + buildMethodBody(block, pcFrom, debug, jitted, ord) + footer;
	}

	private String buildMethodBody(CompiledBlockObject block, int pcFrom,
			boolean debug, CtClass jitted, int fncord) throws Exception {
	
		ByteBuffer buffer = block.getBytedataAsNativeBuffer();
		buffer.position(pcFrom);
		
		StringBuilder body = new StringBuilder();
		boolean hasReturn = false;
		
		body.append("// Locals declaration \n");
		body.append(" PythonObject method = null; \n");
		body.append(" PythonObject a = null; \n");
		body.append(" PythonObject b = null; \n");
		body.append(" PythonObject runnable = null; \n");
		body.append(" PythonObject[] args = null; \n");
		body.append(" String m = null; \n");
		body.append(" String vname = null; \n");
		body.append(" boolean isGlobal = false; \n");
		body.append(" int jv = 0; \n");
		body.append(" FrameObject frame = null; \n");
		body.append(" String s1 = null; \n");
		body.append(" String s2 = null; \n");		
		body.append(" PythonObject type = null; \n");
		body.append(" PythonObject value = null; \n");
		body.append(" boolean doingNew = false; \n");
		body.append(" String[] sarray = null; \n");
		body.append(" String svl = null; \n");
		body.append(" boolean found = false; \n");
		body.append(" int nth = 0; \n");
		body.append(" List<PythonObject> rest = null; \n");
		body.append(" List<String> closureCopy = null; \n");
		
		int itc = 0;
		
		outer:
		while (buffer.hasRemaining()){
			int cpos = buffer.position();
			Bytecode b = Bytecode.fromNumber(buffer.get());
			
			body.append(" if (o.accepts_return) { stack.push(i.returnee == null ? NoneObject.NONE : i.returnee); o.accepts_return = false; } \n");

			if (PythonInterpreter.TRACE_ENABLED){
				body.append("if (PythonInterpreter.TRACE_ALL || PythonInterpreter.TRACE_THREADS.contains(currentOwnerThread.getName())) System.err.println(CompiledBlockObject.dis(o.compiled, true, spc) + \" \" + PythonInterpreter.printStack(stack));");
			}
			
			body.append("\n");
			body.append("// " + b.toString() + " in " + block.toString() + ", pc = " + cpos + " \n");
			if (debug){
				body.append(" // debug enabled version \n");
				body.append(" debugger.debugNextOperation(i, Bytecode." + b.toString() + ", f, " + cpos + "); \n");
			}
			
			++itc;
			
			switch (b){
			case NOP:
				--itc;
				break;
				

			case D_RETURN:
			case D_STARTFUNC:
				--itc;
				buffer.getInt();
				break;
			
			case ADD:
				addBinaryOperation("Add", buffer, body);
				break;
			case AND:
				addBinaryOperation("And", buffer, body);
				break;
			case DCOLON:
				addBinaryOperation("Dcolon", buffer, body);
				break;
			case DEL:
				body.append(" vname = \"" + ((StringObject) block.getConstant(buffer.getInt())).value + "\"; \n");
				body.append(" isGlobal = " + (buffer.getInt() == 1 ? "true" : "false") + ";\n");
				body.append(" i.environment().delete(vname.value, isGlobal); \n");
				break;
			case DELATTR:
				body.append(" runnable = i.environment().getBuiltin(\"delattr\");\n");
				body.append(" args = new PythonObject[2];\n");
				body.append(" args[1] = o.compiled.getConstant(" + buffer.getInt() + "); // attribute \n");
				body.append(" args[0] = stack.pop(); \n");
				body.append(" i.returnee = i.execute(true, runnable, null, args); \n");
				break;
			case DIV:
				addBinaryOperation("Div", buffer, body);
				break;
			case DUP:
				body.append(" jv = " + buffer.getInt() + "; \n");
				body.append(" if (jv == 0) \n");
				body.append("  stack.push(stack.peek());\n");
				body.append(" else\n");
				body.append("  stack.push(stack.get(stack.size() - 1 - jv));\n");
				break;
			case ECALL:
				body.append(" i.checkOverflow(); \n");
				body.append(" runnable = stack.peek(); \n");
				body.append(" try {\n");
				body.append("  returnee = i.execute(true, runnable, null);\n");
				body.append("  frame = i.currentFrame.peekLast(); \n");
				body.append("  if (frame != o) \n");
				body.append("   frame.parentFrame = o; \n");
				body.append("  else \n");
				body.append("    stack.push(o); \n");
				body.append(" } catch (PythonExecutionException e) {\n");
				body.append("  frame = new FrameObject(); \n");
				body.append("  frame.parentFrame = o; \n");
				body.append("  frame.exception = e.getException();\n");
				body.append("  stack.push(frame);\n");
				body.append(" }\n");
				break;
			case EQ:
				addBinaryOperation("Eq", buffer, body);
				break;
			case GE:
				addBinaryOperation("Ge", buffer, body);
				break;
			case GETATTR:
				body.append(" try {\n");
				body.append("  PythonObject apo;\n");
				body.append("  StringObject field = (StringObject) o.compiled.getConstant(" + buffer.getInt() + ");\n");
				body.append("  PythonObject value = stack.pop(); // object to get attribute from");
				body.append("  if (value instanceof FutureObject) { \n");
				body.append("   PythonObject rvalue = null; \n");
				body.append("   while ((rvalue = ((FutureObject) value).getValue()) == null) ; \n");
				body.append("   value = rvalue; \n");
				body.append("  } \n");
				body.append("  apo = value.get(\"__getattribute__\", i.getLocalContext()); \n");
				body.append("  if (apo != null && !(value instanceof ClassObject)) {\n");
				body.append("   // There is __getattribute__ defined, call it directly\n");
				body.append("   i.returnee = i.execute(true, apo, null, field);\n");
				body.append("   o.accepts_return = true; \n");
				body.append("  } else {\n");
				body.append("   // Try to grab argument normally...\n");
				body.append("   apo = value.get(field.value, i.getLocalContext());\n");
				body.append("   if (apo != null) {\n");
				body.append("    i.returnee = apo;\n");
				body.append("    o.accepts_return = true; \n");
				body.append("   } else {\n");
				body.append("    // ... and if that fails, use __getattr__ if available\n");
				body.append("    apo = value.get(\"__getattr__\", i.getLocalContext()); \n");
				body.append("    if (apo != null) {\n");
				body.append("     // There is __getattribute__ defined, call it directly\n");
				body.append("     i.returnee = i.execute(false, apo, null, field);\n");
				body.append("     o.accepts_return = true; \n");
				body.append("    } else\n");
				body.append("     throw new AttributeError(\"\" + value.getType() + \" object has no attribute '\" + field + \"'\"); \n");
				body.append("   }\n");
				body.append("  }\n");
				body.append(" } finally {\n");
				body.append("  if (i.returnee instanceof PropertyObject){\n");
				body.append("   i.returnee = ((PropertyObject)i.returnee).get();\n");
				body.append("  }\n");
				body.append(" }\n");
				break;
			case GT:
				addBinaryOperation("Gt", buffer, body);
				break;
			case IMPORT:
				body.append(" s1 = \"" + ((StringObject) block.getConstant(buffer.getInt())).value + "\"; \n");
				body.append(" s2 = \"" + ((StringObject) block.getConstant(buffer.getInt())).value + "\"; \n");
				body.append(" try { \n");
				body.append("  i.pythonImport(i.environment(), s1, s2, null); \n");
				body.append(" } catch (CastFailedException e1) {\n");
				body.append("  throw new TypeError(\"__all__ must be a list\"); \n");
				body.append(" }\n");
				break;
			case ISINSTANCE:
				body.append(" type = stack.pop(); \n");
				body.append(" value = stack.peek(); \n");
				body.append(" stack.push(BoolObject.fromBoolean(PythonRuntime.doIsInstance(value, type, true))); \n");
				break;
			case KWARG:
				int constLen = buffer.getInt();
				StringBuffer constArray = new StringBuffer();
				constArray.append(" sarray = new String[]{ ");
				for (int i=0; i<constLen; i++){
					constArray.append(block.getConstant(buffer.getInt()).toString() + ", ");
				}
				constArray.append(" }; \n");
				
				body.append(constArray.toString());
				body.append(" jv = " + constLen + "; \n");
				body.append(" doingNew = o.kwargs == null; \n");
				body.append(" if (doingNew) \n");
				body.append("  o.kwargs = new KwArgs.HashMapKWArgs(); \n");
				body.append(" for (int ii = 0; ii < jv; ii++) { \n" );
				body.append("  String key = sarray[ii]; \n");
				body.append("  if (!doingNew) \n");
				body.append("   if (o.kwargs.contains(key))\n");
				body.append("    throw new TypeError(\"got multiple values for keyword argument '\" + key + \"'\"); \n");
				body.append("  o.kwargs.put(key, stack.pop()); \n");
				body.append(" } \n");
				break;
			case LE:
				addBinaryOperation("Le", buffer, body);
				break;
			case LOAD:
				body.append(" svl = \"" + ((StringObject) block.getConstant(buffer.getInt())).value + "\"; \n");
				body.append(" value = i.environment().get(svl, false, false); \n");
				body.append(" if (value == null) \n");
				body.append("  throw new NameError(\"name \" + svl + \" is not defined\"); \n");
				body.append("  if (value instanceof FutureObject) { \n");
				body.append("   PythonObject rvalue = null; \n");
				body.append("   while ((rvalue = ((FutureObject) value).getValue()) == null) ; \n");
				body.append("   value = rvalue; \n");
				body.append("  } \n");
				body.append(" stack.push(value); \n");
				break;
			case LOADBUILTIN:
				body.append(" vname = \"" + ((StringObject) block.getConstant(buffer.getInt())).value + "\"; \n");
				body.append(" value = i.environment().getBuiltin(vname); \n");
				body.append(" if (value == null) \n");
				body.append("  throw new NameError(\"builtin name \" + vname + \" is not defined\"); \n");
				body.append(" stack.push(value); \n");
				break;
			case LOADDYNAMIC:
				body.append(" found = false; \n");
				body.append(" vname = \"" + ((StringObject) block.getConstant(buffer.getInt())).value + "\"; \n");
				body.append(" for (int ii = i.currentFrame.size() - 1; ii >= 0; ii--) { \n");
				body.append("  FrameObject oo = i.currentFrame.get(ii); \n");
				body.append("  InternalDict locals = oo.environment.getLocals(); \n");
				body.append("  if (locals.containsVariable(vname)) { \n");
				body.append("   stack.push(locals.getVariable(vname)); \n");
				body.append("   found = true; \n");
				body.append("   break; \n");
				body.append("  } \n");
				body.append(" }\n");
				body.append(" if (!found) \n");
				body.append("  throw new NameError(\"dynamic variable \" + vname + \" is not defined\"); \n");
				break;
			case LOADGLOBAL:
				body.append(" svl = \"" + ((StringObject) block.getConstant(buffer.getInt())).value + "\"; \n");
				body.append(" value = i.environment().get(svl, true, false); \n");
				body.append(" if (value == null) \n");
				body.append("  throw new NameError(\"name \" + svl + \" is not defined\"); \n");
				body.append("  if (value instanceof FutureObject) { \n");
				body.append("   PythonObject rvalue = null; \n");
				body.append("   while ((rvalue = ((FutureObject) value).getValue()) == null) ; \n");
				body.append("   value = rvalue; \n");
				body.append("  } \n");
				body.append(" stack.push(value); \n");
				break;
			case LOAD_FUTURE:
				body.append(" svl = \"" + ((StringObject) block.getConstant(buffer.getInt())).value + "\"; \n");
				body.append(" value = i.environment().get(svl, false, false); \n");
				body.append(" if (value == null) \n");
				body.append("  throw new NameError(\"name \" + svl + \" is not defined\"); \n");
				body.append("  if (value instanceof FutureObject) { \n");
				body.append("   if (((FutureObject) value).isReady()) {\n");
				body.append("    PythonObject rvalue = null; \n");
				body.append("    while ((rvalue = ((FutureObject) value).getValue()) == null) ; \n");
				body.append("    value = rvalue; \n");
				body.append("   } \n");
				body.append("  }\n");
				body.append(" stack.push(value); \n");
				break;
			case LSHIFT:
				addBinaryOperation("Lshift", buffer, body);
				break;
			case LT:
				addBinaryOperation("Lt", buffer, body);
				break;
			case MAKE_FIRST:
				body.append(" nth = " + buffer.getInt() + "; \n");
				body.append(" rest = new ArrayList<PythonObject>(); \n");
				body.append(" for (int ii = 0; ii < nth; ii++) \n");
				body.append("  rest.add(stack.pop()); \n");
				body.append(" value = stack.pop(); \n");
				body.append(" Collections.reverse(rest); \n");
				body.append(" for (PythonObject datum : rest) \n");
				body.append("  stack.push(datum); \n");
				body.append(" stack.push(newHead); \n");
				break;
			case MAKE_FUTURE:
				constLen = buffer.getInt();
				
				body.append(" closureCopy = new ArrayList<String>(); \n");
				body.append(" for (int ii = 0; ii < jv; i++) {\n");
				for (int i=0; i<constLen; i++){
					body.append(" closureCopy.add(\"" + block.getConstant(buffer.getInt()).toString() + "); \n");
				}
				body.append(" stack.push(new PythonFutureObject((UserFunctionObject) stack.pop(), closureCopy, i.environment())); \n");
				break;
			case MOD:
				addBinaryOperation("Mod", buffer, body);
				break;
			case MUL:
				addBinaryOperation("Mul", buffer, body);
				break;
			case NE:
				addBinaryOperation("Ne", buffer, body);
				break;
			case OPEN_LOCALS:
				body.append(" i.currentFrame.getLast().environment.pushLocals(new StringDictObject()); \n");
				break;
			case OR:
				addBinaryOperation("Or", buffer, body);
				break;
			case POP:
				body.append(" stack.pop(); \n");
				break;
			case POW:
				addBinaryOperation("Pow", buffer, body);
				break;
			case PUSH:
				body.append(" stack.push(o.compiled.getConstant(" + buffer.getInt() + ")); \n");
				break;
			case PUSH_ENVIRONMENT:
				body.append(" o.environment = new EnvironmentObject(); \n");
				body.append(" if (i.currentClosure != null) { \n");
				body.append("  o.environment.add(currentClosure); \n");
				body.append("  currentClosure = null; \n");
				body.append(" } else if (!PythonRuntime.runtime.buildingGlobals()) \n");
				body.append("  o.environment.add(PythonRuntime.runtime.getGlobals()); \n");
				break;
			case PUSH_EXCEPTION:
				body.append(" frame = (FrameObject) stack.peek(); \n");
				body.append(" if (frame.exception == null) \n");
				body.append("  stack.push(NoneObject.NONE); \n");
				body.append(" else \n");
				body.append("  stack.push(frame.exception); \n");
				break;
			case PUSH_LOCALS:
				body.append(" stack.push((PythonObject) i.currentFrame.getLast().environment.getLocals()); \n");
				break;
			case PUSH_LOCAL_CONTEXT:
				body.append(" o.localContext = stack.pop(); \n");
				break;
			case QM:
				addBinaryOperation("Qm", buffer, body);
				break;
			case RARROW:
				addBinaryOperation("Rarrow", buffer, body);
				break;
			case RERAISE:
				body.append(" value = stack.pop(); \n");
				body.append(" if (value != NoneObject.NONE) {\n");
				body.append("  PythonExecutionException pee = new PythonExecutionException(value);\n");
				body.append("  pee.noStackGeneration(true); \n");
				body.append("  throw pee; \n");
				body.append(" }\n");
				break;
			case RESOLVE_ARGS:
				body.append(" synchronized (i.args) {\n");
				body.append("  for (String key : i.args.keySet()) \n");
				body.append("   i.environment().getLocals().putVariable(key, i.args.getVariable(key)); \n");
				body.append(" }\n");
				break;
			case RESOLVE_CLOSURE:
				body.append(" ((UserFunctionObject)stack.peek()).setClosure(environment().toClosure());\n");
				break;
			case RSHIFT:
				addBinaryOperation("Rshift", buffer, body);
				break;
			case SAVE:
				body.append(" svl = \"" + ((StringObject) block.getConstant(buffer.getInt())).value + "\"; \n");
				body.append(" value = stack.pop(); \n");
				body.append(" i.environment().set(ss, v, false, false); \n");
				break;
			case SAVEDYNAMIC:
				body.append(" found = false; \n");
				body.append(" value = stack.pop(); \n");
				body.append(" vname = \"" + ((StringObject) block.getConstant(buffer.getInt())).value + "\"; \n");
				body.append(" for (int ii = i.currentFrame.size() - 1; ii >= 0; ii--) { \n");
				body.append("  FrameObject oo = i.currentFrame.get(ii); \n");
				body.append("  InternalDict locals = oo.environment.getLocals(); \n");
				body.append("  if (locals.containsVariable(vname)) { \n");
				body.append("   locals.putVariable(vname, value); \n");
				body.append("   found = true; \n");
				body.append("   break; \n");
				body.append("  } \n");
				body.append(" }\n");
				body.append(" if (!found) \n");
				body.append("  o.environment.getLocals().putVariable(vname, value); \n");
				break;
			case SAVEGLOBAL:
				body.append(" svl = \"" + ((StringObject) block.getConstant(buffer.getInt())).value + "\"; \n");
				body.append(" value = stack.pop(); \n");
				body.append(" i.environment().set(ss, v, true, false); \n");
				break;
			case SAVE_LOCAL:
				body.append(" svl = \"" + ((StringObject) block.getConstant(buffer.getInt())).value + "\"; \n");
				body.append(" value = stack.pop(); \n");
				body.append(" i.environment().getLocals().putVariable(ss, v); \n");
				break;
			case SETATTR:
				body.append(" runnable = environment().getBuiltin(\"setattr\"); \n");
				body.append(" args = new PythonObject[3]; \n");
				body.append(" value = o.compiled.getConstant(" + buffer.getInt() + "); \n");
				body.append(" if (po == NoneObject.NONE) { \n");
				body.append("  args[1] = stack.pop(); args[0] = stack.pop(); args[2] = stack.pop(); \n");
				body.append(" } else { \n");
				body.append("  args[1] = po; args[0] = stack.pop(); args[2] = stack.pop(); \n ");
				body.append(" } \n");
				body.append(" i.returnee = i.execute(true, runnable, null, args); \n");
				break;
			case SUB:
				addBinaryOperation("Sub", buffer, body);
				break;
			case SWAP_STACK:
				body.append(" a = stack.pop(); \n");
				body.append(" b = stack.pop(); \n");
				body.append(" stack.push(a); stack.push(b); \n");
				break;
			case TEST_FUTURE:
				body.append(" svl = \"" + ((StringObject) block.getConstant(buffer.getInt())).value + "\"; \n");
				body.append(" value = i.environment().get(svl, false, false); \n");
				body.append(" if (value == null) \n");
				body.append("  throw new NameError(\"name \" + svl + \" is not defined\"); \n");
				body.append("  if (value instanceof FutureObject) { \n");
				body.append("   stack.push(BoolObject.fromBoolean(((FutureObject) value).isReady()));\n");
				body.append("  } else { \n");
				body.append("   stack.push(BoolObject.TRUE);\n");
				body.append("  }\n");
				break;
			case XOR:
				addBinaryOperation("Xor", buffer, body);
				break;

			case TRUTH_VALUE:
			case UNPACK_KWARG:
			case UNPACK_SEQUENCE:
			case SETUP_LOOP:
			case ACCEPT_ITER:
			case GET_ITER:
			case GOTO:
			case JUMPIFFALSE:
			case JUMPIFNONE:
			case JUMPIFNORETURN:
			case JUMPIFTRUE:
			case CALL:
			case RETURN:
			case KCALL:
			case RCALL:
			case YIELD:
			case PUSH_FRAME:
			case RAISE:
				buffer.position(buffer.position()-1);
				break outer;			
			}
			
			if (itc > 15){
				// 15 bytecodes created, continue in next func
				hasReturn = true;

				String builtMethod = buildMethod(block, buffer.position(), debug, jitted, fncord+1);
				CtMethod method = CtNewMethod.make(builtMethod, jitted);
				jitted.addMethod(method);
				
				body.append(" // continue execution in another method due to method size constraint \n");
				body.append(String.format(" return execute_%s(i, o, stack, cb, debugger); \n", fncord+1));
				break;
			}
		}
		
		if (!hasReturn)
			addReturnProtocol(buffer.position(), body, ExecutionResult.OK);
		
		return body.toString();
	}

	private void addBinaryOperation(String op, ByteBuffer buffer,
			StringBuilder body) {
		body.append(" b = stack.pop();");
		body.append(" a = stack.pop();");
		body.append(" if (!(a instanceof ClassInstanceObject) && (a instanceof Has" + op + "Method))\n");
		body.append("  stack.push(((Has" + op + "Method)a).add(b));\n");
		body.append(" else {");
		body.append(mathStandard(buffer, op.toLowerCase()));
		body.append(" }\n");
	}

	private String mathStandard(ByteBuffer buffer, String m) {
		StringBuffer body = new StringBuffer();
		body.append("  try {\n");
		body.append("   PythonObject apo;\n");
		body.append("   StringObject field = new StringObject(__" + m + "__);\n");
		body.append("   PythonObject value = a;	// object to get attribute from\n");
		body.append("   apo = value.get(\"__getattribute__\", i.getLocalContext()); \n");
		body.append("   if (apo != null && !(value instanceof ClassObject)) {\n");
		body.append("    // There is __getattribute__ defined, call it directly\n");
		body.append("    method = i.execute(true, apo, null, field);\n");
		body.append("   } else {\n");
		body.append("    // Try to grab argument normally...\n");
		body.append("    apo = value.get(field.value, i.getLocalContext());\n");
		body.append("    if (apo != null) {\n");
		body.append("     method = apo;\n");
		body.append("    } else {\n");
		body.append("     // ... and if that fails, use __getattr__ if available\n");
		body.append("     apo = value.get(\"__getattr__\", i.getLocalContext()); \n");
		body.append("     if (apo != null) {\n");
		body.append("      // There is __getattribute__ defined, call it directly\n");
		body.append("      method = i.execute(true, apo, null, field);\n");
		body.append("     } else\n");
		body.append("      throw new AttributeError(\"\" + value.getType() + \" object has no attribute '\" + field + \"'\"); \n");
		body.append("    }\n");
		body.append("   }\n");
		body.append("   } finally {\n");
		body.append("    if (method instanceof PropertyObject){\n");
		body.append("     method = ((PropertyObject)method).get();\n");
		body.append("    }\n");
		body.append("   }\n");
		body.append("   i.returnee = i.execute(true, method, null, b);\n");
		body.append("   o.accepts_return = true;\n");
		return body.toString();
	}

	private void addReturnProtocol(int position, StringBuilder body, ExecutionResult result) {
		// finalize this by setting pc of the passed frame object to current pc of the byte buffer and then return
		// appropriate execution result
		body.append(" o.pc = " + position + ";\n");
		body.append(" return ExecutionResult." + result.toString() + "; \n");
	}
	
}
