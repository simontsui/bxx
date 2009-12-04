/*
 * Copyright (c) 2008, Chris Leung, simontsui. All rights reserved.
 *
 * The software in this package is distributed under the GNU General Public
 * License version 2, as published by the Free Software Foundation, but with
 * the Classpath exception.  You should have received a copy of the GNU General
 * Public License (GPL) and the Classpath exception along with this program.
 */
package sf.bxx.bxd.tools;

import static sf.bxx.IBxxConstants.DTD;
import static sf.bxx.IBxxConstants.PUBLIC;
import static sf.bxx.IBxxConstants.SYSTEM;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import sf.blacksun.util.Cmdline;
import sf.blacksun.util.FileUtil;
import sf.blacksun.util.ICLIOption;
import sf.blacksun.util.ICmdline;
import sf.blacksun.util.StopWatch;
import sf.blacksun.util.CLIOption.BoolOption;
import sf.blacksun.util.CLIOption.LongOption;
import sf.blacksun.util.CLIOption.StringListOption;
import sf.blacksun.util.CLIOption.StringOption;
import sf.blacksun.util.text.IStringPool;
import sf.blacksun.util.text.TextUtil;
import sf.bxx.bxd.BxdHeader;
import sf.bxx.bxd.BxdTrailer;
import sf.bxx.bxd.BxdWriter;
import sf.bxx.bxd.IBxdWriter;

/**
 * Simple XML to BXD converter using an SAX parser.
 * TODO: Currently, only element, PI, comment, cdata and text nodes are converted.
 * TODO: Currently, cannot handle entity references.
 */
public class XmlBxd {

	public interface Opts {
		ICLIOption<Boolean> help = new BoolOption("help", "Help");
		ICLIOption<Boolean> force = new BoolOption("force", "Force overwrite output file");
		ICLIOption<Long> flags = new LongOption("flags", "Flags");
		ICLIOption<String[]> tags = new StringListOption("tags", "Known tags");
		ICLIOption<String> version = new StringOption("version", "Version");
		ICLIOption<String> encoding = new StringOption("encoding", "Encoding");
		ICLIOption<String> standalone = new StringOption("standalone", "Standalone");
	}
	private static final int BUFSIZE = 128 * 1024;

	////////////////////////////////////////////////////////////////////////

	static boolean DEBUG = false;
	static boolean VERBOSE = false;

	////////////////////////////////////////////////////////////////////////

	public static void main(String[] args) {
		ICmdline<Opts> cmdline = new Cmdline<Opts>(Opts.class, args);
		if (cmdline.isHelp(1, Opts.help)) {
			usage();
			System.exit(1);
		}
		try {
			run(cmdline, new File(args[0]), new File(args[1]));
		} catch (Exception e) {
			e.printStackTrace();
	}}

	public static void usage() {
		System.err.println(
			new Cmdline<Opts>(Opts.class).sprintUsage(
				String.format(
					"Usage:\n\tjava -cp bxx.jar %1$s [-options] <in.xml> <out.bxd>",
					XmlBxd.class.getName())));
	}

	////////////////////////////////////////////////////////////////////////

	public static void run(ICmdline<? extends Opts> cmdline, File inxml, File outbxd)
		throws SAXException, IOException, ParserConfigurationException {
		if (outbxd.exists()) {
			if (!cmdline.getBool(Opts.force))
				throw new IOException("Output file exists, not overwritting: " + outbxd);
			if (!outbxd.delete())
				throw new IOException("Failed to delete existing file: " + outbxd);
		}
		long flags = cmdline.getLong(Opts.flags, 0);
		String version = cmdline.getString(Opts.version, "1.0");
		String encoding = cmdline.getString(Opts.encoding, "UTF-8");
		String standalone = cmdline.getString(Opts.standalone);
		InputStream input = null;
		OutputStream output = null;
		try {
			output = FileUtil.openOutputStream(outbxd, BUFSIZE);
			input = FileUtil.openInputStream(inxml, BUFSIZE);
			StopWatch timer = new StopWatch().start();
			final int[] total = { 0 };
			SAXParserFactory factory = SAXParserFactory.newInstance();
			boolean validating = false;
			factory.setValidating(validating);
			factory.setFeature("http://xml.org/sax/features/external-parameter-entities", validating);
			factory.setFeature(
				"http://apache.org/xml/features/nonvalidating/load-external-dtd", validating);
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", true);
			factory.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
			factory.setFeature("http://xml.org/sax/features/namespaces", false);
			IBxdWriter writer = new BxdWriter(output);
			IStringPool spool = writer.stringPool();
			List<String> tags = cmdline.getStringList(Opts.tags);
			for (String tag: tags)
				spool.intern(tag);
			try {
				XmlBxdHandler handler = new XmlBxdHandler(writer, flags, version, encoding, standalone);
				SAXParser parser = factory.newSAXParser();
				parser.setProperty("http://xml.org/sax/properties/lexical-handler", handler);
				parser.setProperty("http://xml.org/sax/properties/declaration-handler", handler);
				parser.getXMLReader().setEntityResolver(null);
				parser.parse(input, handler);
			} finally {
				writer.close();
			}
			if (VERBOSE)
				System.out.println(
					timer.stop().toString(
						String.format("# Total: %8d: %s", total[0], inxml.getName())));
		} finally {
			FileUtil.close(output);
			FileUtil.close(input);
	}}

	////////////////////////////////////////////////////////////////////////

	public static class XmlBxdHandler extends DefaultHandler2 {

		////////////////////////////////////////////////////////////////////////

		private IBxdWriter writer;
		private long flags;
		private String version;
		private String encoding;
		private String standalone;
		private boolean attributeState = false;
		private StringBuilder buffer = new StringBuilder();

		////////////////////////////////////////////////////////////////////////

		public XmlBxdHandler(IBxdWriter writer, long flags) {
			this(writer, flags, "1.0", "UTF-8", null);
		}

		public XmlBxdHandler(IBxdWriter writer, long flags, String version, String encoding, String standalone) {
			this.writer = writer;
			this.flags = flags;
			this.version = version;
			this.encoding = encoding;
			this.standalone = standalone;
		}

		////////////////////////////////////////////////////////////////////////

		@Override
		public void startDocument() throws SAXException {
			try {
				writer.header(new BxdHeader(flags));
				writer.xmlDeclaration(version, encoding, standalone);
			} catch (IOException e) {
				throw new SAXException(e);
		}}

		@Override
		public void processingInstruction(String target, String data) throws SAXException {
			try {
				writeText(true);
				writer.pi(target, data);
			} catch (IOException e) {
				throw new SAXException(e);
		}}

		@Override
		public void startElement(String uri, String localName, String name, Attributes attributes)
			throws SAXException {
			try {
				writeText(true);
				writer.startTag(name);
				int len = attributes.getLength();
				for (int i = 0; i < len; ++i) {
					writer.startAttr(attributes.getQName(i));
					writer.writeString(attributes.getValue(i));
				}
				attributeState = true;
			} catch (IOException e) {
				throw new SAXException(e);
		}}

		@Override
		public void endElement(String uri, String localName, String name) throws SAXException {
			try {
				writeText(false);
				writer.endTag();
				attributeState = false;
			} catch (IOException e) {
				throw new SAXException(e);
		}}

		@Override
		public void startCDATA() throws SAXException {
			writeText(true);
		}

		@Override
		public void endCDATA() throws SAXException {
			try {
				writer.cdata(buffer);
				buffer.setLength(0);
			} catch (IOException e) {
				throw new SAXException(e);
		}}

		@Override
		public void comment(char[] ch, int start, int length) throws SAXException {
			writeText(true);
			try {
				writer.comment(ch, start, start + length);
			} catch (IOException e) {
				throw new SAXException(e);
		}}

		@Override
		public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
			buffer.append(ch, start, length);
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			buffer.append(ch, start, length);
		}

		@Override
		public void endDocument() throws SAXException {
			try {
				writer.trailer(new BxdTrailer(flags));
			} catch (IOException e) {
				throw new SAXException(e);
		}}

		@Override
		public void startDTD(String name, String publicid, String systemid) throws SAXException {
			try {
				writer.startDoctype(name);
				if (publicid == null && systemid != null) {
					writer.attribute(SYSTEM, "SYSTEM \"" + systemid + "\"");
				} else if (publicid != null && systemid != null) {
					writer.attribute(PUBLIC, "PUBLIC \"" + publicid + "\"");
					writer.attribute(SYSTEM, "\"" + systemid + "\"");
			}} catch (IOException e) {
				throw new SAXException(e);
		}}

		@Override
		public void endDTD() throws SAXException {
			try {
				if (buffer.length() > 0)
					writer.attribute(DTD, "[" + buffer + "]");
				writer.endTag();
			} catch (IOException e) {
				throw new SAXException(e);
		}}


		@Override
		public void internalEntityDecl(String name, String value) throws SAXException {
			System.out.println("internalEntityDecl: " + name + ", " + value);
		}

		@Override
		public void notationDecl(String name, String publicId, String systemId) throws SAXException {
			// TODO Auto-generated method stub
			super.notationDecl(name, publicId, systemId);
		}

		@Override
		public void externalEntityDecl(String name, String publicId, String systemId) throws SAXException {
			buffer.append(TextUtil.getLineSeparator());
			buffer.append("<!ENTITY ");
			buffer.append(name);
			if (publicId != null)
				buffer.append("PUBLIC \"" + publicId + "\"");
			if (systemId != null)
				buffer.append("SYSTEM \"" + systemId + "\"");
			buffer.append(">");
		}

		@Override
		public void elementDecl(String name, String model) throws SAXException {
			// TODO Auto-generated method stub
			super.elementDecl(name, model);
		}

		@Override
		public void attributeDecl(String eName, String aName, String type, String mode, String value)
			throws SAXException {
			// TODO Auto-generated method stub
			super.attributeDecl(eName, aName, type, mode, value);
		}

		////////////////////////////////////////////////////////////////////////

		private void writeText(boolean endattr) throws SAXException {
			if (!endattr && buffer.length() == 0)
				return;
			try {
				if (attributeState) {
					writer.endAttr();
					attributeState = false;
				}
				if (buffer.length() == 0)
					return;
				writer.writeString(buffer);
				buffer.setLength(0);
			} catch (IOException e) {
				throw new SAXException();
		}}

		////////////////////////////////////////////////////////////////////////
	}

	////////////////////////////////////////////////////////////////////////
}
