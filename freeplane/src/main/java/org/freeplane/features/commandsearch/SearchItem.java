package org.freeplane.features.commandsearch;

abstract class SearchItem implements Comparable<SearchItem>{
    
    abstract int getItemTypeRank();
    abstract String getComparedText();

    @Override
    public int compareTo(SearchItem o) {
        int rankCompare = Integer.compare(getItemTypeRank(), o.getItemTypeRank());
        return rankCompare != 0 ? rankCompare :  getComparedText().compareToIgnoreCase(o.getComparedText());
    }

}
