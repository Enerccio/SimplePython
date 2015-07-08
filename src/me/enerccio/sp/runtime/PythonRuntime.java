package me.enerccio.sp.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import me.enerccio.sp.interpret.PythonDataSourceResolver;
import me.enerccio.sp.interpret.PythonInterpret;
import me.enerccio.sp.types.ModuleObject;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.ClassInstanceObject;
import me.enerccio.sp.types.mappings.MapObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.types.BytecodeTypeObject;
import me.enerccio.sp.types.types.IntTypeObject;
import me.enerccio.sp.types.types.StringTypeObject;
import me.enerccio.sp.types.types.TypeTypeObject;
import me.enerccio.sp.utils.Utils;

public class PythonRuntime {
	
	public static final PythonRuntime runtime = new PythonRuntime();
	public static final String GETATTR = "getattr";
	
	private PythonRuntime(){
		
	}
	
	public Map<String, ModuleObject> root = Collections.synchronizedMap(new HashMap<String, ModuleObject>());
	private Map<Long, PythonObject> instances1 = new HashMap<Long, PythonObject>();
	private Map<PythonObject, Long> instances2 = new HashMap<PythonObject, Long>();
	private List<PythonDataSourceResolver> resolvers = new ArrayList<PythonDataSourceResolver>();
	
	private long key = Long.MIN_VALUE;
	
	private CyclicBarrier awaitBarrierEntry;
	private CyclicBarrier awaitBarrierExit;
	private volatile boolean isSaving = false;
	private volatile boolean allowedNewInterpret = true;
	
	public void waitForNewInterpretAvailability() throws InterruptedException{
		if (!allowedNewInterpret)
			Thread.sleep(10);
	}
	
	public void waitIfSaving(PythonInterpret i) throws InterruptedException {
		if (!isSaving)
			return;
		if (!i.isInterpretStoppable())
			return; // continue working
		try {
			awaitBarrierEntry.await();
			awaitBarrierExit.await();
		} catch (BrokenBarrierException e) {
			throw new InterruptedException(e.getMessage());
		}
	}
	
	public synchronized String serializeRuntime() throws Exception{
		allowedNewInterpret = false;
		int numInterprets = PythonInterpret.interprets.size();
		awaitBarrierEntry = new CyclicBarrier(numInterprets + 1); // include self
		awaitBarrierExit = new CyclicBarrier(numInterprets + 1); // include self
		isSaving = true;
		
		awaitBarrierEntry.await();
		String content = doSerializeRuntime();
		awaitBarrierExit.await();
		
		return content;
	}
	
	private String doSerializeRuntime() {
		// TODO Auto-generated method stub
		return null;
	}

	public synchronized void addResolver(PythonDataSourceResolver resolver){
		resolvers.add(resolver);
	}
	
	public synchronized void newInstanceInitialization(PythonObject o){
		instances2.put(o, key);
		instances1.put(key++, o);
	}
	
	public synchronized long getInstanceId(PythonObject o){
		return instances2.get(o);
	}
	
	public synchronized ModuleObject getRoot(String key) {
		if (!root.containsKey(key)){
			root.put(key, getModule(key, null));
		}
		return root.get(key);
	}
	
	private ModuleObject loadModule(ModuleProvider provider){
		MapObject globals = generateGlobals();
		ModuleObject mo = new ModuleObject(globals, provider);
		return mo;
	}
	
	public synchronized ModuleObject getModule(String name, StringObject moduleResolvePath){
		if (moduleResolvePath == null)
			moduleResolvePath = new StringObject("");
		ModuleObject mo = resolveModule(name, moduleResolvePath);
		if (mo == null)
			throw Utils.throwException("ImportError", "Unknown module '" + name + "' with resolve path '" + moduleResolvePath.value + "'");
		String pp = moduleResolvePath.value;
		mo.newObject();
		if (pp.equals(""))
			root.put(name, mo);
		mo.initModule();
		return mo;
	}

	private ModuleObject resolveModule(String name,
			StringObject moduleResolvePath) {
		ModuleProvider provider = null;
		for (PythonDataSourceResolver resolver : resolvers){
			provider = resolver.resolve(name, moduleResolvePath.value);
		}
		if (provider != null)
			return loadModule(provider);
		return null;
	}

	public MapObject generateGlobals() {
		MapObject globals = new MapObject();
		globals.newObject();
		
		globals.put(GETATTR, Utils.staticMethodCall(PythonRuntime.class, GETATTR, PythonObject.class, String.class));
		globals.put(TypeTypeObject.TYPE_CALL, new TypeTypeObject());
		globals.put(StringTypeObject.STRING_CALL, new StringTypeObject());
		globals.put(IntTypeObject.INT_CALL, new IntTypeObject());
		globals.put(BytecodeTypeObject.BYTECODE_CALL, new BytecodeTypeObject());
		
		return globals;
	}
	
	public static PythonObject getattr(PythonObject o, String attribute){
		PythonObject value = o.get(attribute, PythonInterpret.interpret.get().getLocalContext());
		if (value == null){
			PythonObject getattr = getattr(o, ClassInstanceObject.__GETATTR__);
			if (getattr == null)
				throw Utils.throwException("AttributeError", "Unknown attribute " + attribute);
			value = PythonInterpret.interpret.get().execute(getattr, new StringObject(attribute));
		}
		return value;
	}

	public PythonObject runtimeWrapper() {
		// TODO Auto-generated method stub
		return null;
	}
}
