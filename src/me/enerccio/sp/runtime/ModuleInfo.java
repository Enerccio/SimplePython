package me.enerccio.sp.runtime;

/** Provides debug informations about module */
public interface ModuleInfo {
	/** 
	 * Returns module provider used to load this module. Used only when including source from other module,
	 * so may return null if including is not supported, or includes were already parsed; 
	 */
	ModuleProvider getIncludeProvider();

	/**
	 * Returns module name, including package, if possible. Used by trace and dis method.
	 */
	String getName();

	/**
	 * Returns module filename. For modules not generated from file, this may return same as getName()
	 */
	String getFileName();
}
