package org.freeplane.main.application;

import java.awt.Component;

import org.freeplane.features.map.MapModel;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.ui.MapDisplayNameResolver;

import net.infonode.docking.DockingWindow;
import net.infonode.docking.View;
import net.infonode.docking.properties.DockingWindowProperties;
import net.infonode.docking.title.DockingWindowTitleProvider;
import net.infonode.docking.title.SimpleDockingWindowTitleProvider;
import net.infonode.properties.propertymap.PropertyMapValueHandler;
import net.infonode.properties.types.StringProperty;

final class CustomTabTitleProvider implements DockingWindowTitleProvider {
    static final StringProperty CUSTOMIZED_TAB_NAME_PROPERTY = new StringProperty(
            DockingWindowProperties.PROPERTIES, "customizedTabName", "Custom tab name",
            PropertyMapValueHandler.INSTANCE);

    static String getCustomizedTabName(DockingWindow window) {
        return CUSTOMIZED_TAB_NAME_PROPERTY.get(window.getWindowProperties().getMap());
    }

    @Override
    public String getTitle(DockingWindow window) {
        if (window instanceof View) {
            Component component = MapViewDockingWindows.getContainedMapView((View) window);
            if (component instanceof MapView)
                return MapDisplayNameResolver.resolveLabel((MapView) component);
        }
        String name = getCustomizedTabName(window);
        if (name == null)
            return SimpleDockingWindowTitleProvider.INSTANCE.getTitle(window);
        int count = countMapViews(window);
        return name + (count > 1 ? " (" + count + ")" : "") + (isDirty(window) ? " *" : "");
    }

    private int countMapViews(DockingWindow window) {
        if (window instanceof View)
            return MapViewDockingWindows.getContainedMapView((View) window) instanceof MapView ? 1 : 0;
        int count = 0;
        for (int i = 0; i < window.getChildWindowCount(); i++)
            count += countMapViews(window.getChildWindow(i));
        return count;
    }

    private boolean isDirty(DockingWindow window) {
        if (window instanceof View) {
            Component component = MapViewDockingWindows.getContainedMapView((View) window);
            if (!(component instanceof MapView))
                return false;
            MapModel map = ((MapView) component).getMap();
            return !map.isSaved() && !map.isReadOnly();
        }
        for (int i = 0; i < window.getChildWindowCount(); i++)
            if (isDirty(window.getChildWindow(i)))
                return true;
        return false;
    }
}
