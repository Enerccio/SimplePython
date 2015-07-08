package me.enerccio.sp.interpret;

import me.enerccio.sp.runtime.ModuleProvider;

public interface PythonDataSourceResolver {

	ModuleProvider resolve(String name, String resolvePath);
	
}
