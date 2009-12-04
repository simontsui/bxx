/*
 * Copyright (c) 2008, Chris Leung, simontsui. All rights reserved.
 *
 * The software in this package is distributed under the GNU General Public
 * License version 2, as published by the Free Software Foundation, but with
 * the Classpath exception.  You should have received a copy of the GNU General
 * Public License (GPL) and the Classpath exception along with this program.
 */
package sf.bxx.bxd.sax;

import java.io.IOException;

import sf.bxx.bxd.IBxdAttributes;
import sf.bxx.bxd.IBxdHeader;
import sf.bxx.bxd.IBxdTrailer;
import sf.bxx.bxd.IBxdValue;

public class BxdHandlerAdapter implements IBxdHandler {

	public void startDocument() throws IOException {
	}

	public void header(IBxdHeader header) throws IOException {
	}

	public void startDoctype(int name, IBxdAttributes attrs) throws IOException {
	}

	public void startDeclaration(int name, IBxdAttributes attrs) throws IOException {
	}

	public void startPI(int name, IBxdAttributes attrs) throws IOException {
	}

	public void startElement(int name, IBxdAttributes attrs, boolean empty) throws IOException {
	}

	public void endElement(int name) throws IOException {
	}

	public void cdata(IBxdValue text) throws IOException {
	}

	public void comment(IBxdValue text) throws IOException {
	}

	public void text(IBxdValue content) throws IOException {
	}

	public void trailer(IBxdTrailer tailer) throws IOException {
	}

	public void endDocument() throws IOException {
	}
}
