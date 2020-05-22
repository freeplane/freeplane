package org.freeplane.features.fpsearch;

import java.util.LinkedList;
import java.util.List;

import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

class MenuStructureIndexer {
    private List<MenuItem> menuItems;

    MenuStructureIndexer()
    {
        load();
    }

    List<MenuItem> getMenuItems()
    {
        return menuItems;
    }

    private void load()
    {
        menuItems = new LinkedList<>();
        ModeController modeController = Controller.getCurrentModeController();
        final Entry root = modeController.getUserInputListenerFactory()
                .getGenericMenuStructure().getRoot();
        loadMenuItems("Menu", root.getChild("main_menu").children(), true, false);
        //loadMenuItems("Toolbar", root.getChild("main_toolbar").children(), true, false);
        //loadMenuItems("Map Popup", root.getChild("map_popup").children(), true, false);
        //loadMenuItems("Node Popup", root.getChild("node_popup").children(), true, false);
    }

    private String translateMenuItemComponent(final Entry entry) {
        String key;
        if (entry.getAction() != null)
        {
            key = entry.getAction().getTextKey();
        }
        else
        {
            key = entry.getName();
        }
        final String tryText = TextUtils.getRawText(key, null);
        if (tryText != null) {
            return TextUtils.removeMnemonic(tryText);
        } else {
            return "[" + entry.getName() + "]";
        }
    }

    private void loadMenuItems(final String prefix, final List<Entry> menuEntries, boolean toplevel, boolean toplevelPrefix)
    {
        //System.out.format("loadMenuItems(%s, %s, %b)\n", prefix, menuEntries.get(0).getParent().getPath(), toplevel);
        for (Entry menuEntry: menuEntries)
        {
            if (menuEntry.builders().contains("separator"))
            {
                continue;
            }
            Object usedBy = menuEntry.getAttribute("usedBy");
            if (usedBy != null && !usedBy.equals("EDITOR"))
            {
                continue;
            }

            boolean childrenAreToplevel;
            final String path;
            if (menuEntry.getName().equals(""))
            {
                // a meta data entry like <Entry usedBy = "EDITOR" > does not contribute to the path!
                path = prefix;
                childrenAreToplevel = toplevel;
            }
            else
            {
                String component = translateMenuItemComponent(menuEntry);
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
                childrenAreToplevel = false;
            }

            if (menuEntry.isLeaf())
            {
                //System.out.format("menuEntry: %s\n", menuEntry.getPath());
                //System.out.format("menuEntry: %s\n", path);
                menuItems.add(new MenuItem(path, menuEntry.getAction()));
            }
            else
            {
                loadMenuItems(path, menuEntry.children(), childrenAreToplevel, toplevelPrefix);
            }
        }
    }

}
