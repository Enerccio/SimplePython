package me.enerccio.sp.interpret;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.mappings.MapObject;
import me.enerccio.sp.types.sequences.StringObject;

public class EnvironmentObject extends PythonObject {
	private static final long serialVersionUID = -4678903433798210010L;
	private List<MapObject> environments = new ArrayList<MapObject>();
	
	public void add(MapObject... closures){
		environments.addAll(Arrays.asList(closures));
	}
	
	public void add(Collection<MapObject> closures){
		environments.addAll(closures);
	}
	
	public PythonObject get(StringObject key, boolean isGlobal){
		if (isGlobal){
			return environments.get(environments.size()-1).doGet(key);
		}
		
		PythonObject o;
		for (MapObject e : environments){
			o = e.doGet(key);
			if (o != null)
				return o;
		}
		return null;
	}
	
	public PythonObject set(StringObject key, PythonObject value, boolean isGlobal){
		if (isGlobal){
			return environments.get(environments.size()-1).backingMap.put(key, value);
		}
		
		PythonObject o;
		for (MapObject e : environments){
			o = e.doGet(key);
			if (o != null){
				return e.backingMap.put(key, value);
			}
		}
		
		return environments.get(0).backingMap.put(key, value);
	}

	@Override
	public boolean truthValue() {
		return true;
	}

	@Override
	protected String doToString() {
		return "<environment 0x" + Integer.toHexString(hashCode()) + ">"; 
	}
	
}
