package org.freeplane.features.commandsearch;

import javax.swing.Icon;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.filter.condition.StringTransformer;
import java.awt.event.InputEvent;

abstract public class SearchItem implements Comparable<SearchItem> {
    static final String ITEM_PATH_SEPARATOR = "->";
    
    static String normalizeText(String text) {
        return StringTransformer.transform(text, true, true);
    }

    abstract int getItemTypeRank();

    public abstract String getComparedText();

    public abstract Icon getTypeIcon();

    public abstract String getDisplayedText();

    public abstract String getTooltip();

    abstract void execute(InputEvent event);
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

    public abstract String getCopiedText();
}
