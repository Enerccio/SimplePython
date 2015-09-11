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
package me.enerccio.sp.utils;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.Collection;
import java.util.List;
import java.util.RandomAccess;

public class DualSidedArrayList<T> extends AbstractList<T> implements List<T>, Cloneable, Serializable, RandomAccess {
	private static final long serialVersionUID = 4213364887940661171L;
	private int start;
	private int end;
	private int size;
	private Object[] data;
	
	public DualSidedArrayList() {
		this(10);
	}
	
	public DualSidedArrayList(Collection<? extends T> tCollection){
		this();
		for (T col : tCollection){
			add(col);
		}
	}

	public DualSidedArrayList(int i) {
		super();
		
		data = new Object[i*2];
		start = i;
		end = i;
		size = 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T get(int paramInt) {
		checkBounds(paramInt);
		return (T) data[paramInt + start]; 
	}

	private void checkBounds(int paramInt) {
		if (paramInt < 0 || paramInt > size)
			throw new ArrayIndexOutOfBoundsException(paramInt);
	}

	@Override
	public int size() {
		return size;
	}

	public T set(int paramInt, T paramE) {
		++modCount;
		checkBounds(paramInt);
		T value = get(paramInt);
		data[paramInt + start] = paramE;
		return value;
	}

	public void add(int paramInt, T paramE) {
		++modCount;
		checkBounds(paramInt);
		if (paramInt <= size/2)
			addLower(paramInt, paramE);
		else
			addHigher(paramInt, paramE);
	}

	private void addLower(int paramInt, T paramE) {
		ensureCapacityLower();
		if (paramInt == 0){
			data[start-1] = paramE; 
		} else {
			for (int i=start-1; i<start+paramInt-1; i++)
				data[i] = data[i+1];
			data[start+paramInt-1] = paramE;
		}	
		--start;
		++size;
	}

	private void ensureCapacityLower() {
		if (start == 0){
			int len = data.length;
			Object[] newData = new Object[len*2];
			System.arraycopy(data, 0, newData, len, size());
			start = len;
			end += len;
			data = newData;
		}
	}

	private void addHigher(int paramInt, T paramE) {
		ensureCapacityHigher();
		if (paramInt == size){
			data[end] = paramE;
		} else {
			for (int i=end; i>=start+paramInt+1; i--)
				data[i] = data[i-1];
			data[start+paramInt] = paramE;
		}
		++end;
		++size;
	}

	private void ensureCapacityHigher() {
		if (end == data.length){
			int len = data.length;
			Object[] newData = new Object[len*2];
			System.arraycopy(data, start, newData, start, size());
			data = newData;
		}
	}

	public T remove(int paramInt) {
		checkBounds(paramInt);
		++modCount;
		if (paramInt <= size/2)
			return removeLower(paramInt);
		else
			return removeHigher(paramInt);
	}

	@SuppressWarnings("unchecked")
	private T removeLower(int paramInt) {
		T retVal;
		if (paramInt == 0){
			retVal = (T)data[start];
		} else {
			retVal = (T)data[start+paramInt];
			for (int i=start+paramInt; i>start; i--)
				data[i] = data[i-1];
		}
		++start;
		--size;
		return retVal;
	}

	@SuppressWarnings("unchecked")
	private T removeHigher(int paramInt) {
		T retVal;
		if (paramInt == size-1){
			retVal = (T)data[end-1];
		} else {
			retVal = (T)data[start+paramInt];
			for (int i=start+paramInt; i<end-1; i++)
				data[i] = data[i+1];
		}
		--size;
		--end;
		return retVal;
	}
	
	public static void main(String[] ignore){
		List<Integer> ll = new DualSidedArrayList<>(3);
		
		ll.add(5);
		ll.add(6);
		ll.add(7);
		ll.add(0, 1);
		ll.add(0, 2);
		ll.add(0, 3);
		ll.add(1, 4);
		ll.add(5, 9);
		ll.remove(0);
		ll.remove(ll.size()-1);
		ll.remove(1);
		ll.remove(ll.size()-2);
	}
}
