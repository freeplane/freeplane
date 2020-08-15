package org.freeplane.features.commandsearch;

import javax.swing.Icon;

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

