package sf.blacksun.util.struct;

//TEMPLATE_BEGIN
/*
 * Copyright (c) 2004, Chris Leung, simontsui. All rights reserved.
 *
 * The software in this package is distributed under the GNU General Public
 * License version 2, as published by the Free Software Foundation, but with
 * the Classpath exception.  You should have received a copy of the GNU General
 * Public License (GPL) and the Classpath exception along with this program.
 */

/**
 * IIntSequence is a read only random accessible sequence of integers.
 */
public interface IIntSequence extends IIntIterable {

	void copyTo(int[] dst, int dststart, int srcstart, int srcend);
}
