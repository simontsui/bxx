/*
 * Copyright (c) 2008, Chris Leung, simontsui. All rights reserved.
 *
 * The software in this package is distributed under the GNU General Public
 * License version 2, as published by the Free Software Foundation, but with
 * the Classpath exception.  You should have received a copy of the GNU General
 * Public License (GPL) and the Classpath exception along with this program.
 */
package sf.bxx.bxd.tools;

import static sf.bxx.IBxxConstants.DECL_PREFIX;
import static sf.bxx.IBxxConstants.TagAbsent;
import static sf.bxx.IBxxConstants.TagBigInt;
import static sf.bxx.IBxxConstants.TagBlob;
import static sf.bxx.IBxxConstants.TagBlobRef;
import static sf.bxx.IBxxConstants.TagByte;
import static sf.bxx.IBxxConstants.TagChar;
import static sf.bxx.IBxxConstants.TagDouble;
import static sf.bxx.IBxxConstants.TagFalse;
import static sf.bxx.IBxxConstants.TagFloat;
import static sf.bxx.IBxxConstants.TagInt;
import static sf.bxx.IBxxConstants.TagLong;
import static sf.bxx.IBxxConstants.TagShort;
import static sf.bxx.IBxxConstants.TagString;
import static sf.bxx.IBxxConstants.TagStringRef;
import static sf.bxx.IBxxConstants.TagTrue;
import static sf.bxx.IBxxConstants.XML;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import sf.blacksun.util.StopWatch;
import sf.blacksun.util.text.IStringPool;
import sf.blacksun.util.text.TextUtil;
import sf.blacksun.util.text.XmlUtil;
import sf.bxx.bxd.BxdException;
import sf.bxx.bxd.BxdReader;
import sf.bxx.bxd.IBxdAttribute;
import sf.bxx.bxd.IBxdAttributes;
import sf.bxx.bxd.IBxdHeader;
import sf.bxx.bxd.IBxdReader;
import sf.bxx.bxd.IBxdTrailer;
import sf.bxx.bxd.IBxdValue;
import sf.bxx.bxd.sax.IBxdHandler;

public class BxdXml {

	////////////////////////////////////////////////////////////////////////

	static boolean DEBUG = false;
	static boolean VERBOSE = false;

	////////////////////////////////////////////////////////////////////////

	public static void main(String[] args) {
		Map<String, Object> options = new HashMap<String, Object>();
		try {
			run(options, new File(args[0]), new File(args[1]));
		} catch (Exception e) {
			e.printStackTrace();
	}}

	////////////////////////////////////////////////////////////////////////

	public static void run(Map<String, Object> options, File inbxd, File outxml) throws IOException, BxdException {
		if (outxml.exists()) {
			if (options.get("force") != null)
				throw new IOException("Output file exists, not overwritting: " + outxml);
			if (!outxml.delete())
				throw new IOException("Failed to delete existing file: " + outxml);
		}
		IBxdReader reader = null;
		OutputStream output = null;
		try {
			output = new FileOutputStream(outxml, false);
			if (outxml.getName().endsWith(".gz"))
				output = new GZIPOutputStream(output);
			InputStream input = new FileInputStream(inbxd);
			if (inbxd.getName().endsWith(".gz"))
				input = new GZIPInputStream(input);
			reader = new BxdReader(input, null);
			run(new PrintWriter(output), inbxd.getName(), reader);
		} finally {
			if (output != null)
				try {
					output.close();
				} catch (IOException e) {
				}
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
	}}}

	public static void run(PrintWriter output, String path, IBxdReader reader) throws BxdException {
		StopWatch timer = new StopWatch().start();
		final int[] total = { 0 };
		reader.setHandler(new BxdXmlHandler(output, reader));
		reader.parse();
		if (VERBOSE)
			System.out.println(timer.stop().toString(String.format("# Total: %8d: %s", total[0], path)));
	}

	////////////////////////////////////////////////////////////////////////

	public static class BxdXmlHandler implements IBxdHandler {

		private PrintWriter output;
		private IStringPool sPool;
		private Stack<CharSequence> tagStack = new Stack<CharSequence>();

		public BxdXmlHandler(PrintWriter out, IBxdReader reader) {
			this.output = out;
			this.sPool = reader.stringPool();
		}

		public void startDocument() throws BxdException {
		}

		public void header(IBxdHeader header) throws BxdException {
		}

		public void startDoctype(int name, IBxdAttributes attrs) throws BxdException {
			output.print("<!DOCTYPE " + sPool.get(name));
			printDeclAttrs(attrs);
			output.println(">");
		}

		public void startDeclaration(int name, IBxdAttributes attrs) throws BxdException {
			output.print("<!" + sPool.get(name));
			printDeclAttrs(attrs);
			output.println(">");
		}

		public void startPI(int name, IBxdAttributes attrs) throws BxdException {
			output.print("<?" + sPool.get(name));
			if (name == XML) {
				printAttrs(attrs);
			} else {
				printDeclAttrs(attrs);
			}
			output.println(" ?>");
		}

		public void startElement(int name, IBxdAttributes attrs, boolean empty) throws BxdException {
			CharSequence tag = sPool.get(name);
			output.print("<" + tag);
			printAttrs(attrs);
			if (empty)
				output.print("\n/>");
			else {
				output.print("\n>");
				tagStack.push(tag);
		}}

		public void endElement(int name) throws BxdException {
			CharSequence s = tagStack.pop();
			output.print("</" + s + "\n>");
		}

		public void cdata(IBxdValue content) throws BxdException {
			output.print("<![CDATA[" + content.stringValue() + "]]>");
		}

		public void comment(IBxdValue content) throws BxdException {
			output.println("<!--" + content.stringValue() + "-->");
		}

		public void text(IBxdValue content) throws BxdException {
			output.println(textString(content));
		}

		public void trailer(IBxdTrailer trailer) throws BxdException {
		}

		public void endDocument() throws BxdException {
			output.flush();
		}

		private void printDeclAttrs(IBxdAttributes attrs) throws BxdException {
			for (IBxdAttribute attr: attrs) {
				CharSequence name = sPool.get(attr.name());
				if (name.length() > 0 && name.charAt(0) == DECL_PREFIX) {
					output.print(' ');
					output.print(attr.value().stringValue());
				} else {
					printAttr(attr);
		}}}

		private void printAttrs(IBxdAttributes attrs) throws BxdException {
			for (IBxdAttribute attr: attrs) {
				printAttr(attr);
		}}

		private void printAttr(IBxdAttribute attr) throws BxdException {
			IBxdValue value = attr.value();
			int kind = value.kind();
			CharSequence s = sPool.get(attr.name());
			if (kind == TagAbsent) {
				output.println(s);
				return;
			}
			output.print(' ');
			output.print(s);
			output.print("=\"");
			output.print(attrString(value));
			output.print("\"");
		}

		private CharSequence attrString(IBxdValue value) throws BxdException {
			int kind = value.kind();
			switch (kind) {
			case TagAbsent:
				return "";
			case TagFalse:
				return "false";
			case TagTrue:
				return "true";
			case TagByte:
			case TagShort:
			case TagInt:
			case TagChar:
				return String.valueOf(value.intValue());
			case TagLong:
				return String.valueOf(value.longValue());
			case TagFloat:
				return String.valueOf(value.floatValue());
			case TagDouble:
				return String.valueOf(value.doubleValue());
			case TagBigInt:
				return value.bigIntValue().toString();
			case TagString:
			case TagStringRef:
				return XmlUtil.escAttrValueStrict(value.stringValue());
			case TagBlob:
			case TagBlobRef:
				return TextUtil.sprintArray("0x%02x ", value.blobValue());
			default :
				throw new BxdException(String.format("Invalid value type: 0x%02x", kind));
		}}

		private CharSequence textString(IBxdValue value) throws BxdException {
			int kind = value.kind();
			switch (kind) {
			case TagString:
			case TagStringRef:
				return XmlUtil.escTextStrict(value.stringValue());
			case TagBlob:
			case TagBlobRef:
				return TextUtil.sprintHex(16, "0x%02x", value.blobValue());
			default :
				return attrString(value);
		}}
	}

	////////////////////////////////////////////////////////////////////////
}
