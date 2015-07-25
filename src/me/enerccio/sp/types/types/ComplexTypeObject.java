package me.enerccio.sp.types.types;

import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.ComplexObject;
import me.enerccio.sp.types.base.NumberObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

public class ComplexTypeObject extends TypeObject {
	private static final long serialVersionUID = -6989323431265671329L;
	public static final String COMPLEX_CALL = "complex";

	@Override
	public String getTypeIdentificator() {
		return "complex";
	}

	@Override
	public PythonObject call(TupleObject o) {
		if (o.len() > 2)
			throw Utils.throwException("TypeError", "complex(): requires up to 2 parameters");
		
		double real = 0;
		double imag = 0;
		
		try {
			if (o.len() == 2){
				real = ((NumberObject)o.valueAt(0)).getJavaFloat();
				imag = ((NumberObject)o.valueAt(0)).getJavaFloat();
			} else if (o.len() == 1){
				real = ((NumberObject)o.valueAt(0)).getJavaFloat();
			}
		} catch (ClassCastException e){
			throw Utils.throwException("TypeError", "complex(): parameters must be numbers");
		}
		
		return new ComplexObject(real, imag);
	}
	
	

}
