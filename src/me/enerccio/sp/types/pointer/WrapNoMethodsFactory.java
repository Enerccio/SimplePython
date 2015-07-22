package me.enerccio.sp.types.pointer;

public class WrapNoMethodsFactory implements PointerFactory {
	private static final long serialVersionUID = -4372859400649832530L;

	@Override
	public PointerObject doInitialize(Object instance) {
		return new PointerObject(instance);
	}

}
