package sf.blacksun.util.struct;

/*
 * Copyright (c) 2003-2005, Chris Leung, simontsui. All rights reserved.
 *
 * The software in this package is distributed under the GNU General Public
 * License version 2, as published by the Free Software Foundation, but with
 * the Classpath exception.  You should have received a copy of the GNU General
 * Public License (GPL) and the Classpath exception along with this program.
 */

import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;


/**
 * BytePool is similar to CharPool works for byte[].
 * The first entry, with id=0, is implicit and always null.
 */
public class BytePool implements IBytePool {

	////////////////////////////////////////////////////////////////////////

	protected static final boolean DEBUG = false;
	protected static final int MIN_SIZE = 4;
	protected static final int MAX_SIZE = 1 << 30;
	protected static final int DEF_SIZE = 256;
	protected static final float DEF_LOAD_FACTOR = 0.75f;

	protected Entry[] ordered;
	protected Entry[] entries;
	protected int entryCount;
	protected int threshold;
	protected int initCapacity;
	protected int indexMask;
	protected float loadFactor;

	////////////////////////////////////////////////////////////////////////

	private static class Entry {
		int id;
		int hashcode;
		byte[] s;
		Entry link;
		public Entry(int id, int hash, byte[] value, Entry link) {
			this.id = id;
			this.hashcode = hash;
			this.s = value;
			this.link = link;
		}
		public final boolean equals(Object a) {
			return a == this;
		}
		public final int hashCode() {
			return hashcode;
		}
		public final String toString() {
			return new String(s);
		}
		static final int hash(byte[] cs, int start, int end) {
			int h = 0;
			while (end > start)
				h = h * 31 + cs[--end];
			return h;
		}
	}

	private class EntryIterator implements Iterator<byte[]> {
		private int limit;
		private int index;
		public EntryIterator() {
			this.index = 0;
			this.limit = entryCount;
		}
		public boolean hasNext() {
			return index < limit;
		}
		public byte[] next() {
			if (limit != entryCount)
				throw new ConcurrentModificationException();
			return ordered[index++].s;
		}
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	////////////////////////////////////////////////////////////////////////

	public BytePool() {
		this(DEF_SIZE, DEF_LOAD_FACTOR);
	}

	public BytePool(int capacity) {
		this(capacity, DEF_LOAD_FACTOR);
	}

	public BytePool(float loadfactor) {
		this(DEF_SIZE, loadfactor);
	}

	public BytePool(int capacity, float loadfactor) {
		init(capacity, loadfactor);
	}

	private void init(int capacity, float loadfactor) {
		if (capacity < MIN_SIZE)
			capacity = MIN_SIZE;
		if (capacity > MAX_SIZE)
			capacity = MAX_SIZE;
		int cap = 1;
		while (cap < capacity)
			cap <<= 1;
		this.ordered = new Entry[cap];
		this.entries = new Entry[cap];
		this.loadFactor = loadfactor;
		this.initCapacity = cap;
		this.indexMask = cap - 1;
		this.entryCount = 1;
		this.threshold = (int)(cap * loadfactor);
		ordered[0] = new Entry(0, 0, null, null);
	}

	////////////////////////////////////////////////////////////////////////

	public final int size() {
		return entryCount;
	}

	public final int intern(byte[] a) {
		if (a == null)
			return 0;
		int len = a.length;
		int hash = Entry.hash(a, 0, len);
		int index = (hash & indexMask);
		byte[] es;
		for (Entry e = entries[index]; e != null; e = e.link) {
			es = e.s;
			if (es == a || es.length == len && equals(es, a, 0, len))
				return e.id;
		}
		return add(Arrays.copyOf(a, len), hash, index);
	}

	public final int intern(byte[] a, int start, int end) {
		if (a == null)
			return 0;
		int hash = Entry.hash(a, start, end);
		int index = (hash & indexMask);
		int len = end - start;
		byte[] es;
		for (Entry e = entries[index]; e != null; e = e.link) {
			es = e.s;
			if (es.length == len && equals(es, a, start, len))
				return e.id;
		}
		return add(Arrays.copyOfRange(a, start, end), hash, index);
	}


	public final byte[] get(int index) {
		return ordered[index].s;
	}

	public final Iterator<byte[]> iterator() {
		return new EntryIterator();
	}


	////////////////////////////////////////////////////////////////////////

	private static final boolean equals(byte[] s, byte[] a, int start, int len) {
		int end = start + len;
		while (--len >= 0) {
			if (s[len] != a[--end])
				return false;
		}
		return true;
	}

	private final int add(byte[] a, int hash, int index) {
		Entry e = new Entry(entryCount, hash, a, entries[index]);
		entries[index] = e;
		if (entryCount >= ordered.length)
			ordered = Arrays.copyOf(ordered, ordered.length * 2);
		ordered[entryCount] = e;
		++entryCount;
		if (entryCount >= threshold && entries.length < MAX_SIZE)
			rehash(entries.length << 1);
		return e.id;
	}

	private final void rehash(int capacity) {
		if (DEBUG)
			System.err.println("### rehash: old=" + entries.length + ", new=" + capacity);
		if (capacity > MAX_SIZE)
			capacity = MAX_SIZE;
		Entry[] a = new Entry[capacity];
		int m = capacity - 1;
		Entry e, next;
		for (int i = entries.length - 1; i >= 0; --i) {
			e = entries[i];
			entries[i] = null;
			while (e != null) {
				next = e.link;
				int index = (e.hashcode & m);
				e.link = a[index];
				a[index] = e;
				e = next;
		}}
		if (capacity >= MAX_SIZE)
			threshold = Integer.MAX_VALUE;
		else
			threshold = (int)(capacity * loadFactor);
		indexMask = m;
		entries = a;
	}

	////////////////////////////////////////////////////////////////////////
}
