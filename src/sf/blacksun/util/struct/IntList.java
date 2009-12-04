package sf.blacksun.util.struct;

/*
 * Copyright (c) 2004, Chris Leung, simontsui. All rights reserved.
 *
 * The software in this package is distributed under the GNU General Public
 * License version 2, as published by the Free Software Foundation, but with
 * the Classpath exception.  You should have received a copy of the GNU General
 * Public License (GPL) and the Classpath exception along with this program.
 */

import java.io.Serializable;

public class IntList implements IIntList, IIntStack, Cloneable, Serializable {

	////////////////////////////////////////////////////////////////////////

	private static final long serialVersionUID = 5681418481728541841L;
	private static final int CAPACITY = 8;

	////////////////////////////////////////////////////////////////////////

	protected int capacity = CAPACITY;
	protected int size = 0;
	protected int[] list;

	////////////////////////////////////////////////////////////////////////

	public IntList() {
		list = new int[capacity];
	}

	public IntList(int cap) {
		capacity = cap;
		list = new int[capacity];
	}

	public IntList(int[] a) {
		size = a.length;
		capacity = size + CAPACITY;
		list = new int[capacity];
		System.arraycopy(a, 0, list, 0, size);
	}

	public IntList(IIntVector a) {
		size = a.size();
		capacity = a.size() + CAPACITY;
		list = new int[capacity];
		a.copyTo(list, 0, 0, size);
	}


	public int get(int index) {
		if (index >= size)
			throw indexSizeException("Expected index < ", index);
		return list[index];
	}

	public int size() {
		return size;
	}


	public void copyTo(int[] dst, int dststart, int srcstart, int srcend) {
		System.arraycopy(list, srcstart, dst, dststart, srcend - srcstart);
	}


	public String toString() {
		return toString(8);
	}

	public String toString(int count) {
		StringBuffer buf = new StringBuffer();
		int count1 = count - 1;
		for (int i = 0; i < size; ++i) {
			buf.append(list[i]);
			buf.append(' ');
			if ((i % count) == count1)
				buf.append("\n");
		}
		if ((size % count) != 0)
			buf.append("\n");
		return buf.toString();
	}

	public Object clone() throws CloneNotSupportedException {
		IntList ret = (IntList)super.clone();
		ret.list = list.clone();
		return ret;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public void push(int value) {
		if (size == capacity)
			expand();
		list[size] = value;
		++size;
	}

	public int peek() {
		return list[size - 1];
	}

	public int pop() {
		--size;
		return list[size];
	}


	////////////////////////////////////////////////////////////////////////

	protected void expand() {
		expand(capacity + (capacity >> 1) + 1);
	}

	protected void expand(int cap) {
		this.capacity = cap;
		int[] ret = new int[cap];
		System.arraycopy(list, 0, ret, 0, size);
		list = ret;
	}

	private IndexOutOfBoundsException indexSizeException(String msg, int index) {
		return new IndexOutOfBoundsException(msg + size + ", index=" + index);
	}

	////////////////////////////////////////////////////////////////////////

	protected class IntListIterator implements IIntIterator {
	}

	////////////////////////////////////////////////////////////////////////
}
