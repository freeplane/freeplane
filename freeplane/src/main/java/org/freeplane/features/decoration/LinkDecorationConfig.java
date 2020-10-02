package org.freeplane.features.decoration;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.freeplane.main.application.ApplicationResourceController;
import org.freeplane.util.io.FileUtil;

/**
 * Loads and represents the contents of
 * <code>FREEPLANE_CONF/linkDecoration.ini</code>
 * 
 * @author Stuart Robertson <stuartro@gmail.com>
 */
public class LinkDecorationConfig {
	public static final String LINK_DECORATION_INI = "linkDecoration.ini";

	private File iniFile = ApplicationResourceController.getLinkDecorationPropertiesFile();

	private List<LinkDecorationRule> rules;

	private static String lineSplitterRegex = "([^|#]*)(\\|[^#]*)*([#].*)*";
	Pattern lineSplitterPattern;

	private long iniFileLastModified = -1;

	public LinkDecorationConfig() {

		lineSplitterPattern = Pattern.compile(lineSplitterRegex);
	}

//	public void setIniFile(File iniFile) {
//		this.iniFile = iniFile;
//	}

	/**
	 * Returns a list of 0 or more {@link LinkDecorationRule} instances representing
	 * the contents of the <code>FREEMIND_HOME/conf/linkDecoration.ini</code> file
	 * (or some other ini file as specified via {@link #setIniFile(File)}.
	 */
	public List<LinkDecorationRule> getRules() {
		if (rules == null || rulesFileHasChangedSinceLastLoad()) {
			rules = new ArrayList<LinkDecorationRule>();
			loadRules();
		}
		return rules;
	}

	private boolean rulesFileHasChangedSinceLastLoad() {
		if (iniFileLastModified == -1) {
			return true;
		}

		return iniFileLastModified == iniFile.lastModified();
	}

	/**
	 * Loads all lines of the file specified by {@link #iniFile} and attempts to
	 * parse each non-blank line into a {@link LinkDecorationRule}, adding all such
	 * rules to {@link #rules}.
	 */
	private void loadRules() {
		InputStream inputStream;
		try {
			inputStream = new FileInputStream(iniFile);
			List<String> lines = FileUtil.getTokenizedFileContents(inputStream, "\n");
			for (String line : lines) {
				line = line.trim();
				if (isBlank(line) || isComment(line)) {
					continue;
				}
				Matcher matcher = lineSplitterPattern.matcher(line);
				if (matcher.matches()) {
					LinkDecorationRule rule = new LinkDecorationRule();
					String regexesString = matcher.group(1);
					List<String> regexes = parseRegexes(regexesString, rule);
					rule.setRegexes(regexes);

					String iconName = matcher.group(2);
					if (iconName != null) {
						rule.setIconName(iconName.substring(1).trim());
					}

					String nodeColor = matcher.group(3);
					if (nodeColor != null) {
						rule.setNodeColor(nodeColor.substring(1).trim());
					}

					rules.add(rule);
				}
			}
			iniFileLastModified = iniFile.lastModified();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	private boolean isBlank(String line) {
		return line.length() == 0;
	}

	private boolean isComment(String line) {
		return line.startsWith("#");
	}

	private List<String> parseRegexes(String regexesString, LinkDecorationRule rule) {
		List<String> regexes = new ArrayList<String>();
		StringTokenizer tokenizer = new StringTokenizer(regexesString, ",");
		while (tokenizer.hasMoreTokens()) {
			String regex = tokenizer.nextToken().trim();
			regex = "(" + regex.substring(1, regex.length() - 1) + ")";

			if (regex.startsWith("(^")) {
				rule.setPrefixRule(true);
			} else {
				regex = "^.*" + regex;
			}

			if (regex.endsWith("$)")) {
				rule.setSuffixRule(true);
			} else {
				regex = regex + ".*$";
			}

			regexes.add(regex);
		}
		return regexes;
	}
}
