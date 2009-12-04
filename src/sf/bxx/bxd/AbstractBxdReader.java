/*
 * Copyright (c) 2008, Chris Leung, simontsui. All rights reserved.
 *
 * The software in this package is distributed under the GNU General Public
 * License version 2, as published by the Free Software Foundation, but with
 * the Classpath exception.  You should have received a copy of the GNU General
 * Public License (GPL) and the Classpath exception along with this program.
 */
package sf.bxx.bxd;

import static sf.bxx.IBxxConstants.TagAbsent;
import static sf.bxx.IBxxConstants.TagBigInt;
import static sf.bxx.IBxxConstants.TagBlob;
import static sf.bxx.IBxxConstants.TagBlobDef;
import static sf.bxx.IBxxConstants.TagBlobRef;
import static sf.bxx.IBxxConstants.TagByte;
import static sf.bxx.IBxxConstants.TagCDATA;
import static sf.bxx.IBxxConstants.TagChar;
import static sf.bxx.IBxxConstants.TagComment;
import static sf.bxx.IBxxConstants.TagDeclaration;
import static sf.bxx.IBxxConstants.TagDoctype;
import static sf.bxx.IBxxConstants.TagDouble;
import static sf.bxx.IBxxConstants.TagEndAttr;
import static sf.bxx.IBxxConstants.TagEndTag;
import static sf.bxx.IBxxConstants.TagExtension;
import static sf.bxx.IBxxConstants.TagFalse;
import static sf.bxx.IBxxConstants.TagFloat;
import static sf.bxx.IBxxConstants.TagInt;
import static sf.bxx.IBxxConstants.TagLong;
import static sf.bxx.IBxxConstants.TagPI;
import static sf.bxx.IBxxConstants.TagShort;
import static sf.bxx.IBxxConstants.TagString;
import static sf.bxx.IBxxConstants.TagStringDef;
import static sf.bxx.IBxxConstants.TagStringRef;
import static sf.bxx.IBxxConstants.TagTrailer;
import static sf.bxx.IBxxConstants.TagTrue;
import static sf.bxx.IBxxConstants.TagUByte;
import static sf.bxx.IBxxConstants.TagUInt;
import static sf.bxx.IBxxConstants.TagULong;
import static sf.bxx.IBxxConstants.TagUShort;

import java.io.IOException;

import sf.blacksun.util.struct.IntList;
import sf.bxx.bxd.sax.BxdHandlerAdapter;
import sf.bxx.bxd.sax.IBxdHandler;

public abstract class AbstractBxdReader extends BxdReaderBase implements IBxdReader {

	////////////////////////////////////////////////////////////////////////

	protected static IBxdHandler NO_HANDLER = new BxdHandlerAdapter();

	////////////////////////////////////////////////////////////////////////

	protected IBxdHandler handler;
	//
	protected BxdHeader header;
	protected IntList tagStack = new IntList();
	protected BxdAttributes attributes = new BxdAttributes();
	protected BxdValue textValue = new BxdValue();

	////////////////////////////////////////////////////////////////////////

	protected abstract int read0(byte[] a, int start, int len) throws IOException;
	protected abstract void skip0(long len) throws IOException;
	protected abstract void initPools() throws IOException;

	////////////////////////////////////////////////////////////////////////

	public AbstractBxdReader(IBxdHandler handler) {
		super();
		this.handler = handler;
	}

	public AbstractBxdReader(IBxdHandler handler, int bufsize) {
		super(bufsize);
		this.handler = handler;
	}

	////////////////////////////////////////////////////////////////////////

	public void setHandler(IBxdHandler handler) {
		this.handler = handler;
	}

	public IBxdHeader getHeader() {
		return header;
	}

	public void parse() throws BxdException {
		try {
			boolean isattr = false;
			IBxdHeader header = readHeader();
			handler.header(header);
			handler.startDocument();
			DONE: while (true) {
				int tag = readTag();
				if (isattr) {
					switch (tag) {
					case TagStringDef:
						stringDef();
						break;
					case TagBlobDef:
						blobDef();
						break;
					case TagEndAttr:
						endAttr();
						isattr = false;
						break;
					case TagEndTag:
						endTag(true);
						isattr = false;
						break;
					default :
						if (tag <= 0) {
							throw new BxdException(
								String.format(
									"Invalid tag in attribute context: 0x%02x", tag),
								fileOffset());
						}
						readAttrValue(attributes, tag);
				}} else {
					switch (tag) {
					case TagStringDef:
						stringDef();
						break;
					case TagBlobDef:
						blobDef();
						break;
					case TagTrailer:
						IBxdTrailer trailer = readTrailer();
						handler.trailer(trailer);
						break DONE;
					case TagDoctype:
						tagStack.push(readU31());
						tagStack.push(tag);
						attributes.clear();
						isattr = true;
						break;
					case TagDeclaration:
						tagStack.push(readU31());
						tagStack.push(tag);
						attributes.clear();
						isattr = true;
						break;
					case TagPI:
						tagStack.push(readU31());
						tagStack.push(tag);
						attributes.clear();
						isattr = true;
						break;
					case TagComment:
						readComment();
						break;
					case TagCDATA:
						readCDATA();
						break;
					case TagAbsent:
					case TagFalse:
					case TagTrue:
					case TagUByte:
					case TagUShort:
					case TagUInt:
					case TagULong:
					case TagByte:
					case TagShort:
					case TagInt:
					case TagLong:
					case TagBigInt:
					case TagFloat:
					case TagDouble:
					case TagChar:
					case TagString:
					case TagBlob:
					case TagStringRef:
					case TagBlobRef:
						readText(tag);
						break;
					case TagExtension:
						skipExtension();
						break;
					case TagEndAttr:
						throw new BxdException(
							"End attribute when not in attribute context", fileOffset());
					case TagEndTag:
						endTag(false);
						break;
					default :
						if (tag <= 0) {
							throw new BxdException(
								String.format(
									"Expecting start tag, actual=0x%02x", tag),
								fileOffset());
						}
						tagStack.push(tag);
						attributes.clear();
						isattr = true;
			}}}
			handler.endDocument();
			this.handler = null;
		} catch (IOException e) {
			throw new BxdException(e, fileOffset());
	}}

	////////////////////////////////////////////////////////////////////////

	public void readComment() throws IOException {
		readValue(textValue, readTag());
		handler.comment(textValue);
	}

	public void readCDATA() throws IOException {
		readValue(textValue, readTag());
		handler.cdata(textValue);
	}

	public void readText(int tag) throws IOException {
		readValue(textValue, tag);
		handler.text(textValue);
	}

	////////////////////////////////////////////////////////////////////////

	protected void endAttr() throws IOException {
		int tag = tagStack.peek();
		if (tag <= 0)
			throw new BxdException(String.format("Invalid end tag: 0x%02x", tag), fileOffset());
		handler.startElement(tag, attributes, false);
	}

	protected void endTag(boolean isattr) throws IOException {
		int tag = tagStack.pop();
		if (isattr) {
			switch (tag) {
			case TagDoctype:
				handler.startDoctype(tagStack.pop(), attributes);
				break;
			case TagDeclaration:
				handler.startDeclaration(tagStack.pop(), attributes);
				break;
			case TagPI:
				handler.startPI(tagStack.pop(), attributes);
				break;
			default :
				if (tag <= 0)
					throw new BxdException(
						String.format("Invalid end tag: 0x%02x", tag), fileOffset());
				handler.startElement(tag, attributes, true);
		}} else {
			if (tag <= 0)
				throw new BxdException(String.format("Invalid end tag: 0x%02x", tag), fileOffset());
			handler.endElement(tag);
	}}

	////////////////////////////////////////////////////////////////////////

	protected void readAttrValue(IBxdAttributes attributes, int name) throws IOException {
		while (true) {
			int kind = readTag();
			switch (kind) {
			case TagStringDef:
				stringDef();
				continue;
			case TagBlobDef:
				blobDef();
				continue;
			case TagAbsent:
			case TagFalse:
			case TagTrue:
				attributes.putAttr(name, kind);
				break;
			case TagUByte:
				attributes.putAttr(name, kind, readU8());
				break;
			case TagUShort:
				attributes.putAttr(name, kind, readU32());
				break;
			case TagUInt:
				attributes.putAttr(name, kind, readU32());
				break;
			case TagULong:
				attributes.putAttr(name, kind, readU64());
				break;
			case TagByte:
				attributes.putAttr(name, kind, readI8());
				break;
			case TagShort:
				attributes.putAttr(name, kind, readV32());
				break;
			case TagInt:
				attributes.putAttr(name, kind, readV32());
				break;
			case TagLong:
				attributes.putAttr(name, kind, readV64());
				break;
			case TagFloat:
				attributes.putAttr(name, kind, readV32());
				break;
			case TagDouble:
				attributes.putAttr(name, kind, readV64());
				break;
			case TagChar:
			case TagStringRef:
			case TagBlobRef:
				attributes.putAttr(name, kind, readU31());
				break;
			case TagString:
				attributes.putAttr(name, kind, readBlob());
				break;
			case TagBlob:
			case TagBigInt:
				attributes.putAttr(name, kind, readBlob());
				break;
			default :
				throw new BxdException(
					String.format("Invalid attribute value type: 0x%02x", kind), fileOffset());
			}
			break;
	}}

	protected void readValue(BxdValue value, int kind) throws IOException {
		while (true) {
			value.clear();
			value.kind = kind;
			switch (kind) {
			case TagStringDef:
				stringDef();
				continue;
			case TagBlobDef:
				blobDef();
				continue;
			case TagAbsent:
			case TagFalse:
			case TagTrue:
				break;
			case TagUByte:
				value.longValue = readU8();
				break;
			case TagUShort:
			case TagUInt:
				value.longValue = readU32();
				break;
			case TagULong:
				value.longValue = readU64();
				break;
			case TagByte:
				value.longValue = readI8();
				break;
			case TagShort:
			case TagInt:
			case TagFloat:
				value.longValue = readV32();
				break;
			case TagLong:
			case TagDouble:
				value.longValue = readV64();
				break;
			case TagChar:
				value.longValue = readU31();
				break;
			case TagBigInt:
			case TagString:
			case TagBlob:
				value.blobValue = readBlob();
				break;
			case TagStringRef:
				value.stringValue = stringPool.get(readU31());
				break;
			case TagBlobRef:
				value.blobValue = blobPool.get(readU31());
				break;
			default :
				throw new BxdException(
					String.format("Invalid text value type: 0x%02x", kind), fileOffset());
			}
			break;
	}}

	////////////////////////////////////////////////////////////////////////
}
