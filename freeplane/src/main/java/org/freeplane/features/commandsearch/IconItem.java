package org.freeplane.features.commandsearch;

import javax.swing.Icon;

import org.freeplane.core.ui.AFreeplaneAction;

public class IconItem extends SearchItem {
    private final Icon icon;

    private final AFreeplaneAction action;

    private final String comparedText;

    private final String iconDescription;

    private final String searchedText;

    public IconItem(final Icon icon, final AFreeplaneAction action, final String iconName,
            final String path) {
        this.icon = icon;
        this.action = action;
        this.iconDescription = iconName + ", " + path;
        this.searchedText = normalizeText(iconDescription);
        this.comparedText = path + SearchItem.ITEM_PATH_SEPARATOR + iconName;
    }

    @Override
    Icon getTypeIcon() {
        return icon;
    }

    @Override
    String getDisplayedText() {
        String accelerator = AcceleratorDescriptionCreator.INSTANCE.createAcceleratorDescription(action);
        return iconDescription + (accelerator != null ? " (" + accelerator + ")" : "");
    }

    private String getSearchedText() {
        String accelerator = AcceleratorDescriptionCreator.INSTANCE.createAcceleratorDescription(action);
        return searchedText + (accelerator != null ? " (" + normalizeText(accelerator) + ")" : "");
    }

    @Override
    String getTooltip() {
        return null;
    }

    @Override
    void execute() {
        action.actionPerformed(null);
    }
    
    @Override
    void assignNewAccelerator() {
        assignNewAccelerator(action);
    }

    @Override
    boolean shouldUpdateResultList() {
        return true;
    }

    @Override
    int getItemTypeRank() {
        return 3;
    }

    @Override
    String getComparedText() {
        return comparedText;
    }

    @Override
    protected boolean checkAndMatch(String searchTerm, ItemChecker textChecker) {
        return textChecker.contains(getSearchedText(), searchTerm);
    }

    @Override
    public String toString() {
        return "IconItem [" + getDisplayedText() + "]";
    }
}
