/*
 *  JOrtho
 *
 *  Copyright (C) 2005-2008 by i-net software
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as 
 *  published by the Free Software Foundation; either version 2 of the
 *  License, or (at your option) any later version. 
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA.
 *  
 *  Created on 10.11.2005
 */
package com.inet.jortho;

import java.util.ResourceBundle;

/**
 * @author Volker Berlin
 */
class Utils {
	/**
	 * Create a String where the first letter is written with a uppercase.
	 * 
	 * @param word
	 *            the word that should be change
	 * @return the new String if needed
	 */
	static String getCapitalized(final String word) {
		if ((word.length() > 0) && Character.isLowerCase(word.charAt(0))) {
			return word.substring(0, 1).toUpperCase() + word.substring(1);
		}
		return word;
	}

	/**
	 * Create a String with inverted case for the first letter. If it is lowercase then it will change to uppercase and
	 * vice versa.
	 * 
	 * @param word
	 *            the word that should be change
	 * @return the new String if needed
	 */
	static String getInvertedCapitalizion(final String word) {
		if (word.length() > 0) {
			if (Character.isLowerCase(word.charAt(0))) {
				return word.substring(0, 1).toUpperCase() + word.substring(1);
			}
			if (Character.isUpperCase(word.charAt(0))) {
				return word.substring(0, 1).toLowerCase() + word.substring(1);
			}
		}
		return word;
	}

	/**
	 * Translate a GUI string in one of the supported languages. If the value was not find then the key is returned.
	 * 
	 * @param value
	 *            the key of the language resource.
	 * @return the translation result
	 */
	static String getResource(final String value) {
		try {
			final ResourceBundle resource = ResourceBundle.getBundle("com.inet.jortho.i18n.resource");
			return resource.getString(value);
		}
		catch (final Exception e) {
			e.printStackTrace();
		}
		return value;
	}

	/**
	 * Check if all letter are uppercase. Character that are not letters are ignored.
	 * 
	 * @param word
	 *            the word that should be check. It can not be null or empty.
	 * @return if all character are a uppercase letter
	 */
	static boolean isAllCapitalized(final String word) {
		for (int i = 0; i < word.length(); i++) {
			final char ch = word.charAt(i);
			if (Character.isLetter(ch) && !Character.isUpperCase(ch)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Check if the first character is a uppcase letter
	 * 
	 * @param word
	 *            the word that should be check. It can not be null.
	 * @return true if the first character is a uppercase letter
	 */
	static boolean isFirstCapitalized(final String word) {
		return (word.length() > 0) && Character.isUpperCase(word.charAt(0));
	}

	/**
	 * Check if the word include a digit.
	 * 
	 * @param word
	 *            the word that should be check. It can not be null.
	 * @return if there is any number in the word.
	 */
	static boolean isIncludeNumbers(final String word) {
		for (int i = 0; i < word.length(); i++) {
			final char ch = word.charAt(i);
			if (Character.isDigit(ch)) {
				return true;
			}
		}
		return false;
	}
}
