/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author foltin
 */
public class Tools {
	public static class BooleanHolder {
		private boolean value;

		public BooleanHolder() {
		}

		public BooleanHolder(final boolean initialValue) {
			value = initialValue;
		}

		public boolean getValue() {
			return value;
		}

		public void setValue(final boolean value) {
			this.value = value;
		}
	}

	static public class IntHolder {
		private int value;

		public IntHolder() {
		}

		public IntHolder(final int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		public void setValue(final int value) {
			this.value = value;
		}

		public String toString() {
			return new String("IntHolder(") + value + ")";
		}
	}

	/**
	 * Tests a string to be equals with "true".
	 *
	 * @return true, iff the String is "true".
	 */
	public static boolean isPreferenceTrue(final String option) {
		return Tools.safeEquals(option, "true");
	}

	public static String listToString(final List list) {
		final ListIterator it = list.listIterator(0);
		String str = new String();
		while (it.hasNext()) {
			str += it.next().toString() + ";";
		}
		return str;
	}

	public static void logException(final Throwable e) {
		Tools.logException(e, "");
	}

	public static void logException(final Throwable e, final String comment) {
		Logger.global.log(Level.SEVERE, "An exception occured: " + comment, e);
	}

	public static void logTransferable(final Transferable t) {
		System.err.println();
		System.err.println("BEGIN OF Transferable:\t" + t);
		final DataFlavor[] dataFlavors = t.getTransferDataFlavors();
		for (int i = 0; i < dataFlavors.length; i++) {
			System.out.println("  Flavor:\t" + dataFlavors[i]);
			System.out.println("    Supported:\t" + t.isDataFlavorSupported(dataFlavors[i]));
			try {
				System.out.println("    Content:\t" + t.getTransferData(dataFlavors[i]));
			}
			catch (final Exception e) {
			}
		}
		System.err.println("END OF Transferable");
		System.err.println();
	}

	/** \0 is not allowed: */
	public static String makeValidXml(final String pXmlNoteText) {
		return pXmlNoteText.replace('\0', ' ');
	}

	public static boolean safeEquals(final String string1, final String string2) {
		return (string1 != null && string2 != null && string1.equals(string2))
		        || (string1 == null && string2 == null);
	}

	public static boolean safeEqualsIgnoreCase(final String string1, final String string2) {
		return (string1 != null && string2 != null && string1.toLowerCase().equals(
		    string2.toLowerCase()))
		        || (string1 == null && string2 == null);
	}
}
