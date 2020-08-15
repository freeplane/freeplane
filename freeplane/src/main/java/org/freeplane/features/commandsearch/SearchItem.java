package org.freeplane.features.commandsearch;

import javax.swing.Icon;

abstract class SearchItem implements Comparable<SearchItem>{
    
    abstract int getItemTypeRank();
    abstract String getComparedText();
    abstract Icon getTypeIcon();
    abstract String getDisplayText();
    abstract String getDisplayTooltip();

    /**
     *
     * @return whether to refresh list afterwards
     */
    abstract boolean execute();

    @Override
    public int compareTo(SearchItem o) {
        int rankCompare = Integer.compare(getItemTypeRank(), o.getItemTypeRank());
        return rankCompare != 0 ? rankCompare :  getComparedText().compareToIgnoreCase(o.getComparedText());
    }

}
