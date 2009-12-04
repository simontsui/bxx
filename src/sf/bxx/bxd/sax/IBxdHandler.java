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

public interface IBxdHandler {

	void startDocument() throws IOException;
	void header(IBxdHeader header) throws IOException;
	void startDoctype(int name, IBxdAttributes attrs) throws IOException;
	void startDeclaration(int name, IBxdAttributes attrs) throws IOException;
	void startPI(int name, IBxdAttributes attrs) throws IOException;
	void startElement(int name, IBxdAttributes attrs, boolean empty) throws IOException;
	void endElement(int name) throws IOException;
	void cdata(IBxdValue value) throws IOException;
	void comment(IBxdValue value) throws IOException;
	void text(IBxdValue value) throws IOException;
	void trailer(IBxdTrailer trailer) throws IOException;
	void endDocument() throws IOException;
}
