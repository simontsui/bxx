/*
 * Copyright (c) 2007, Chris Leung, simontsui. All rights reserved.
 *
 * The software in this package is distributed under the GNU General Public
 * License version 2, as published by the Free Software Foundation, but with
 * the Classpath exception.  You should have received a copy of the GNU General
 * Public License (GPL) and the Classpath exception along with this program.
 */
package sf.blacksun.util.io;

import java.io.IOException;
import java.io.OutputStream;

public class ByteIOUtil {


	private ByteIOUtil() {
	}


	public static void write16BE(OutputStream output, int value) throws IOException {
		byte[] b = new byte[2];
		b[0] = (byte)((value >> 8) & 0xff);
		b[1] = (byte)(value & 0xff);
		output.write(b);
	}


	public static void write32BE(OutputStream output, int value) throws IOException {
		byte[] b = new byte[4];
		b[0] = (byte)((value >> 24) & 0xff);
		b[1] = (byte)((value >> 16) & 0xff);
		b[2] = (byte)((value >> 8) & 0xff);
		b[3] = (byte)((value) & 0xff);
		output.write(b);
	}
}
