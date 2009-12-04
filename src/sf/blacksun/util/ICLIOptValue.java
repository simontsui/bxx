/*
 * Copyright (c) 2008, Chris Leung, simontsui. All rights reserved.
 *
 * The software in this package is distributed under the GNU General Public
 * License version 2, as published by the Free Software Foundation, but with
 * the Classpath exception.  You should have received a copy of the GNU General
 * Public License (GPL) and the Classpath exception along with this program.
 */
package sf.blacksun.util;

import java.util.List;

public interface ICLIOptValue<T> {

	/** @return true if option value is (ignore case) "true", otherwise return false. */
	boolean getBool();
	/** @return int value of option 'optname', def if option value is not specified. */
	long getLong();
	/** @return String value of option 'optname'. */
	String getString();
	List<String> getStringList(List<String> ret);
}
