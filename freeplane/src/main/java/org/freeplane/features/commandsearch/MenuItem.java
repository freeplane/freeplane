/*
 *  Freeplane - mind map editor
 *
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

import org.freeplane.core.ui.AFreeplaneAction;

class MenuItem extends SearchItem{

    final String path;
    final AFreeplaneAction action;
    final String accelerator;

    MenuItem(final String path, final AFreeplaneAction action, final String accelerator)
    {
        this.path = path;
        this.action = action;
        this.accelerator = accelerator;
    }

    @Override
    int getItemTypeRank() {
        return 2;
    }

    @Override
    String getComparedText() {
        return path;
    }

    public String toString()
    {
        if (accelerator != null)
            return path + " (" + accelerator + ")";
        else
            return path;
    }

}
