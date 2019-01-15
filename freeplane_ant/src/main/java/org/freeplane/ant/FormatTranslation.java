/**
 * FormatTranslation.java
 *
 * Copyright (C) 2010,  Volker Boerchers
 *
 * FormatTranslation.java is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * FormatTranslation.java is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package org.freeplane.ant;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/** formats a translation file and writes the result to another file.
 * The following transformations are made:
 * <ol>
 * <li> sort lines (case insensitive)
 * <li> remove duplicates
 * <li> if a key is present multiple times entries marked as [translate me]
 *      and [auto] are removed in favor of normal entries.
 * <li> newline style is changed to &lt;eolStyle&gt;.
 * </ol>
 *
 * Attributes:
 * <ul>
 * <li> dir: the input directory (default: ".")
 * <li> outputDir: the output directory. Overwrites existing files if outputDir
 *      equals the input directory (default: the input directory)
 * <li> includes: wildcard pattern (default: all regular files).
 * <li> excludes: wildcard pattern, overrules includes (default: no excludes).
 * <li> eolStyle: unix|mac|windows (default: platform default).
 * </ul>
 *
 * Build messages:
 * <table border=1>
 * <tr><th>Message</th><th>Action</th><th>Description</th></tr>
 * <tr><td>&lt;file&gt;: no key/val: &lt;line&gt;</td><td>drop line</td><td>broken line with an empty key or without an '=' sign</td></tr>
 * <tr><td>&lt;file&gt;: drop duplicate: &lt;line&gt;</td><td>drop line</td><td>two completely identical lines</td></tr>
 * <tr><td>&lt;file&gt;: drop: &lt;line&gt;</td><td>drop line</td>
 *     <td>this translation is dropped since a better one was found
 *        (quality: [translate me] -> [auto] -> manually translated)
 *     </td>
 * </tr>
 * <tr><td>&lt;file&gt;: drop one of two of equal quality (revisit!):keep: &lt;line&gt;</td><td>keep line</td>
 *     <td>for one key two manual translations were found. This one (arbitrarily chosen) will be kept.
 *         Printout of the complete line allows to correct an action of FormatTranslation via Copy and Past
 *         if it chose the wrong translation.
 *     </td>
 * </tr>
 * <tr><td>&lt;file&gt;: drop one of two of equal quality (revisit!):drop: &lt;line&gt;</td><td>drop line</td>
 *     <td>accompanies the :keep: line: This is the line that is dropped.
 *     </td>
 * </tr>
 * </table>
 * Note that CheckTranslation does not remove anything but produces the same messages!
 */
public class FormatTranslation extends Task {
	static Comparator<String> KEY_COMPARATOR = new Comparator<String>() {
		@Override
        public int compare(String s1, String s2) {
			int n1 = s1.length(), n2 = s2.length();
			for (int i1 = 0, i2 = 0; i1 < n1 && i2 < n2; i1++, i2++) {
				char c1 = s1.charAt(i1);
				char c2 = s2.charAt(i2);
				boolean c1Terminated = c1 == ' ' || c1 == '\t' || c1 == '=';
				boolean c2Terminated = c2 == ' ' || c2 == '\t' || c2 == '=';
				if (c1Terminated && c2Terminated)
					return 0;
				if (c1Terminated && !c2Terminated)
					return -1;
				if (c2Terminated && !c1Terminated)
					return 1;
				if (c1 != c2) {
					c1 = Character.toUpperCase(c1);
					c2 = Character.toUpperCase(c2);
					if (c1 != c2) {
						c1 = Character.toLowerCase(c1);
						c2 = Character.toLowerCase(c2);
						if (c1 != c2) {
							return c1 - c2;
						}
					}
				}
			}
			return n1 - n2;
		}
	};
	private final static int QUALITY_NULL = 0; // for empty values
	private final static int QUALITY_TRANSLATE_ME = 1;
	private final static int QUALITY_AUTO_TRANSLATED = 2;
	private final static int QUALITY_MANUALLY_TRANSLATED = 3;
	private File outputDir;
	private boolean writeIfUnchanged = false;
	private File inputDir = new File(".");
	private final ArrayList<Pattern> includePatterns = new ArrayList<Pattern>();
	private final ArrayList<Pattern> excludePatterns = new ArrayList<Pattern>();
	private String lineSeparator = System.getProperty("line.separator");

	@Override
    public void execute() {
		final int countFormatted = executeImpl(false);
		log(inputDir + ": formatted " + countFormatted + " file" + (countFormatted == 1 ? "" : "s"));
	}

	public int checkOnly() {
		return executeImpl(true);
	}

	/** returns the number of unformatted files. */
	private int executeImpl(boolean checkOnly) {
		validate();
		File[] inputFiles = inputDir.listFiles(new TaskUtils.IncludeFileFilter(includePatterns, excludePatterns));
		return process(inputFiles, checkOnly);
	}

	static public void main(final String argc[]) {
		File[] inputFiles = new File[argc.length];
		int i = 0;
		for (String arg : argc) {
			inputFiles[i++] = new File(arg);
		}
		new FormatTranslation().configureFromDefines().process(inputFiles, false);
	}

	private FormatTranslation configureFromDefines() {
		final String eolStyle = getConfigurationProperty("eolStyle");
		if(eolStyle != null)
			setEolStyle(eolStyle);
		final String dir = getConfigurationProperty("dir");
		if(dir != null)
			setDir(dir);
		final String includes = getConfigurationProperty("includes");
		if(includes != null)
			setIncludes(includes);
		final String excludes = getConfigurationProperty("excludes");
		if(excludes != null)
			setExcludes(excludes);
		final String outputDir = getConfigurationProperty("outputdir");
		if(outputDir != null)
			setOutputDir(outputDir);
		final String writeIfUnchanged = getConfigurationProperty("writeIfUnchanged");
		if(writeIfUnchanged != null)
			setWriteIfUnchanged(Boolean.parseBoolean(writeIfUnchanged));
	    return this;
    }

	protected String getConfigurationProperty(String key) {
	    String propertyName = getClass().getName() + "." + key;
		return System.getProperty(propertyName, null);
    }

	private int process(File[] inputFiles, boolean checkOnly) {
		try {
			int countFormattingRequired = 0;
			for (int i = 0; i < inputFiles.length; i++) {
				File inputFile = inputFiles[i];
				log("processing " + inputFile + "...", Project.MSG_DEBUG);
				final String input = TaskUtils.readFile(inputFile);
				final ArrayList<String> lines = new ArrayList<String>(2048);
				boolean eolStyleMatches = TaskUtils.checkEolStyleAndReadLines(input, lines, lineSeparator);
				final ArrayList<String> sortedLines = processLines(inputFile.getName(), new ArrayList<String>(lines));
				final boolean contentChanged = !lines.equals(sortedLines);
				final boolean formattingRequired = !eolStyleMatches || contentChanged;
				if (formattingRequired) {
					++countFormattingRequired;
					if (checkOnly)
						warn(inputFile + " requires formatting - " + formatCause(contentChanged, eolStyleMatches));
					else
						log(inputFile + "formatted - " + formatCause(contentChanged, eolStyleMatches),
						    Project.MSG_DEBUG);
				}
				if (!checkOnly && (formattingRequired || writeIfUnchanged)) {
					File outputFile;
					if (outputDir != null)
						outputFile = new File(outputDir, inputFile.getName());
					else
						outputFile = inputFile;
					TaskUtils.writeFile(outputFile, sortedLines, lineSeparator);
				}
			}
			return countFormattingRequired;
		}
		catch (IOException e) {
			throw new BuildException(e);
		}
	}

	private String formatCause(boolean contentChanged, boolean eolStyleMatches) {
		final String string1 = eolStyleMatches ? "" : "wrong eol style";
		final String string2 = contentChanged ? "content changed" : "";
		return string1 + (string1.length() > 0 && string2.length() > 0 ? ", " : "") + string2;
	}

	private void validate() {
		if (inputDir == null)
			throw new BuildException("missing attribute 'dir'");
		if (outputDir == null)
			outputDir = inputDir;
		if (!inputDir.isDirectory())
			throw new BuildException("input directory '" + inputDir + "' does not exist");
		if (!outputDir.isDirectory() && !outputDir.mkdirs())
			throw new BuildException("cannot create output directory '" + outputDir + "'");
	}

	ArrayList<String> processLines(final String filename, ArrayList<String> lines) {
		Collections.sort(lines, KEY_COMPARATOR);
		ArrayList<String> result = new ArrayList<String>(lines.size());
		String lastKey = null;
		String lastValue = null;
		for (final String line : lines) {
			if (line.indexOf('#') == 0 || line.matches("\\s*"))
				continue;
			final String standardUnicodeLine = convertUnicodeCharacterRepresentation(line);
			final String[] keyValue = standardUnicodeLine.split("\\s*=\\s*", 2);
			if (keyValue.length != 2 || keyValue[0].length() == 0) {
				// broken line: no '=' sign or empty key (we had " = ======")
				warn(filename + ": no key/val: " + line);
				continue;
			}
			final String thisKey = keyValue[0];
			String thisValue = keyValue[1].trim();
			if (thisValue.matches("(\\[auto\\]|\\[translate me\\])?")) {
				warn(filename + ": drop empty translation: " + line);
				continue;
			}
			if (thisValue.indexOf("{1}") != -1 && keyValue[1].indexOf("{0}") == -1) {
				warn(filename + ": errorneous placeholders usage: {1} used without {0}: " + line);
			}
			if (thisValue.matches(".*\\$\\d.*")) {
				warn(filename + ": use '{0}' instead of '$1' as placeholder! (likewise for $2...): " + line);
				thisValue = thisValue.replaceAll("\\$1", "{0}").replaceAll("\\$2", "{1}");
			}
			if (thisValue.matches(".*\\{\\d[^},]*")) {
				warn(filename + ": mismatched braces in placeholder: '{' not closed by '}': " + line);
			}
			if (thisValue.matches(".*[^']'[^'].*\\{\\d\\}.*") || thisValue.matches(".*\\{\\d\\}.*[^']'[^'].*")) {
				warn(filename + ": replaced single quotes in strings containing placeholders by two: "
				        + "\"'{0}' n'a\" -> \"''{0}'' n''a\": " + line);
				thisValue = thisValue.replaceAll("([^'])'([^'])", "$1''$2");
			}
			if (lastKey != null && thisKey.equals(lastKey)) {
				if (quality(thisValue) < quality(lastValue)) {
					log(filename + ": drop " + TaskUtils.toLine(lastKey, thisValue));
					continue;
				}
				else if (quality(thisValue) == quality(lastValue)) {
					if (thisValue.equals(lastValue)) {
						log(filename + ": drop duplicate " + TaskUtils.toLine(lastKey, thisValue));
					}
					else if (quality(thisValue) == QUALITY_MANUALLY_TRANSLATED) {
						warn(filename //
						        + ": drop one of two of equal quality (revisit!):keep: "
						        + TaskUtils.toLine(lastKey, lastValue));
						warn(filename //
						        + ": drop one of two of equal quality (revisit!):drop: "
						        + TaskUtils.toLine(thisKey, thisValue));
					}
					else {
						log(filename + ": drop " + TaskUtils.toLine(lastKey, thisValue));
					}
					continue;
				}
				else {
					log(filename + ": drop " + TaskUtils.toLine(lastKey, lastValue));
				}
				lastValue = thisValue;
			}
			else {
				if (lastKey != null)
					result.add(TaskUtils.toLine(lastKey, lastValue));
				lastKey = thisKey;
				lastValue = thisValue;
			}
		}
		if (lastKey != null)
			result.add(TaskUtils.toLine(lastKey, lastValue));
		return result;
	}

	private int quality(String value) {
		if (value.length() == 0)
			return QUALITY_NULL;
		if (value.indexOf("[translate me]") > 0)
			return QUALITY_TRANSLATE_ME;
		if (value.indexOf("[auto]") > 0)
			return QUALITY_AUTO_TRANSLATED;
		return QUALITY_MANUALLY_TRANSLATED;
	}

	private void warn(String msg) {
		log(msg, Project.MSG_WARN);
	}

	/** per default output files will only be created if the output would
	 * differ from the input file. Set attribute <code>writeIfUnchanged</code>
	 * to "true" to enforce file creation. */
	public void setWriteIfUnchanged(boolean writeIfUnchanged) {
		this.writeIfUnchanged = writeIfUnchanged;
	}

	public void setDir(String inputDir) {
		setDir(new File(inputDir));
	}

	public void setDir(File inputDir) {
		this.inputDir = inputDir;
	}

	public void setIncludes(String pattern) {
		includePatterns.add(Pattern.compile(TaskUtils.wildcardToRegex(pattern)));
	}

	public void setExcludes(String pattern) {
		excludePatterns.add(Pattern.compile(TaskUtils.wildcardToRegex(pattern)));
	}

	/** parameter is set in the build file via the attribute "outputDir" */
	public void setOutputDir(String outputDir) {
		setOutputDir(new File(outputDir));
	}

	/** parameter is set in the build file via the attribute "outputDir" */
	public void setOutputDir(File outputDir) {
		this.outputDir = outputDir;
	}

	/** parameter is set in the build file via the attribute "eolStyle" */
	public void setEolStyle(String eolStyle) {
		if (eolStyle.toLowerCase().startsWith("unix"))
			lineSeparator = "\n";
		else if (eolStyle.toLowerCase().startsWith("win"))
			lineSeparator = "\r\n";
		else if (eolStyle.toLowerCase().startsWith("mac"))
			lineSeparator = "\r";
		else
			throw new BuildException("unknown eolStyle, known: unix|win|mac");
	}

	public String convertUnicodeCharacterRepresentation(String input) {
		final char[] chars = input.toCharArray();
		int nonAsciiCharCount = countNonAsciiCharacters(chars);
		if(! (input.contains("\\u") || input.contains("\\U")) && nonAsciiCharCount == 0)
			return input;
		int replacedNonAsciiCharacterCount = 0;
		final char[] result = nonAsciiCharCount == 0 ? chars : new char[chars.length + 5 * nonAsciiCharCount];
		for (int offset = 0; offset < chars.length;  offset++) {
			int resultOffset = offset + 5 * replacedNonAsciiCharacterCount;
			final char c = chars[offset];
			if (c == '\\' && (chars[offset+1] == 'u' || chars[offset+1] == 'U')) {
				putFormattedUnicodeRepresentation(chars, offset, result, resultOffset);
				offset+=5;
			} else if(c <= 127) {
				if(resultOffset >= result.length ) {
					throw new AssertionError(input + "//" + new String(result));
				}
				result[resultOffset] = c;
			} else {
				putEncodedNonAsciiCharacter(c, result, resultOffset);
				replacedNonAsciiCharacterCount++;
	        }

        }
		return new String(result);
    }

	private void putFormattedUnicodeRepresentation(final char[] input, int inputOffset, final char[] target,
			int targetOffset) {
		target[targetOffset+1] = 'u';
		for(int i = 2; i < 6; i++){
			target[targetOffset+i] = Character.toUpperCase(input[inputOffset+i]);
		}
	}

	private void putEncodedNonAsciiCharacter(final char c, final char[] result, int offset) {
		result[offset++] = '\\';
		result[offset++] = 'u';
		final String hex = String.format("%04X", (int)c);
		for(char digit : hex.toCharArray()){
			result[offset++] = digit;
		}
	}

	private int countNonAsciiCharacters(final char[] chars) {
		int nonAsciiCharCount = 0;
		for (int offset = 0; offset < chars.length;  offset++) {
	        if (chars[offset] > 127) {
	        	nonAsciiCharCount++;
	        }
        }
		return nonAsciiCharCount;
	}
}
