/*
 *  Freeplane - mind map editor
 *
 *  Copyright (C) 2020 Felix Natter
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.features.commandsearch;

import static org.freeplane.features.commandsearch.SearchItem.ITEM_PATH_SEPARATOR;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.icon.IconController;
import org.freeplane.features.icon.IconGroup;
import org.freeplane.features.icon.MindIcon;
import org.freeplane.features.icon.factory.IconStoreFactory;
import org.freeplane.features.icon.mindmapmode.MIconController;

class IconIndexer {
    private List<IconItem> iconItems;

    IconIndexer()
    {
        iconItems = new LinkedList<>();
        load();
    }

    List<IconItem> getIconItems()
    {
        return iconItems;
    }

    private void load()
    {
        MIconController iconController = (MIconController) IconController.getController();
        Map<String, AFreeplaneAction> iconActions = iconController.getAllIconActions();
        for (final IconGroup iconGroup : IconStoreFactory.ICON_STORE.getGroups()) {
            addIconGroup("", iconGroup, iconActions);
        }
    }

    private void addIconGroup(String prefix, final IconGroup group, Map<String, AFreeplaneAction> iconActions) {
        if (group.getIcons().size() < 1)
            return;
        String groupPath = prefix + group.getDescription();
        String subgroupPrefix = groupPath + ITEM_PATH_SEPARATOR;
        for (final IconGroup childGroup : group.getGroups()) {
            if(childGroup.isLeaf()) {
                MindIcon mindIcon = childGroup.getGroupIcon();
                AFreeplaneAction action = iconActions.get(mindIcon.getName());
                String acceleratorText = AcceleratorDescriptionCreator.INSTANCE.createAcceleratorDescription(action);
                IconItem iconItem = new IconItem(mindIcon.getIcon(), action,  action.getRawText(), acceleratorText, groupPath);
                iconItems.add(iconItem);
            } else {
                addIconGroup(subgroupPrefix, childGroup, iconActions);
            }
        }
    }

 }
