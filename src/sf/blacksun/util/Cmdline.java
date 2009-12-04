/*
 * Copyright (c) 2003, Chris Leung, simontsui. All rights reserved.
 *
 * The software in this package is distributed under the GNU General Public
 * License version 2, as published by the Free Software Foundation, but with
 * the Classpath exception.  You should have received a copy of the GNU General
 * Public License (GPL) and the Classpath exception along with this program.
 */
package sf.blacksun.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import sf.blacksun.util.CLIUtil.InvalidOptionException;
import sf.blacksun.util.struct.ArrayIterable;
import sf.blacksun.util.struct.Empty;
import sf.blacksun.util.struct.StructUtil;

public class Cmdline<S> implements ICmdline<S> {


	////////////////////////////////////////////////////////////////////////

	Map<String, ICLIOption<?>> optionSpecs = new HashMap<String, ICLIOption<?>>();
	Map<ICLIOption<?>, ICLIOptValue<?>> cliOptions = new IdentityHashMap<ICLIOption<?>, ICLIOptValue<?>>();
	String[] cliArguments = new String[0];

	////////////////////////////////////////////////////////////////////////

	public Cmdline(Class<? extends S> clazz, String[] args, String...xargs) {
		this(clazz, StructUtil.concat(args, xargs));
	}

	public Cmdline(Class<? extends S> clazz, Collection<String> args) {
		this(clazz, args.toArray(new String[args.size()]));
	}

	public Cmdline(Class<? extends S> clazz, String...args) {
		Field[] fields = clazz.getFields();
		List<ICLIOption<?>> specs = new ArrayList<ICLIOption<?>>();
		for (Field f: fields) {
			if (ICLIOption.class.isAssignableFrom(f.getType())) {
				try {
					specs.add((ICLIOption<?>)f.get(null));
				} catch (IllegalArgumentException e) {
					throw new InvalidOptionException(e);
				} catch (IllegalAccessException e) {
					throw new InvalidOptionException(e);
		}}}
		parseCmdline(specs, args);
	}

	public Cmdline(ICLIOption<?>[] specs, String...args) {
		parseCmdline(ArrayIterable.wrap(specs), args);
	}

	private Cmdline() {
	}


	@SuppressWarnings("unchecked")
	public void parseCmdline(String...args) {
		Map<String, Object> opts = new HashMap<String, Object>();
		cliArguments = CLIUtil.getArgsArray(opts, args, optionSpecs.values());
		for (Map.Entry<String, Object> e: opts.entrySet()) {
			String key = e.getKey();
			ICLIOption spec = optionSpecs.get(key);
			if (spec == null)
				throw new InvalidOptionException("Invalid option: " + key);
			Object value = e.getValue();
			String[] a;
			if (value == null) {
				a = Empty.STRING_ARRAY;
			} else if (value instanceof List<?>) {
				List<String> list = (List<String>)value;
				a = list.toArray(new String[list.size()]);
			} else {
				a = new String[] { value.toString() };
			}
			put(spec, a);
	}}

	private void parseCmdline(Iterable<ICLIOption<?>> specs, String...args) {
		for (ICLIOption<?> spec: specs)
			optionSpecs.put(spec.getLongOpt(), spec);
		parseCmdline(args);
	}

	////////////////////////////////////////////////////////////////////////

	public boolean isHelp(int argcount, ICLIOption<Boolean> helpopt) {
		return (getArgCount() <= argcount) || (helpopt != null && getBool(helpopt));
	}


	public int getArgCount() {
		return cliArguments.length;
	}


	public ICmdline<S> put(ICLIOption<?> spec, String...values) {
		if (!optionSpecs.containsKey(spec.getLongOpt()))
			throw new InvalidOptionException("ERROR: Invalid option: " + spec.getLongOpt());
		cliOptions.put(spec, spec.fromString(values == null ? Empty.STRING_ARRAY : values));
		return this;
	}


	public String sprintUsage(String header) {
		return CLIUtil.sprintUsage(header, "\t", new TreeSet<ICLIOption<?>>(optionSpecs.values()), null);
	}


	public boolean getBool(ICLIOption<Boolean> spec) {
		ICLIOptValue<?> value = cliOptions.get(spec);
		return value == null ? false : value.getBool();
	}


	public long getLong(ICLIOption<Long> spec, long def) {
		ICLIOptValue<?> value = cliOptions.get(spec);
		return value == null ? def : value.getLong();
	}


	public String getString(ICLIOption<String> spec) {
		ICLIOptValue<?> value = cliOptions.get(spec);
		return value == null ? null : value.getString();
	}

	public String getString(ICLIOption<String> spec, String def) {
		ICLIOptValue<?> ret = cliOptions.get(spec);
		return ret == null ? def : ret.getString();
	}


	public List<String> getStringList(ICLIOption<String[]> spec) {
		return getstringlist(new ArrayList<String>(), spec);
	}


	private List<String> getstringlist(List<String> ret, ICLIOption<?> spec) {
		ICLIOptValue<?> value = cliOptions.get(spec);
		return value == null ? ret : value.getStringList(ret);
	}


	////////////////////////////////////////////////////////////////////////
}
