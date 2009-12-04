/*
 * Copyright (c) 2009, Chris Leung, simontsui. All rights reserved.
 *
 * The software in this package is distributed under the GNU General Public
 * License version 2, as published by the Free Software Foundation, but with
 * the Classpath exception.  You should have received a copy of the GNU General
 * Public License (GPL) and the Classpath exception along with this program.
 */
package sf.blacksun.util;

import sf.blacksun.util.CLIUtil.InvalidOptionException;

public interface ICLIOption<T> extends Comparable<ICLIOption<T>> {

	public String getShortOpt();
	public String getLongOpt();
	public boolean optionalArgs();
	public boolean hasArg();
	public boolean arrayArgs();
	public String getDescription();
	public ICLIOptValue<T> fromString(String...values) throws InvalidOptionException;
}
