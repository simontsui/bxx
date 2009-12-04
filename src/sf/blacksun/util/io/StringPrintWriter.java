/*
 * Copyright (c) 2009, Chris Leung, simontsui. All rights reserved.
 *
 * The software in this package is distributed under the GNU General Public
 * License version 2, as published by the Free Software Foundation, but with
 * the Classpath exception.  You should have received a copy of the GNU General
 * Public License (GPL) and the Classpath exception along with this program.
 */
package sf.blacksun.util.io;

import java.io.PrintWriter;
import java.io.StringWriter;

public class StringPrintWriter extends PrintWriter {

	private StringWriter stringWriter;

	public StringPrintWriter() {
		super(new StringWriter());
		this.stringWriter = (StringWriter)out;
	}


	public String toString() {
		stringWriter.flush();
		return stringWriter.toString();
	}
}
