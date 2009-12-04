package sf.blacksun.util.text;

/*
 * Copyright (c) 2004, Chris Leung, simontsui. All rights reserved.
 *
 * The software in this package is distributed under the GNU General Public
 * License version 2, as published by the Free Software Foundation, but with
 * the Classpath exception.  You should have received a copy of the GNU General
 * Public License (GPL) and the Classpath exception along with this program.
 */

import java.io.Serializable;


import java.util.Comparator;


import java.util.Hashtable;
import java.util.Iterator;

import java.util.Map;







/**
 * Static text utility methods.
 *
 * TODO: . Remove synchronized for functions that are classified as synchronized just because of using Perl5Util
 * re.
 */
public class TextUtil {

	private static String LINE_SEP;
	private static Map<String, String> uriEscapeChars = new Hashtable<String, String>();
	private static Map<String, String> filepathEscapeChars = new Hashtable<String, String>();
	static {
		uriEscapeChars.put(";", "%3b");
		uriEscapeChars.put("/", "%2f");
		uriEscapeChars.put("?", "%3f");
		uriEscapeChars.put(":", "%3a");
		uriEscapeChars.put("\"", "%22");
		uriEscapeChars.put("@", "%40");
		uriEscapeChars.put("&", "%26");
		uriEscapeChars.put("=", "%3d");
		uriEscapeChars.put("+", "%2b");
		uriEscapeChars.put("$", "%24");
		uriEscapeChars.put(",", "%2c");
		uriEscapeChars.put("%", "%25");
		uriEscapeChars.put("%3b", ";");
		uriEscapeChars.put("%2f", "/");
		uriEscapeChars.put("%3f", "?");
		uriEscapeChars.put("%3a", ":");
		uriEscapeChars.put("%22", "\"");
		uriEscapeChars.put("%40", "@");
		uriEscapeChars.put("%26", "&");
		uriEscapeChars.put("%3d", "=");
		uriEscapeChars.put("%2b", "+");
		uriEscapeChars.put("%24", "$");
		uriEscapeChars.put("%2c", ",");
		uriEscapeChars.put("%25", "%");
		filepathEscapeChars.put("~", "^7e");
		filepathEscapeChars.put("!", "^21");
		filepathEscapeChars.put("$", "^24");
		filepathEscapeChars.put("&", "^26");
		filepathEscapeChars.put("*", "^2a");
		filepathEscapeChars.put("(", "^28");
		filepathEscapeChars.put(")", "^29");
		filepathEscapeChars.put("[", "^5b");
		filepathEscapeChars.put("]", "^5d");
		filepathEscapeChars.put("|", "^7c");
		filepathEscapeChars.put("?", "^3f");
		filepathEscapeChars.put(";", "^3b");
		filepathEscapeChars.put("<", "^3c");
		filepathEscapeChars.put(">", "^3e");
		filepathEscapeChars.put("\"", "^22");
		filepathEscapeChars.put("'", "^27");
		filepathEscapeChars.put("^7e", "~");
		filepathEscapeChars.put("^21", "!");
		filepathEscapeChars.put("^24", "$");
		filepathEscapeChars.put("^26", "&");
		filepathEscapeChars.put("^2a", "*");
		filepathEscapeChars.put("^28", "(");
		filepathEscapeChars.put("^29", ")");
		filepathEscapeChars.put("^5b", "[");
		filepathEscapeChars.put("^5d", "]");
		filepathEscapeChars.put("^7c", "|");
		filepathEscapeChars.put("^3f", "?");
		filepathEscapeChars.put("^3b", ";");
		filepathEscapeChars.put("^3c", "<");
		filepathEscapeChars.put("^3e", ">");
		filepathEscapeChars.put("^22", "\"");
		filepathEscapeChars.put("^27", "'");
	}

	protected TextUtil() {
	}


	public static String getLineSeparator() {
		if (LINE_SEP == null)
			LINE_SEP = System.getProperty("line.separator");
		return LINE_SEP;
	}


	/**
	 * Dump byte array in hexdump -C format.
	 *
	 * @param format	Offset output format string, eg. "%08x: "
	 */
	public static CharSequence sprintHexDump(String format, byte[] a, int start, int end) {
		StringBuilder ret = new StringBuilder();
		int PERLINE = 16;
		int len = a.length;
		if (len == 0 || start == end) {
			ret.append("\n");
			return ret;
		}
		int xstart = (start / PERLINE) * PERLINE;
		int xend = (((end - 1) / PERLINE) + 1) * PERLINE;
		for (; xstart < xend; xstart += PERLINE) {
			ret.append(String.format(format, xstart));
			for (int i = 0; i < PERLINE; ++i) {
				if (i == PERLINE / 2)
					ret.append(' ');
				int index = xstart + i;
				if (index >= start && index < end)
					ret.append(String.format("%1$02x ", a[index]));
				else
					ret.append("   ");
			}
			ret.append(" |");
			for (int i = 0; i < PERLINE; ++i) {
				int index = xstart + i;
				if (index >= start && index < end) {
					int c = a[index] & 0xff;
					if (c < 0x20 || c >= 0x7f)
						ret.append('.');
					else
						ret.append((char)c);
				} else
					ret.append(' ');
			}
			ret.append("|\n");
		}
		return ret;
	}

	/**
	 * Print byte array in hex.
	 */
	public static CharSequence sprintHex(int perline, String format, byte[] a) {
		return sprintHex(perline, format, a, 0, a.length);
	}

	/**
	 * Print byte array in hex.
	 */
	public static CharSequence sprintHex(int perline, String format, byte[] a, int start, int end) {
		StringBuilder ret = new StringBuilder();
		int len = a.length;
		if (len == 0 || start == end) {
			ret.append("\n");
			return ret;
		}
		for (; start < end; start += perline) {
			for (int i = 0; i < perline && start + i < end; ++i) {
				if (i == perline / 2)
					ret.append(' ');
				ret.append(String.format(format, a[start + i]));
			}
			ret.append(getLineSeparator());
		}
		return ret;
	}


	public static String sprintArray(String format, byte[] a) {
		return sprintArray(format, a, 0, a.length);
	}

	public static String sprintArray(String format, byte[] a, int start, int end) {
		StringBuilder ret = new StringBuilder();
		for (; start < end; ++start)
			ret.append(String.format(format, a[start]));
		return ret.toString();
	}


	////////////////////////////////////////////////////////////////////////

	public static class Basename {
		public String dir;
		public String name;
		public String base;
		public String ext;
		public Basename(String dir, String name, String base, String ext) {
			this.dir = dir;
			this.name = name;
			this.base = base;
			this.ext = ext;
		}
	}


	////////////////////////////////////////////////////////////////////////

	public static class ManifestValueScanner implements Iterator<String>, Iterable<String> {
		CharSequence value;
		int length;
		int start;
		int end;
		public ManifestValueScanner(CharSequence value) {
			this.value = value;
			this.length = value.length();
			this.start = 0;
			this.end = 0;
		}
		public Iterator<String> iterator() {
			return this;
		}
		public boolean hasNext() {
			return start < length;
		}
		public String next() {
			while (end < length) {
				char c = value.charAt(end);
				if (c == '"') {
					skipString('"');
					continue;
				} else if (c == ',') {
					String ret = value.subSequence(start, end).toString();
					++end;
					start = end;
					return ret;
				}
				++end;
			}
			String ret = value.subSequence(start, end).toString();
			start = end;
			return ret;
		}
		public void remove() {
			throw new UnsupportedOperationException();
		}
		private void skipString(char delim) {
			for (++end; end < length; ++end) {
				if (value.charAt(end) == delim) {
					++end;
					return;
		}}}
	}

	////////////////////////////////////////////////////////////////////////

	public static class StringComparator implements Comparator<String>, Serializable {
		private static final long serialVersionUID = -4106319796409788626L;
		public int compare(String a, String b) {
			if (a == null)
				return b == null ? 0 : -1;
			return a.compareTo(b);
		}
	}

	public static class StringIgnorecaseComparator implements Comparator<String>, Serializable {
		private static final long serialVersionUID = -1698275111742700972L;
		public int compare(String a, String b) {
			if (a == null)
				return b == null ? 0 : -1;
			return a.compareToIgnoreCase(b);
		}
	}

	////////////////////////////////////////////////////////////////////////
}
