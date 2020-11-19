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

class MenuItem extends SearchItem {

    private static final ImageIcon menuIcon = FreeplaneIconFactory.toImageIcon(ResourceController
            .getResourceController().getIcon("/images/menu_items.svg"));

    private final AFreeplaneAction action;

    private final String path;

    private final String tooltip;

    MenuItem(final AFreeplaneAction action, final String path) {
        this.path = path;
        this.action = action;
        this.tooltip = (String) action.getValue(Action.SHORT_DESCRIPTION);
    }

    @Override
    Icon getTypeIcon() {
        return menuIcon;
    }

    @Override
    String getDisplayedText() {
        String accelerator = AcceleratorDescriptionCreator.INSTANCE.createAcceleratorDescription(action);
        return accelerator != null ? this.path + " (" + accelerator + ")" : this.path;
    }

    @Override
    String getTooltip() {
        return tooltip;
    }

    @Override
    void execute() {
        if(action.isEnabled())
            action.actionPerformed(null);
    }
    
    @Override
    void assignNewAccelerator() {
        assignNewAccelerator(action);
    }

    @Override
    boolean shouldUpdateResultList() {
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
    protected boolean checkAndMatch(String searchTerm, ItemChecker textChecker) {
        return textChecker.contains(getDisplayedText(), searchTerm);
    }

    @Override
    public String toString() {
        return "MenuItem [" + getDisplayedText() + "]";
    }
}
