package org.freeplane.features.commandsearch;

import javax.swing.Icon;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;

abstract class SearchItem implements Comparable<SearchItem> {
    static final String ITEM_PATH_SEPARATOR = "->";

    abstract int getItemTypeRank();

    abstract String getComparedText();

    abstract Icon getTypeIcon();

    abstract String getDisplayedText();

    abstract String getTooltip();

    abstract void execute();
    abstract void assignNewAccelerator();

    abstract boolean shouldUpdateResultList();

    protected void assignNewAccelerator(AFreeplaneAction action) {
        ResourceController.getResourceController().getAcceleratorManager().newAccelerator(action, null);
    }

    @Override
    public int compareTo(SearchItem o) {
        int rankCompare = Integer.compare(getItemTypeRank(), o.getItemTypeRank());
        return rankCompare != 0 ? rankCompare
                : getComparedText().compareToIgnoreCase(o.getComparedText());
    }

    boolean checkAndMatch(final String[] searchTerms, ItemChecker textChecker) {
        for (int i = 0; i < searchTerms.length; i++) {
            if (!checkAndMatch(searchTerms[i], textChecker)) {
                return false;
            }
        }
        return true;
    }

    abstract protected boolean checkAndMatch(final String searchTerm, ItemChecker textChecker);
}
