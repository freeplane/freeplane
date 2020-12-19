package org.freeplane.view.swing.map.linkicons;


/**
 * Value class used to represent a link-match result.
 * 
 * @author Stuart Robertson <stuartro@gmail.com>
 */
class LinkMatchResult
{
	boolean matches;

	int matchLength;

	LinkDecorationRule rule;

	LinkMatchResult(boolean matches, int matchLength, LinkDecorationRule matchingRule)
	{
		super();
		this.matches = matches;
		this.matchLength = matchLength;
		this.rule = matchingRule;
	}

	boolean ruleHasIcon() {
		return rule.getIconName() != null;
	}
}
