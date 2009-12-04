/*
 * Copyright (c) 2009, Chris Leung, simontsui. All rights reserved.
 *
 * The software in this package is distributed under the GNU General Public
 * License version 2, as published by the Free Software Foundation, but with
 * the Classpath exception.  You should have received a copy of the GNU General
 * Public License (GPL) and the Classpath exception along with this program.
 */
package sf.blacksun.util.struct;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ArrayIterable<T> implements Iterable<T> {

	T[] array;
	int index;

	public static <E> ArrayIterable<E> wrap(E[] a) {
		return new ArrayIterable<E>(a);
	}

	private ArrayIterable(T[] a) {
		this.array = a;
		this.index = 0;
	}
	public Iterator<T> iterator() {
		return new ArrayIterator();
	}

	public class ArrayIterator implements Iterator<T> {

		public boolean hasNext() {
			return index < array.length;
		}

		public T next() {
			if (!hasNext())
				throw new NoSuchElementException();
			++index;
			return array[index - 1];
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
