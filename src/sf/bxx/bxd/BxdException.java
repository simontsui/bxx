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

public class BxdException extends IOException {
	private static final long serialVersionUID = -1765214032444779728L;

	public BxdException() {
	}

	public BxdException(String message, long offset) {
		super(String.format("@%1$d(%1$08x): %2$s", offset, message));
	}

	public BxdException(Throwable e, long offset) {
		super(String.format("@%1$d(%1$08x): %2$s", offset, e.getMessage()), e);
	}

	public BxdException(String message) {
		super(message);
	}

	public BxdException(String message, Throwable cause) {
		super(message, cause);
	}

	public BxdException(Throwable cause) {
		super(cause);
	}
}
