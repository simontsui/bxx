/*
 * Copyright (c) 2008, Chris Leung, simontsui. All rights reserved.
 *
 * The software in this package is distributed under the GNU General Public
 * License version 2, as published by the Free Software Foundation, but with
 * the Classpath exception.  You should have received a copy of the GNU General
 * Public License (GPL) and the Classpath exception along with this program.
 */
package sf.bxx.bxd;

import java.math.BigInteger;

public interface IBxdValue {

	int kind();
	boolean boolValue();
	int intValue();
	long longValue();
	float floatValue();
	double doubleValue();
	BigInteger bigIntValue();
	byte[] blobValue();
	CharSequence stringValue();
	CharSequence toCharSequence();
}
