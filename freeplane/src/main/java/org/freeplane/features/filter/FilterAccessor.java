package org.freeplane.features.filter;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;

public interface FilterAccessor {
    FilterInfo getFilterInfo(NodeModel node);
    Filter getFilter(MapModel map);
    void setFilter(MapModel map, Filter filter);
}
