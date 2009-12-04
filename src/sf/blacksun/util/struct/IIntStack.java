package sf.blacksun.util.struct;

/*
 * Copyright (c) 2004, Chris Leung, simontsui. All rights reserved.
 *
 * The software in this package is distributed under the GNU General Public
 * License version 2, as published by the Free Software Foundation, but with
 * the Classpath exception.  You should have received a copy of the GNU General
 * Public License (GPL) and the Classpath exception along with this program.
 */

public interface IIntStack extends Cloneable {

	int size();
	int get(int index);
	boolean isEmpty();
}
