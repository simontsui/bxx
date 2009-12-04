/*
 * Copyright (c) 2008, Chris Leung, simontsui. All rights reserved.
 *
 * The software in this package is distributed under the GNU General Public
 * License version 2, as published by the Free Software Foundation, but with
 * the Classpath exception.  You should have received a copy of the GNU General
 * Public License (GPL) and the Classpath exception along with this program.
 */
package sf.bxx.bxd;

public class BxdElement implements IBxdElement {

	private int name;
	private IBxdAttributes attributes;

	public BxdElement() {
		this.name = 0;
		this.attributes = new BxdAttributes();
	}

	public BxdElement(int name, IBxdAttributes attrs) {
		this.name = name;
		this.attributes = attrs;
	}

	public int name() {
		return name;
	}

	public IBxdAttributes attributes() {
		return attributes;
	}
}
