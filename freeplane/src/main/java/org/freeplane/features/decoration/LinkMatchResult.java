package org.freeplane.features.decoration;


/**
 * Value class used to represent a link-match result.
 * 
 * @author Stuart Robertson <stuartro@gmail.com>
 */
public class LinkMatchResult
{
	public boolean matches;

	public int matchLength;

	public LinkDecorationRule rule;

	public LinkMatchResult(boolean matches, int matchLength, LinkDecorationRule matchingRule)
	{
		super();
		this.matches = matches;
		this.matchLength = matchLength;
		this.rule = matchingRule;
	}

	public boolean ruleHasIcon() {
		return rule.getIconName() != null;
	}
}
