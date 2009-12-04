/*
 * Copyright (c) 2008, Chris Leung, simontsui. All rights reserved.
 *
 * The software in this package is distributed under the GNU General Public
 * License version 2, as published by the Free Software Foundation, but with
 * the Classpath exception.  You should have received a copy of the GNU General
 * Public License (GPL) and the Classpath exception along with this program.
 */
package sf.bxx.bxd;

import static sf.bxx.IBxxConstants.TagAbsent;
import static sf.bxx.IBxxConstants.TagBigInt;
import static sf.bxx.IBxxConstants.TagBlob;
import static sf.bxx.IBxxConstants.TagBlobRef;
import static sf.bxx.IBxxConstants.TagByte;
import static sf.bxx.IBxxConstants.TagChar;
import static sf.bxx.IBxxConstants.TagDouble;
import static sf.bxx.IBxxConstants.TagFalse;
import static sf.bxx.IBxxConstants.TagFloat;
import static sf.bxx.IBxxConstants.TagInt;
import static sf.bxx.IBxxConstants.TagInvalid;
import static sf.bxx.IBxxConstants.TagLong;
import static sf.bxx.IBxxConstants.TagShort;
import static sf.bxx.IBxxConstants.TagString;
import static sf.bxx.IBxxConstants.TagStringRef;
import static sf.bxx.IBxxConstants.TagTrue;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;

import sf.blacksun.util.text.TextUtil;
import sf.bxx.BxxUtil;
import sf.bxx.bxd.BxdValueBase.BlobValue;
import sf.bxx.bxd.BxdValueBase.BoolValue;
import sf.bxx.bxd.BxdValueBase.LongValue;
import sf.bxx.bxd.BxdValueBase.StringValue;

/**
 * A universal value holder that can holds any kind bxd builtin data types.
 * For more space efficient, use the create() factory methods instead.
 */
public class BxdValue implements IBxdValue {

	int kind;
	long longValue;
	byte[] blobValue;
	CharSequence stringValue;

	public BxdValue() {
		kind = TagInvalid;
	}

	public BxdValue(int kind, boolean value) {
		this.kind = kind;
		switch (kind) {
		case TagAbsent:
		case TagFalse:
		case TagTrue:
			break;
		default :
			throw error();
		}
	}

	public BxdValue(int kind, int value) {
		this.kind = kind;
		this.longValue = value;
		switch (kind) {
		case TagByte:
		case TagShort:
		case TagInt:
		case TagChar:
		case TagFloat:
			break;
		default :
			throw error();
		}
	}

	public BxdValue(int kind, long value) {
		this.kind = kind;
		this.longValue = value;
		switch (kind) {
		case TagLong:
		case TagDouble:
			break;
		default :
			throw error();
		}
	}

	public BxdValue(int kind, CharSequence value) {
		this.kind = kind;
		this.stringValue = value;
		switch (kind) {
		case TagString:
		case TagStringRef:
			break;
		default :
			throw error();
		}
	}

	public BxdValue(int kind, byte[] value) {
		this.kind = kind;
		this.blobValue = value;
		switch (kind) {
		case TagString:
		case TagStringRef:
		case TagBlob:
		case TagBlobRef:
		case TagBigInt:
			break;
		default :
			throw error();
		}
	}

	public void clear() {
		kind = 0;
		longValue = 0;
		blobValue = null;
		stringValue = null;
	}

	public int kind() {
		return kind;
	}

	public boolean boolValue() {
		switch (kind) {
		case TagAbsent:
		case TagFalse:
			return false;
		case TagTrue:
			return true;
		default :
			throw error();
	}}

	public int intValue() {
		switch (kind) {
		case TagByte:
		case TagShort:
		case TagInt:
		case TagChar:
			return (int)longValue;
		default :
			throw error();
	}}

	public long longValue() {
		switch (kind) {
		case TagByte:
		case TagShort:
		case TagInt:
		case TagChar:
		case TagLong:
			return longValue;
		default :
			throw error();
	}}

	public float floatValue() {
		if (kind != TagFloat)
			throw error();
		return Float.intBitsToFloat((int)longValue);
	}

	public double doubleValue() {
		if (kind != TagDouble)
			throw error();
		return Double.longBitsToDouble(longValue);
	}

	public BigInteger bigIntValue() {
		if (kind != TagBigInt)
			throw error();
		return new BigInteger(blobValue);
	}

	public byte[] blobValue() {
		if (kind != TagBlob && kind != TagBlobRef)
			throw error();
		return blobValue;
	}

	public CharSequence stringValue() {
		if (kind != TagString && kind != TagStringRef)
			throw error();
		if (stringValue == null && blobValue != null) {
			try {
				stringValue = BxxUtil.utf8decoder.decode(ByteBuffer.wrap(blobValue));
			} catch (CharacterCodingException e) {
				throw new RuntimeException(e);
		}}
		return stringValue;
	}

	public CharSequence toCharSequence() {
		switch (kind) {
		case TagFalse:
			return "false";
		case TagTrue:
			return "true";
		case TagByte:
			return String.format("0x%02x", (byte)longValue);
		case TagShort:
			return String.format("0x%04x", (short)longValue);
		case TagInt:
			return String.format("0x%08x", (int)longValue);
		case TagLong:
			return String.format("0x%016x", longValue);
		case TagFloat:
			return String.format("%f", floatValue());
		case TagDouble:
			return String.format("%f", doubleValue());
		case TagChar:
			return String.format("'%c'", (int)longValue);
		case TagBigInt:
			return bigIntValue().toString();
		case TagBlob:
		case TagBlobRef:
			return sprint(blobValue);
		case TagString:
		case TagStringRef:
			return stringValue();
		case TagAbsent:
			return "";
		default :
			throw error();
	}}

	public String toString() {
		CharSequence s = toCharSequence();
		return s == null ? "null" : s.toString();
	}

	private String sprint(byte[] a) {
		String sep = TextUtil.getLineSeparator();
		StringBuilder b = new StringBuilder();
		b.append('[');
		for (int i = 0; i < a.length;) {
			b.append(String.format("0x%02x ", a[i++]));
			if ((i & 0x0f) == 0)
				b.append(sep);
		}
		b.append(']');
		return b.toString();
	}

	private AssertionError error() {
		return new AssertionError("ASSERT: Invalid kind: " + BxxUtil.getTagName(kind));
	}

	public static IBxdValue create(int kind, boolean value) {
		switch (kind) {
		case TagAbsent:
		case TagFalse:
		case TagTrue:
			return new BoolValue(kind);
		default :
			throw error(kind);
	}}

	public static IBxdValue create(int kind, int value) {
		switch (kind) {
		case TagByte:
		case TagShort:
		case TagInt:
		case TagChar:
		case TagFloat:
			return new LongValue(kind, value);
		default :
			throw error(kind);
	}}

	public static IBxdValue create(int kind, long value) {
		switch (kind) {
		case TagLong:
		case TagDouble:
			return new LongValue(kind, value);
		default :
			throw error(kind);
	}}

	public static IBxdValue create(int kind, CharSequence value) {
		switch (kind) {
		case TagString:
		case TagStringRef:
			return new StringValue(kind, value);
		default :
			throw error(kind);
	}}

	public static IBxdValue create(int kind, byte[] value) {
		switch (kind) {
		case TagString:
		case TagStringRef:
			return new StringValue(kind, value);
		case TagBigInt:
		case TagBlob:
		case TagBlobRef:
			return new BlobValue(kind, value);
		default :
			throw error(kind);
	}}

	protected static AssertionError error(int kind) {
		return new AssertionError("ASSERT: Invalid kind: " + BxxUtil.getTagName(kind));
	}
}
