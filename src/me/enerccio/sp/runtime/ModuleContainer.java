package me.enerccio.sp.runtime;

import java.util.Map;
import java.util.TreeMap;

import me.enerccio.sp.types.ModuleObject;

public class ModuleContainer {
	public ModuleObject module;
	public Map<String, ModuleObject> submodules = new TreeMap<String, ModuleObject>();
	public Map<String, ModuleContainer> subpackages = new TreeMap<String, ModuleContainer>();
}
