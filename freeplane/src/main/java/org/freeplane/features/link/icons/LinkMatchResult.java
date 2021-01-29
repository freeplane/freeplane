package org.freeplane.features.link.icons;

class LinkMatchResult
{
	private final int score;

    private final String iconName;

	LinkMatchResult(int score, String iconName) {
	    this.score = score;
        this.iconName = iconName;
    }

    public boolean matches() {
        return score > 0;
    }
    
    public int getScore() {
        return score;
    }

    public String getIconName() {
        return iconName;
    }
}
