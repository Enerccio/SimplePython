package me.enerccio.sp.types.pointer;

import java.io.Serializable;

public interface PointerFactory extends Serializable {
	
	PointerObject doInitialize(Object instance);
	
}
