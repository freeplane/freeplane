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

import java.util.Locale;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.components.ShowPreferencesAction;
import org.freeplane.core.ui.svgicons.FreeplaneIconFactory;

class PreferencesItem extends SearchItem {

    private static final ImageIcon prefsIcon = FreeplaneIconFactory.toImageIcon(ResourceController.getResourceController().getIcon(ShowPreferencesAction.KEY + ".icon"));

    final String tab;
    final String separator;
    final String key;
    final String text;
    final String path;
    final String tooltip;

    PreferencesItem(final String tab, final String separator, final String key, final String text, final String path, final String tooltip)
    {
        this.tab = tab;
        this.separator = separator;
        this.key = key;
        this.text = text;
        this.path = path;
        this.tooltip = tooltip;
    }

    @Override
    int getItemTypeRank() {
        return 1;
    }

    @Override
    String getComparedText() {
        return path;
    }

    @Override
    Icon getTypeIcon() {
        return prefsIcon;
    }

    @Override
    String getDisplayText() {
        return path;
    }

    @Override
    String getDisplayTooltip() {
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
        return String.format("PreferencesItem[%s:%s:%s:%s]", tab, separator, key, text);
    }

    @Override
    protected boolean checkAndMatch(String searchTerm) {
        return key.toLowerCase(Locale.ENGLISH).contains(searchTerm) 
                || path.toLowerCase(Locale.ENGLISH).contains(searchTerm);
    }
}
