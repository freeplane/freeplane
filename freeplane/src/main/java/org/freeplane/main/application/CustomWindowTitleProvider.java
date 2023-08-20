package org.freeplane.main.application;

import net.infonode.docking.DockingWindow;
import net.infonode.docking.title.DockingWindowTitleProvider;
import net.infonode.docking.title.SimpleDockingWindowTitleProvider;

public class CustomWindowTitleProvider implements DockingWindowTitleProvider {

    CustomWindowTitleProvider() {
    }

    @Override
    public String getTitle(DockingWindow window){
        String windowName = window.getName();
        if (windowName == null) {
            return SimpleDockingWindowTitleProvider.INSTANCE.getTitle(window);
        } else {
            boolean dirty = false;
            for (int i = 0; i < window.getChildWindowCount(); i++) {
                dirty = dirty || window.getChildWindow(i).getTitle().endsWith("*");
            }
            return windowName + (dirty?" *":"");
        }
    }
}
