package me.enerccio.sp.types.sequences;

import me.enerccio.sp.interpret.PythonInterpret;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.IntObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.utils.Utils;

public class OrderedSequenceIterator extends PythonObject {
	private static final long serialVersionUID = 4746975236443204424L;
	public static final String NEXT = "next";
	private SequenceObject sequence;
	private int cp = 0;
	private int len = 0;

	public OrderedSequenceIterator(SequenceObject sequenceObject) {
		this.sequence = sequenceObject;
		this.len = sequence.size().intValue();
	}
	
	@Override
	public void newObject() {
		super.newObject();
		
		try {
			Utils.putPublic(this, SequenceObject.__ITER__, new JavaMethodObject(this, this.getClass().getMethod("__iter__", 
					new Class<?>[]{TupleObject.class}), true));
			Utils.putPublic(this, NEXT, new JavaMethodObject(this, this.getClass().getMethod("next", 
					new Class<?>[]{TupleObject.class}), true));
		} catch (NoSuchMethodException e){
			// will not happen
		}
	}
	
	public PythonObject __iter__(TupleObject args){
		if (args.size().intValue() > 0)
			throw Utils.throwException("TypeError", "__iter__(): method requires no arguments");
		return this;
	}
	
	public PythonObject next(TupleObject args){
		if (args.size().intValue() > 0)
			throw Utils.throwException("TypeError", "next(): method requires no arguments");
		if (cp >= len)
			throw Utils.throwException("StopIteration");
		PythonObject value = PythonInterpret.interpret.get().execute(false, Utils.get(sequence, SequenceObject.__GETITEM__), new IntObject(cp++));
		return value;
	}

	@Override
	public boolean truthValue() {
		return true;
	}

	@Override
	protected String doToString() {
		return "<iterator of " + sequence.toString()  + ">";
	}

}
