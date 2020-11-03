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

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.svgicons.FreeplaneIconFactory;

class MenuItem extends SearchItem{

    private static final ImageIcon menuIcon = FreeplaneIconFactory.toImageIcon(ResourceController.getResourceController().getIcon("/images/menu_items.svg"));

    private final AFreeplaneAction action;
    private final String content;
    private final String path;

	private final String tooltip;


    MenuItem(final AFreeplaneAction action, final String path, final String accelerator)
    {
        this.path = path;
        this.action = action;
		this.tooltip = (String) action.getValue(Action.SHORT_DESCRIPTION);
        this.content = accelerator != null ? path + " (" + accelerator + ")" : path;
    }

    @Override
    Icon getTypeIcon() {
        return menuIcon;
    }

    @Override
    String getDisplayedText() {
        return content;
    }

    @Override
    String getTooltip() {
        return tooltip;
    }

    @Override
    boolean execute() {
        action.actionPerformed(null);
        return true;
    }

    @Override
    int getItemTypeRank() {
        return 2;
    }

    @Override
    String getComparedText() {
        return path;
    }

    @Override
    protected boolean checkAndMatch(String searchTerm) {
        return action.isEnabled() 
                && contains(content, searchTerm);
    }

    public String toString()
    {
    	return content;
    }

}
