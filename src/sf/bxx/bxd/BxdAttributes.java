/*
 * Copyright (c) 2008, Chris Leung, simontsui. All rights reserved.
 *
 * The software in this package is distributed under the GNU General Public
 * License version 2, as published by the Free Software Foundation, but with
 * the Classpath exception.  You should have received a copy of the GNU General
 * Public License (GPL) and the Classpath exception along with this program.
 */
package sf.bxx.bxd;

import java.util.Arrays;
import java.util.Iterator;

public class BxdAttributes implements IBxdAttributes {

	static final int DEF_CAPACITY = 0x80;
	BxdAttribute[] attributes = new BxdAttribute[DEF_CAPACITY];
	int capacity = DEF_CAPACITY;
	int count;
	{
		for (int i = 0; i < attributes.length; ++i) {
			attributes[i] = new BxdAttribute();
	}}

	public void clear() {
		count = 0;
	}

	public int size() {
		return count;
	}

	public void putAttr(int name, int kind) {
		put(name, kind);
	}

	public void putAttr(int name, int kind, int value) {
		BxdAttribute a = put(name, kind);
		a.value.longValue = value;
	}

	public void putAttr(int name, int kind, long value) {
		BxdAttribute a = put(name, kind);
		a.value.longValue = value;
	}

	public void putAttr(int name, int kind, byte[] value) {
		BxdAttribute a = put(name, kind);
		a.value.blobValue = value;
	}

	public IBxdAttribute getAttr(int name) {
		for (int i = 0; i < count; ++i) {
			BxdAttribute a = attributes[i];
			if (a.name == name)
				return a;
		}
		return null;
	}

	public Iterator<IBxdAttribute> iterator() {
		return new BxdIterator();
	}

	BxdAttribute put(int name, int kind) {
		if (count >= capacity)
			grow();
		BxdAttribute a = attributes[count];
		a.name = name;
		a.value.clear();
		a.value.kind = kind;
		++count;
		return a;
	}

	private void grow() {
		int newcap = capacity << 1;
		attributes = Arrays.copyOf(attributes, newcap);
		for (int i = capacity; i < newcap; ++i)
			attributes[i] = new BxdAttribute();
		capacity = newcap;
	}

	private class BxdIterator implements Iterator<IBxdAttribute> {
		int index = 0;
		int limit = count;
		BxdIterator() {
		}
		public boolean hasNext() {
			return index < limit;
		}
		public IBxdAttribute next() {
			return attributes[index++];
		}
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
