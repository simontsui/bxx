/*
 * Copyright (c) 2003, Chris Leung, simontsui. All rights reserved.
 *
 * The software in this package is distributed under the GNU General Public
 * License version 2, as published by the Free Software Foundation, but with
 * the Classpath exception.  You should have received a copy of the GNU General
 * Public License (GPL) and the Classpath exception along with this program.
 */
package sf.blacksun.util;

import java.util.List;

public interface ICmdline<T> {

	/** @return true if argCount <= argcount or the given helpopt is true. */
	public abstract boolean isHelp(int argcount, ICLIOption<Boolean> helpopt);
	/** @return true if option value is (ignore case) "true", otherwise return false. */
	public abstract boolean getBool(ICLIOption<Boolean> spec);
	/** @return int value of option 'optname', def if option value is not specified. */
	public abstract long getLong(ICLIOption<Long> spec, long def);
	/** @return String value of option 'optname'. */
	public abstract String getString(ICLIOption<String> spec);
	/** @return String value of option 'optname', def if option value is not specified. */
	public abstract String getString(ICLIOption<String> spec, String def);
	public abstract List<String> getStringList(ICLIOption<String[]> spec);
}
