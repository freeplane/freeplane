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
 *  Created on 23.12.2007
 */
package com.inet.jortho;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Locale;

/**
 * This is a reference implementation of the interface {@link UserDictionaryProvider}.
 * It save the user dictionaries on the local disk as text files.
 * @author Volker Berlin
 */
public class FileUserDictionary implements UserDictionaryProvider {
	private File file;
	private final String fileBase;

	/** 
	 * Create a FileUserDictionary with the dictionaries in the root of the current
	 * application.
	 */
	public FileUserDictionary() {
		this("");
	}

	/**
	 * Create a FileUserDictionary with the dictionaries on a specific location.
	 * @param fileBase the base 
	 */
	public FileUserDictionary(String fileBase) {
		if (fileBase == null) {
			fileBase = "";
		}
		fileBase = fileBase.trim();
		fileBase = fileBase.replace('\\', '/');
		if (fileBase.length() > 0 && !fileBase.endsWith("/")) {
			fileBase += "/";
		}
		this.fileBase = fileBase;
	}

	/**
	 * {@inheritDoc}
	 */
	public void addWord(final String word) {
		try {
			try (final Writer writer = new OutputStreamWriter(new FileOutputStream(file, true), "UTF8")) {
	            if (file.length() > 0) {
	                writer.write("\n");
	            }
	            writer.write(word);
			}
		}
		catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getUserWords(final Locale locale) {
		file = new File(fileBase + "UserDictionary_" + locale + ".txt");
		try {
			final FileInputStream input = new FileInputStream(file);
            final StringBuilder builder = new StringBuilder();
            final char[] buffer = new char[4096];
			try (final Reader reader = new InputStreamReader(input, "UTF8")) {
	            int count = 0 ;
	            while ((count = reader.read(buffer)) > 0) {
	                builder.append(buffer, 0, count);
	            }
			}
			return builder.toString();
		}
		catch (final IOException ex) {
			/* ignore FileNotFound */
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setUserWords(final String wordList) {
		try {
			try (final Writer writer = new OutputStreamWriter(new FileOutputStream(file), "UTF8")){
			    writer.write(wordList);
			}
		}
		catch (final Exception ex) {
			ex.printStackTrace();
		}
	}
}
