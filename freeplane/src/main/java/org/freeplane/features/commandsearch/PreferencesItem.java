/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2020 Felix Natter
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General License for more details.
 *
 *  You should have received a copy of the GNU General License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.features.commandsearch;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.components.ShowPreferencesAction;
import org.freeplane.core.ui.svgicons.FreeplaneIconFactory;

class PreferencesItem extends SearchItem {

    private static final ImageIcon prefsIcon = FreeplaneIconFactory.toImageIcon(ResourceController.getResourceController().getIcon(ShowPreferencesAction.KEY + ".icon"));

    private final String tab;
    private final String separator;
    private final String key;
    private final String displayedText;
    private final String path;
    private final String tooltip;

    PreferencesItem(final String tab, final String separator, final String key, final String displayText, final String path, final String tooltip)
    {
        this.tab = tab;
        this.separator = separator;
        this.key = key;
        this.displayedText = displayText;
        this.path = path;
        this.tooltip = tooltip;
    }

    @Override
    int getItemTypeRank() {
        return 1;
    }

    @Override
    String getComparedText() {
        return displayedText;
    }

    @Override
    Icon getTypeIcon() {
        return prefsIcon;
    }

    @Override
    String getDisplayedText() {
        return displayedText;
    }

    @Override
    String getTooltip() {
        return tooltip;
    }

    @Override
    boolean execute() {
        new ShowPreferenceItemAction(this).actionPerformed(null);
        return false;
    }

    @Override
    public String toString()
    {
        return String.format("PreferencesItem[%s:%s:%s:%s]", getTab(), separator, getKey(), displayedText);
    }

    @Override
    protected boolean checkAndMatch(String searchTerm, ItemChecker textChecker) {
        return textChecker.contains(getKey(), searchTerm)
                || textChecker.contains(path, searchTerm);
    }

	String getTab() {
		return tab;
	}

	String getKey() {
		return key;
	}
    
    
}
