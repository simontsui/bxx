/*
 * Copyright (c) 2008, Chris Leung, simontsui. All rights reserved.
 *
 * The software in this package is distributed under the GNU General Public
 * License version 2, as published by the Free Software Foundation, but with
 * the Classpath exception.  You should have received a copy of the GNU General
 * Public License (GPL) and the Classpath exception along with this program.
 */
package sf.bxx.bxd;

public interface IBxdAttributes extends Iterable<IBxdAttribute> {
	void putAttr(int name, int kind);
	void putAttr(int name, int kind, int value);
	void putAttr(int name, int kind, long value);
	void putAttr(int name, int kind, byte[] value);
	IBxdAttribute getAttr(int name);
	int size();
}
