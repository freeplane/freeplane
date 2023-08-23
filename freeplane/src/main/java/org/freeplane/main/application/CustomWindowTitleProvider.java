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
		if (windowName == null || (window instanceof ConnectedToMenuView)) {
			return SimpleDockingWindowTitleProvider.INSTANCE.getTitle(window);
		} else {
			boolean dirty = false;
			int childWindowsCount = window.getChildWindowCount();
			for (int i = 0; i < childWindowsCount; i++) {
				dirty = dirty || window.getChildWindow(i).getTitle().endsWith("*");
			}
			return windowName + (childWindowsCount>1?" (" + childWindowsCount + ")":"") +(dirty?" *":"");
		}
	}
}
