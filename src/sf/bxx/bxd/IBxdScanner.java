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

import sf.blacksun.util.struct.IBytePool;
import sf.blacksun.util.text.IStringPool;

public interface IBxdScanner extends Closeable {
	IBxdHeader getHeader();
	IStringPool stringPool();
	IBytePool blobPool();
	CharSequence getString(int id);
	byte[] getBlob(int id);
	long fileOffset();
}
