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

import sf.blacksun.util.text.XmlUtil;
import sf.bxx.IBxxConstants;

public class BxdAttribute implements IBxdAttribute {

	int name;
	BxdValue value;

	public BxdAttribute() {
		value = new BxdValue();
	}

	public BxdAttribute(int name, BxdValue value) {
		this.name = name;
		this.value = value;
	}

	public boolean hasValue() {
		return value.kind != IBxxConstants.TagAbsent;
	}

	public int name() {
		return name;
	}

	public IBxdValue value() {
		return value;
	}

	public BigInteger bigIntValue() {
		return value.bigIntValue();
	}

	public byte[] blobValue() {
		return value.blobValue();
	}

	public void clear() {
		value.clear();
	}

	public boolean boolValue() {
		return value.boolValue();
	}

	public double doubleValue() {
		return value.doubleValue();
	}

	public float floatValue() {
		return value.floatValue();
	}

	public int intValue() {
		return value.intValue();
	}

	public int kind() {
		return value.kind();
	}

	public long longValue() {
		return value.longValue();
	}

	public CharSequence stringValue() {
		return value.stringValue();
	}

	public CharSequence toCharSequence() {
		return value.toCharSequence();
	}

	public CharSequence toString(IBxdScanner pools) throws BxdException {
		return pools.getString(name) + "=" + XmlUtil.quoteAttrValue(value.toCharSequence());
	}
}
