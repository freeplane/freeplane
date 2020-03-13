package org.freeplane.features.filter;

import java.util.WeakHashMap;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.NodeModel;

class SingleFilterAccessor implements IExtension {
    private final WeakHashMap<NodeModel, FilterInfo> filterInfos = new WeakHashMap<>();
    private Filter filter = FilterController.getCurrentFilterController().createTransparentFilter();

    FilterInfo getFilterInfo(NodeModel node) {
        return filterInfos.computeIfAbsent(node, x -> new FilterInfo());
    }

    Filter getFilter() {
        return filter;
    }

    void setFilter(Filter filter) {
        this.filter = filter;
    }

}
