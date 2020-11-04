package org.freeplane.features.commandsearch;

import javax.swing.Icon;

import org.freeplane.core.ui.AFreeplaneAction;

public class IconItem extends SearchItem
{
    private final Icon icon;
    private final AFreeplaneAction action;
	private final String displayedText;
	private final String comparedText;

    public IconItem(final Icon icon, final AFreeplaneAction action, final String iconName, final String accelerator, final String path)
    {
        this.icon = icon;
        this.action = action;
        this.displayedText = iconName + ", " + path + (accelerator != null ? " (" + accelerator + ")" : "");
        this.comparedText = path + SearchItem.ITEM_PATH_SEPARATOR + iconName;
    }

    @Override
    Icon getTypeIcon() {
        return icon;
    }

    @Override
    String getDisplayedText() {
		return displayedText;
    }

    @Override
    String getTooltip() {
        return null;
    }

    @Override
    boolean execute() {
        action.actionPerformed(null);
        return true;
    }

    @Override
    int getItemTypeRank() {
        return 3;
    }

    @Override
    String getComparedText() {
		return  comparedText;
    }

    @Override
    protected boolean checkAndMatch(String searchTerm, ItemChecker textChecker) {
        return textChecker.contains(displayedText, searchTerm);
    }

	@Override
	public String toString() {
		return "IconItem [" + displayedText + "]";
	}
}

