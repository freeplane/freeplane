package org.freeplane.features.commandsearch;

import javax.swing.Icon;

abstract class SearchItem implements Comparable<SearchItem>{
    
    static final String ITEM_PATH_SEPARATOR = "->";
    static final String TOP_LEVEL_SEPARATOR = ": ";

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
    
    protected boolean checkAndMatch(final String[] searchTerms)
    {
        for (int i = 0; i < searchTerms.length; i++)
        {
            if (!checkAndMatch(searchTerms[i]))
            {
                return false;
            }
        }
        return true;
    }
    
    abstract protected boolean checkAndMatch(final String searchTerm);



}
