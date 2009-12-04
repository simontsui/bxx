/*
 * Copyright (c) 2008, Chris Leung, simontsui. All rights reserved.
 *
 * The software in this package is distributed under the GNU General Public
 * License version 2, as published by the Free Software Foundation, but with
 * the Classpath exception.  You should have received a copy of the GNU General
 * Public License (GPL) and the Classpath exception along with this program.
 */
package sf.bxx.bxd;

import java.io.Closeable;
import java.io.IOException;
import java.math.BigInteger;

import sf.blacksun.util.struct.IBytePool;
import sf.blacksun.util.text.IStringPool;

public interface IBxdWriter extends Closeable {

	////////////////////////////////////////////////////////////////////////

	public abstract IStringPool stringPool();

	public abstract IBytePool blobPool();

	public abstract long fileOffset();

	public abstract int intern(CharSequence s);

	public abstract void flushStringPool() throws IOException;

	public abstract void flushBlobPool() throws IOException;

	////////////////////////////////////////////////////////////////////////

	/** Write header, flush pools. */
	public abstract void header(IBxdHeader header) throws IOException;

	/** Flush pools, write trailer, flush output stream. */
	public abstract void trailer(IBxdTrailer trailer) throws IOException;

	////////////////////////////////////////////////////////////////////////

	public abstract void xmlDeclaration(String version, String encoding, String standalone) throws IOException;

	public abstract void startDoctype(int tag) throws IOException;

	public abstract void startTag(int tag) throws IOException;

	/** Emit a startTag and followed by an endAttr. */
	public abstract void startSimpleTag(int tag) throws IOException;

	public abstract void startAttr(int name) throws IOException;

	public abstract void startDeclaration(int name) throws IOException;

	public abstract void startPI(int name) throws IOException;

	public abstract void comment(CharSequence s) throws IOException;

	public abstract void comment(char[] s, int start, int len) throws IOException;

	public abstract void cdata(CharSequence s) throws IOException;

	public abstract void cdata(char[] s, int start, int len) throws IOException;

	public abstract void endAttr() throws IOException;

	public abstract void endTag() throws IOException;

	////////////////////////////////////////////////////////////////////////

	public abstract void startDoctype(CharSequence name) throws IOException;

	/**
	 * Intern the tag name and write StringDef if neccessary, then write the stringId.
	 */
	public abstract int startTag(CharSequence name) throws IOException;

	/**
	 * Intern the tag name and write StringDef if neccessary, then write the stringId and an endAttr.
	 */
	public abstract int startSimpleTag(CharSequence name) throws IOException;

	/**
	 * Intern the attribute name and write StringDef if neccessary, then write the stringId.
	 */
	public abstract int startAttr(CharSequence name) throws IOException;

	public abstract void declaration(CharSequence name, CharSequence data) throws IOException;

	public abstract void pi(CharSequence name, CharSequence data) throws IOException;

	////////////////////////////////////////////////////////////////////////

	public abstract void writeBlobDef(int id, byte[] value) throws IOException;

	public abstract void writeBlobDef(int id, byte[] value, int start, int end) throws IOException;

	////////////////////////////////////////////////////////////////////////

	public abstract void attribute(int name, boolean value) throws IOException;

	public abstract void attribute(int name, int value) throws IOException;

	public abstract void attribute(int name, long value) throws IOException;

	public abstract void attribute(int name, BigInteger value) throws IOException;

	public abstract void attribute(int name, float value) throws IOException;

	public abstract void attribute(int name, double value) throws IOException;

	public abstract void attribute(int name, CharSequence value) throws IOException;

	public abstract void attribute(int name, byte[] value) throws IOException;

	public abstract void attribute(int name, byte[] value, int start, int end) throws IOException;

	////////////////////////////////////////////////////////////////////////

	public abstract void attribute(CharSequence name, boolean value) throws IOException;

	public abstract void attribute(CharSequence name, int value) throws IOException;

	public abstract void attribute(CharSequence name, long value) throws IOException;

	public abstract void attribute(CharSequence name, BigInteger value) throws IOException;

	public abstract void attribute(CharSequence name, float value) throws IOException;

	public abstract void attribute(CharSequence name, double value) throws IOException;

	public abstract void attribute(CharSequence name, CharSequence value) throws IOException;

	public abstract void attribute(CharSequence name, byte[] value) throws IOException;

	public abstract void attribute(CharSequence name, byte[] value, int start, int end) throws IOException;

	////////////////////////////////////////////////////////////////////////

	public abstract void element(int name, boolean value) throws IOException;

	public abstract void element(int name, int value) throws IOException;

	public abstract void element(int name, long value) throws IOException;

	public abstract void element(int name, BigInteger value) throws IOException;

	public abstract void element(int name, float value) throws IOException;

	public abstract void element(int name, double value) throws IOException;

	public abstract void element(int name, CharSequence value) throws IOException;

	public abstract void element(int name, byte[] value) throws IOException;

	public abstract void element(int name, byte[] value, int start, int end) throws IOException;

	////////////////////////////////////////////////////////////////////////

	public abstract void element(CharSequence name, boolean value) throws IOException;

	public abstract void element(CharSequence name, int value) throws IOException;

	public abstract void element(CharSequence name, long value) throws IOException;

	public abstract void element(CharSequence name, BigInteger value) throws IOException;

	public abstract void element(CharSequence name, float value) throws IOException;

	public abstract void element(CharSequence name, double value) throws IOException;

	public abstract void element(CharSequence name, CharSequence value) throws IOException;

	public abstract void element(CharSequence name, byte[] value) throws IOException;

	public abstract void element(CharSequence name, byte[] value, int start, int end) throws IOException;

	////////////////////////////////////////////////////////////////////////

	public abstract void writeBool(boolean value) throws IOException;

	public abstract void writeUByte(int value) throws IOException;

	public abstract void writeUShort(int value) throws IOException;

	public abstract void writeUInt(int value) throws IOException;

	public abstract void writeULong(long value) throws IOException;

	public abstract void writeByte(int value) throws IOException;

	public abstract void writeShort(int value) throws IOException;

	public abstract void writeInt(int value) throws IOException;

	public abstract void writeLong(long value) throws IOException;

	public abstract void writeBigInt(BigInteger value) throws IOException;

	public abstract void writeFloat(float value) throws IOException;

	public abstract void writeDouble(double value) throws IOException;

	public abstract void writeChar(int value) throws IOException;

	public abstract void writeString(CharSequence value) throws IOException;

	public abstract void writeString(char[] value, int start, int end) throws IOException;

	public abstract void writeBlob(byte[] value) throws IOException;

	public abstract void writeBlob(byte[] value, int start, int end) throws IOException;

	/** Intern the value and write StringDef if neccessary, the write the value as <stringref>. */
	public abstract int writeStringRef(CharSequence name) throws IOException;

	/** Intern the value and write BlobDef if neccessary, the write the value as <blobref>. */
	public abstract int writeBlobRef(byte[] name) throws IOException;

	/** Intern the value and write BlobDef if neccessary, the write the value as <blobref>. */
	public abstract int writeBlobRef(byte[] name, int start, int end) throws IOException;

	public abstract void writeStringDef(int id, CharSequence value) throws IOException;

	////////////////////////////////////////////////////////////////////////

	/** @return The minimium number of bytes required to encode the given value. */
	public abstract int countU31(int v);

	/** @return The minimium number of bytes required to encode the given value. */
	public abstract int countV32(int v);

	/** @return The minimium number of bytes required to encode the given value. */
	public abstract int countV64(long v);

	////////////////////////////////////////////////////////////////////////
}
