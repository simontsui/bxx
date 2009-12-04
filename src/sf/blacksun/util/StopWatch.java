package sf.blacksun.util;


/*
 * Copyright (c) 2004, Chris Leung, simontsui. All rights reserved.
 *
 * The software in this package is distributed under the GNU General Public
 * License version 2, as published by the Free Software Foundation, but with
 * the Classpath exception.  You should have received a copy of the GNU General
 * Public License (GPL) and the Classpath exception along with this program.
 */

/** Stopwatch for timing elapse time.
 */
public class StopWatch implements IStopWatch {


	private long startTime = -1;
	private float elapsed = 0;

	public StopWatch() {
	}

	public StopWatch(boolean start) {
		if (start)
			start();
	}

	public StopWatch start() {
		startTime = System.currentTimeMillis();
		return this;
	}

	public StopWatch stop() {
		if (startTime < 0)
			throw new RuntimeException("StopWatch is not running.");
		elapsed += (System.currentTimeMillis() - startTime) / 1000f;
		startTime = -1;
		return this;
	}


	/** @return Time elapsed since start() in sec. */
	public float elapsed() {
		if (startTime < 0)
			return elapsed;
		return elapsed + (System.currentTimeMillis() - startTime) / 1000f;
	}

	public String toString() {
		return String.format("%1$8.2f (sec)", new Float(elapsed()));
	}

	public String toString(String msg) {
		return format(msg, elapsed());
	}


	////////////////////////////////////////////////////////////////////////

	private String format(String msg, float time) {
		return String.format("%1$-32s: %2$8.2f (sec)", msg, time);
	}


	////////////////////////////////////////////////////////////////////////
}
