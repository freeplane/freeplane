package org.freeplane.features.fpsearch;

import org.freeplane.core.ui.AFreeplaneAction;

public class MenuItem {

    public String path;
    public AFreeplaneAction action;
    public String accelerator;

    MenuItem(final String path, final AFreeplaneAction action, final String accelerator)
    {
        this.path = path;
        this.action = action;
        this.accelerator = accelerator;
    }

    public String toString()
    {
        if (accelerator != null)
            return path + " (" + accelerator + ")";
        else
            return path;
    }
}
