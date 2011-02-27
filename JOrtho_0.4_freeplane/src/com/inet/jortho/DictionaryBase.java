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
 *  Created on 02.11.2005
 */
package com.inet.jortho;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Volker Berlin
 */
abstract class DictionaryBase {
	protected static final char LAST_CHAR = 0xFFFF;
	protected int idx;
	protected int size;
	protected char[] tree;

	/**
	 * Empty Constructor.
	 */
	protected DictionaryBase() {
		/* empty */
	}

	DictionaryBase(final char[] tree) {
		this.tree = tree;
		size = tree.length;
	}

	/**
	 * Returns an int that describe the dissimilarity of the characters. 
	 * The value is ever larger 0. A value of means only a small difference.
	 * @param a first char
	 * @param b second char
	 * @return the dissimilarity
	 */
	private int charDiff(char a, char b) {
		a = Character.toLowerCase(a);
		b = Character.toLowerCase(b);
		if (a == b) {
			return 1;
		}
		if (Character.getType(a) != Character.getType(b)) {
			return 6;
		}
		return 5;
	}

	/**
	 * Check if the word exist in this dictinary.
	 * @param word the word to check. Can't be null.
	 * @return true if the word exist.
	 */
	public boolean exist(final String word) {
		idx = 0;
		for (int i = 0; i < word.length(); i++) {
			final char c = word.charAt(i);
			while (idx < size && tree[idx] < c) {
				idx += 3;
			}
			if ((idx >= size || tree[idx] != c)) {
				return false;
			}
			if (i == word.length() - 1 && isWordMatch()) {
				return true;
			}
			idx = readIndex();
			if (idx <= 0) {
				return false;
			}
		}
		return false;
	}

	/**
	 * Check if on the current item position a word ends.
	 */
	private boolean isWordMatch() {
		return (tree[idx + 1] & 0x8000) > 0;
	}

	/**
	 * Read the offset in the tree of the next character. 
	 */
	final int readIndex() {
		return ((tree[idx + 1] & 0x7fff) << 16) + tree[idx + 2];
	}

	/**
	 * Search if the character exist in the current node. If found then the variable <code>idx</code> point to the location.
	 * If not found then it point on the next character (char value) item in the node. 
	 * @param c the searching character
	 * @return true if found
	 */
	private boolean searchChar(final char c) {
		while (idx < size && tree[idx] < c) {
			idx += 3;
		}
		if ((idx >= size || tree[idx] != c)) {
			return false;
		}
		return true;
	}

	/**
	 * Returns a list of suggestions if the word is not in the dictionary.
	 * @param word the wrong spelled word. Can't be null.
	 * @return a list of class Suggestion.
	 * @see Suggestion
	 */
	public List<Suggestion> searchSuggestions(final String word) {
		if (word.length() == 0 || exist(word)) {
			return new ArrayList<Suggestion>();
		}
		final Suggestions suggesions = new Suggestions(Math.min(20, 4 + word.length()));
		idx = 0;
		searchSuggestions(suggesions, word, 0, 0, 0);
		final List<Suggestion> list = suggesions.getlist();
		Collections.sort(list);
		return list;
	}

	/**
	 * Es wird nach verschiedenen Regeln nach aehnlichen Woertern gesucht.
	 * Je nach Regel gibt es einen anderen diff. Jekleiner der diff desto aehnlicher.
	 * Diese Methode ruft sich rekursiv auf.
	 * @param list Kontainer fuer die gefundenen Woerter
	 * @param chars bis zur charPosition bereits gemappte Buchstaben, danach noch zu mappende des orignal Wortes
	 * @param charPosition Zeichenposition im char array
	 * @param lastIdx Position im Suchindex der zur aktuellen Zeichenposition zeigt.
	 * @param diff Die Unaehnlichkeit bis zur aktuellen Zeichenposition
	 */
	private void searchSuggestions(final Suggestions list, final CharSequence chars, final int charPosition,
	                               final int lastIdx, final int diff) {
		if (diff > list.getMaxDissimilarity()) {
			return;
		}
		// First with the correct letters to go on 
		idx = lastIdx;
		char currentChar = chars.charAt(charPosition);
		if (searchChar(currentChar)) {
			if (isWordMatch()) {
				if (charPosition + 1 == chars.length()) {
					// exact match at this character position
					list.add(new Suggestion(chars, diff));
				}
				else {
					// a shorter match, we need to cut the string
					final int length = charPosition + 1;
					final CharSequence chars2 = chars.subSequence(0, length);
					list.add(new Suggestion(chars2, diff + (chars.length() - length) * 5));
				}
			}
			idx = readIndex();
			if (idx <= 0) {
				// no more characters in the tree
				return;
			}
			if (charPosition + 1 == chars.length()) {
				searchSuggestionsLonger(list, chars, chars.length(), idx, diff + 5);
				return;
			}
			searchSuggestions(list, chars, charPosition + 1, idx, diff);
		}
		// transposed letters and additional letter
		if (charPosition + 1 < chars.length()) {
			idx = lastIdx;
			currentChar = chars.charAt(charPosition + 1);
			if (searchChar(currentChar)) {
				final int tempIdx = idx;
				//transposed letters (German - Buchstabendreher)
				idx = readIndex();
				if (idx > 0) {
					final StringBuilder buffer = new StringBuilder(chars);
					buffer.setCharAt(charPosition + 1, chars.charAt(charPosition));
					buffer.setCharAt(charPosition, currentChar);
					searchSuggestions(list, buffer, charPosition + 1, idx, diff + 3);
				}
				// Additional character in the misspelled word
				idx = tempIdx;
				final StringBuilder buffer = new StringBuilder();
				buffer.append(chars, 0, charPosition);
				buffer.append(chars, charPosition + 1, chars.length());
				searchSuggestions(list, buffer, charPosition, lastIdx, diff + 5);
			}
		}
		// Missing letters, we need to add one character
		{
			int tempIdx = idx = lastIdx;
			while (idx < size && tree[idx] < LAST_CHAR) {
				final char newChar = tree[idx];
				idx = readIndex();
				if (idx > 0 && newChar != currentChar) {
					final StringBuilder buffer = new StringBuilder(chars);
					buffer.insert(charPosition, newChar);
					searchSuggestions(list, buffer, charPosition + 1, idx, diff + 5);
				}
				idx = tempIdx += 3;
			}
		}
		// Typos - wrong letters (One character is replaced with any character)
		if (charPosition < chars.length()) {
			currentChar = chars.charAt(charPosition);
			int tempIdx = idx = lastIdx;
			while (idx < size && tree[idx] < LAST_CHAR) {
				if (isWordMatch()) {
					final StringBuilder buffer = new StringBuilder();
					buffer.append(chars, 0, charPosition);
					buffer.append(tree[idx]);
					list.add(new Suggestion(buffer, diff + 5 + (chars.length() - buffer.length()) * 5));
				}
				if (charPosition + 1 < chars.length()) {
					final char newChar = tree[idx];
					idx = readIndex();
					if (idx > 0 && newChar != currentChar) {
						final StringBuilder buffer = new StringBuilder(chars);
						buffer.setCharAt(charPosition, newChar);
						searchSuggestions(list, buffer, charPosition + 1, idx, diff + charDiff(currentChar, newChar));
					}
				}
				idx = tempIdx += 3;
			}
		}
	}

	private void searchSuggestionsLonger(final Suggestions list, final CharSequence chars, final int originalLength,
	                                     final int lastIdx, final int diff) {
		idx = lastIdx;
		while (idx < size && tree[idx] < LAST_CHAR) {
			if (isWordMatch()) {
				list.add(new Suggestion(chars.toString() + tree[idx], diff));
			}
			idx += 3;
		}
	}
}
