package org.freeplane.features.fpsearch;

import org.freeplane.core.ui.AFreeplaneAction;

public class MenuItem {

    public String path;
    public AFreeplaneAction action;

    MenuItem(final String path, AFreeplaneAction action)
    {
        this.path = path;
        this.action = action;
    }

    public String toString()
    {
        return path;
    }
}
