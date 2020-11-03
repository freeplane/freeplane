package org.freeplane.features.commandsearch;

import javax.swing.Icon;

import org.freeplane.core.ui.AFreeplaneAction;

public class IconItem extends SearchItem
{
    final private Icon icon;
    final private AFreeplaneAction action;
    final private String path;
    final private String iconName;

    public IconItem(final Icon icon, final AFreeplaneAction action, final String iconName, final String path)
    {
        this.icon = icon;
        this.action = action;
        this.path = path;
        this.iconName = iconName;
    }

    @Override
    Icon getTypeIcon() {
        //return ResourceController.getResourceController().getIcon(action.getIconKey());
        return icon;
    }

    @Override
    String getDisplayedText() {
        return iconName + ", " + path;
    }

    @Override
    String getTooltip() {
        return null;
    }

    @Override
    boolean execute() {
        action.actionPerformed(null);
        return true;
    }

    @Override
    int getItemTypeRank() {
        return 3;
    }

    @Override
    String getComparedText() {
        return  path + SearchItem.ITEM_PATH_SEPARATOR + iconName;
    }

    @Override
    protected boolean checkAndMatch(String searchTerm, ItemChecker textChecker) {
        return textChecker.contains(iconName, searchTerm) || textChecker.contains(path, searchTerm);
    }

    @Override
    public String toString()
    {
        return String.format("IconItem[%s:%s:%s:%s]", icon, action, path);
    }
}

