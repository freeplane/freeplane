package org.freeplane.features.commandsearch;

import javax.swing.Icon;

public class InformationItem extends SearchItem {

    private final String message;
    private final Icon icon;
    private final int rank;
    

    public InformationItem(String message, Icon icon, int rank) {
        super();
        this.message = message;
        this.icon = icon;
        this.rank = rank;
    }

    @Override
    int getItemTypeRank() {
        return rank;
    }

    @Override
    String getComparedText() {
        return message;
    }

    @Override
    Icon getTypeIcon() {
        return icon;
    }

    @Override
    String getDisplayedText() {
        return message;
    }

    @Override
    String getTooltip() {
        return null;
    }

    @Override
    boolean execute() {
        return false;
    }

    @Override
    protected boolean checkAndMatch(String searchTerm, ItemChecker textChecker) {
        return true;
    }

}
