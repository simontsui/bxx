package sf.blacksun.util;

/*
 * Copyright (c) 2004, Chris Leung, simontsui. All rights reserved.
 *
 * The software in this package is distributed under the GNU General Public
 * License version 2, as published by the Free Software Foundation, but with
 * the Classpath exception.  You should have received a copy of the GNU General
 * Public License (GPL) and the Classpath exception along with this program.
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;


import java.io.IOException;
import java.io.InputStream;

import java.io.OutputStream;


import java.io.PrintWriter;

import java.io.Serializable;
import java.io.Writer;





import java.util.Collection;

import java.util.Comparator;





import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;








/**
 * Static file utilities.
 */
public class FileUtil {


	protected FileUtil() {
	}


	public static String rpath(File file, File basedir) {
		return rpath(file, basedir, false);
	}

	public static String rpath(File file, File basedir, boolean allowdotdot) {
		return rpath(file, basedir, allowdotdot, true);
	}

	public static String rpath(File file, File basedir, boolean allowdotdot, boolean all) {
		if (file.equals(basedir))
			return ".";
		String path = file.getAbsolutePath();
		File dir = file;
		while ((dir = dir.getParentFile()) != null) {
			if (dir.equals(basedir)) {
				return removeLeadingSeparator(path.substring(dir.getAbsolutePath().length()));
		}}
		if (allowdotdot) {
			for (; file != null; file = file.getParentFile()) {
				StringBuilder prefix = new StringBuilder();
				dir = basedir;
				while ((dir = dir.getParentFile()) != null) {
					prefix.append(".." + File.separatorChar);
					if (file.equals(dir)) {
						return removeTrailingSeparator(
							prefix.toString()
								+ removeLeadingSeparator(
									path.substring(dir.getAbsolutePath().length())));
		}}}}
		if (all)
			throw new RuntimeException("File is not under basedir: file=" + file + ", basedir=" + basedir);
		return null;
	}


	public static void listAllRecursive(
		final Collection<String> ret,
		final File basedir,
		File dir,
		final boolean absolute,
		final FileFilter filter) {
		dir.listFiles(
			new FileFilter() {
				public boolean accept(File file) {
					if (filter.accept(file)) {
						if (ret != null) {
							ret.add(
								absolute
									? file.getAbsolutePath()
									: rpath(file, basedir));
					}}
					if (file.isDirectory())
						listAllRecursive(ret, basedir, file, absolute, filter);
					return false;
				}
			});
	}


	public static void findRecursive(final Collection<File> ret, File base, final FileFilter filter) {
		if (!base.isDirectory())
			throw new IllegalArgumentException("Expected directory: " + base.getAbsolutePath());
		base.listFiles(
			new FileFilter() {
				public boolean accept(File file) {
					if (filter.accept(file)) {
						if (ret != null)
							ret.add(file);
					}
					if (file.isDirectory())
						findRecursive(ret, file, filter);
					return false;
				}
			});
	}


	public static int rmdirRecursive(File dir, final FileFilter filter, boolean keeproot) {
		if (!dir.isDirectory())
			return 0;
		final int[] count = new int[] { 0 };
		dir.listFiles(
			new FileFilter() {
				public boolean accept(File path) {
					if (filter != null && !filter.accept(path))
						return false;
					if (path.isDirectory()) {
						rmdirRecursive(path, filter, false);
						if (path.delete())
							++count[0];
						return false;
					}
					if (path.delete())
						++count[0];
					return false;
				}
			});
		if (!keeproot && dir.delete())
			++count[0];
		return count[0];
	}


	////////////////////////////////////////////////////////////////////

	/**
	 * Open given path as InputStream or GZIPInputStream depends on file extension.
	 * @throws IOException if open failed. Any opened stream is closed before return.
	 */
	public static InputStream openInputStream(File file) throws IOException {
		if (file == null)
			return System.in;
		InputStream is = new FileInputStream(file);
		String name = file.getName();
		int len = name.length();
		if (len > 3 && name.substring(len - 3, len).equalsIgnoreCase(".gz")) {
			try {
				is = new GZIPInputStream(is);
			} catch (IOException e) {
				is.close();
				throw e;
		}}
		return is;
	}

	/**
	 * Open given path as BufferedInputSteram depends on file extension.
	 * @throws IOException if open failed. Any opened stream is closed before return.
	 */
	public static InputStream openInputStream(File file, int bufsize) throws IOException {
		return new BufferedInputStream(openInputStream(file), bufsize);
	}


	/**
	 * Open given path as OutputStream or GZIPOutputStream depends on file extension.
	 * @throws IOException if open failed. Any opened stream is closed before return.
	 */
	public static OutputStream openOutputStream(File file) throws IOException {
		if (file == null)
			return System.out;
		OutputStream is = new FileOutputStream(file);
		String name = file.getName();
		int len = name.length();
		if (len > 3 && name.substring(len - 3, len).equalsIgnoreCase(".gz")) {
			try {
				is = new GZIPOutputStream(is);
			} catch (IOException e) {
				is.close();
				throw e;
		}}
		return is;
	}

	/**
	 * Open given path as BufferedOutputStream depends on file extension.
	 * @throws IOException if open failed. Any opened stream is closed before return.
	 */
	public static OutputStream openOutputStream(File file, int bufsize) throws IOException {
		return new BufferedOutputStream(openOutputStream(file), bufsize);
	}


	/**
	 * Open given path as PrintWriter depends on file extension.
	 * NOTE: PrintWriter is internally buffered by default, so there is no need for a BufferedOutputStream,
	 * but a larger buffer can be specified if neccessary
	 * @throws IOException if open failed. Any opened stream is closed before return.
	 */
	public static PrintWriter openWriter(File file, int bufsize) throws IOException {
		return new PrintWriter(openOutputStream(file, bufsize));
	}

	/** Close I/O stream, ignore IOException. */
	public static void close(Closeable s) {
		if (s == null)
			return;
		try {
			s.close();
		} catch (IOException e) {
	}}


	/**
	 * If path is not null, close writer. Otherwise just flush.
	 * Ignore IOException in both clase.
	 */
	public static void close(Writer s, String path) {
		if (s == null)
			return;
		try {
			if (path == null) {
				s.flush();
				return;
			}
			s.close();
		} catch (IOException e) {
	}}


	////////////////////////////////////////////////////////////////////

	private static String removeLeadingSeparator(String path) {
		while (path.startsWith(File.separator))
			path = path.substring(1);
		return path;
	}

	private static String removeTrailingSeparator(String path) {
		while (path.endsWith(File.separator))
			path = path.substring(0, path.length() - 1);
		return path;
	}

	////////////////////////////////////////////////////////////////////

	public static class FileComparator implements Comparator<File>, Serializable {
		private static final long serialVersionUID = -7931411981132951772L;
		public int compare(File a, File b) {
			if (a == null)
				return b == null ? 0 : -1;
			return a.compareTo(b);
		}
	}

	public static class FileIgnorecaseComparator implements Comparator<File>, Serializable {
		private static final long serialVersionUID = 4725680571692221448L;
		public int compare(File a, File b) {
			if (a == null)
				return b == null ? 0 : -1;
			return a.getAbsolutePath().compareToIgnoreCase(b.getAbsolutePath());
		}
	}

	////////////////////////////////////////////////////////////////////
}
