package me.enerccio.sp.runtime;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import me.enerccio.sp.types.pointer.PointerFactory;

public class FactoryResolver {
	
	private class FactoryPath {
		private PointerFactory f;
		private Map<String, FactoryPath> search = new TreeMap<String, FactoryPath>();
	}
	
	private FactoryPath root;
	
	public synchronized void set(PointerFactory f, String path){
		if (path.equals("")){
			root = new FactoryPath();
			root.f = f;
		} else {
			String[] split = path.split("\\.");
			FactoryPath r = root;
			int i = 0;
			for (String key : split){
				FactoryPath newf;
				if (i == split.length-1){
					newf = new FactoryPath();
					newf.f = f;
					r.search.put(key, newf);
					return;
				}
				++i;
				if (r.search.containsKey(key)){
					r = r.search.get(key);
				} else {
					newf = new FactoryPath();
					newf.f = r.f;
					r.search.put(key, newf);
					r = newf;
				}
			}
		}
	}

	public synchronized PointerFactory get(List<String> c) {
		FactoryPath lastf = null;
		for (String k : c){
			if (k.equals("")){
				lastf = root;
			} else {
				if (lastf.search.containsKey(k)){
					lastf = lastf.search.get(k);
				} else {
					break;
				}
			}
		}
		if (lastf == null)
			return null;
		return lastf.f;
	}
}
