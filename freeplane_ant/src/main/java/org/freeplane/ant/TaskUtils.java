/**
 * TaskUtils.java
 *
 * Copyright (C) 2010,  Volker Boerchers
 *
 * Translator.java is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Translator.java is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package org.freeplane.ant;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.input.InputHandler;
import org.apache.tools.ant.input.InputRequest;
import org.apache.tools.ant.input.MultipleChoiceInputRequest;
import org.apache.tools.ant.util.StringUtils;

public class TaskUtils {
	static class IncludeFileFilter implements FileFilter {
		private ArrayList<Pattern> includePatterns = new ArrayList<Pattern>();
		private ArrayList<Pattern> excludePatterns = new ArrayList<Pattern>();

		IncludeFileFilter(ArrayList<Pattern> includePatterns, ArrayList<Pattern> excludePatterns) {
			this.includePatterns = includePatterns;
			this.excludePatterns = excludePatterns;
		}

		public boolean accept(File pathname) {
			if (pathname.isDirectory())
				return false;
			for (Pattern pattern : excludePatterns) {
				if (pattern.matcher(pathname.getName()).matches())
					return false;
			}
			if (includePatterns.isEmpty())
				return true;
			for (Pattern pattern : includePatterns) {
				if (pattern.matcher(pathname.getName()).matches())
					return true;
			}
			return false;
		}
	}

	static void writeFile(File outputFile, ArrayList<String> sortedLines, String lineSeparator) throws IOException {
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "US-ASCII"));
			for (String line : sortedLines) {
				out.write(line.replaceAll("\\\\[\n\r]+", "\\\\" + lineSeparator));
				// change this to write(<sep>) to enforce Unix or Dos or Mac newlines
				out.write(lineSeparator);
			}
		}
		finally {
			if (out != null) {
				try {
					out.close();
				}
				catch (IOException e) {
					// can't help it
				}
			}
		}
	}

	// adapted from http://www.rgagnon.com/javadetails/java-0515.html, Real Gagnon
	public static String wildcardToRegex(String wildcard) {
		StringBuilder s = new StringBuilder(wildcard.length());
		s.append('^');
		for (int i = 0, is = wildcard.length(); i < is; i++) {
			char c = wildcard.charAt(i);
			switch (c) {
				case '*':
					s.append(".*");
					break;
				case '?':
					s.append(".");
					break;
				// escape special regexp-characters
				case '(':
				case ')':
				case '$':
				case '^':
				case '.':
				case '{':
				case '}':
				case '|':
				case '\\':
					s.append("\\");
					s.append(c);
					break;
				default:
					s.append(c);
					break;
			}
		}
		s.append('$');
		return (s.toString());
	}

	static String readFile(final File inputFile) throws IOException {
		InputStreamReader in = null;
		try {
			in = new InputStreamReader(new FileInputStream(inputFile), "ISO-8859-1");
			StringBuilder builder = new StringBuilder();
			final char[] buf = new char[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				builder.append(buf, 0, len);
			}
			return builder.toString();
		}
		finally {
			if (in != null) {
				try {
					in.close();
				}
				catch (IOException e) {
					// can't help it
				}
			}
		}
	}

	/** returns true if all eols match <code>lineSep</code>. */
	/*package*/static boolean checkEolStyleAndReadLines(String input, ArrayList<String> resultList, String lineSep) {
		resultList.clear();
		boolean eolStyleMatches = true;
		final Matcher matcher = Pattern.compile("(?<!\\\\)(\r\n?|\n)").matcher(input);
		int index = 0;
		while (matcher.find()) {
			final String match = input.subSequence(index, matcher.start()).toString();
			final String separator = matcher.group(1);
			if (separator.equals("\n") && match.endsWith("\\\r")) {
				// only windows: catch escaped CRLF (\\r\n) which will be parsed as \\r<split>\n
				// not setting index will simply skip this match
			}
			else {
				if (!matchEolStyle(separator, lineSep)) {
					eolStyleMatches = false;
				}
				resultList.add(match);
				index = matcher.end();
			}
		}
		// Add remaining segment
		if (input.length() > index)
			resultList.add(input.subSequence(index, input.length()).toString());
		return eolStyleMatches;
	}

	/*package*/static boolean matchEolStyle(String eol, String lineSep) {
		// quick success in the normal case
		if (lineSep.equals(eol))
			return true;
		int i = 0;
		for (; i < eol.length(); i += lineSep.length()) {
			if (eol.indexOf(lineSep, i) != i)
				return false;
		}
		return i == eol.length();
	}

	static String toLine(String key, String value) {
		return key + "=" + value;
	}

	static Project createProject(final Task task) {
		final Project project = new Project();
		final DefaultLogger logger = new DefaultLogger();
		logger.setMessageOutputLevel(Project.MSG_INFO);
		logger.setOutputPrintStream(System.out);
		logger.setErrorPrintStream(System.err);
		project.addBuildListener(logger);
		task.setProject(project);
		return project;
	}

	static String firstToUpper(String string) {
    	if (string == null || string.length() < 2)
    		return string;
    	return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

	@SuppressWarnings("unchecked")
    static String multipleChoice(Project project, String message, String validValues, String defaultValue) {
    	InputRequest request = null;
    	if (validValues != null) {
    		Vector<String> accept = StringUtils.split(validValues, ',');
    		request = new MultipleChoiceInputRequest(message, accept);
    	}
    	else {
    		request = new InputRequest(message);
    	}
    	InputHandler handler = project.getInputHandler();
    	handler.handleInput(request);
    	final String value = request.getInput();
    	if ((value == null || value.trim().length() == 0) && defaultValue != null) {
    		return defaultValue;
    	}
    	return value;
    }

	static String ask(Project project, String message, String defaultValue) {
    	return multipleChoice(project, message, defaultValue, null);
    }
}
