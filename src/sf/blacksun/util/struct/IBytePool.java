package sf.blacksun.util.struct;


//TEMPLATE_BEGIN

/*
 * Copyright (c) 2008, Chris Leung, simontsui. All rights reserved.
 *
 * The software in this package is distributed under the GNU General Public
 * License version 2, as published by the Free Software Foundation, but with
 * the Classpath exception.  You should have received a copy of the GNU General
 * Public License (GPL) and the Classpath exception along with this program.
 */

public interface IBytePool extends Iterable<byte[]> {
	int intern(byte[] s);
	int intern(byte[] s, int start, int end);
	byte[] get(int index);
	int size();
}
