package org.freeplane.features.fpsearch;

import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.KeyStroke;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.menubuilders.FreeplaneResourceAccessor;
import org.freeplane.core.ui.menubuilders.action.IAcceleratorMap;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

class MenuStructureIndexer {
    private EntryAccessor entryAccessor;
    IAcceleratorMap acceleratorMap;
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
        // this has some methods for getting stuff about menu items...
        entryAccessor = new EntryAccessor(new FreeplaneResourceAccessor());
        acceleratorMap = ResourceController.getResourceController().getAcceleratorManager();

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
        String menuItemLabel = entryAccessor.getText(entry);
        if (menuItemLabel != null)
        {
            menuItemLabel = TextUtils.removeMnemonic(menuItemLabel);
        }
        return menuItemLabel;
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

                //System.out.format("menuEntry: %s\n", menuEntry.getPath());
                //System.out.format("menuEntry: %s (accel = %s)\n", path, acceleratorText);
                menuItems.add(new MenuItem(path, menuEntry.getAction(), acceleratorText));
            }
            else
            {
                loadMenuItems(path, menuEntry.children(), childrenAreToplevel, toplevelPrefix);
            }
        }
    }

}
