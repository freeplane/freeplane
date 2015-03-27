/*
 * Created on 14.10.2008
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.inet.jorthotests;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.inet.jortho.SpellChecker;

public class AllTests {
	private static boolean isInit;

	/**
	 * register the dictionaries
	 */
	static void init() {
		if (!isInit) {
			isInit = true;
			final int threadCount = Thread.activeCount();
			SpellChecker.registerDictionaries(null, null);
			// wait until the dictionaries are loaded.
			for (int i = 0; i < 50; i++) {
				if (threadCount >= Thread.activeCount()) {
					break;
				}
				try {
					Thread.sleep(100);
				}
				catch (final InterruptedException e) {
					break;
				}
			}
		}
	}

	public static Test suite() {
		final TestSuite suite = new TestSuite("JOrtho Tests");
		suite.addTestSuite(EventTest.class);
		suite.addTestSuite(MemoryTest.class);
		return suite;
	}
}
