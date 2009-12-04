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
import static sf.bxx.IBxxConstants.TagLong;
import static sf.bxx.IBxxConstants.TagShort;
import static sf.bxx.IBxxConstants.TagString;
import static sf.bxx.IBxxConstants.TagStringRef;
import static sf.bxx.IBxxConstants.TagTrue;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;

import sf.bxx.BxxUtil;

public class BxdValueBase implements IBxdValue {

	int kind;

	BxdValueBase() {
	}

	BxdValueBase(int kind) {
		this.kind = kind;
	}

	public void clear() {
		kind = 0;
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
		throw new UnsupportedOperationException();
	}

	public long longValue() {
		throw new UnsupportedOperationException();
	}

	public float floatValue() {
		throw new UnsupportedOperationException();
	}

	public double doubleValue() {
		throw new UnsupportedOperationException();
	}

	public BigInteger bigIntValue() {
		throw new UnsupportedOperationException();
	}

	public byte[] blobValue() {
		throw new UnsupportedOperationException();
	}


	public CharSequence stringValue() {
		throw new UnsupportedOperationException();
	}

	public CharSequence toCharSequence() {
		switch (kind) {
		case TagFalse:
			return "false";
		case TagTrue:
			return "true";
		case TagByte:
			return String.format("0x%02x", longValue());
		case TagShort:
			return String.format("0x%04x", longValue());
		case TagInt:
			return String.format("0x%08x", longValue());
		case TagLong:
			return String.format("0x%016x", longValue());
		case TagFloat:
			return String.format("%f", floatValue());
		case TagDouble:
			return String.format("%f", doubleValue());
		case TagChar:
			return String.format("'%c'", (int)longValue());
		case TagBigInt:
			return bigIntValue().toString();
		case TagBlob:
		case TagBlobRef:
			return sprint(blobValue());
		case TagString:
		case TagStringRef:
			return stringValue();
		case TagAbsent:
			return null;
		default :
			throw error();
	}}

	public String toString() {
		return toCharSequence().toString();
	}

	private String sprint(byte[] a) {
		StringBuilder b = new StringBuilder();
		b.append('[');
		for (int i = 0; i < a.length; ++i)
			b.append(String.format("0x%02x", a[i]));
		b.append(']');
		return b.toString();
	}

	protected AssertionError error() {
		return BxdValue.error(kind);
	}

	static class BoolValue extends BxdValueBase {
		public BoolValue(int kind) {
			super(kind);
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
	}

	static class LongValue extends BxdValueBase {
		private long longValue;
		public LongValue(int kind, long value) {
			super(kind);
			this.longValue = value;
		}
		public int intValue() {
			return (int)longValue;
		}
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
	}

	static class BlobValue extends BxdValueBase {
		private byte[] blobValue;
		public BlobValue(int kind, byte[] value) {
			super(kind);
			this.blobValue = value;
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
	}

	static class StringValue extends BxdValueBase {
		private byte[] blobValue;
		private CharSequence stringValue;
		public StringValue(int kind, byte[] value) {
			super(kind);
			this.blobValue = value;
		}
		public StringValue(int kind, CharSequence value) {
			super(kind);
			this.stringValue = value;
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
	}
}
