package sf.blacksun.util.struct;

/*
 * Copyright (c) 2004, Chris Leung, simontsui. All rights reserved.
 *
 * The software in this package is distributed under the GNU General Public
 * License version 2, as published by the Free Software Foundation, but with
 * the Classpath exception.  You should have received a copy of the GNU General
 * Public License (GPL) and the Classpath exception along with this program.
 */

import java.lang.reflect.Array;











/**
 * Some static utilities for data structure manipulation.
 */
public class StructUtil {


	@SuppressWarnings("unchecked")
	public static <T> T[] concat(T[] a, T...b) {
		if (b.length == 0)
			return a;
		if (a == null)
			return b.clone();
		T[] ret = (T[])Array.newInstance(a.getClass().getComponentType(), a.length + b.length);
		System.arraycopy(a, 0, ret, 0, a.length);
		System.arraycopy(b, 0, ret, a.length, b.length);
		return ret;
	}
}
