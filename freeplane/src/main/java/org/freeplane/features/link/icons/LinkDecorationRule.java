package org.freeplane.features.link.icons;

class LinkDecorationRule
{

	private final String iconName;
	private final DecorationRuleMatcher matcher;
	

	public LinkDecorationRule(DecorationRuleMatcher matcher, String iconName) {
        super();
        this.matcher = matcher;
        this.iconName = iconName;
    }

    LinkMatchResult matches(String link)
	{
		return new LinkMatchResult(matcher.getMatchLength(link), iconName);
	}
    
    int getMaximalScore() {
        return matcher.getMaximalMatchLength();
    }
}
