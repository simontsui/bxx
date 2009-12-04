/*
 * Copyright (c) 2008, Chris Leung, simontsui. All rights reserved.
 *
 * The software in this package is distributed under the GNU General Public
 * License version 2, as published by the Free Software Foundation, but with
 * the Classpath exception.  You should have received a copy of the GNU General
 * Public License (GPL) and the Classpath exception along with this program.
 */
package sf.bxx.bxd;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;

import sf.bxx.BxxUtil;

public class BxdString implements CharSequence {

	private byte[] bytes;
	private int start;
	private int end;
	private CharSequence string;

	public BxdString(byte[] a) {
		this.bytes = a;
		this.start = 0;
		this.end = a.length;
	}

	public BxdString(byte[] a, int start, int end) {
		this.bytes = a;
		this.start = start;
		this.end = end;
	}

	public char charAt(int index) {
		return toCharSequence().charAt(index);
	}

	public int length() {
		return toCharSequence().length();
	}
	public CharSequence subSequence(int start, int end) {
		return toCharSequence().subSequence(start, end);
	}

	@Override
	public String toString() {
		String s = string.toString();
		string = s;
		return s;
	}

	private CharSequence toCharSequence() {
		if (string == null && bytes != null) {
			try {
				string = BxxUtil.utf8decoder.decode(ByteBuffer.wrap(bytes, start, end - start))
					.toString();
				bytes = null;
			} catch (CharacterCodingException e) {
				throw new RuntimeException(e);
		}}
		return string;
	}
}
