/*
 * Copyright (c) 2008, Chris Leung, simontsui. All rights reserved.
 *
 * The software in this package is distributed under the GNU General Public
 * License version 2, as published by the Free Software Foundation, but with
 * the Classpath exception.  You should have received a copy of the GNU General
 * Public License (GPL) and the Classpath exception along with this program.
 */
package sf.bxx.bxd.tools;

import static sf.bxx.IBxxConstants.TagAbsent;
import static sf.bxx.IBxxConstants.TagBigInt;
import static sf.bxx.IBxxConstants.TagBlob;
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
import static sf.bxx.IBxxConstants.TagFalse;
import static sf.bxx.IBxxConstants.TagFloat;
import static sf.bxx.IBxxConstants.TagInt;
import static sf.bxx.IBxxConstants.TagLong;
import static sf.bxx.IBxxConstants.TagNames;
import static sf.bxx.IBxxConstants.TagPI;
import static sf.bxx.IBxxConstants.TagShort;
import static sf.bxx.IBxxConstants.TagString;
import static sf.bxx.IBxxConstants.TagStringRef;
import static sf.bxx.IBxxConstants.TagTrailer;
import static sf.bxx.IBxxConstants.TagTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import sf.blacksun.util.FileUtil;
import sf.blacksun.util.struct.IntList;
import sf.blacksun.util.text.TextUtil;
import sf.bxx.bxd.BxdException;
import sf.bxx.bxd.BxdValue;
import sf.bxx.bxd.BxdWalker;
import sf.bxx.bxd.IBxdAttribute;
import sf.bxx.bxd.IBxdValue;

public class DumpBxd extends BxdWalker {

	////////////////////////////////////////////////////////////////////////

	public static void main(String[] args) {
		if (args.length == 0) {
			usage();
			System.exit(1);
		}
		int index = 0;
		char c = args[index].charAt(0);
		int bufsize = BUFSIZE;
		if (c >= '0' && c <= '9') {
			bufsize = Integer.parseInt(args[index]);
			++index;
		}
		InputStream is = null;
		PrintWriter out = null;
		String outfile = args.length > index + 1 ? args[index + 1] : null;
		try {
			is = FileUtil.openInputStream(new File(args[index]));
			if (outfile != null)
				out = FileUtil.openWriter(new File(outfile), BUFSIZE);
			new DumpBxd(is, bufsize, out).run();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtil.close(out, outfile);
			FileUtil.close(is);
	}}

	public static void usage() {
		System.err.println(
			String.format("Usage: java %s [bufsize] <bxdfile> [ <outfile> ]", DumpBxd.class.getName()));
	}

	////////////////////////////////////////////////////////////////////////

	PrintWriter out;
	IntList tStack = new IntList();
	boolean isEmpty = true;
	long position = 0;
	BxdValue value = new BxdValue();
	StringBuilder indent = new StringBuilder();

	////////////////////////////////////////////////////////////////////////

	public DumpBxd(InputStream in, PrintWriter out) {
		super(in);
		this.out = out;
	}

	public DumpBxd(InputStream in, int bufsize, PrintWriter out) {
		super(in, bufsize);
		this.out = out;
	}

	////////////////////////////////////////////////////////////////////////

	public void run() throws BxdException, IOException {
		readHeader();
		for (int t = next();; t = next()) {
			switch (t) {
			case TagDoctype:
			case TagDeclaration:
			case TagPI:
				dumpStart(t, target());
				tStack.push(t);
				break;
			case TagComment:
			case TagCDATA:
			case TagAbsent:
			case TagFalse:
			case TagTrue:
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
				dumpValue(readValue());
				break;
			case TagEndAttr:
				isEmpty = false;
				break;
			case TagEndTag:
				t = tStack.pop();
				dumpEnd(t);
				break;
			case TagTrailer:
				readTrailer();
				return;
			default :
				if (t <= 0)
					throw new AssertionError("ASSERT: Invalid tag: " + TagNames[t]);
				dumpStart(t, null);
				for (IBxdAttribute attr; (attr = readAttribute()) != null;) {
					dumpAttr(attr);
				}
				tStack.push(t);
				isEmpty = true;
	}}}

	protected StringBuilder indent(int level) {
		if (indent.length() >= level)
			indent.setLength(level);
		else {
			for (int i = indent.length(); i < level; ++i)
				indent.append(' ');
		}
		return indent;
	}

	private void dumpStart(int t, CharSequence target) {
		int level = tStack.size();
		out.println(String.format("+%6d: %s%s", level, indent(level), getString(t)));
		hexdump();
	}

	private void dumpEnd(int t) {
		int level = tStack.size();
		out.println(String.format("-%6d: %s%s", level, indent(level), getString(t)));
		hexdump();
	}

	private void dumpAttr(IBxdAttribute attr) {
		int level = tStack.size();
		IBxdValue value = attr.value();
		out.println(
			String.format(" %6d* %s%s=%s", level, indent(level), getString(attr.name()), value.toString()));
		hexdump();
	}

	private void dumpValue(IBxdValue value) {
		int level = tStack.size();
		int kind = value.kind();
		if (kind == TagBlob) {
			out.println(
				String.format(" %6d# %s%s", level, indent(level), ("blob#" + value.blobValue().length)));
		} else if (kind == TagString) {
			CharSequence s = value.stringValue();
			out.println(
				String.format(
					" %6d# %sstring#%d: %s",
					level,
					indent(level),
					(s == null ? 0 : s.length()),
					(s == null ? "null" : s)));
		} else {
			out.println(String.format(" %6d# %s%s", level, indent(level), value.toString().trim()));
		}
		hexdump();
	}

	private void hexdump() {
		int level = tStack.size();
		if (position < tailOffset)
			throw new AssertionError(
				"ASSERT: Buffer underrun: position=" + position + ", tailOffset=" + tailOffset);
		int start = (int)(position - tailOffset);
		if (start < offset) {
			out.println(
				TextUtil.sprintHexDump(
					"       : " + indent(level).toString() + "%08x: ", buffer, start, offset));
		}
		position = tailOffset + offset;
		// FIXME This do not work if there are huge blob value that is larger than the buffer.
		mark(position);
	}

	////////////////////////////////////////////////////////////////////////
}
