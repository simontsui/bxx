package sf.blacksun.util.text;

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
 * NamePool is a singleton set of Name.  NamePool entry 0 is always null.
 * Once added, Name cannot be removed from the pool.
 */
public class NamePool implements INamePool {

	////////////////////////////////////////////////////////////////////////

	protected static final boolean DEBUG = false;
	protected static final int MAX_SIZE = 1 << 30;
	protected static final int DEF_SIZE = 256;
	protected static final float DEF_LOAD_FACTOR = 0.75f;

	protected Name[] ordered;
	protected Name[] entries;
	protected int entryCount;
	protected int threshold;
	protected int initCapacity;
	protected int indexMask;
	protected float loadFactor;

	////////////////////////////////////////////////////////////////////////

	public static class Name implements IName {
		int id;
		int length;
		int hashcode;
		String s;
		Name link;
		Name() {
		}
		public Name(int id, int hash, String value, Name link) {
			this.id = id;
			this.length = value.length();
			this.hashcode = hash;
			this.s = value;
			this.link = link;
		}
		public final int id() {
			return id;
		}
		public final int length() {
			return length;
		}
		public final char charAt(int index) {
			return s.charAt(index);
		}

		public final CharSequence subSequence(int start, int end) {
			return s.substring(start, end);
		}
		public final boolean equals(Object a) {
			return this == a;
		}
		public final int hashCode() {
			return hashcode;
		}
		public final String toString() {
			return s;
		}
		static final int hash(CharSequence cs, int start, int end) {
			int h = 0;
			while (end > start)
				h = h * 31 + cs.charAt(--end);
			return h;
		}
	}

	private class NameIterator implements Iterator<IName> {
		private int limit;
		private int index;
		public NameIterator() {
			this.index = 0;
			this.limit = entryCount;
		}
		public final boolean hasNext() {
			return index < limit;
		}
		public final IName next() {
			if (limit != entryCount)
				throw new ConcurrentModificationException();
			return ordered[index++];
		}
		public final void remove() {
			throw new UnsupportedOperationException();
		}
	}

	////////////////////////////////////////////////////////////////////////

	public NamePool() {
		this(DEF_SIZE, DEF_LOAD_FACTOR);
	}

	public NamePool(int capacity) {
		this(capacity, DEF_LOAD_FACTOR);
	}

	public NamePool(float loadfactor) {
		this(DEF_SIZE, loadfactor);
	}

	public NamePool(int capacity, float loadfactor) {
		init(capacity, loadfactor);
	}

	public NamePool(CharSequence...a) {
		this(a.length + DEF_SIZE);
		for (CharSequence s: a)
			intern(s);
	}

	private void init(int capacity, float loadfactor) {
		if (capacity > MAX_SIZE)
			capacity = MAX_SIZE;
		int cap = 1;
		while (cap < capacity)
			cap <<= 1;
		this.ordered = new Name[cap];
		this.entries = new Name[cap];
		this.loadFactor = loadfactor;
		this.initCapacity = cap;
		this.indexMask = cap - 1;
		this.entryCount = 1;
		this.threshold = (int)(cap * loadfactor);
		ordered[0] = null;
	}

	////////////////////////////////////////////////////////////////////////

	public int size() {
		return entryCount;
	}


	public final IName intern(CharSequence a) {
		if (a == null)
			return null;
		int len = a.length();
		int hash = Name.hash(a, 0, len);
		int index = (hash & indexMask);
		for (Name e = entries[index]; e != null; e = e.link) {
			if (e.s == a || e.length == len && equals(e, a, 0, len))
				return e;
		}
		return add(a.toString(), hash, index);
	}


	public final IName get(int index) {
		return ordered[index];
	}

	public final Iterator<IName> iterator() {
		return new NameIterator();
	}


	////////////////////////////////////////////////////////////////////////

	private static final boolean equals(Name k, CharSequence a, int start, int len) {
		String s = k.s;
		int end = start + len;
		while (--len >= 0) {
			if (s.charAt(len) != a.charAt(--end))
				return false;
		}
		return true;
	}


	private final Name add(String a, int hash, int index) {
		Name e = new Name(entryCount, hash, a, entries[index]);
		entries[index] = e;
		if (entryCount >= ordered.length)
			ordered = Arrays.copyOf(ordered, ordered.length * 2);
		ordered[entryCount] = e;
		++entryCount;
		if (entryCount >= threshold && entries.length < MAX_SIZE)
			rehash(entries.length << 1);
		return e;
	}

	private final void rehash(int capacity) {
		if (DEBUG)
			System.err.println("### rehash: old=" + entries.length + ", new=" + capacity);
		if (capacity > MAX_SIZE)
			capacity = MAX_SIZE;
		Name[] a = new Name[capacity];
		int m = capacity - 1;
		Name e, next;
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
