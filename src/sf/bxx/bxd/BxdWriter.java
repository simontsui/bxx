/*
 * Copyright (c) 2008, Chris Leung, simontsui. All rights reserved.
 *
 * The software in this package is distributed under the GNU General Public
 * License version 2, as published by the Free Software Foundation, but with
 * the Classpath exception.  You should have received a copy of the GNU General
 * Public License (GPL) and the Classpath exception along with this program.
 */
package sf.bxx.bxd;

import static sf.bxx.IBxxConstants.DATA;
import static sf.bxx.IBxxConstants.EMPTY_BLOB;
import static sf.bxx.IBxxConstants.EMPTY_STRING;
import static sf.bxx.IBxxConstants.ENCODING;
import static sf.bxx.IBxxConstants.HEADER_PADDING;
import static sf.bxx.IBxxConstants.LSB03;
import static sf.bxx.IBxxConstants.LSB04;
import static sf.bxx.IBxxConstants.LSB06;
import static sf.bxx.IBxxConstants.LSB07;
import static sf.bxx.IBxxConstants.LSB12;
import static sf.bxx.IBxxConstants.LSB20;
import static sf.bxx.IBxxConstants.LSB27;
import static sf.bxx.IBxxConstants.LSB28;
import static sf.bxx.IBxxConstants.LSB28L;
import static sf.bxx.IBxxConstants.LSB31;
import static sf.bxx.IBxxConstants.LSB32;
import static sf.bxx.IBxxConstants.LSB32L;
import static sf.bxx.IBxxConstants.MAGIC;
import static sf.bxx.IBxxConstants.NULL;
import static sf.bxx.IBxxConstants.NullValue;
import static sf.bxx.IBxxConstants.STANDALONE;
import static sf.bxx.IBxxConstants.SigNONE;
import static sf.bxx.IBxxConstants.StandardTagEnd;
import static sf.bxx.IBxxConstants.StandardTagStrings;
import static sf.bxx.IBxxConstants.TagAbsent;
import static sf.bxx.IBxxConstants.TagBigInt;
import static sf.bxx.IBxxConstants.TagBlob;
import static sf.bxx.IBxxConstants.TagBlobDef;
import static sf.bxx.IBxxConstants.TagBlobRef;
import static sf.bxx.IBxxConstants.TagByte;
import static sf.bxx.IBxxConstants.TagCDATA;
import static sf.bxx.IBxxConstants.TagChar;
import static sf.bxx.IBxxConstants.TagComment;
import static sf.bxx.IBxxConstants.TagDeclaration;
import static sf.bxx.IBxxConstants.TagDoctype;
import static sf.bxx.IBxxConstants.TagDouble;
import static sf.bxx.IBxxConstants.TagEndAttr;
import static sf.bxx.IBxxConstants.TagEndTag;
import static sf.bxx.IBxxConstants.TagFalse;
import static sf.bxx.IBxxConstants.TagFloat;
import static sf.bxx.IBxxConstants.TagInt;
import static sf.bxx.IBxxConstants.TagLong;
import static sf.bxx.IBxxConstants.TagPI;
import static sf.bxx.IBxxConstants.TagShort;
import static sf.bxx.IBxxConstants.TagString;
import static sf.bxx.IBxxConstants.TagStringDef;
import static sf.bxx.IBxxConstants.TagStringRef;
import static sf.bxx.IBxxConstants.TagTrailer;
import static sf.bxx.IBxxConstants.TagTrue;
import static sf.bxx.IBxxConstants.TagUByte;
import static sf.bxx.IBxxConstants.TagUInt;
import static sf.bxx.IBxxConstants.TagULong;
import static sf.bxx.IBxxConstants.TagUShort;
import static sf.bxx.IBxxConstants.VERSION;
import static sf.bxx.IBxxConstants.XML;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import sf.blacksun.util.io.ByteIOUtil;
import sf.blacksun.util.struct.BytePool;
import sf.blacksun.util.struct.Empty;
import sf.blacksun.util.struct.IBytePool;
import sf.blacksun.util.struct.IntList;
import sf.blacksun.util.text.IStringPool;
import sf.blacksun.util.text.NameStringPool;
import sf.bxx.BxxUtil;

public class BxdWriter implements IBxdWriter {

	////////////////////////////////////////////////////////////////////////

	private static CharsetEncoder utf8Encoder = Charset.forName("UTF-8").newEncoder();

	public static IStringPool createStringPool() {
		IStringPool pool = new NameStringPool(StandardTagStrings);
		assert pool.size() == StandardTagEnd;
		return pool;
	}

	public static IBytePool createBytePool() {
		IBytePool pool = new BytePool(8);
		pool.intern(Empty.BYTE_ARRAY);
		assert pool.size() == EMPTY_BLOB + 1;
		return pool;
	}

	////////////////////////////////////////////////////////////////////////

	protected OutputStream output;
	protected long position;
	protected IStringPool stringPool;
	protected IBytePool blobPool;
	//
	private int stringPoolIndex; 
	private int blobPoolIndex; 
	private IntList tagStack = new IntList();

	////////////////////////////////////////////////////////////////////////

	public BxdWriter(OutputStream out) {
		this(out, 0);
	}

	public BxdWriter(OutputStream out, int position) {
		this(out, 0, createStringPool());
	}

	public BxdWriter(OutputStream out, int position, IStringPool pool) {
		this.output = out;
		this.position = position;
		this.stringPool = pool;
		this.blobPool = createBytePool();
		stringPoolIndex = EMPTY_STRING + 1;
		blobPoolIndex = EMPTY_BLOB + 1;
	}

	public IStringPool stringPool() {
		return stringPool;
	}

	public IBytePool blobPool() {
		return blobPool;
	}

	public long fileOffset() {
		return position;
	}

	public int intern(CharSequence s) {
		return stringPool.intern(s);
	}

	public void close() throws IOException {
		flush();
		output.close();
	}

	////////////////////////////////////////////////////////////////////////

	public void header(IBxdHeader header) throws IOException {
		output.write(MAGIC);
		position += MAGIC.length;
		write16(header.major());
		write16(header.minor());
		write64(header.flags());
		output.write(HEADER_PADDING);
		position += HEADER_PADDING.length;
		flushStringPool();
		flushBlobPool();
		BxxUtil.assertTagStackEmpty(stringPool, tagStack);
	}

	public void trailer(IBxdTrailer trailer) throws IOException {
		flushStringPool();
		flushBlobPool();
		BxxUtil.assertTagStackEmpty(stringPool, tagStack);
		writeTag(TagTrailer);
		writeSignatures(trailer);
		output.flush();
	}

	public void writeSignatures(IBxdTrailer trailer) throws IOException {
		// TODO
		writeU31(SigNONE);
	}

	public void flushStringPool() throws IOException {
		int end = stringPool.size();
		if (end <= stringPoolIndex)
			return;
		for (int id = stringPoolIndex; id < end; ++id) {
			writeStringDef(id, stringPool.get(id));
		}
		stringPoolIndex = end;
	}

	public void flushBlobPool() throws IOException {
		int size = blobPool.size();
		if (size <= blobPoolIndex)
			return;
		for (int id = blobPoolIndex; id < size; ++id) {
			writeBlobDef(id, blobPool.get(id));
		}
		blobPoolIndex = size;
	}

	////////////////////////////////////////////////////////////////////////

	public void xmlDeclaration(String version, String encoding, String standalone) throws IOException {
		writeTag(TagPI);
		writeU31(XML);
		if (version != null) {
			attribute(VERSION, version);
		}
		if (encoding != null) {
			attribute(ENCODING, encoding);
		}
		if (standalone != null) {
			attribute(STANDALONE, standalone);
		}
		writeTag(TagEndTag);
	}

	public void startDoctype(int name) throws IOException {
		writeTag(TagDoctype);
		writeU31(name);
		tagStack.push(TagDoctype);
	}

	public void startDoctype(CharSequence name) throws IOException {
		int id = stringDef(name);
		writeTag(TagDoctype);
		writeU31(id);
		tagStack.push(TagDoctype);
	}

	public void startTag(int tag) throws IOException {
		writeTag(tag);
		tagStack.push(tag);
	}

	public void startSimpleTag(int tag) throws IOException {
		writeTag(tag);
		tagStack.push(tag);
		endAttr();
	}

	public int startTag(CharSequence name) throws IOException {
		int id = writeTag(name);
		tagStack.push(id);
		return id;
	}

	public int startSimpleTag(CharSequence name) throws IOException {
		int id = writeTag(name);
		tagStack.push(id);
		endAttr();
		return id;
	}

	public void startAttr(int tag) throws IOException {
		writeTag(tag);
	}

	public int startAttr(CharSequence name) throws IOException {
		return writeTag(name);
	}

	public void startPI(int name) throws IOException {
		writeTag(TagPI);
		writeU31(name);
		tagStack.push(TagPI);
	}

	public void pi(CharSequence name, CharSequence data) throws IOException {
		int id = stringDef(name);
		writeTag(TagPI);
		writeU31(id);
		writeTag(DATA);
		writeString(data);
		endTag();
	}

	public void startDeclaration(int name) throws IOException {
		writeTag(TagDeclaration);
		writeU31(name);
		tagStack.push(TagDeclaration);
	}

	public void declaration(CharSequence name, CharSequence data) throws IOException {
		int id = stringDef(name);
		writeTag(TagDeclaration);
		writeU31(id);
		writeTag(DATA);
		writeString(data);
		endTag();
	}

	public void cdata(CharSequence s) throws IOException {
		writeTag(TagCDATA);
		writeString(s);
	}

	public void cdata(char[] s, int start, int len) throws IOException {
		writeTag(TagCDATA);
		writeString(s, start, len);
	}

	public void comment(CharSequence s) throws IOException {
		writeTag(TagComment);
		writeString(s);
	}

	public void comment(char[] s, int start, int len) throws IOException {
		writeTag(TagComment);
		writeString(s, start, len);
	}

	public void endAttr() throws IOException {
		writeTag(TagEndAttr);
	}

	public void endTag() throws IOException {
		writeTag(TagEndTag);
		tagStack.pop();
	}

	////////////////////////////////////////////////////////////////////////

	public void attribute(int name, boolean value) throws IOException {
		writeTag(name);
		writeBool(value);
	}

	public void attribute(int name, int value) throws IOException {
		writeTag(name);
		writeInt(value);
	}

	public void attribute(int name, long value) throws IOException {
		writeTag(name);
		writeLong(value);
	}

	public void attribute(int name, BigInteger value) throws IOException {
		writeTag(name);
		writeBigInt(value);
	}

	public void attribute(int name, float value) throws IOException {
		writeTag(name);
		writeFloat(value);
	}

	public void attribute(int name, double value) throws IOException {
		writeTag(name);
		writeDouble(value);
	}

	public void attribute(int name, CharSequence value) throws IOException {
		writeTag(name);
		writeString(value);
	}

	public void attribute(int name, byte[] value) throws IOException {
		writeTag(name);
		writeBlob(value);
	}

	public void attribute(int name, byte[] value, int start, int end) throws IOException {
		writeTag(name);
		writeBlob(value, start, end);
	}

	////////////////////////////////////////////////////////////////////////

	public void attribute(CharSequence name, boolean value) throws IOException {
		writeTag(name);
		writeBool(value);
	}

	public void attribute(CharSequence name, int value) throws IOException {
		writeTag(name);
		writeInt(value);
	}

	public void attribute(CharSequence name, long value) throws IOException {
		writeTag(name);
		writeLong(value);
	}

	public void attribute(CharSequence name, BigInteger value) throws IOException {
		writeTag(name);
		writeBigInt(value);
	}

	public void attribute(CharSequence name, float value) throws IOException {
		writeTag(name);
		writeFloat(value);
	}

	public void attribute(CharSequence name, double value) throws IOException {
		writeTag(name);
		writeDouble(value);
	}

	public void attribute(CharSequence name, CharSequence value) throws IOException {
		writeTag(name);
		writeString(value);
	}

	public void attribute(CharSequence name, byte[] value) throws IOException {
		writeTag(name);
		writeBlob(value);
	}

	public void attribute(CharSequence name, byte[] value, int start, int end) throws IOException {
		writeTag(name);
		writeBlob(value, start, end);
	}

	////////////////////////////////////////////////////////////////////////

	public void element(int name, boolean value) throws IOException {
		writeTag(name);
		writeTag(TagEndAttr);
		writeBool(value);
		writeTag(TagEndTag);
	}

	public void element(int name, int value) throws IOException {
		writeTag(name);
		writeTag(TagEndAttr);
		writeInt(value);
		writeTag(TagEndTag);
	}

	public void element(int name, long value) throws IOException {
		writeTag(name);
		writeTag(TagEndAttr);
		writeLong(value);
		writeTag(TagEndTag);
	}

	public void element(int name, BigInteger value) throws IOException {
		writeTag(name);
		writeTag(TagEndAttr);
		writeBigInt(value);
		writeTag(TagEndTag);
	}

	public void element(int name, float value) throws IOException {
		writeTag(name);
		writeTag(TagEndAttr);
		writeFloat(value);
		writeTag(TagEndTag);
	}

	public void element(int name, double value) throws IOException {
		writeTag(name);
		writeTag(TagEndAttr);
		writeDouble(value);
		writeTag(TagEndTag);
	}

	public void element(int name, CharSequence value) throws IOException {
		writeTag(name);
		writeTag(TagEndAttr);
		writeString(value);
		writeTag(TagEndTag);
	}

	public void element(int name, byte[] value) throws IOException {
		writeTag(name);
		writeTag(TagEndAttr);
		writeBlob(value);
		writeTag(TagEndTag);
	}

	public void element(int name, byte[] value, int start, int end) throws IOException {
		writeTag(name);
		writeTag(TagEndAttr);
		writeBlob(value, start, end);
		writeTag(TagEndTag);
	}

	////////////////////////////////////////////////////////////////////////

	public void element(CharSequence name, boolean value) throws IOException {
		writeTag(name);
		writeTag(TagEndAttr);
		writeBool(value);
		writeTag(TagEndTag);
	}

	public void element(CharSequence name, int value) throws IOException {
		writeTag(name);
		writeTag(TagEndAttr);
		writeInt(value);
		writeTag(TagEndTag);
	}

	public void element(CharSequence name, long value) throws IOException {
		writeTag(name);
		writeTag(TagEndAttr);
		writeLong(value);
		writeTag(TagEndTag);
	}

	public void element(CharSequence name, BigInteger value) throws IOException {
		writeTag(name);
		writeTag(TagEndAttr);
		writeBigInt(value);
		writeTag(TagEndTag);
	}

	public void element(CharSequence name, float value) throws IOException {
		writeTag(name);
		writeTag(TagEndAttr);
		writeFloat(value);
		writeTag(TagEndTag);
	}

	public void element(CharSequence name, double value) throws IOException {
		writeTag(name);
		writeTag(TagEndAttr);
		writeDouble(value);
		writeTag(TagEndTag);
	}

	public void element(CharSequence name, CharSequence value) throws IOException {
		writeTag(name);
		writeTag(TagEndAttr);
		writeString(value);
		writeTag(TagEndTag);
	}

	public void element(CharSequence name, byte[] value) throws IOException {
		writeTag(name);
		writeTag(TagEndAttr);
		writeBlob(value);
		writeTag(TagEndTag);
	}

	public void element(CharSequence name, byte[] value, int start, int end) throws IOException {
		writeTag(name);
		writeTag(TagEndAttr);
		writeBlob(value, start, end);
		writeTag(TagEndTag);
	}

	////////////////////////////////////////////////////////////////////////

	public void writeAbsent() throws IOException {
		writeTag(TagAbsent);
	}

	public void writeBool(boolean value) throws IOException {
		writeTag(value ? TagTrue : TagFalse);
	}

	public void writeUByte(int value) throws IOException {
		writeTag(TagUByte);
		write8(value);
	}

	public void writeUShort(int value) throws IOException {
		writeTag(TagUShort);
		writeU32(value);
	}

	public void writeUInt(int value) throws IOException {
		writeTag(TagUInt);
		writeU32(value);
	}

	public void writeULong(long value) throws IOException {
		writeTag(TagULong);
		writeU64(value);
	}

	public void writeByte(int value) throws IOException {
		writeTag(TagByte);
		write8(value);
	}

	public void writeShort(int value) throws IOException {
		writeTag(TagShort);
		writeV32(value);
	}

	public void writeInt(int value) throws IOException {
		writeTag(TagInt);
		writeV32(value);
	}

	public void writeLong(long value) throws IOException {
		writeTag(TagLong);
		writeV64(value);
	}

	public void writeBigInt(BigInteger value) throws IOException {
		writeTag(TagBigInt);
		write(value.toByteArray());
	}

	public void writeChar(int value) throws IOException {
		writeTag(TagChar);
		writeU31(value);
	}

	public void writeFloat(float value) throws IOException {
		writeTag(TagFloat);
		write(value);
	}

	public void writeDouble(double value) throws IOException {
		writeTag(TagDouble);
		write(value);
	}

	public void writeString(CharSequence value) throws IOException {
		writeTag(TagString);
		writeUtf8(value);
	}

	public void writeString(char[] value) throws IOException {
		writeTag(TagString);
		writeUtf8(value, 0, value == null ? 0 : value.length);
	}

	public void writeString(char[] value, int start, int len) throws IOException {
		writeTag(TagString);
		writeUtf8(value, start, len);
	}

	public void writeBlob(byte[] value) throws IOException {
		writeTag(TagBlob);
		write(value);
	}

	public void writeBlob(byte[] value, int start, int end) throws IOException {
		writeTag(TagBlob);
		write(value, start, end);
	}

	public int writeStringRef(CharSequence value) throws IOException {
		int id = value == null ? NULL : stringPool.intern(value);
		if (id >= stringPoolIndex)
			flushStringPool();
		writeTag(TagStringRef);
		writeU31(id);
		return id;
	}

	public int writeBlobRef(byte[] value) throws IOException {
		return writeBlobRef(value, 0, value == null ? 0 : value.length);
	}

	public int writeBlobRef(byte[] value, int start, int end) throws IOException {
		int id = blobPool.intern(value, start, end);
		if (id >= blobPoolIndex)
			flushBlobPool();
		writeTag(TagBlobRef);
		writeU31(id);
		return id;
	}

	public void writeStringDef(int id, CharSequence value) throws IOException {
		writeTag(TagStringDef);
		writeU31(id);
		writeUtf8(value);
	}

	public void writeBlobDef(int id, byte[] value) throws IOException {
		writeBlobDef(id, value, 0, value.length);
	}

	public void writeBlobDef(int id, byte[] value, int start, int end) throws IOException {
		writeTag(TagBlobDef);
		writeU31(id);
		write(value, start, end);
	}

	////////////////////////////////////////////////////////////////////////

	public int countU31(int v) {
		if ((v & ~LSB07) == 0)
			return 1;
		for (int i = 2, mask = ~LSB12; mask != 0; mask <<= 8, ++i) {
			if ((v & mask) == 0)
				return i;
		}
		if ((v & ~LSB31) == 0)
			return 5;
		throw new IllegalArgumentException(String.format("U60 value overflow: 0x%08x", v));
	}

	public int countV32(int v) {
		if ((v & ~LSB07) == 0)
			return 1;
		for (int i = 2, mask = ~LSB12, x; mask != 0; mask <<= 8, ++i) {
			if ((x = v & mask) == 0 || x == mask)
				return i;
		}
		return 5;
	}

	public int countV64(long v) {
		long x = v & ~LSB28L;
		if (x == 0 || x == ~LSB28L)
			return countU31((int)v);
		int u = (int)(v >> 32);
		for (int i = 5, mask = ~LSB04; mask != 0; mask <<= 8, ++i) {
			if ((x = u & mask) == 0 || x == mask)
				return i;
		}
		return 9;
	}

	////////////////////////////////////////////////////////////////////////

	protected void writeUtf8(char[] s) throws IOException {
		if (s == null) {
			output.write(NullValue);
			++position;
			return;
		}
		writeUtf8(s, 0, s.length);
	}

	protected void writeUtf8(char[] s, int start, int len) throws IOException {
		if (s == null) {
			output.write(NullValue);
			++position;
			return;
		}
		ByteBuffer b = utf8Encoder.encode(CharBuffer.wrap(s, start, start + len));
		writeU31(b.remaining());
		len = b.remaining();
		output.write(b.array(), b.position(), len);
		position += len;
	}

	protected void writeUtf8(CharSequence s) throws IOException {
		if (s == null) {
			output.write(NullValue);
			++position;
			return;
		}
		writeUtf8(s, 0, s.length());
	}

	protected void writeUtf8(CharSequence s, int start, int len) throws IOException {
		if (s == null) {
			output.write(NullValue);
			++position;
			return;
		}
		ByteBuffer b = utf8Encoder.encode(CharBuffer.wrap(s, start, start + len));
		writeU31(b.remaining());
		len = b.remaining();
		output.write(b.array(), b.position(), len);
		position += len;
	}

	protected void write(byte[] a) throws IOException {
		if (a == null) {
			output.write(NullValue);
			++position;
			return;
		}
		write(a, 0, a.length);
	}

	protected void write(byte[] a, int start, int len) throws IOException {
		if (a == null) {
			output.write(NullValue);
			++position;
			return;
		}
		writeU31(len);
		output.write(a, start, len);
		position += len;
	}

	protected int stringDef(CharSequence value) throws IOException {
		int id = stringPool.intern(value);
		if (id >= stringPoolIndex)
			flushStringPool();
		return id;
	}

	protected int blobId(byte[] value) throws IOException {
		return blobId(value, 0, value.length);
	}

	protected int blobId(byte[] value, int start, int end) throws IOException {
		int id = blobPool.intern(value, start, end);
		if (id >= blobPoolIndex)
			flushBlobPool();
		writeU31(id);
		return id;
	}

	////////////////////////////////////////////////////////////////////////

	protected void write8(int v) throws IOException {
		output.write(v);
		++position;
	}

	protected void write16(int v) throws IOException {
		ByteIOUtil.write16BE(output, v);
		position += 2;
	}

	protected void write32(int v) throws IOException {
		ByteIOUtil.write32BE(output, v);
		position += 4;
	}

	protected void write64(long v) throws IOException {
		ByteIOUtil.write32BE(output, (int)(v >>> 32));
		ByteIOUtil.write32BE(output, (int)(v & LSB32L));
		position += 8;
	}

	protected void write(float v) throws IOException {
		int n = Float.floatToIntBits(v);
		writeV32(n);
	}

	protected void write(double v) throws IOException {
		long n = Double.doubleToLongBits(v);
		writeV64(n);
	}

	////////////////////////////////////////////////////////////////////////

	protected final void writeTag(int tag) throws IOException {
		writeV32(tag);
	}

	protected int writeTag(CharSequence value) throws IOException {
		int id = stringDef(value);
		writeV32(id);
		return id;
	}

	protected void writeU31(int v) throws IOException {
		if ((v & ~LSB07) == 0) {
			output.write(v);
			++position;
			return;
		}
		if ((v & ~LSB12) == 0) {
			output.write(0x80 | (v >> 8));
			output.write(v);
			position += 2;
			return;
		}
		if ((v & ~LSB20) == 0) {
			writeU(0x900000 | v, 16);
			return;
		}
		if ((v & ~LSB28) == 0) {
			writeU(0xa0000000 | v, 24);
			return;
		}
		if ((v & ~LSB31) == 0) {
			output.write(0xb0);
			++position;
			writeU(v, 24);
			return;
		}
		throw new IllegalArgumentException(String.format("U31 value overflow: 0x%08x", v));
	}


	protected void writeU32(int v) throws IOException {
		if ((v & ~LSB28L) == 0) {
			writeU31(v);
			return;
		}
		write8(0xb0);
		ByteIOUtil.write32BE(output, v);
		position += 4;
	}

	protected void writeU64(long v) throws IOException {
		int l = (int)v;
		int u = (int)(v >> 32);
		if (u == 0) {
			writeU32(l);
			return;
		}
		if ((u & ~LSB04) == 0) {
			writeU(0xb0 | u, 0);
		} else if ((u & ~LSB12) == 0) {
			writeU(0xc000 | u, 8);
		} else if ((u & ~LSB20) == 0) {
			writeU(0xd00000 | u, 16);
		} else if ((u & ~LSB28) == 0) {
			writeU(0xe0000000 | u, 24);
		} else {
			write8(0xf0);
			ByteIOUtil.write32BE(output, u);
		}
		ByteIOUtil.write32BE(output, l);
		position += 4;
	}

	protected void writeV32(int v) throws IOException {
		int x = (v & ~LSB06) >> 6;
		if (x == 0 || x == LSB32) {
			output.write(v << 1);
			++position;
			return;
		}
		if ((x >>= 5) == 0 || x == LSB32) {
			writeV((v >> 4) & 0xf0 | 0x01, v, 0);
			return;
		}
		if ((x >>= 8) == 0 || x == LSB32) {
			writeV((v >> 12) & 0xf0 | 0x03, v, 8);
			return;
		}
		if ((x >>= 8) == 0 || x == LSB32) {
			writeV((v >> 20) & 0xf0 | 0x05, v, 16);
			return;
		}
		writeV((v >> 28) & 0xf0 | 0x07, v, 24);
	}

	protected void writeV64(long v) throws IOException {
		int l = (int)v;
		int u = (int)(v >> 32);
		int x = (l & ~LSB27);
		if (u == 0 && x == 0 || u == LSB32 && x == ~LSB27) {
			writeV32(l);
			return;
		}
		x = (u & ~LSB03) >> 3;
		if (x == 0 || x == LSB32) {
			output.write((u << 4) | 0x07);
			++position;
		} else if ((x >>= 8) == 0 || x == LSB32) {
			output.write((u >> 4) & 0xf0 | 0x09);
			output.write(u);
			position += 4;
		} else if ((x >>= 8) == 0 || x == LSB32) {
			writeV((u >> 12) & 0xf0 | 0x0b, u, 8);
		} else if ((x >>= 8) == 0 || x == LSB32) {
			writeV((u >> 20) & 0xf0 | 0x0d, u, 16);
		} else {
			writeV((u >> 28) & 0xf0 | 0x0f, u, 24);
		}
		ByteIOUtil.write32BE(output, l);
		position += 4;
	}

	////////////////////////////////////////////////////////////////////////

	protected final void writeU(int v, int shift) throws IOException {
		for (; shift >= 0; shift -= 8) {
			output.write(v >> shift);
			++position;
	}}

	protected final void writeV(int v1, int v, int shift) throws IOException {
		output.write(v1);
		++position;
		for (; shift >= 0; shift -= 8) {
			output.write(v >> shift);
			++position;
	}}

	protected void flush() throws IOException {
		flushStringPool();
		flushBlobPool();
		output.flush();
	}

	////////////////////////////////////////////////////////////////////////
}
