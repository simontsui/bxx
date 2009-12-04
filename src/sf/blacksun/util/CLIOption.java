/*
 * Copyright (c) 2003, Chris Leung, simontsui. All rights reserved.
 *
 * The software in this package is distributed under the GNU General Public
 * License version 2, as published by the Free Software Foundation, but with
 * the Classpath exception.  You should have received a copy of the GNU General
 * Public License (GPL) and the Classpath exception along with this program.
 */
package sf.blacksun.util;

import gnu.getopt.LongOpt;

import java.util.List;

import sf.blacksun.util.CLIUtil.InvalidOptionException;
import sf.blacksun.util.io.StringPrintWriter;

public abstract class CLIOption<T> extends LongOpt implements ICLIOption<T> {

	public enum Arg {
		None, Require, Optional, RequireArray, OptionalArray;

		public int kind() {
			return (this == Arg.Require || this == Arg.RequireArray)
				? REQUIRED_ARGUMENT
				: (this == Arg.Optional || this == Arg.OptionalArray) //
					? OPTIONAL_ARGUMENT
					: NO_ARGUMENT;
		}

		public boolean isArray() {
			return this == RequireArray || this == OptionalArray;
		}


		public boolean isOptional() {
			return this == Optional || this == OptionalArray;
		}
	}

	////////////////////////////////////////////////////////////////////////

	private String shortOpt;
	private String description;
	private Arg kind;

	////////////////////////////////////////////////////////////////////////

	public CLIOption(String longopt, Arg kind, String desc) {
		super(longopt, kind.kind(), null, 0);
		this.kind = kind;
		description = desc;
	}

	public CLIOption(String longopt, Character shortopt, Arg kind, String desc) {
		super(longopt, kind.kind(), null, 0);
		this.shortOpt = shortopt == null ? null : shortopt.toString();
		this.kind = kind;
		description = desc;
	}

	////////////////////////////////////////////////////////////////////////

	public String getLongOpt() {
		return name;
	}

	public String getShortOpt() {
		return shortOpt;
	}

	public boolean optionalArgs() {
		return kind.isOptional();
	}


	public boolean hasArg() {
		return kind != Arg.None;
	}

	public boolean arrayArgs() {
		return kind.isArray();
	}

	public String getDescription() {
		return description;
	}

	public int compareTo(ICLIOption<T> o) {
		return getLongOpt().compareTo(o.getLongOpt());
	}

	@Override
	public String toString() {
		return getLongOpt()
			+ (hasArg()
				? (arrayArgs() //
					? (optionalArgs() ? "@@" : "@")
					: (optionalArgs() ? "::" : ":"))
				: "");
	}

	protected InvalidOptionException invalidOption(String[] args) {
		return invalidOption(getLongOpt(), args);
	}

	protected static InvalidOptionException invalidOption(String option, String[] args) {
		StringPrintWriter w = new StringPrintWriter();
		try {
			w.println("ERROR: Invalid value for option: " + option + ":");
			if (args == null) {
				w.println("# null");
			} else {
				for (String s: args)
					w.println("# " + s);
			}
			return new InvalidOptionException(w.toString());
		} finally {
			w.close();
	}}


	////////////////////////////////////////////////////////////////////////

	public static class LegacyOption extends CLIOption<Object> {
		public LegacyOption(String longopt, Character shortopt, Arg kind, String desc) {
			super(longopt, shortopt, kind, desc);
		}
		public ICLIOptValue<Object> fromString(String...values) {
			if (values != null) {
				switch (values.length) {
				case 0:
					return null;
				case 1:
					return new LegacyValue(getLongOpt(), values);
			}}
			throw invalidOption(values);
		}
		private static class LegacyValue extends OptValue<Object> {
			private String[] value;
			LegacyValue(String option, String[] value) {
				super(option);
				this.value = value;
			}
			public boolean getBool() {
				if (value.length != 1)
					throw invalidOption(option, value);
				return (value.length == 1 && value[0].equalsIgnoreCase("true"));
			}

			@Override
			public long getLong() {
				if (value.length != 1)
					throw invalidOption(option, value);
				return Long.parseLong(value[0]);
			}

			@Override
			public String getString() {
				if (value.length != 1)
					throw invalidOption(option, value);
				return value[0];
			}

			@Override
			public List<String> getStringList(List<String> ret) {
				for (String s: value)
					ret.add(s);
				return ret;
			}
		}
	}

	public static class BoolOption extends CLIOption<Boolean> {
		public BoolOption(String longopt, Character shortopt, String desc) {
			super(longopt, shortopt, Arg.None, desc);
		}
		public BoolOption(String longopt, String desc) {
			super(longopt, null, Arg.None, desc);
		}
		public BoolOption(Character shortopt, String desc) {
			super(String.valueOf(shortopt), shortopt, Arg.None, desc);
		}
		public ICLIOptValue<Boolean> fromString(String...values) {
			if (values != null) {
				switch (values.length) {
				case 0:
					return new BoolValue(getLongOpt(), Boolean.FALSE);
				case 1:
					return new BoolValue(getLongOpt(), Boolean.valueOf(values[0]));
			}}
			throw invalidOption(values);
		}
		private static class BoolValue extends OptValue<Boolean> {
			private Boolean value;
			BoolValue(String option, Boolean value) {
				super(option);
				this.value = value;
			}
			public boolean getBool() {
				return value != null && value.equals(Boolean.TRUE);
			}
		}
	}

	public static class IntOption extends CLIOption<Integer> {
		public IntOption(String longopt, Character shortopt, String desc) {
			super(longopt, shortopt, Arg.Require, desc);
		}
		public IntOption(String longopt, String desc) {
			super(longopt, null, Arg.Require, desc);
		}
		public IntOption(Character shortopt, String desc) {
			super(String.valueOf(shortopt), shortopt, Arg.Require, desc);
		}
		public ICLIOptValue<Integer> fromString(String...values) throws InvalidOptionException {
			if (values != null) {
				int size = values.length;
				if (size == 0)
					return null;
				if (size == 1) {
					int value = Integer.parseInt(values[0]);
					return new IntValue(getLongOpt(), value);
			}}
			throw invalidOption(values);
		}
		private static class IntValue extends OptValue<Integer> {
			private int value;
			IntValue(String option, int value) {
				super(option);
				this.value = value;
			}

			@Override
			public long getLong() {
				return value;
			}
		}
	}

	public static class LongOption extends CLIOption<Long> {
		public LongOption(String longopt, Character shortopt, String desc) {
			super(longopt, shortopt, Arg.Require, desc);
		}
		public LongOption(String longopt, String desc) {
			super(longopt, null, Arg.Require, desc);
		}
		public LongOption(Character shortopt, String desc) {
			super(String.valueOf(shortopt), shortopt, Arg.Require, desc);
		}
		public ICLIOptValue<Long> fromString(String...values) throws InvalidOptionException {
			if (values != null) {
				int size = values.length;
				if (size == 0)
					return null;
				if (size == 1) {
					return new LongValue(getLongOpt(), Long.parseLong(values[0]));
			}}
			throw invalidOption(values);
		}
		private static class LongValue extends OptValue<Long> {
			private long value;
			LongValue(String option, long value) {
				super(option);
				this.value = value;
			}
			public long getLong() {
				return value;
			}
		}
	}

	public static class FloatOption extends CLIOption<Float> {
		public FloatOption(String longopt, Character shortopt, String desc) {
			super(longopt, shortopt, Arg.Require, desc);
		}
		public FloatOption(String longopt, String desc) {
			super(longopt, null, Arg.Require, desc);
		}
		public FloatOption(Character shortopt, String desc) {
			super(String.valueOf(shortopt), shortopt, Arg.Require, desc);
		}
		public ICLIOptValue<Float> fromString(String...values) throws InvalidOptionException {
			if (values != null) {
				int size = values.length;
				if (size == 0)
					return null;
				if (size == 1) {
					return new FloatValue(getLongOpt(), Float.parseFloat(values[0]));
			}}
			throw invalidOption(values);
		}
		private static class FloatValue extends OptValue<Float> {
			private float value;
			FloatValue(String option, float value) {
				super(option);
				this.value = value;
			}
		}
	}

	public static class DoubleOption extends CLIOption<Double> {
		public DoubleOption(String longopt, Character shortopt, String desc) {
			super(longopt, shortopt, Arg.Require, desc);
		}
		public DoubleOption(String longopt, String desc) {
			super(longopt, null, Arg.Require, desc);
		}
		public DoubleOption(Character shortopt, String desc) {
			super(String.valueOf(shortopt), shortopt, Arg.Require, desc);
		}
		public ICLIOptValue<Double> fromString(String...values) throws InvalidOptionException {
			if (values != null) {
				int size = values.length;
				if (size == 0)
					return null;
				if (size == 1) {
					return new DoubleValue(getLongOpt(), Double.parseDouble(values[0]));
			}}
			throw invalidOption(values);
		}
		private static class DoubleValue extends OptValue<Double> {
			private double value;
			DoubleValue(String option, double value) {
				super(option);
				this.value = value;
			}
		}
	}

	public static class StringOption extends CLIOption<String> {
		public StringOption(String longopt, Character shortopt, String desc) {
			super(longopt, shortopt, Arg.Require, desc);
		}
		public StringOption(String longopt, String desc) {
			super(longopt, null, Arg.Require, desc);
		}
		public StringOption(Character shortopt, String desc) {
			super(String.valueOf(shortopt), shortopt, Arg.Require, desc);
		}
		public ICLIOptValue<String> fromString(String...values) throws InvalidOptionException {
			if (values != null) {
				int len = values.length;
				if (len == 0)
					return null;
				if (len == 1)
					return new StringValue(getLongOpt(), values[0]);
			}
			throw invalidOption(values);
		}
		private static class StringValue extends OptValue<String> {
			private String value;
			StringValue(String option, String value) {
				super(option);
				this.value = value;
			}
			@Override
			public String getString() {
				return value;
			}
		}
	}

	public static class StringListOption extends CLIOption<String[]> {
		public StringListOption(String longopt, Character shortopt, String desc) {
			super(longopt, shortopt, Arg.RequireArray, desc);
		}
		public StringListOption(String longopt, String desc) {
			super(longopt, null, Arg.RequireArray, desc);
		}
		public StringListOption(Character shortopt, String desc) {
			super(String.valueOf(shortopt), shortopt, Arg.RequireArray, desc);
		}
		public ICLIOptValue<String[]> fromString(String...values) throws InvalidOptionException {
			if (values != null) {
				return new StringListValue(getLongOpt(), values);
			}
			throw invalidOption(null);
		}
		private static class StringListValue extends OptValue<String[]> {
			private String[] value;
			StringListValue(String option, String[] value) {
				super(option);
				this.value = value;
			}

			@Override
			public List<String> getStringList(List<String> ret) {
				for (String s: value)
					ret.add(s);
				return ret;
			}
		}
	}

	////////////////////////////////////////////////////////////////////////

	private abstract static class OptValue<T> implements ICLIOptValue<T> {

		String option;

		protected OptValue(String optname) {
			this.option = optname;
		}

		public boolean getBool() {
			throw new UnsupportedOperationException("ERROR: getBool() not supported: option=" + option);
		}


		public long getLong() {
			throw new UnsupportedOperationException("ERROR: getLong() not supported: option=" + option);
		}

		public String getString() {
			throw new UnsupportedOperationException("ERROR: getString() not supported: option=" + option);
		}


		public List<String> getStringList(List<String> ret) {
			throw new UnsupportedOperationException(
				"ERROR: getStringList() not supported: option=" + option);
		}
	}

	////////////////////////////////////////////////////////////////////////
}
