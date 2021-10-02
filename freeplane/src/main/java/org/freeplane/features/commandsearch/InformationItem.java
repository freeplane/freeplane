package org.freeplane.features.commandsearch;

import javax.swing.Icon;
import java.awt.event.InputEvent;

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
    public String getComparedText() {
        return message;
    }

    @Override
    public Icon getTypeIcon() {
        return icon;
    }

    @Override
    public String getDisplayedText() {
        return message;
    }

    @Override
    public String getTooltip() {
        return null;
    }

    @Override
    void execute(InputEvent event) {
    }
    
    @Override
    void assignNewAccelerator() {
    }
    
	@Override
	boolean shouldUpdateResultList() {
		return false;
	}

	@Override
    protected boolean checkAndMatch(String searchTerm, ItemChecker textChecker) {
        return true;
    }

    @Override
    public String getCopiedText() {
        return getDisplayedText();
    }
}
