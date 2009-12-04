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
import static sf.bxx.IBxxConstants.TagNames;
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

import sf.blacksun.util.struct.IIntStack;
import sf.blacksun.util.struct.IntList;
import sf.bxx.BxxUtil;

public abstract class AbstractBxdWalker extends BxdReaderBase implements IBxdWalker {

	////////////////////////////////////////////////////////////////////////

	BxdHeader header = new BxdHeader();
	BxdAttributes attributes = new BxdAttributes();
	BxdAttribute attribute = new BxdAttribute();
	BxdValue value = new BxdValue();
	State state = State.HEADER;
	int current;
	int tag;
	CharSequence target = null;
	IntList tagStack = new IntList();


	private enum State {
		HEADER, TAG, STARTATTR, ATTR, VALUE, ENDATTR, ENDTAG, TRAILER, 
	}

	////////////////////////////////////////////////////////////////////////

	protected abstract int read0(byte[] a, int start, int len) throws IOException;
	protected abstract void skip0(long len) throws IOException;
	protected abstract void initPools() throws IOException;

	////////////////////////////////////////////////////////////////////////

	public AbstractBxdWalker() {
	}

	public AbstractBxdWalker(int bufsize) {
		super(bufsize);
	}

	////////////////////////////////////////////////////////////////////////

	public CharSequence target() {
		return target;
	}

	public IBxdHeader getHeader() {
		return header;
	}

	////////////////////////////////////////////////////////////////////////

	public IIntStack tagStack() {
		return tagStack;
	}

	public int current() {
		return current;
	}

	public boolean isValue(int tag) {
		switch (tag) {
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
			return true;
		default :
			return false;
	}}

	public int next() throws BxdException {
		try {
			switch (state) {
			case HEADER:
				readHeader();
				return next();
			case STARTATTR:
				tag = readTag(); 
				state = State.ATTR;
				readAttributes(); 
				current = tag;
				tag = readTag();
				state = State.TAG;
				return current;
			case ATTR:
				for (;; tag = readTag()) {
					switch (tag) {
					case TagStringDef:
						stringDef();
						break;
					case TagBlobDef:
						blobDef();
						break;
					case TagEndAttr:
						current = tag;
						state = State.TAG;
						tag = readTag();
						return current;
					case TagEndTag:
						current = tag;
						state = State.TAG;
						tagStack.pop();
						tag = readTag();
						return current;
					default :
						current = tag;
						readAttributes();
						return current;
				}}
			case TAG:
				for (;; tag = readTag()) {
					switch (tag) {
					case TagStringDef:
						stringDef();
						break;
					case TagBlobDef:
						blobDef();
						break;
					case TagEndTag:
						state = State.TAG;
						current = tag;
						tagStack.pop();
						tag = readTag();
						return current;
					case TagDoctype:
					case TagDeclaration:
					case TagPI:
						state = State.ATTR;
						current = tag;
						tagStack.push(current);
						target = readTarget();
						return current;
					case TagTrailer:
						current = tag;
						return tag;
					case TagComment:
					case TagCDATA:
						current = tag;
						tag = readTag();
						return current;
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
						state = State.VALUE;
						current = tag;
						return tag;
					case TagExtension:
						skipExtension();
						return next();
					default :
						if (tag <= 0)
							throw new BxdException(
								"ERROR: Invalid tag for next(): " + TagNames[tag]);
						state = State.STARTATTR;
						current = tag;
						tagStack.push(current);
						return tag;
				}}
			case VALUE:
				readValue();
				return next();
			case ENDATTR:
				current = tag;
				tag = readTag();
				state = State.TAG;
				return current;
			case ENDTAG:
				current = tag;
				tagStack.pop();
				tag = readTag();
				state = State.TAG;
				return current;
			default :
				throw new BxdException("ERROR: Invalid statei for next(): " + state);
		}} catch (IOException e) {
			tag = 0;
			throw new BxdException(e, fileOffset());
	}}

	public int nextElement() throws BxdException {
		while (true) {
			int t = next();
			switch (t) {
			case TagDoctype:
			case TagDeclaration:
			case TagPI:
			case TagTrailer:
				return t;
			default :
				if (t > 0)
					return t;
	}}}

	////////////////////////////////////////////////////////////////////////

	@Override
	public IBxdHeader readHeader() throws BxdException {
		BxxUtil.assertTagStackEmpty(stringPool, tagStack);
		try {
			IBxdHeader ret = super.readHeader();
			tag = readTag();
			state = State.TAG;
			return ret;
		} catch (IOException e) {
			throw new BxdException(e);
	}}

	@Override
	public IBxdTrailer readTrailer() throws BxdException {
		BxxUtil.assertTagStackEmpty(stringPool, tagStack);
		try {
			IBxdTrailer ret = super.readTrailer();
			tag = 0;
			state = State.TRAILER;
			return ret;
		} catch (IOException e) {
			throw new BxdException(e);
	}}

	protected CharSequence readTarget() throws BxdException {
		try {
			switch (state) {
			case ATTR:
				for (tag = readTag();; tag = readTag()) {
					switch (tag) {
					case TagStringDef:
						stringDef();
						break;
					case TagBlobDef:
						blobDef();
						break;
					default : {
						CharSequence ret = stringPool.get(tag);
						tag = readTag();
						return ret;
				}}}
			default :
				throw new BxdException("Invalid state for readTarget(): " + state, fileOffset());
		}} catch (IOException e) {
			throw new BxdException(e, fileOffset());
	}}

	public IBxdAttribute readAttribute() throws BxdException {
		try {
			switch (state) {
			case STARTATTR:
				tag = readTag();
				state = State.ATTR;
				return readAttribute();
			case ATTR:
				for (;; tag = readTag()) {
					switch (tag) {
					case TagStringDef:
						stringDef();
						break;
					case TagBlobDef:
						blobDef();
						break;
					case TagEndAttr:
						state = State.ENDATTR;
						return null;
					case TagEndTag:
						state = State.ENDTAG;
						return null;
					default :
						if (tag <= 0)
							throw new BxdException(
								"Invalid tag for readAttribute(): " + TagNames[tag],
								fileOffset());
						attribute.clear();
						attribute.name = tag;
						readValue(attribute.value, readTag());
						tag = readTag();
						return attribute;
				}}
			case ENDATTR:
			case ENDTAG:
				return null;
			default :
				throw new BxdException("Invalid state for readAttribute(): " + state, fileOffset());
		}} catch (IOException e) {
			throw new BxdException(e, fileOffset());
	}}

	public IBxdAttributes readAttributes() throws BxdException {
		attributes.clear();
		try {
			switch (state) {
			case STARTATTR:
				tag = readTag();
				state = State.ATTR;
				return readAttributes();
			case ATTR:
				for (;; tag = readTag()) {
					switch (tag) {
					case TagStringDef:
						stringDef();
						break;
					case TagBlobDef:
						blobDef();
						break;
					case TagEndAttr:
						state = State.ENDATTR;
						return attributes;
					case TagEndTag:
						state = State.ENDTAG;
						return attributes;
					default :
						if (tag <= 0)
							throw new BxdException(
								"Invalid tag for readAttributes(): " + TagNames[tag],
								fileOffset());
						BxdAttribute a = attributes.put(tag, TagAbsent);
						readValue(a.value, readTag());
				}}
			case ENDATTR:
			case ENDTAG:
				return null;
			default :
				throw new BxdException("Invalid state for readAttributes(): " + state, fileOffset());
		}} catch (IOException e) {
			throw new BxdException(e, fileOffset());
	}}

	public IBxdValue readValue() throws BxdException {
		try {
			switch (state) {
			case TAG:
			case VALUE:
				for (;; tag = readTag()) {
					switch (tag) {
					case TagStringDef:
						stringDef();
						break;
					case TagBlobDef:
						blobDef();
						break;
					case TagComment:
						value.clear();
						readValue(value, readTag());
						state = State.TAG;
						continue;
					case TagCDATA:
						value.clear();
						readValue(value, readTag());
						state = State.TAG;
						tag = readTag();
						return value;
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
						value.clear();
						readValue(value, tag);
						state = State.TAG;
						tag = readTag();
						return value;
					case TagExtension:
						skipExtension();
						return readValue();
					case TagEndTag:
						return null;
					default :
						throw new BxdException(
							"Invalid tag for readValue(): " + tagname(tag), fileOffset());
				}}
			case ENDATTR:
			case ENDTAG:
				return null;
			default :
				throw new BxdException("Invalid state for readValue(): " + state, fileOffset());
		}} catch (IOException e) {
			throw new BxdException(e, fileOffset());
	}}

	////////////////////////////////////////////////////////////////////////

	/**
	 * @enter tag should have been consumed.
	 * @exit tag is not yet produced.
	 */
	protected void readValue(BxdValue ret, int tag) throws BxdException {
		try {
			for (;; tag = readTag()) {
				switch (tag) {
				case TagStringDef:
					stringDef();
					break;
				case TagBlobDef:
					blobDef();
					break;
				case TagAbsent:
				case TagFalse:
				case TagTrue:
					ret.kind = tag;
					return;
				case TagUByte:
					ret.kind = tag;
					ret.longValue = readU8();
					return;
				case TagUShort:
				case TagUInt:
					ret.kind = tag;
					ret.longValue = readU32();
					return;
				case TagULong:
					ret.kind = tag;
					ret.longValue = readU64();
					return;
				case TagShort:
				case TagInt:
				case TagFloat:
					ret.kind = tag;
					ret.longValue = readV32();
					return;
				case TagLong:
				case TagDouble:
					ret.kind = tag;
					ret.longValue = readV64();
					return;
				case TagBigInt:
					ret.kind = tag;
					ret.blobValue = readBlob();
					return;
				case TagChar:
					ret.kind = tag;
					ret.longValue = readU31();
					return;
				case TagString:
				case TagBlob:
					ret.kind = tag;
					ret.blobValue = readBlob();
					return;
				case TagStringRef:
					ret.kind = tag;
					ret.stringValue = stringPool.get(readU31());
					return;
				case TagBlobRef:
					ret.kind = tag;
					ret.blobValue = blobPool.get(readU31());
					return;
				default :
					throw new BxdException(
						"Invalid tag for readValue(BxdValue): " + tagname(tag), fileOffset());
		}}} catch (IOException e) {
			throw new BxdException(e, fileOffset());
	}}

	private String tagname(int tag) {
		return (tag <= 0 && -tag < TagNames.length ? TagNames[-tag] : String.format("<0x%02x>", tag));
	}

	////////////////////////////////////////////////////////////////////////
}
