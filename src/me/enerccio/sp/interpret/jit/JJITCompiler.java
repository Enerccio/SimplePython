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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
	private Map<JitRequest, CompiledPython> finished = new HashMap<JitRequest, CompiledPython>();
	
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
		if (finished.containsKey(rq)){
			rq.result = finished.get(rq);
			rq.hasResult = true;
			return;
		}
		
		lastPC = -1;
		
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
		
		Class<?> cls = cp.toClass(jitted, cl, null);
		
		if (lastPC != -1)
			rq.getBlock().container.enschedule(rq.getBlock(), lastPC, rq.getDebug());
		
		CompiledPython inst = (CompiledPython) cls.newInstance();
		finished.put(rq, inst);
		
		rq.result = inst;
		rq.hasResult = true;
	}
	
	private int lastPC;

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
			cp.importPackage("me.enerccio.sp.types.properties");
			cp.importPackage("me.enerccio.sp.types.sequences");
			cp.importPackage("me.enerccio.sp.types.system");
			cp.importPackage("me.enerccio.sp.types.types");
			cp.importPackage("me.enerccio.sp.utils");
			
			pools.put(cl, cp);
		}
		return pools.get(cl);
	}
	
	private static final String header
		= "public ExecutionResult execute(PythonInterpreter i, FrameObject o, Stack stack, CompiledBlockObject cb, Debugger debugger){\n";
	
	private static final String footer
		= "}\n";
	
	private static final String header_ord
		= "public ExecutionResult execute_%s(PythonInterpreter i, FrameObject o, Stack stack, CompiledBlockObject cb, Debugger debugger){\n";

	
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
	
		ByteBuffer buffer = ByteBuffer.wrap(block.getBytedata());
		buffer.position(pcFrom);
		
		StringBuilder body = new StringBuilder();
		boolean hasReturn = false;
		Set<String> locals = new HashSet<String>();
		
		int itc = 0;
		
		outer:
		while (buffer.hasRemaining()){
			int cpos = buffer.position();
			Bytecode b = Bytecode.fromNumber(buffer.get() & 0xFF);
			
			++itc;
			
			switch (b){
			case NOP:
				header(body, b, block, cpos, debug);
				--itc;
				break;
				

			case D_RETURN:
			case D_STARTFUNC:
				header(body, b, block, cpos, debug);
				--itc;
				buffer.getInt();
				break;
			
			case ADD:
				header(body, b, block, cpos, debug);
				addBinaryOperation("Add", buffer, body, locals);
				break;
			case AND:
				header(body, b, block, cpos, debug);
				addBinaryOperation("And", buffer, body, locals);
				break;
			case DCOLON:
				header(body, b, block, cpos, debug);
				addBinaryOperation("Dcolon", buffer, body, locals);
				break;
			case DEL:
				header(body, b, block, cpos, debug);
				locals.add(" String vname = null; \n");
				locals.add(" boolean isGlobal = false; \n");
				
				body.append(" vname = \"" + ((StringObject) block.getConstant(buffer.getInt())).value + "\"; \n");
				body.append(" isGlobal = " + (buffer.getInt() == 1 ? "true" : "false") + ";\n");
				body.append(" i.environment().delete(vname.value, isGlobal); \n");
				break;
			case DELATTR:
				header(body, b, block, cpos, debug);
				locals.add(" PythonObject runnable = null; \n");
				locals.add(" PythonObject[] args = null; \n");
				
				body.append(" runnable = i.environment().getBuiltin(\"delattr\");\n");
				body.append(" args = new PythonObject[2];\n");
				body.append(" args[1] = o.compiled.getConstant(" + buffer.getInt() + "); // attribute \n");
				body.append(" args[0] = (PythonObject)stack.pop(); \n");
				body.append(" i.returnee = i.execute(true, runnable, null, args); \n");
				break;
			case DIV:
				header(body, b, block, cpos, debug);
				addBinaryOperation("Div", buffer, body, locals);
				break;
			case DUP:
				header(body, b, block, cpos, debug);
				locals.add(" int jv = 0; \n");
				
				body.append(" jv = " + buffer.getInt() + "; \n");
				body.append(" if (jv == 0) \n");
				body.append("  stack.push(stack.peek());\n");
				body.append(" else\n");
				body.append("  stack.push(stack.get(stack.size() - 1 - jv));\n");
				break;
			case ECALL:
				header(body, b, block, cpos, debug);
				locals.add(" PythonObject runnable = null; \n");
				locals.add(" FrameObject frame = null; \n");
				
				body.append(" i.checkOverflow(); \n");
				body.append(" runnable = (PythonObject)stack.peek(); \n");
				body.append(" try {\n");
				body.append("  i.returnee = i.execute(true, runnable, null, new PythonObject[0]);\n");
				body.append("  frame = (FrameObject)i.currentFrame.peekLast(); \n");
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
				header(body, b, block, cpos, debug);
				addBinaryOperation("Eq", buffer, body, locals);
				break;
			case GE:
				header(body, b, block, cpos, debug);
				addBinaryOperation("Ge", buffer, body, locals);
				break;
			case GETATTR:
				header(body, b, block, cpos, debug);
				body.append(" try {\n");
				body.append("  PythonObject apo;\n");
				body.append("  StringObject field = (StringObject) o.compiled.getConstant(" + buffer.getInt() + ");\n");
				body.append("  PythonObject value = (PythonObject)stack.pop(); // object to get attribute from \n");
				body.append("  if (value instanceof FutureObject) { \n");
				body.append("   PythonObject rvalue = null; \n");
				body.append("   while ((rvalue = ((FutureObject) value).getValue()) == null) ; \n");
				body.append("   value = rvalue; \n");
				body.append("  } \n");
				body.append("  apo = value.get(\"__getattribute__\", i.getLocalContext()); \n");
				body.append("  if (apo != null && !(value instanceof ClassObject)) {\n");
				body.append("   // There is __getattribute__ defined, call it directly\n");
				body.append("   i.returnee = i.execute(true, apo, null, new PythonObject[]{field});\n");
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
				body.append("     i.returnee = i.execute(false, apo, null, new PythonObject[]{field});\n");
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
				header(body, b, block, cpos, debug);
				addBinaryOperation("Gt", buffer, body, locals);
				break;
			case IMPORT:
				header(body, b, block, cpos, debug);
				locals.add(" String s1 = null; \n");
				locals.add(" String s2 = null; \n");
				
				body.append(" s1 = \"" + ((StringObject) block.getConstant(buffer.getInt())).value + "\"; \n");
				body.append(" s2 = \"" + ((StringObject) block.getConstant(buffer.getInt())).value + "\"; \n");
				body.append(" try { \n");
				body.append("  i.pythonImport(i.environment(), s1, s2, null); \n");
				body.append(" } catch (CastFailedException e1) {\n");
				body.append("  throw new TypeError(\"__all__ must be a list\"); \n");
				body.append(" }\n");
				break;
			case ISINSTANCE:
				header(body, b, block, cpos, debug);
				locals.add(" PythonObject type = null; \n");
				locals.add(" PythonObject value = null; \n");
				
				body.append(" type = (PythonObject)stack.pop(); \n");
				body.append(" value = (PythonObject)stack.peek(); \n");
				body.append(" stack.push(BoolObject.fromBoolean(PythonRuntime#doIsInstance(value, type, true))); \n");
				break;
			case KWARG:
				header(body, b, block, cpos, debug);
				locals.add(" int jv = 0; \n");
				locals.add(" String[] sarray = null; \n");
				locals.add(" boolean doingNew = false; \n");
				
				int constLen = buffer.getInt();
				body.append(" sarray = new String[" + constLen + "]; \n");
				for (int i=0; i<constLen; i++){
					body.append(" sarray[" + i + "] = \"" + block.getConstant(buffer.getInt()).toString() + "\"; \n");
				}
				body.append(" jv = " + constLen + "; \n");
				body.append(" doingNew = o.kwargs == null; \n");
				body.append(" if (doingNew) \n");
				body.append("  o.kwargs = new KwArgs.HashMapKWArgs(); \n");
				body.append(" for (int ii = 0; ii < jv; ii++) { \n" );
				body.append("  String key = sarray[ii]; \n");
				body.append("  if (!doingNew) \n");
				body.append("   if (o.kwargs.contains(key))\n");
				body.append("    throw new TypeError(\"got multiple values for keyword argument '\" + key + \"'\"); \n");
				body.append("  o.kwargs.put(key, (PythonObject)stack.pop()); \n");
				body.append(" } \n");
				break;
			case LE:
				header(body, b, block, cpos, debug);
				addBinaryOperation("Le", buffer, body, locals);
				break;
			case LOAD:
				header(body, b, block, cpos, debug);
				locals.add(" String svl = null; \n");
				locals.add(" PythonObject value = null; \n");
				
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
				header(body, b, block, cpos, debug);
				locals.add(" String vname = null; \n");
				locals.add(" PythonObject value = null; \n");
				
				body.append(" vname = \"" + ((StringObject) block.getConstant(buffer.getInt())).value + "\"; \n");
				body.append(" value = i.environment().getBuiltin(vname); \n");
				body.append(" if (value == null) \n");
				body.append("  throw new NameError(\"builtin name \" + vname + \" is not defined\"); \n");
				body.append(" stack.push(value); \n");
				break;
			case LOADDYNAMIC:
				header(body, b, block, cpos, debug);
				locals.add(" boolean found = false; \n");
				locals.add(" String vname = null; \n");
				
				body.append(" found = false; \n");
				body.append(" vname = \"" + ((StringObject) block.getConstant(buffer.getInt())).value + "\"; \n");
				body.append(" for (int ii = i.currentFrame.size() - 1; ii >= 0; ii--) { \n");
				body.append("  FrameObject oo = (FrameObject)i.currentFrame.get(ii); \n");
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
				header(body, b, block, cpos, debug);
				locals.add(" String svl = null; \n");
				locals.add(" PythonObject value = null; \n");
				
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
				header(body, b, block, cpos, debug);
				locals.add(" String svl = null; \n");
				locals.add(" PythonObject value = null; \n");
				
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
				header(body, b, block, cpos, debug);
				addBinaryOperation("Lshift", buffer, body, locals);
				break;
			case LT:
				header(body, b, block, cpos, debug);
				addBinaryOperation("Lt", buffer, body, locals);
				break;
			case MAKE_FIRST:
				header(body, b, block, cpos, debug);
				locals.add(" int nth = 0; \n");
				locals.add(" List rest = null; \n");
				locals.add(" PythonObject value = null; \n");
				
				body.append(" nth = " + buffer.getInt() + "; \n");
				body.append(" rest = new ArrayList(); \n");
				body.append(" for (int ii = 0; ii < nth; ii++) \n");
				body.append("  rest.add((PythonObject)stack.pop()); \n");
				body.append(" value = (PythonObject)stack.pop(); \n");
				body.append(" Collections.reverse(rest); \n");
				body.append(" for (Object datum : rest) \n");
				body.append("  stack.push((PythonObject)datum); \n");
				body.append(" stack.push(newHead); \n");
				break;
			case MAKE_FUTURE:	
				header(body, b, block, cpos, debug);
				constLen = buffer.getInt();
				body.append(" closureCopy = new ArrayList(); \n");
				for (int i=0; i<constLen; i++){
					body.append(" closureCopy.add(\"" + block.getConstant(buffer.getInt()).toString() + "); \n");
				}
				body.append(" stack.push(new PythonFutureObject((UserFunctionObject) (PythonObject)stack.pop(), closureCopy, i.environment())); \n");
				break;
			case MOD:
				header(body, b, block, cpos, debug);
				addBinaryOperation("Mod", buffer, body, locals);
				break;
			case MUL:
				header(body, b, block, cpos, debug);
				addBinaryOperation("Mul", buffer, body, locals);
				break;
			case NE:
				header(body, b, block, cpos, debug);
				addBinaryOperation("Ne", buffer, body, locals);
				break;
			case OPEN_LOCALS:
				header(body, b, block, cpos, debug);
				body.append(" ((FrameObject)i.currentFrame.getLast()).environment.pushLocals(new StringDictObject()); \n");
				break;
			case OR:
				header(body, b, block, cpos, debug);
				addBinaryOperation("Or", buffer, body, locals);
				break;
			case POP:
				header(body, b, block, cpos, debug);
				body.append(" stack.pop(); \n");
				break;
			case POW:
				header(body, b, block, cpos, debug);
				addBinaryOperation("Pow", buffer, body, locals);
				break;
			case PUSH:
				header(body, b, block, cpos, debug);
				body.append(" stack.push(o.compiled.getConstant(" + buffer.getInt() + ")); \n");
				break;
			case PUSH_ENVIRONMENT:
				header(body, b, block, cpos, debug);
				body.append(" o.environment = new EnvironmentObject(); \n");
				body.append(" if (i.currentClosure != null) { \n");
				body.append("  o.environment.add(i.currentClosure); \n");
				body.append("  i.currentClosure = null; \n");
				body.append(" } else if (!PythonRuntime#runtime.buildingGlobals()) \n");
				body.append("  o.environment.add(Utils#asList((InternalDict)PythonRuntime#runtime.getGlobals())); \n");
				break;
			case PUSH_EXCEPTION:
				header(body, b, block, cpos, debug);
				locals.add(" FrameObject frame = null; \n");
				
				body.append(" frame = (FrameObject) stack.peek(); \n");
				body.append(" if (frame.exception == null) \n");
				body.append("  stack.push(NoneObject#NONE); \n");
				body.append(" else \n");
				body.append("  stack.push(frame.exception); \n");
				break;
			case PUSH_LOCALS:
				header(body, b, block, cpos, debug);
				body.append(" stack.push((PythonObject) ((FrameObject)i.currentFrame.getLast()).environment.getLocals()); \n");
				break;
			case PUSH_LOCAL_CONTEXT:
				header(body, b, block, cpos, debug);
				body.append(" o.localContext = (PythonObject)stack.pop(); \n");
				break;
			case QM:
				header(body, b, block, cpos, debug);
				addBinaryOperation("Qm", buffer, body, locals);
				break;
			case RARROW:
				header(body, b, block, cpos, debug);
				addBinaryOperation("Rarrow", buffer, body, locals);
				break;
			case RERAISE:
				header(body, b, block, cpos, debug);
				locals.add(" PythonObject value = null; \n");
				
				body.append(" value = (PythonObject)stack.pop(); \n");
				body.append(" if (value != NoneObject#NONE) {\n");
				body.append("  PythonExecutionException pee = new PythonExecutionException(value);\n");
				body.append("  pee.noStackGeneration(true); \n");
				body.append("  throw pee; \n");
				body.append(" }\n");
				break;
			case RESOLVE_ARGS:
				header(body, b, block, cpos, debug);
				locals.add(" Iterator iter = null; \n");
				
				body.append(" synchronized (i.args) {\n");
				body.append("   iter = i.args.keySet().iterator(); \n");
				body.append("   while (iter.hasNext()){ String v = (String)iter.next(); \n");
				body.append("   i.environment().getLocals().putVariable(v, (PythonObject)i.args.getVariable(v)); } \n");
				body.append(" }\n");
				break;
			case RESOLVE_CLOSURE:
				header(body, b, block, cpos, debug);
				body.append(" ((UserFunctionObject)stack.peek()).setClosure(i.environment().toClosure());\n");
				break;
			case RSHIFT:
				header(body, b, block, cpos, debug);
				addBinaryOperation("Rshift", buffer, body, locals);
				break;
			case SAVE:
				header(body, b, block, cpos, debug);
				locals.add(" String svl = null; \n");
				locals.add(" PythonObject value = null; \n");
				
				body.append(" svl = \"" + ((StringObject) block.getConstant(buffer.getInt())).value + "\"; \n");
				body.append(" value = (PythonObject)stack.pop(); \n");
				body.append(" i.environment().set(svl, value, false, false); \n");
				break;
			case SAVEDYNAMIC:
				header(body, b, block, cpos, debug);
				locals.add(" boolean found = false; \n");
				locals.add(" PythonObject value = null; \n");
				locals.add(" String vname = null; \n");
				
				body.append(" found = false; \n");
				body.append(" value = (PythonObject)stack.pop(); \n");
				body.append(" vname = \"" + ((StringObject) block.getConstant(buffer.getInt())).value + "\"; \n");
				body.append(" for (int ii = i.currentFrame.size() - 1; ii >= 0; ii--) { \n");
				body.append("  FrameObject oo = (FrameObject)i.currentFrame.get(ii); \n");
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
				header(body, b, block, cpos, debug);
				locals.add(" String svl = null; \n");
				locals.add(" PythonObject value = null; \n");
				
				body.append(" svl = \"" + ((StringObject) block.getConstant(buffer.getInt())).value + "\"; \n");
				body.append(" value = (PythonObject)stack.pop(); \n");
				body.append(" i.environment().set(svl, value, true, false); \n");
				break;
			case SAVE_LOCAL:
				header(body, b, block, cpos, debug);
				locals.add(" String svl = null; \n");
				locals.add(" PythonObject value = null; \n");
				
				body.append(" svl = \"" + ((StringObject) block.getConstant(buffer.getInt())).value + "\"; \n");
				body.append(" value = (PythonObject)stack.pop(); \n");
				body.append(" i.environment().getLocals().putVariable(svl, value); \n");
				break;
			case SETATTR:
				header(body, b, block, cpos, debug);
				locals.add(" PythonObject runnable = null; \n");
				locals.add(" PythonObject[] args = null; \n");
				locals.add(" PythonObject value = null; \n");
				
				body.append(" runnable = i.environment().getBuiltin(\"setattr\"); \n");
				body.append(" args = new PythonObject[3]; \n");
				body.append(" value = o.compiled.getConstant(" + buffer.getInt() + "); \n");
				body.append(" if (value == NoneObject#NONE) { \n");
				body.append("  args[1] = (PythonObject)stack.pop(); args[0] = (PythonObject)stack.pop(); args[2] = (PythonObject)stack.pop(); \n");
				body.append(" } else { \n");
				body.append("  args[1] = value; args[0] = (PythonObject)stack.pop(); args[2] = (PythonObject)stack.pop(); \n ");
				body.append(" } \n");
				body.append(" i.returnee = i.execute(true, runnable, null, args); \n");
				break;
			case SUB:
				header(body, b, block, cpos, debug);
				addBinaryOperation("Sub", buffer, body, locals);
				break;
			case SWAP_STACK:
				header(body, b, block, cpos, debug);
				locals.add(" PythonObject a = null; \n");
				locals.add(" PythonObject b = null; \n");
				
				body.append(" a = (PythonObject)stack.pop(); \n");
				body.append(" b = (PythonObject)stack.pop(); \n");
				body.append(" stack.push(a); stack.push(b); \n");
				break;
			case TEST_FUTURE:
				header(body, b, block, cpos, debug);
				locals.add(" String svl = null; \n");
				locals.add(" PythonObject value = null; \n");
				
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
				header(body, b, block, cpos, debug);
				addBinaryOperation("Xor", buffer, body, locals);
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
				lastPC = buffer.position();
				break outer;			
			}
			
			if (itc > 25){
				// 15 bytecodes created, continue in next func
				hasReturn = true;

				String builtMethod = buildMethod(block, buffer.position(), debug, jitted, fncord+1);
				CtMethod method = CtNewMethod.make(builtMethod, jitted);
				jitted.addMethod(method);
				
				body.append("\n");
				body.append(" // continue execution in another method due to method size constraint \n");
				body.append(String.format(" return execute_%s(i, o, stack, cb, debugger); \n", fncord+1));
				break;
			}
		}
		
		if (!hasReturn)
			addReturnProtocol(buffer.position(), body, ExecutionResult.OK);
		
		String mainBody = body.toString();
		
		StringBuilder withLocals = new StringBuilder();
		
		withLocals.append(" // locals \n");
		for (String local : locals)
			withLocals.append(local);
		
		
		withLocals.append("\n // main body \n");
		withLocals.append(mainBody);
		
		return withLocals.toString();
	}

	private void header(StringBuilder body, Bytecode b, CompiledBlockObject block, int cpos, boolean debug) {
		body.append(" if (o.accepts_return) { stack.push(i.returnee == null ? NoneObject#NONE : i.returnee); o.accepts_return = false; } \n");

		if (PythonInterpreter.TRACE_ENABLED){
			body.append("if (PythonInterpreter.TRACE_ALL || PythonInterpreter.TRACE_THREADS.contains(currentOwnerThread.getName())) System.err.println(CompiledBlockObject.dis(o.compiled, true, spc) + \" \" + PythonInterpreter.printStack(stack));");
		}
		
		body.append("\n");
		body.append("// " + b.toString() + " in " + block.toString() + ", pc = " + cpos + " \n");
		if (debug){
			body.append(" // debug enabled version \n");
			body.append(" debugger.debugNextOperation(i, Bytecode#" + b.toString() + ", o, " + cpos + "); \n");
		}
	}

	private void addBinaryOperation(String op, ByteBuffer buffer,
			StringBuilder body, Set<String> locals) {
		
		locals.add(" PythonObject method = null; \n");
		locals.add(" PythonObject a = null; \n");
		locals.add(" PythonObject b = null; \n");
		
		body.append(" b = (PythonObject)stack.pop();");
		body.append(" a = (PythonObject)stack.pop();");
		body.append(" if (!(a instanceof ClassInstanceObject) && (a instanceof InterpreterMathExecutorHelper$Has" + op + "Method))\n");
		body.append("  stack.push(((InterpreterMathExecutorHelper$Has" + op + "Method)a)." + op.toLowerCase() + "(b));\n");
		body.append(" else {");
		body.append(mathStandard(buffer, op.toLowerCase()));
		body.append(" }\n");
	}

	private String mathStandard(ByteBuffer buffer, String m) {
		StringBuffer body = new StringBuffer();
		body.append("  try {\n");
		body.append("   PythonObject apo;\n");
		body.append("   StringObject field = new StringObject(\"__" + m + "__\");\n");
		body.append("   PythonObject value = a;	// object to get attribute from\n");
		body.append("   apo = value.get(\"__getattribute__\", i.getLocalContext()); \n");
		body.append("   if (apo != null && !(value instanceof ClassObject)) {\n");
		body.append("    // There is __getattribute__ defined, call it directly\n");
		body.append("    method = i.execute(true, apo, null, new PythonObject[]{field});\n");
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
		body.append("      method = i.execute(true, apo, null, new PythonObject[]{field});\n");
		body.append("     } else\n");
		body.append("      throw new AttributeError(\"\" + value.getType() + \" object has no attribute '\" + field + \"'\"); \n");
		body.append("    }\n");
		body.append("   }\n");
		body.append("   } finally {\n");
		body.append("    if (method instanceof PropertyObject){\n");
		body.append("     method = ((PropertyObject)method).get();\n");
		body.append("    }\n");
		body.append("   }\n");
		body.append("   i.returnee = i.execute(true, method, null, new PythonObject[]{b});\n");
		body.append("   o.accepts_return = true;\n");
		return body.toString();
	}

	private void addReturnProtocol(int position, StringBuilder body, ExecutionResult result) {
		// finalize this by setting pc of the passed frame object to current pc of the byte buffer and then return
		// appropriate execution result
		body.append(" o.pc = " + position + ";\n");
		body.append(" return ExecutionResult#" + result.toString() + "; \n");
	}
	
}
