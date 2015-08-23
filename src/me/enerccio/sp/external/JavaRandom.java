/*
 * SimplePython - embeddable python interpret in java
 * Copyright (c) Peter Vanusanik, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package me.enerccio.sp.external;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;

import me.enerccio.sp.errors.TypeError;
import me.enerccio.sp.errors.ValueError;
import me.enerccio.sp.interpret.KwArgs;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.callables.JavaMethodObject.SpyDoc;
import me.enerccio.sp.types.pointer.WrapAnnotationFactory.WrapMethod;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.CastFailedException;
import me.enerccio.sp.utils.Coerce;

public class JavaRandom {
	private Random rnd = null;

	public JavaRandom() {
		rnd = new Random();
	}
	
	@WrapMethod
	@SpyDoc("Get the next random number in the range [0.0, 1.0).")
	public float random() {
		return rnd.nextFloat();
	}
	
	@WrapMethod
	public void seed(TupleObject args, KwArgs kw) {
		kw.notExpectingKWArgs();
		if (args.len() > 1)
			throw new TypeError("seed() takes at most 1 arguments");
		if (args.len() == 1) {
			if (args.get(0) != NoneObject.NONE) {
				// TODO: Is this good way to get hashCode?
				rnd = new java.util.Random(args.get(0).hashCode());
				return;
			}
		}
		rnd = new java.util.Random();
	}
	
	@WrapMethod
	@SpyDoc("Return internal state; can be passed to setstate() later.")
	public PythonObject getstate() {
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(bs);
			oos.writeObject(rnd);
			oos.close();
		} catch (IOException e) {
			throw new RuntimeException("Impossible happened", e);
		}
		return Coerce.toPython(bs.toByteArray());
	}

	@WrapMethod
	@SpyDoc("Restore internal state from object returned by getstate().")
	public void setstate(PythonObject p) {
		byte[] d;
		try {
			d = Coerce.toJava(p, byte[].class);
		} catch (CastFailedException e) {
			throw new TypeError("Passed value is not saved state");
		}
		try {
			ByteArrayInputStream bs = new ByteArrayInputStream(d); 
			ObjectInputStream ois = new ObjectInputStream(bs);
			rnd = (java.util.Random)ois.readObject();
		} catch (ClassNotFoundException|ClassCastException e) {
			throw new TypeError("Invalid saved state");
		} catch (IOException e) {
			throw new RuntimeException("Impossible happened", e);
		}
	}
	
	@WrapMethod
	@SpyDoc("Choose a random item from range(start, stop[, step])")
	public int randrange(TupleObject args, KwArgs kw) {
		int start = 0;
		int stop = 0;
		int step = 1;
		kw.notExpectingKWArgs();
		if (args.len() > 1)
			throw new TypeError("randrange() takes 1 to 3 arguments");
		try {
			if (args.len() == 1) {
				// Only one argument - range end
					stop = Coerce.toJava(args.get(0), int.class);
			} else if (args.len() == 2) {
				// start - end
				start = Coerce.toJava(args.get(0), int.class);
				stop = Coerce.toJava(args.get(1), int.class);
			} else {
				// start - end, step
				start = Coerce.toJava(args.get(0), int.class);
				stop = Coerce.toJava(args.get(1), int.class);
				step = Coerce.toJava(args.get(2), int.class);
			}
		} catch (CastFailedException e) {
			throw new ValueError("non-integer arg for randrange()");
		}
		int width = stop - start;
		if ((step == 1) && (width > 0))
            return start + rnd.nextInt(width);
		
        if (step == 1)
            throw new ValueError("empty range for randrange()");
        
        int n = 0;
        if (step > 0)
            n = (width + step - 1);
        else if (step < 0)
            n = (width + step + 1);
        else
        	throw new ValueError("zero step for randrange()");

        if (n <= 0)
        	throw new ValueError("empty range for randrange()");

        return start + step * rnd.nextInt(n);
	}
	
	@WrapMethod
	@SpyDoc("getrandbits(k) -> x.  Generates a long int with k random bits.")
    public long getrandbits(int k) {
        if (k <= 0)
            throw new ValueError("number of bits must be greater than zero");
        long x = 0;
        while (k > 0) {
            x = x << 1;
            x += randint(0, 1);
            k --;
        }
        return x;
	}

	@WrapMethod
	@SpyDoc("Return random integer in range [a, b], including both end points.")
    public int randint(int a, int b) {
		int width = b - a;
		if (width > 0)
			return a + rnd.nextInt(width + 1);
		throw new ValueError("empty range for randint()");
	}

	@WrapMethod
	protected String __str__() {
		return String.format("<random object at 0x%h>", this.hashCode());
	}
	
}
