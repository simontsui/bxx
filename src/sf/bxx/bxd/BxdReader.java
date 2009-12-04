/*
 * Copyright (c) 2008, Chris Leung, simontsui. All rights reserved.
 *
 * The software in this package is distributed under the GNU General Public
 * License version 2, as published by the Free Software Foundation, but with
 * the Classpath exception.  You should have received a copy of the GNU General
 * Public License (GPL) and the Classpath exception along with this program.
 */
package sf.bxx.bxd;

import java.io.IOException;
import java.io.InputStream;

import sf.bxx.bxd.sax.IBxdHandler;

public class BxdReader extends AbstractBxdReader {

	////////////////////////////////////////////////////////////////////////

	private InputStream input;

	////////////////////////////////////////////////////////////////////////

	public BxdReader(InputStream in) {
		this(in, NO_HANDLER);
	}

	public BxdReader(InputStream in, IBxdHandler handler) {
		super(handler);
		this.input = in;
	}

	public void close() throws IOException {
		input.close();
	}

	////////////////////////////////////////////////////////////////////////

	@Override
	protected int read0(byte[] a, int start, int len) throws IOException {
		return input.read(a, start, len);
	}

	@Override
	protected void skip0(long len) throws IOException {
		long n = input.skip(len);
		if (n != len)
			throw new IOException("Skip failed, expected = " + len + ", actual = " + n);
	}

	@Override
	protected void initPools() throws IOException {
		throw new UnsupportedOperationException();
	}

	////////////////////////////////////////////////////////////////////////
}
