package sf.blacksun.util.text;

//TEMPLATE_BEGIN

/*
 * Copyright (c) 2009, Chris Leung, simontsui. All rights reserved.
 *
 * The software in this package is distributed under the GNU General Public
 * License version 2, as published by the Free Software Foundation, but with
 * the Classpath exception.  You should have received a copy of the GNU General
 * Public License (GPL) and the Classpath exception along with this program.
 */

public interface IName extends ICharSequence {
	/** @return unique id for the name, start from 1. 0 is invalid. */
	int id();
}
