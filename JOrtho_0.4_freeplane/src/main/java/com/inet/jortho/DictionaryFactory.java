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
 *  Created on 15.06.2007
 */
package com.inet.jortho;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.zip.InflaterInputStream;

/** 
 * With the DictionaryFactory you can create / load a Dictionary. A Dictionary is list of word with a API for searching. 
 * The list is saved internal as a tree.
 * @see Dictionary
 * @author Volker Berlin
 */
class DictionaryFactory {
	/**
	 * A node in the search tree. Every Node can include a list of NodeEnties
	 */
	private final static class Node extends ArrayList<NodeEntry> {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		Node() {
			super(1);
		}

		int save(final DictionaryFactory factory) {
			int idx;
			final int start = idx = factory.size;
			//reserve the needed memory
			final int newSize = factory.size + size() * 3 + 1;
			factory.checkSize(newSize);
			factory.size = newSize;
			for (int i = 0; i < size(); i++) {
				final NodeEntry entry = get(i);
				factory.tree[idx++] = entry.c;
				final Node nextNode = entry.nextNode;
				int offset = 0;
				if (nextNode != null) {
					offset = nextNode.save(factory);
				}
				if (entry.isWord) {
					offset |= 0x80000000;
				}
				factory.tree[idx++] = (char) (offset >> 16);
				factory.tree[idx++] = (char) (offset);
			}
			factory.tree[idx] = DictionaryBase.LAST_CHAR;
			return start;
		}

		NodeEntry searchCharOrAdd(final char c) {
			for (int i = 0; i < size(); i++) {
				NodeEntry entry = get(i);
				if (entry.c < c) {
					continue;
				}
				if (entry.c == c) {
					return entry;
				}
				entry = new NodeEntry(c);
				add(i, entry);
				trimToSize(); //reduce the memory consume, there is a very large count of this Nodes.
				return entry;
			}
			final NodeEntry entry = new NodeEntry(c);
			add(entry);
			trimToSize(); //reduce the memory consume, there is a very large count of this Nodes.
			return entry;
		}
	}

	/**
	 * Descript a single charchter in the Dictionary tree.
	 */
	private final static class NodeEntry {
		final char c;
		boolean isWord;
		Node nextNode;

		NodeEntry(final char c) {
			this.c = c;
		}

		/**
		 * Create a new Node and set it as nextNode
		 * @return the nextNode
		 */
		Node createNewNode() {
			return nextNode = new Node();
		}
	}

	private final Node root = new Node();
	private int size;
	private char[] tree;

	/**
	 * Empty Constructor.
	 */
	public DictionaryFactory() {
		/* empty */
	}

	/**
	 * Add a word to the tree. If it already exist then it has no effect. 
	 * @param word the new word.
	 */
	public void add(final String word) {
		Node node = root;
		for (int i = 0; i < word.length(); i++) {
			final char c = word.charAt(i);
			final NodeEntry entry = node.searchCharOrAdd(c);
			if (i == word.length() - 1) {
				entry.isWord = true;
				return;
			}
			final Node nextNode = entry.nextNode;
			if (nextNode == null) {
				node = entry.createNewNode();
			}
			else {
				node = nextNode;
			}
		}
	}

	/**
	 * Check the size of the array and resize it if needed.
	 * @param newSize the requied size
	 */
	final void checkSize(final int newSize) {
		if (newSize > tree.length) {
			final char[] puffer = new char[Math.max(newSize, 2 * tree.length)];
			System.arraycopy(tree, 0, puffer, 0, size);
			tree = puffer;
		}
	}

	/**
	 * Create from the data in this factory a Dictionary object. If there 
	 * are no word added then the Dictionary is empty. The Dictionary need fewer memory as the DictionaryFactory.
	 * @return a Dictionary object.
	 */
	public Dictionary create() {
		tree = new char[10000];
		root.save(this);
		//shrink the array
		final char[] temp = new char[size];
		System.arraycopy(tree, 0, temp, 0, size);
		tree = temp;
		return new Dictionary(tree);
	}

	/**
	 * Load the directory from plain a list of words. The words must be delimmited with newlines. This method can be
	 * called multiple times.
	 * 
	 * @param stream
	 *            a InputStream with words
	 * @param charsetName
	 *            the name of a codepage for example "UTF8" or "Cp1252"
	 * @throws IOException
	 *             If an I/O error occurs.
	 * @throws NullPointerException
	 *             If stream or charsetName is null.
	 */
	public void loadPlainWordList(final InputStream stream, final String charsetName) throws IOException {
		final Reader reader = new InputStreamReader(stream, charsetName);
		loadPlainWordList(reader);
	}

	/**
	 * Load the directory from plain a list of words. The words must be delimmited with newlines. This method can be
	 * called multiple times.
	 * 
	 * @param reader
	 *            a Reader with words
	 * @throws IOException
	 *             If an I/O error occurs.
	 * @throws NullPointerException
	 *             If reader is null.
	 */
	public void loadPlainWordList(final Reader reader) throws IOException {
		final BufferedReader input = new BufferedReader(reader);
		String word = input.readLine();
		while (word != null) {
			if (word.length() > 1) {
				add(word);
			}
			word = input.readLine();
		}
	}

	/**
	 * Load the directory from a compressed list of words with UTF8 encoding. The words must be delimmited with
	 * newlines. This method can be called multiple times.
	 * 
	 * @param filename
	 *            the name of the file
	 * @throws IOException
	 *             If an I/O error occurs.
	 * @throws NullPointerException
	 *             If filename is null.
	 */
	public void loadWordList(final URL filename) throws IOException {
		final URLConnection conn = filename.openConnection();
		conn.setReadTimeout(5000);
		InputStream input = conn.getInputStream();
		input = new InflaterInputStream(input);
		input = new BufferedInputStream(input);
		loadPlainWordList(input, "UTF8");
	}
}
