/*
 * Copyright (c) 2008, Chris Leung, simontsui. All rights reserved.
 *
 * The software in this package is distributed under the GNU General Public
 * License version 2, as published by the Free Software Foundation, but with
 * the Classpath exception.  You should have received a copy of the GNU General
 * Public License (GPL) and the Classpath exception along with this program.
 */
package sf.bxx.bxd;

import static sf.bxx.IBxxConstants.EMPTY_BLOB;
import static sf.bxx.IBxxConstants.EMPTY_STRING;
import static sf.bxx.IBxxConstants.HEADER_PADDING;
import static sf.bxx.IBxxConstants.LSB31L;
import static sf.bxx.IBxxConstants.LSB32L;
import static sf.bxx.IBxxConstants.MAGIC;
import static sf.bxx.IBxxConstants.SigNONE;
import static sf.bxx.IBxxConstants.TagBlob;
import static sf.bxx.IBxxConstants.TagBlobDef;
import static sf.bxx.IBxxConstants.TagNames;
import static sf.bxx.IBxxConstants.TagString;
import static sf.bxx.IBxxConstants.TagStringDef;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import sf.blacksun.util.struct.BytePool;
import sf.blacksun.util.struct.Empty;
import sf.blacksun.util.struct.IBytePool;
import sf.blacksun.util.text.IStringPool;
import sf.blacksun.util.text.NameStringPool;
import sf.bxx.IBxxConstants;

public abstract class BxdReaderBase {

	////////////////////////////////////////////////////////////////////////

	protected static final int BUFSIZE = 256 * 1024;
	protected static final int TMPSIZE = 1024;

	////////////////////////////////////////////////////////////////////////

	protected byte[] buffer;
	protected int head;		
	protected int offset;		
	protected long tailOffset;	
	protected boolean isEOF;
	//
	protected IStringPool stringPool;
	protected IBytePool blobPool;
	//
	protected long mark = 0;
	protected CharsetDecoder utf8Decoder = Charset.forName("UTF-8").newDecoder();

	////////////////////////////////////////////////////////////////////////

	protected abstract int read0(byte[] a, int start, int len) throws IOException;
	protected abstract void skip0(long len) throws IOException;
	protected abstract void initPools() throws IOException;

	////////////////////////////////////////////////////////////////////////

	public BxdReaderBase() {
		this(BUFSIZE);
	}

	public BxdReaderBase(int bufsize) {
		this.buffer = new byte[bufsize];
		init();
	}

	protected void init() {
		this.head = 0;
		this.offset = 0;
		this.tailOffset = 0;
		this.isEOF = false;
		this.stringPool = new NameStringPool(null, "");
		this.blobPool = new BytePool();
		this.blobPool.intern(Empty.BYTE_ARRAY);
		assert (stringPool.size() == EMPTY_STRING + 1);
		assert (blobPool.size() == EMPTY_BLOB + 1);
	}

	////////////////////////////////////////////////////////////////////////

	public void skip(long len) throws IOException {
		offset += len;
		if (offset > head) {
			skip0(offset - head);
			tailOffset += len;
			head = 0;
			offset = 0;
	}}

	public IStringPool stringPool() {
		return stringPool;
	}

	public IBytePool blobPool() {
		return blobPool;
	}

	public long fileOffset() {
		return tailOffset + offset;
	}

	////////////////////////////////////////////////////////////////////////

	public int peek() throws IOException {
		if (offset >= head)
			fill(1);
		return buffer[offset] & 0xff;
	}

	public void read(byte[] a) throws IOException {
		read(a, 0, a.length);
	}

	public void read(byte[] a, int start, int len) throws IOException {
		while (len > 0) {
			int n = len;
			if (offset + len > head) {
				fill(len);
				if (len > head - offset) {
					n = head - offset;
			}}
			System.arraycopy(buffer, offset, a, start, n);
			offset += n;
			start += n;
			len -= n;
	}}

	public int readI8() throws IOException {
		if (offset >= head)
			fill(1);
		return buffer[offset++];
	}

	public int readV32() throws IOException {
		if (offset >= head)
			fill(1);
		int v = buffer[offset++];
		if ((v & 1) == 0)
			return v >> 1;
		int count = ((v >> 1) & 0x7) + 1;
		if (offset + count > head)
			fill(count);
		v >>= 4;
		if (count <= 4) {
			while (--count >= 0)
				v = v << 8 | (buffer[offset++] & 0xff);
			return v;
		}
		long lret = v;
		while (--count >= 0)
			lret = lret << 8 | (buffer[offset++] & 0xff);
		long sign = lret & ~LSB31L;
		if (sign == 0 || sign == ~LSB31L)
			return (int)lret;
		throw new BxdException(String.format("V32 out of range: 0x%016x", lret), fileOffset());
	}

	public long readV64() throws IOException {
		if (offset >= head)
			fill(1);
		int v = buffer[offset++];
		if ((v & 1) == 0)
			return v >> 1;
		int count = ((v >> 1) & 0x7) + 1;
		if (offset + count > head)
			fill(count);
		long ret = v >> 4;
		while (--count >= 0)
			ret = ret << 8 | (buffer[offset++] & 0xff);
		return ret;
	}
	public int readU8() throws IOException {
		if (offset >= head)
			fill(1);
		return buffer[offset++] & 0xff;
	}

	/**
	 * Read a U31 value as int.
	 * @return Unsigned value, >=0, <0 for special flags (eg. -1 for null value)
	 */
	public int readU31() throws IOException {
		if (offset >= head)
			fill(1);
		int v = buffer[offset++];
		if ((v & 0xffffff80) == 0)
			return v;
		int count = v & 0x70;
		v &= 0x0f;
		if (count == 0x00) {
			if (offset >= head)
				fill(1);
			return v << 8 | (buffer[offset++] & 0xff);
		}
		if (count >= 0x40) {
			if (count == 0x70) {
				return ~v;
			}
			throw new BxdException(
				String.format("Invalid length encoding for U31: 0x%02x", count), fileOffset());
		}
		count = (count >> 4) + 1;
		if (offset + count > head)
			fill(count);
		if (count < 4 || count == 4 && v == 0) {
			while (--count >= 0)
				v = v << 8 | (buffer[offset++] & 0xff);
			return v;
		}
		long lret = v;
		while (--count >= 0)
			lret = lret << 8 | (buffer[offset++] & 0xff);
		if (lret <= Integer.MAX_VALUE)
			return (int)lret;
		throw new BxdException(String.format("U31 out of range: 0x%016x", lret), fileOffset());
	}

	protected final int readCount() throws IOException {
		return readU31();
	}

	/**
	 * Read a U32 value as int.
	 */
	protected int readU32() throws IOException {
		if (offset >= head)
			fill(1);
		int v = buffer[offset++];
		if ((v & 0xffffff80) == 0)
			return v;
		int count = v & 0x70;
		v &= 0x0f;
		if (count == 0x00) {
			if (offset >= head)
				fill(1);
			return v << 8 | (buffer[offset++] & 0xff);
		}
		count = (count >> 4) + 1;
		if (offset + count > head)
			fill(count);
		if (count < 4 || count == 4 && v == 0) {
			while (--count >= 0)
				v = v << 8 | (buffer[offset++] & 0xff);
			return v;
		}
		long lret = v;
		while (--count >= 0)
			lret = lret << 8 | (buffer[offset++] & 0xff);
		if (lret <= Integer.MAX_VALUE)
			return (int)lret;
		throw new BxdException(String.format("U32 out of range: 0x%016x", lret), fileOffset());
	}

	/**
	 * Read a U64 value as long.
	 */
	protected long readU64() throws IOException {
		if (offset >= head)
			fill(1);
		int v = buffer[offset++];
		if ((v & 0xffffff80) == 0)
			return v;
		int count = v & 0x70;
		v &= 0x0f;
		if (count == 0x00) {
			if (offset >= head)
				fill(1);
			return v << 8 | (buffer[offset++] & 0xff);
		}
		count = (count >> 4) + 1;
		if (offset + count > head)
			fill(count);
		long ret = v;
		if (count < 8 || count == 8 && v == 0) {
			while (--count >= 0)
				ret = ret << 8 | (buffer[offset++] & 0xff);
			return ret;
		}
		throw new BxdException(String.format("U64 out of range"), fileOffset());
	}

	public float readFloat() throws IOException {
		return Float.intBitsToFloat(readV32());
	}

	public double readDouble() throws IOException {
		return Double.longBitsToDouble(readV64());
	}

	public CharSequence readString() throws IOException {
		int len = readCount();
		if (len == -1)
			return null;
		byte[] b = new byte[len];
		read(b, 0, len);
		return new BxdString(b, 0, len);
	}

	public byte[] readBlob() throws IOException {
		int len = readCount();
		if (len == -1)
			return null;
		byte[] b = new byte[len];
		read(b, 0, len);
		return b;
	}

	////////////////////////////////////////////////////////////////////////

	public void match(int v) throws IOException {
		if (offset >= head)
			fill(1);
		int n = buffer[offset++];
		if (n != v)
			throw new BxdException(
				String.format("Match error: expected=0x%02x, actula=0x%02x", v, n), fileOffset() - 1);
	}

	public void match(byte[] a) throws IOException {
		match(a, 0, a.length);
	}

	public void match(byte[] a, int start, int len) throws IOException {
		if (offset + len > head)
			fill(len);
		byte v, aa;
		while (--len >= 0) {
			v = buffer[offset++];
			aa = a[start++];
			if (v != aa)
				throw new BxdException(
					String.format("Match error: expected=0x%02x, actula=0x%02x", aa, v),
					fileOffset() - 1);
	}}

	public CharSequence getString(int id) {
		if (id <= 0)
			return -id < TagNames.length ? TagNames[-id] : null;
		return stringPool.get(id);
	}

	public byte[] getBlob(int id) {
		return blobPool.get(id);
	}

	public IBxdHeader readHeader() throws IOException {
		match(MAGIC);
		BxdHeader header = new BxdHeader();
		header.major = read16();
		header.minor = read16();
		header.flags = read64();
		skip(HEADER_PADDING.length);
		if (header.major != IBxxConstants.MAJOR)
			throw new RuntimeException(
				"ERROR: Invalid file format version: expected="
					+ IBxxConstants.MAJOR
					+ ", actual="
					+ header.major);
		mark();
		for (int tag = readTag(); tag == TagStringDef || tag == TagBlobDef;) {
			if (tag == TagStringDef)
				stringDef();
			else
				blobDef();
			mark();
			tag = readTag();
		}
		rewind();
		return header;
	}

	public IBxdTrailer readTrailer() throws IOException {
		IBxdTrailer ret = new BxdTrailer();
		readSignatures(ret); 
		return ret;
	}

	public void readSignatures(IBxdTrailer ret) throws IOException {
		for (int kind = readU31();; kind = readU31()) {
			if (kind == SigNONE)
				break;
			int tag = readTag();
			if (tag == TagString) {
				CharSequence value = readString();
				checkSignature(kind, value);
			} else if (tag == TagBlob) {
				byte[] value = readBlob();
				checkSignature(kind, value);
			} else {
				throw new AssertionError("ASSERT: Invalid value type: " + tag);
	}}}

	////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////

	protected void resetBuffer(long offset) {
		this.head = 0;
		this.offset = 0;
		this.tailOffset = offset;
		this.isEOF = false;
	}

	protected void skipBlob() throws IOException {
		int len = readCount();
		if (len > 0) {
			skip(len);
	}}

	protected void skipExtension() throws IOException {
		read();
		skipBlob();
	}

	protected int read() throws IOException {
		if (offset >= head)
			fill(1);
		return buffer[offset++];
	}

	protected int read16() throws IOException {
		if (offset + 2 > head)
			fill(2);
		return (((buffer[offset++] & 0xff) << 8) | (buffer[offset++] & 0xff));
	}

	protected int read32() throws IOException {
		if (offset + 4 > head)
			fill(4);
		return (((((buffer[offset++] << 8) //
			| (buffer[offset++] & 0xff)) << 8) //
			| (buffer[offset++] & 0xff)) << 8) //
			| (buffer[offset++] & 0xff);
	}

	protected long read64() throws IOException {
		if (offset + 8 > head)
			fill(8);
		int u = (((((buffer[offset++] << 8) //
			| (buffer[offset++] & 0xff)) << 8) //
			| (buffer[offset++] & 0xff)) << 8) //
			| (buffer[offset++] & 0xff);
		int l = (((((buffer[offset++] << 8) //
			| (buffer[offset++] & 0xff)) << 8) //
			| (buffer[offset++] & 0xff)) << 8) //
			| (buffer[offset++] & 0xff);
		return ((long)u << 32) | (l & LSB32L);
	}

	protected final int readTag() throws IOException {
		return readV32();
	}

	protected void stringDef() throws IOException {
		int id = readU31();
		CharSequence s = readString();
		int sid = stringPool.intern(s);
		if (sid != id)
			throw new AssertionError("ASSERT: StringId not match, expected=" + id + ", actual=" + sid);
	}

	protected void blobDef() throws IOException {
		int id = readU31();
		byte[] a = readBlob();
		int ret = blobPool.intern(a);
		if (ret != id)
			throw new AssertionError("ASSERT: BlobId not match, expected=" + id + ", actual=" + ret);
	}

	protected void checkSignature(int kind, CharSequence value) {
		// TODO
	}

	protected void checkSignature(int kind, byte[] value) {
		// TODO
	}

	protected void mark() {
		this.mark = tailOffset + offset;
	}

	protected void mark(long fileposition) {
		this.mark = fileposition;
	}

	protected void unmark() {
		this.mark = 0;
	}

	protected void rewind() {
		int newoffset = (int)(mark - tailOffset);
		if (newoffset < offset)
			isEOF = false;
		offset = newoffset;
		mark = 0;
	}

	////////////////////////////////////////////////////////////////////////

	/**
	 * Fill to make sure require number of byte available.
	 * Grow buffer if neccessary.
	 */
	private void fill(int require) throws IOException {
		if (isEOF)
			throw new IOException("Reading pass EOF");
		int keep = head - offset;
		int cap = buffer.length;
		if (mark > 0) {
			long c = head + tailOffset - mark;
			if (c > keep)
				keep = (int)c;
			if (keep >= cap)
				throw new IOException("Buffer underrun, buffer.length=" + cap + ", keep=" + keep);
		}
		int notkeep = head - keep;
		if (keep + require > cap) {
			cap = keep + require + BUFSIZE;
			byte[] b = new byte[cap];
			System.arraycopy(buffer, notkeep, b, 0, keep);
			buffer = b;
		} else if (offset > 0 && keep > 0) {
			System.arraycopy(buffer, notkeep, buffer, 0, keep);
		}
		tailOffset += notkeep;
		offset -= notkeep;
		head = keep;
		int toread = cap - keep;
		int n = 0;
		while (toread > 0) {
			n = read0(buffer, head += n, toread -= n);
			if (n < 0) {
				isEOF = true;
				break;
		}}
		if (require > head - offset)
			throw new IOException(
				"Reading pass EOF: required=" + require + ", available=" + (head - offset));
	}

	////////////////////////////////////////////////////////////////////////
}
