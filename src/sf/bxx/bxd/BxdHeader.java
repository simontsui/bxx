/*
 * Copyright (c) 2008, Chris Leung, simontsui. All rights reserved.
 *
 * The software in this package is distributed under the GNU General Public
 * License version 2, as published by the Free Software Foundation, but with
 * the Classpath exception.  You should have received a copy of the GNU General
 * Public License (GPL) and the Classpath exception along with this program.
 */
package sf.bxx.bxd;

public class BxdHeader implements IBxdHeader {

	int major = 2;
	int minor = 0;
	long flags = 0;

	public BxdHeader() {
	}

	public BxdHeader(long flags) {
		this.flags = flags;
	}

	public long flags() {
		return flags;
	}

	public int major() {
		return major;
	}

	public int minor() {
		return minor;
	}
}
