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

import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.KeyStroke;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.menubuilders.FreeplaneResourceAccessor;
import org.freeplane.core.ui.menubuilders.action.IAcceleratorMap;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

class MenuStructureIndexer {
    private EntryAccessor entryAccessor;
    IAcceleratorMap acceleratorMap;
    private List<MenuItem> menuItems;
    private boolean debug;

    MenuStructureIndexer(boolean debug)
    {
        this.debug = debug;
        load();
    }

    List<MenuItem> getMenuItems()
    {
        return menuItems;
    }

    private void load()
    {
        // this has some methods for getting stuff about menu items...
        entryAccessor = new EntryAccessor(new FreeplaneResourceAccessor());
        acceleratorMap = ResourceController.getResourceController().getAcceleratorManager();
        menuItems = new LinkedList<>();
        ModeController modeController = Controller.getCurrentModeController();
        final Entry root = modeController.getUserInputListenerFactory()
                .getGenericMenuStructure().getRoot();
        loadMenuItems("Menu", root.getChild("main_menu").children(), true, false, 0);
        //loadMenuItems("Toolbar", root.getChild("main_toolbar").children(), true, false, 0);
        //loadMenuItems("Map Popup", root.getChild("map_popup").children(), true, false, 0);
        //loadMenuItems("Node Popup", root.getChild("node_popup").children(), true, false, 0);
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

    // This is for indenting debug messages
    private void indent(int depth)
    {
        for (int i = 0; i < depth; i++)
        {
            System.out.print(' ');
        }
    }

    private void loadMenuItems(final String prefix, final List<Entry> menuEntries,
                               boolean toplevel, boolean toplevelPrefix, int depth)
    {
        if (debug)
        {
            indent(depth);
            System.out.format("loadMenuItems(%s, %s, %b)\n", prefix, menuEntries.get(0).getParent().getPath(), toplevel);
        }
        for (Entry menuEntry: menuEntries)
        {
            processMenuEntry(menuEntry, prefix, toplevelPrefix, toplevel, depth);
        }
    }

    private void processMenuEntry(Entry menuEntry, String prefix, boolean toplevelPrefix, boolean toplevel, int depth) {
        if (debug)
        {
            indent(depth);
            System.out.format("entry: %s\n", menuEntry.toString());
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
        final String path = computePath(menuEntry, prefix, toplevel, toplevelPrefix, childrenAreToplevel, depth);
        if (path == null)
        {
            return;
        }

        if (menuEntry.isLeaf())
        {
            recordLeafMenuEntry(menuEntry, path, depth);
        }
        else
        {
            loadMenuItems(path, menuEntry.children(), childrenAreToplevel[0], toplevelPrefix, depth + 1);
        }
    }

    private String computePath(Entry menuEntry, String prefix, boolean toplevel, boolean toplevelPrefix, boolean[] childrenAreToplevel, int depth)
    {
        final String path;
        if (menuEntry.getName().equals(""))
        {
            // a meta data entry like <Entry usedBy = "EDITOR" > does not contribute to the path!
            path = prefix;
            childrenAreToplevel[0] = toplevel;

            if (!menuEntry.builders().isEmpty())
            {
                if (debug)
                {
                    indent(depth);
                    System.out.format("menuEntry without name: %s, numChildren=%d\n",
                            menuEntry.getPath(), menuEntry.children().size());
                }
            }
        }
        else
        {
            String component = translateMenuItemComponent(menuEntry);
            if (component == null)
            {
                // item [component] could not be translated, omit it (like LastOpenedMaps)
                return null;
            }
            if (toplevel)
            {
                if (toplevelPrefix) {
                    path = prefix + ": " + component;
                }
                else
                {
                    path = component;
                }
            }
            else
            {
                path = prefix + "->" + component;
            }
            childrenAreToplevel[0] = false;
        }
        return path;
    }

    private void recordLeafMenuEntry(Entry menuEntry, String path, int depth) {
        KeyStroke accelerator = menuEntry.getAction() != null ? acceleratorMap.getAccelerator(menuEntry.getAction()) : null;
        String acceleratorText =  null;
        if (accelerator !=  null)
        {
            acceleratorText = "";
            int modifiers = accelerator.getModifiers();
            if (modifiers > 0)
            {
                acceleratorText += KeyEvent.getKeyModifiersText(modifiers);
                acceleratorText += "+";
            }
            acceleratorText += KeyEvent.getKeyText(accelerator.getKeyCode());
        }

        if (debug)
        {
            indent(depth);
            System.out.format("getLocationDescription=%s\n", entryAccessor.getLocationDescription(menuEntry));
            System.out.format("menuEntry: %s/%s, %s\n", path, menuEntry.getPath(), menuEntry.getAction());
            System.out.format("menuEntry: %s (accel = %s)\n", path, acceleratorText);
        }
        menuItems.add(new MenuItem(path, menuEntry.getAction(), acceleratorText));
    }

}
