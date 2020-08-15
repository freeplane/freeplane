package org.freeplane.features.commandsearch;

import javax.swing.Icon;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;

public class IconItem extends SearchItem
{
    final Icon icon;
    final AFreeplaneAction action;
    final String path;

    public IconItem(final Icon icon, final AFreeplaneAction action, final String path)
    {
        this.icon = icon;
        this.action = action;
        this.path = path;
    }

    @Override
    Icon getTypeIcon() {
        //return ResourceController.getResourceController().getIcon(action.getIconKey());
        return icon;
    }

    @Override
    String getDisplayText() {
        return path;
    }

    @Override
    String getDisplayTooltip() {
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
        return path;
    }

    @Override
    public String toString()
    {
        return String.format("IconItem[%s:%s:%s:%s]", icon, action, path);
    }
}

