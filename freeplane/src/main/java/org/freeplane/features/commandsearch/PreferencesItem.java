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

import java.awt.event.InputEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.components.ShowPreferencesAction;
import org.freeplane.core.ui.svgicons.FreeplaneIconFactory;
import org.freeplane.core.util.TextUtils;

public class PreferencesItem extends SearchItem {

    private static final ImageIcon OPTION_ICON = FreeplaneIconFactory.toImageIcon(ResourceController.getResourceController().getIcon("Option.icon"));
    private static final ImageIcon SELECTED_OPTION_ICON = FreeplaneIconFactory.toImageIcon(ResourceController.getResourceController().getIcon("SelectedOption.icon"));
    private static final String PREFERENCES_PATH =  TextUtils.getText(ShowPreferencesAction.KEY + ".text") + ITEM_PATH_SEPARATOR;

    private final String tab;
    private final String propertyName;
    private final String displayedText;
    private final String searchedText;
    private final String tooltip;

    PreferencesItem(final String tab, final String propertyName, final String path, final String tooltip)
    {
        this.tab = tab;
        this.propertyName = propertyName;
        this.displayedText =  tab + ITEM_PATH_SEPARATOR + path;
        this.searchedText = normalizeText(path);
        this.tooltip = tooltip;
    }

    @Override
    int getItemTypeRank() {
        return 1;
    }

    @Override
    public String getComparedText() {
        return displayedText;
    }

    @Override
    public Icon getTypeIcon() {
        return ResourceController.getResourceController().getBooleanProperty(propertyName, false) ? SELECTED_OPTION_ICON : OPTION_ICON;
    }

    @Override
    public String getDisplayedText() {
        return displayedText;
    }

    @Override
    public String getTooltip() {
        return tooltip;
    }

    @Override
    void execute(InputEvent event) {
        new ShowPreferenceItemAction(this).actionPerformed(null);
    }

    @Override
    void assignNewAccelerator() {
    }

	@Override
	boolean shouldUpdateResultList() {
		return false;
	}

    @Override
	public String toString() {
		return "PreferencesItem [displayedText=" + displayedText + "]";
	}

	@Override
    protected boolean checkAndMatch(String searchTerm, ItemChecker textChecker) {
        return textChecker.contains(searchedText, searchTerm);
    }

	String getTab() {
		return tab;
	}

	String getPropertyName() {
		return propertyName;
	}

    @Override
    public String getCopiedText() {
        return PREFERENCES_PATH + getDisplayedText();
    }


}
