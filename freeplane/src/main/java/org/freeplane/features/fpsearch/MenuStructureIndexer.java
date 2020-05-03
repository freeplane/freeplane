package org.freeplane.features.fpsearch;

import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

import java.util.LinkedList;
import java.util.List;

class MenuStructureIndexer {
    private List<String> menuItems;

    MenuStructureIndexer()
    {
        load();
    }

    List<String> getMenuItems()
    {
        return menuItems;
    }

    private void load()
    {
        menuItems = new LinkedList<>();
        ModeController modeController = Controller.getCurrentModeController();
        loadMenuItems("Menu", modeController.getUserInputListenerFactory()
                .getGenericMenuStructure().getRoot().getChild("main_menu").children(), true);
        loadMenuItems("Toolbar", modeController.getUserInputListenerFactory()
                .getGenericMenuStructure().getRoot().getChild("main_toolbar").children(), true);
        loadMenuItems("Popup", modeController.getUserInputListenerFactory()
                .getGenericMenuStructure().getRoot().getChild("node_popup").children(), true);
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
        final String tryText = TextUtils.getRawText(key);
        if (tryText != null) {
            return TextUtils.removeMnemonic(tryText);
        } else {
            return "[" + entry.getName() + "]";
        }
    }

    private void loadMenuItems(final String prefix, final List<Entry> menuEntries, boolean toplevel)
    {
        System.out.format("loadMenuItems(%s, %s, %b)\n", prefix, menuEntries.get(0).getParent().getPath(), toplevel);
        for (Entry menuEntry: menuEntries)
        {
            if (menuEntry.builders().contains("separator"))
            {
                continue;
            }

            String component = translateMenuItemComponent(menuEntry);
            final String path;
            if (toplevel)
            {
                path = prefix + ": "  + component;
            }
            else
            {
                path = prefix + "->" + component;
            }
            if (menuEntry.isLeaf())
            {
                System.out.format("menuEntry: %s\n", menuEntry.getPath());
                System.out.format("menuEntry: %s\n", path);
            }
            else
            {
                loadMenuItems(path, menuEntry.children(), false);
            }
        }
    }

}
