/*
 * Copyright (c) 2008, Chris Leung, simontsui. All rights reserved.
 *
 * The software in this package is distributed under the GNU General Public
 * License version 2, as published by the Free Software Foundation, but with
 * the Classpath exception.  You should have received a copy of the GNU General
 * Public License (GPL) and the Classpath exception along with this program.
 */
package sf.blacksun.util.text;

import java.util.Iterator;

/** Implementation of IStringPool interface using NamePool .*/
public class NameStringPool implements IStringPool {

	private INamePool pool = new NamePool();

	public NameStringPool() {
	}

	public NameStringPool(String...init) {
		init(init);
	}

	private void init(String...init) {
		for (String s: init)
			intern(s);
	}


	public CharSequence get(int id) {
		return pool.get(id);
	}


	public int intern(CharSequence str) {
		IName ret = pool.intern(str);
		return ret == null ? 0 : ret.id();
	}


	public int size() {
		return pool.size();
	}

	public Iterator<CharSequence> iterator() {
		return new IteratorAdapter(pool.iterator());
	}

	private static class IteratorAdapter implements Iterator<CharSequence> {
		Iterator<IName> iterator;
		public IteratorAdapter(Iterator<IName> it) {
			this.iterator = it;
		}
		public boolean hasNext() {
			return iterator.hasNext();
		}
		public CharSequence next() {
			return iterator.next();
		}
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
