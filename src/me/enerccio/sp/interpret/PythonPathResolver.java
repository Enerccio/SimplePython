package me.enerccio.sp.interpret;

import java.io.File;
import java.io.FileInputStream;

import me.enerccio.sp.runtime.ModuleProvider;
import me.enerccio.sp.utils.Utils;

public class PythonPathResolver implements PythonDataSourceResolver {

	private PythonPathResolver(){
		
	}
	
	private File rootPath;
	
	@Override
	public ModuleProvider resolve(String name, String resolvePath) {
		String pp = resolvePath.replace(".", File.pathSeparator);
		File path = new File(new File(rootPath, pp), name + ".spy");
		if (!path.exists())
			path = new File(new File(rootPath, pp), name);
		if (path.exists()){
			if (path.isDirectory()){
				File init = new File(path, "__init__.spy");
				if (init.exists() && !init.isDirectory()){
					try {
						String fname = path.getName();
						return doResolve(init, fname, name);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else {
				try {
					String fname = path.getName();
					fname.replace(".spy", "");
					return doResolve(path, fname, name);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	private ModuleProvider doResolve(File path, String name, String mname) throws Exception {
		return new ModuleProvider(mname, path.getName(), 
				Utils.toByteArray(new FileInputStream(path)), 
				path.getParentFile().equals(rootPath) ? "" : path.getParentFile().getName());
	}

	public static PythonPathResolver make(String string) {
		PythonPathResolver p = new PythonPathResolver();
		
		p.rootPath = new File(string);
		
		return p;
	}

}
