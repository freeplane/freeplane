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

class PreferencesItem {

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
    public String toString()
    {
        return String.format("PreferencesItem[%s:%s:%s:%s]", tab, separator, key, text);
    }
}
