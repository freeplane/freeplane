package org.freeplane.view.swing.map.linkicons;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents information used to decorate links.
 * 
 * @author Stuart Robertson <stuartro@gmail.com>
 */
class LinkDecorationRule
{

	private String iconName;
	private boolean isPrefixRule;
	private boolean isSuffixRule;
	private Map<String, Pattern> patternCache = new HashMap<String, Pattern>();
	private List<String> regexes;

	LinkDecorationRule() {

	}

	String getIconName()
	{
		return iconName;
	}

	boolean isPrefixRule()
	{
		return isPrefixRule;
	}

	List<String> getRegexes()
	{
		return regexes;
	}

	boolean isSuffixRule()
	{
		return isSuffixRule;
	}

	/**
	 * Returns <code>true</code> if any of this rule's regexes match the given <code>link</code> or <code>false</code>
	 * otherwise.
	 */
	LinkMatchResult matches(String link)
	{
		for (String regex : regexes) {
			Pattern pattern = getPattern(regex);
			Matcher matcher = pattern.matcher(link);
			if (matcher.matches()) {
				int matchLength = matcher.group(1).length();
				LinkMatchResult result = new LinkMatchResult(true, matchLength, this);
				return result;
			}
		}
		return new LinkMatchResult(false, 0, null);
	}

	void setIconName(String iconName)
	{
		this.iconName = iconName;
	}

	void setPrefixRule(boolean isPrefixRule)
	{
		this.isPrefixRule = isPrefixRule;
	}

	void setRegexes(List<String> regexes)
	{
		this.regexes = regexes;
	}

	void setSuffixRule(boolean isSuffixRule)
	{
		this.isSuffixRule = isSuffixRule;
	}

	/**
	 * Returns the {@link Pattern} compiled from the given <code>regex</code>, lazily compiling regex the first time it
	 * is seen.
	 */
	private Pattern getPattern(String regex)
	{
		Pattern pattern = patternCache.get(regex);
		if (pattern == null) {
			pattern = Pattern.compile(regex);
			patternCache.put(regex, pattern);
		}
		return pattern;
	}

}
