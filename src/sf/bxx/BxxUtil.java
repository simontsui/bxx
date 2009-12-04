/*
 * Copyright (c) 2008, Chris Leung, simontsui. All rights reserved.
 *
 * The software in this package is distributed under the GNU General Public
 * License version 2, as published by the Free Software Foundation, but with
 * the Classpath exception.  You should have received a copy of the GNU General
 * Public License (GPL) and the Classpath exception along with this program.
 */
package sf.bxx;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import sf.blacksun.util.struct.IIntStack;
import sf.blacksun.util.text.IStringPool;

public class BxxUtil {

	public static CharsetDecoder utf8decoder = Charset.forName("UTF-8").newDecoder();

	private BxxUtil() {
	}

	public static String getTagName(int tagid) {
		if (tagid <= 0 && -tagid < IBxxConstants.TagNames.length)
			return IBxxConstants.TagNames[-tagid];
		return String.format("<0x%02x>", tagid);
	}

	public static void assertTagStackEmpty(IStringPool spool, IIntStack stack) {
		if (!stack.isEmpty()) {
			StringBuilder b = new StringBuilder();
			for (int i = 0; i < stack.size(); ++i) {
				if (i > 0)
					b.append(' ');
				b.append(spool.get(stack.get(i)));
			}
			throw new AssertionError("ASSERT: tagStack.isEmpty(): " + stack.size() + ": " + b);
	}}

	public static void assertEquals(int expected, int actual) {
		if (actual != expected)
			throw new AssertionError("ASSERT: expected=" + expected + ", actual=" + actual);
	}
}
