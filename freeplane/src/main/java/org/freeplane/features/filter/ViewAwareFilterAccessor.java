package org.freeplane.features.filter;

import java.awt.Component;

import javax.swing.JComponent;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.IMapViewChangeListener;

class ViewAwareFilterAccessor implements FilterAccessor, IMapViewChangeListener {

    private MapModel map;
    private SingleFilterAccessor accessor;

    @Override
    public FilterInfo getFilterInfo(NodeModel node) {
        return accessor(node.getMap()).getFilterInfo(node);
    }

    @Override
    public Filter getFilter(MapModel map) {
        return accessor(map).getFilter();
    }
    @Override
    public void setFilter(MapModel map, Filter filter) {
        accessor(map).setFilter(filter);
    }
    
    SingleFilterAccessor accessor(MapModel map) {
        if(accessor != null && this.map == map) {
            return accessor;
        }
        this.map = map;
        Controller controller = Controller.getCurrentController();
        if (controller.getMap() == map) {
            JComponent mapViewComponent = controller.getMapViewManager().getMapViewComponent();
            accessor = (SingleFilterAccessor) mapViewComponent.getClientProperty(SingleFilterAccessor.class);
            if(accessor == null) {
                accessor = new SingleFilterAccessor();
                mapViewComponent.putClientProperty(SingleFilterAccessor.class, accessor);
            }
            return accessor;
        }
        if(map.containsExtension(SingleFilterAccessor.class)) {
            accessor = map.getExtension(SingleFilterAccessor.class);
        }
        else {
            accessor = new SingleFilterAccessor();
            map.putExtension(SingleFilterAccessor.class, accessor);
        }
        return accessor;
    }

    @Override
    public void afterViewChange(Component oldView, Component newView) {
        reset();
    }

    @Override
    public void beforeViewChange(Component oldView, Component newView) {
        reset();
    }
    
    private void reset() {
        map = null;
        accessor = null;
    }
}
