/*
 * Created on 27 Sept 2024
 *
 * author dimitry
 */
package org.freeplane.view.swing.map;

import java.util.List;

import javax.swing.Icon;
import javax.swing.SwingUtilities;

import org.freeplane.core.ui.components.IconListComponent;
import org.freeplane.core.ui.components.TagIcon;

public class MapViewIconListComponent extends IconListComponent{
    private static final long serialVersionUID = 1L;

    public MapViewIconListComponent() {
        super();
    }

    public MapViewIconListComponent(List<TagIcon> icons) {
        super(icons);
    }

    @Override
    protected float getZoom() {
        final float zoom = getMap().getZoom();
        return zoom;
    }

    private MapView getMap() {
        return getNodeView().getMap();
    }

    private NodeView getNodeView() {
        return (NodeView) SwingUtilities.getAncestorOfClass(NodeView.class, this);
    }

    @Override
    protected boolean useFractionalMetrics() {
        final MapView map = getMap();
        if (map.isPrinting()) {
            return true;
        }
        final float zoom = map.getZoom();
        return 1f != zoom;
    }

}