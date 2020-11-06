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

import java.util.LinkedList;
import java.util.List;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.menubuilders.FreeplaneResourceAccessor;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

class MenuStructureIndexer {
    private EntryAccessor entryAccessor;

    private List<MenuItem> menuItems;

    MenuStructureIndexer()
    {
        loadMenuItems();
    }

    List<MenuItem> getMenuItems()
    {
        return menuItems;
    }

    private void loadMenuItems()
    {
        entryAccessor = new EntryAccessor(new FreeplaneResourceAccessor());
        menuItems = new LinkedList<>();
        ModeController modeController = Controller.getCurrentModeController();
        final Entry root = modeController.getUserInputListenerFactory()
                .getGenericMenuStructure().getRoot();
        loadMenuItems("Menu", root.getChild("main_menu").children(), true, 0);
    }

    private String translateMenuItemComponent(final Entry entry) {
        String menuItemLabel = entryAccessor.getText(entry);
        if (menuItemLabel != null)
        {
            menuItemLabel = TextUtils.removeMnemonic(menuItemLabel);
            menuItemLabel = HtmlUtils.htmlToPlain(menuItemLabel);
        }
        return menuItemLabel;
    }

    private void loadMenuItems(final String prefix, final List<Entry> menuEntries,
                               boolean toplevel, int depth)
    {
         for (Entry menuEntry: menuEntries)
        {
            processMenuEntry(menuEntry, prefix, toplevel, depth);
        }
    }

    private void processMenuEntry(Entry menuEntry, String prefix, boolean toplevel, int depth) {
        if (menuEntry.getName().equals("icons"))
        {
            return;
        }
        if (menuEntry.builders().contains("separator"))
        {
            return;
        }
        Object usedBy = menuEntry.getAttribute("usedBy");
        if (usedBy != null && !usedBy.equals("EDITOR"))
        {
            return;
        }

        boolean[] childrenAreToplevel = new boolean[1];
        final String path = computePath(menuEntry, prefix, toplevel, childrenAreToplevel);
        if (path == null)
        {
            return;
        }

        if (menuEntry.isLeaf())
        {
            recordLeafMenuEntry(menuEntry, path);
        }
        else
        {
            loadMenuItems(path, menuEntry.children(), childrenAreToplevel[0], depth + 1);
        }
    }

    private String computePath(Entry menuEntry, String prefix, boolean toplevel, boolean[] childrenAreToplevel)
    {
        final String path;
        if (contributesToPath(menuEntry)) {
            String component = translateMenuItemComponent(menuEntry);
            if (component == null)
            {
                // item [component] could not be translated, omit it (like LastOpenedMaps)
                return null;
            }
            if (toplevel)
            {
                     path = component;
            }
            else
            {
                path = prefix + SearchItem.ITEM_PATH_SEPARATOR + component;
            }
            childrenAreToplevel[0] = false;
        } else {
            path = prefix;
            childrenAreToplevel[0] = toplevel;

        }
        return path;
    }

    public boolean contributesToPath(Entry menuEntry) {
    	return !menuEntry.getName().isEmpty();
    }

    private void recordLeafMenuEntry(Entry menuEntry, String path) {
    	AFreeplaneAction action = menuEntry.getAction();
    	if (action != null) {
    		menuItems.add(new MenuItem(action, path));
    	}
    }
}
