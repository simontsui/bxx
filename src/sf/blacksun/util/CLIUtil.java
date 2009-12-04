/*
 * Copyright (c) 2003-2005, Chris Leung, simontsui. All rights reserved.
 *
 * The software in this package is distributed under the GNU General Public
 * License version 2, as published by the Free Software Foundation, but with
 * the Classpath exception.  You should have received a copy of the GNU General
 * Public License (GPL) and the Classpath exception along with this program.
 */
package sf.blacksun.util;

import gnu.getopt.Getopt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;






import sf.blacksun.util.text.TextUtil;

public class CLIUtil {

	static final int TABWIDTH = 8;

	public static class InvalidOptionException extends RuntimeException {
		public static final long serialVersionUID = -1121695880602635288L;
		public InvalidOptionException() {
			super();
		}
		public InvalidOptionException(String message, Throwable cause) {
			super(message, cause);
		}
		public InvalidOptionException(String message) {
			super(message);
		}
		public InvalidOptionException(Throwable cause) {
			super(cause);
		}
	}

	////////////////////////////////////////////////////////////////////////

	private CLIUtil() {
	}


	public static <S> String[] getArgsArray(
		Map<String, Object> opts, String[] args, Collection<ICLIOption<?>> specs) {
		List<String> a = getArgs(opts, args, specs);
		return a.toArray(new String[a.size()]);
	}

	public static <S> List<String> getArgs(
		Map<String, Object> opts, String[] args, Collection<ICLIOption<?>> specs) {
		StringBuilder shortopts = new StringBuilder();
		List<ICLIOption<?>> longopts = new ArrayList<ICLIOption<?>>();
		Map<String, ICLIOption<?>> alias = new HashMap<String, ICLIOption<?>>();
		for (ICLIOption<?> a: specs) {
			String s = a.getShortOpt();
			if (s != null) {
				shortopts.append(s);
				if (a.optionalArgs())
					shortopts.append("::");
				else if (a.hasArg() || a.arrayArgs())
					shortopts.append(':');
				alias.put(s, a);
			}
			String l = a.getLongOpt();
			if (l != null) {
				longopts.add(a);
				opts.put(l, null);
		}}
		Getopt opt = new Getopt(
			"Getopt", args, shortopts.toString(), longopts.toArray(new CLIOption[longopts.size()]), false);
		int optc;
		String optarg;
		while ((optc = opt.getopt()) >= 0) {
			ICLIOption<?> option;
			if (optc == 0) {
				option = longopts.get(opt.getLongind());
			} else {
				option = alias.get(String.valueOf((char)optc));
			}
			if (option == null)
				throw new AssertionError(
					"Invalid optoin: optc=" + (char)optc + ", optarg=" + opt.getOptarg());
			String key = option.getLongOpt();
			if (key == null)
				key = option.getShortOpt();
			if (option.arrayArgs()) {
				List<String> a = getstringlist(opts, key);
				if (a == null)
					opts.put(key, a = new ArrayList<String>(4));
				optarg = opt.getOptarg();
				a.add(optarg == null ? "" : optarg);
			} else if (option.hasArg()) {
				optarg = opt.getOptarg();
				opts.put(key, optarg == null ? "" : optarg);
			} else {
				opts.put(key, "true");
		}}
		List<String> arguments = new LinkedList<String>();
		for (int i = opt.getOptind(); i < args.length; ++i) {
			arguments.add(args[i]);
		}
		return arguments;
	}


	public static <S> String sprintUsage(
		String header, String indent, Iterable<ICLIOption<?>> specs, String footer) {
		StringBuilder b = new StringBuilder();
		String linesep = TextUtil.getLineSeparator();
		if (header != null) {
			b.append(header);
			b.append(linesep);
		}
		List<String> opts = new ArrayList<String>();
		int width = 0;
		for (ICLIOption<?> option: specs) {
			if (option == null) {
				opts.add(null);
				continue;
			}
			String longopt = option.getLongOpt();
			String shortopt = option.getShortOpt();
			if (longopt == null)
				longopt = shortopt;
			else if (shortopt != null)
				longopt = longopt + "|" + shortopt;
			if (option.arrayArgs())
				longopt += (option.optionalArgs() ? "@@" : "@");
			else if (option.hasArg())
				longopt += (option.optionalArgs() ? "::" : ":");
			String desc = option.getDescription();
			if (longopt.length() > width)
				width = longopt.length();
			opts.add(longopt);
			opts.add(desc == null ? "" : desc);
		}
		int extra = ((width % TABWIDTH) > TABWIDTH / 2) ? 2 : 1;
		width = ((width / TABWIDTH) + extra) * TABWIDTH;
		for (Iterator<String> it = opts.iterator(); it.hasNext();) {
			String s = it.next();
			if (s == null) {
				b.append(linesep);
				continue;
			}
			b.append(String.format(indent + "%-" + width + "s%s" + linesep, s, it.next()));
		}
		if (footer != null)
			b.append(footer);
		return b.toString();
	}

	/**
	 * Parse option spec. with the following format:
	 * 	[LongOpt=]ShortOpt [ArgType] [(Description] | ...
	 * Exampe:
	 * 	help=h 		Help.
	 * 	| verbose		Verbose.
	 * 	| include=e@	Include if match given regex.
	 */
	public static class OptionSpecParser {
	}


	@SuppressWarnings("unchecked")
	private static List<String> getstringlist(Map<String, ?> options, String key) {
		return (List<String>)options.get(key);
	}

	////////////////////////////////////////////////////////////////////////
}
