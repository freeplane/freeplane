/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.features.filter;

import java.util.List;
import java.util.Objects;
import java.util.WeakHashMap;

import javax.swing.Icon;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.filter.condition.ICondition;
import org.freeplane.features.filter.hidden.NodeVisibility;
import org.freeplane.features.filter.hidden.NodeVisibilityConfiguration;
import org.freeplane.features.link.ConnectorModel;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

/**
 * @author Dimitry Polivaev
 */
public class Filter implements IExtension {

    public enum FilteredElement{NODE, CONNECTOR, NODE_AND_CONNECTOR}

    public static Filter createTransparentFilter() {
		final ResourceController resourceController = ResourceController.getResourceController();
		FilteredElement filteredElement = resourceController.getEnumProperty("filter.filteredElement", FilteredElement.NODE);
		return new Filter(null, false, resourceController.getBooleanProperty("filter.showAncestors"), resourceController.getBooleanProperty("filter.showDescendants"), false, filteredElement, null);
	}

	static class FilterInfoAccessor {
	    private final WeakHashMap<NodeModel, FilterInfo> filterInfos = new WeakHashMap<>();

	    FilterInfo getFilterInfo(NodeModel node) {
	        return filterInfos.computeIfAbsent(node, x -> new FilterInfo());
	    }

	}

	static public Filter createFilter(final ICondition condition, final boolean areAncestorsShown,
            final boolean areDescendantsShown, final boolean appliesToVisibleElementsOnly, Filter baseFilter) {
		return new Filter(condition, false, areAncestorsShown, areDescendantsShown, appliesToVisibleElementsOnly, baseFilter);
	}

	final private ICondition condition;
	final int options;

	private FilterInfoAccessor accessor;
	private final boolean hidesMatchingElements;
	private final boolean appliesToVisibleElementsOnly;
	private final Filter baseFilter;
    private final FilteredElement filteredElement;

	public Filter(final ICondition condition, final boolean hidesMatchingElements, final boolean areAncestorsShown,
	        final boolean areDescendantsShown, final boolean appliesToVisibleElementsOnly, Filter baseFilter) {
	    this(condition, hidesMatchingElements, areAncestorsShown, areDescendantsShown,
	            appliesToVisibleElementsOnly, FilteredElement.NODE, baseFilter);
	}

	public Filter(final ICondition condition, final boolean hidesMatchingElements, final boolean areAncestorsShown,
	        final boolean areDescendantsShown, final boolean appliesToVisibleElementsOnly, FilteredElement filteredElement,
	        Filter baseFilter) {
	    super();
	    this.condition = condition;
	    this.hidesMatchingElements = hidesMatchingElements;
	    this.appliesToVisibleElementsOnly = appliesToVisibleElementsOnly;
        this.filteredElement = filteredElement;
		this.accessor = new FilterInfoAccessor();

		int options;
		if(hidesMatchingElements) {
            options = FilterInfo.SHOW_AS_HIDDEN;
            if (areAncestorsShown) {
                options += FilterInfo.SHOW_AS_HIDDEN_ANCESTOR;
            }
            if (areDescendantsShown) {
                options += FilterInfo.SHOW_AS_HIDDEN_DESCENDANT;
            }
		}
		else {
		    options = FilterInfo.SHOW_AS_MATCHED;
		    if (areAncestorsShown) {
		        options += FilterInfo.SHOW_AS_MATCHED_ANCESTOR;
		    }
		    if (areDescendantsShown) {
		        options += FilterInfo.SHOW_AS_MATCHED_DESCENDANT;
		    }
		}
		this.options = options;
		this.baseFilter = baseFilter;
	}

    void addFilterResult(final NodeModel node, final int flags) {
        getFilterInfo(node).add(flags);
    }

    void setFilterResult(final NodeModel node, final int flags) {
        getFilterInfo(node).set(flags);
    }

	protected boolean appliesToVisibleElementsOnly() {
		return appliesToVisibleElementsOnly;
	}

	static private Icon filterIcon;

	void displayFilterStatus() {
		if (filterIcon == null) {
			filterIcon = ResourceController.getResourceController().getIcon("ShowFilterToolbarAction.icon");
		}
		if (getCondition() != null) {
			Controller.getCurrentController().getViewController().addStatusInfo("filter", null, filterIcon);
		}
		else {
			Controller.getCurrentController().getViewController().removeStatus("filter");
		}
	}

	public void calculateFilterResults(final MapModel map) {
	    this.accessor = new FilterInfoAccessor();
		final NodeModel root = map.getRootNode();
		resetFilter(root);
		int ownStateAsAncestor = checkNode(root) ? FilterInfo.HAS_MATCHED_ANCESTOR : FilterInfo.HAS_HIDDEN_ANCESTOR;
        addFilterResult(root, filterChildrenGetDescendantState(root, ownStateAsAncestor));
	}

    public void calculateFilterResults(final NodeModel root) {
        this.accessor = new FilterInfoAccessor();
        applyFilterGetDescendantState(root, 0);
    }

    private int filterChildrenGetDescendantState(final NodeModel node, int state) {
        int descendantState = 0;
        for (final NodeModel child : children(node)) {
            descendantState = applyFilterGetDescendantState(child, state) | descendantState;
        }
        return descendantState;
    }

	private int applyFilterGetDescendantState(final NodeModel node, int ancestorState) {
		final boolean conditionSatisfied =  (condition == null || condition.checkNode(node));
		final boolean matchesCombinedFilter;
		if(appliesToVisibleElementsOnly()) {
		    matchesCombinedFilter = conditionSatisfied  && baseFilter.accepts(node);
		}
		else {
		    matchesCombinedFilter = conditionSatisfied;
		}
		int ownStateAsAncestor;
		if(ancestorState != 0 || ! node.isRoot())
		    ownStateAsAncestor = matchesCombinedFilter ?  FilterInfo.HAS_MATCHED_ANCESTOR : FilterInfo.HAS_HIDDEN_ANCESTOR;
		else
		    ownStateAsAncestor = 0;
        int childrenAncestorState = ancestorState | ownStateAsAncestor;
		int descendantState = filterChildrenGetDescendantState(node, childrenAncestorState);
        setFilterResult(node, ancestorState | descendantState | (matchesCombinedFilter ? FilterInfo.MATCHES : FilterInfo.NO_MATCH));
		int ownStateAsDescendant = matchesCombinedFilter ? FilterInfo.HAS_MATCHED_DESCENDANT : FilterInfo.HAS_HIDDEN_DESCENDANT;
        return descendantState | ownStateAsDescendant;
	}

	/*
	 * (non-Javadoc)
	 * @see freeplane.controller.filter.Filter#areAncestorsShown()
	 */
	public boolean areAncestorsShown() {
		return 0 != (options & (FilterInfo.SHOW_AS_MATCHED_ANCESTOR|FilterInfo.SHOW_AS_HIDDEN_ANCESTOR));
	}

	boolean areMatchingElementsHidden() {
	    return hidesMatchingElements;
	}


	/*
	 * (non-Javadoc)
	 * @see freeplane.controller.filter.Filter#areDescendantsShown()
	 */
	public boolean areDescendantsShown() {
		return 0 != (options & (FilterInfo.SHOW_AS_MATCHED_DESCENDANT | FilterInfo.SHOW_AS_HIDDEN_DESCENDANT));
	}

	public FilteredElement getFilteredElement() {
        return filteredElement;
    }

    private boolean checkNode(final NodeModel node) {
		return condition == null || ! shouldRemainInvisible(node) && condition.checkNode(node);
	}

	private boolean shouldRemainInvisible(final NodeModel node) {
		return condition != null && appliesToVisibleElementsOnly() && (node.isHiddenSummary() || !baseFilter.accepts(node));
	}

    protected List<NodeModel> children(final NodeModel node) {
        return node.getChildren();
    }

	public ICondition getCondition() {
		return condition;
	}

	public boolean canUseFilterResultsFrom(final Filter oldFilter) {
		return (! oldFilter.appliesToVisibleElementsOnly || appliesToVisibleElementsOnly)
		        && Objects.equals(condition, oldFilter.getCondition());
	}


    public void useFilterResultsFrom(Filter oldFilter) {
        accessor = oldFilter.accessor;
    }

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.controller.filter.Filter#isVisible(freeplane.modes.MindMapNode)
	 */
	public boolean isVisible(final NodeModel node) {
		return filteredElement == FilteredElement.CONNECTOR || accepts(node);
	}

	public boolean isVisible(final ConnectorModel connector) {
	    return filteredElement == FilteredElement.NODE
	            || accepts(connector.getSource())
	            && accepts(connector.getTarget());
	}

    public boolean accepts(NodeModel source) {
        return accepts(source, options);
    }

    public boolean isFoldable(final NodeModel node) {
        return  filteredElement == FilteredElement.CONNECTOR || (hidesMatchingElements ?
                accepts(node, options | FilterInfo.SHOW_AS_HIDDEN_ANCESTOR)
                : accepts(node, options | FilterInfo.SHOW_AS_MATCHED_ANCESTOR));
    }

    private boolean accepts(final NodeModel node, int options) {
        if(node.getExtension(NodeVisibility.class) == NodeVisibility.HIDDEN
				&& node.getMap().getRootNode().getExtension(NodeVisibilityConfiguration.class) != NodeVisibilityConfiguration.SHOW_HIDDEN_NODES)
			return false;
		if (condition == null || node.isRoot()) {
			return true;
		}
		FilterInfo filterInfo = getFilterInfo(node);
        return filterInfo.isNotChecked() || filterInfo.matches(options);
    }


	void resetFilter(final NodeModel node) {
		getFilterInfo(node).reset();
	}

	public FilterInfo getFilterInfo(final NodeModel node) {
		return accessor.getFilterInfo(node);
	}

    public void showAsMatched(NodeModel node) {
        FilterInfo filterInfo = getFilterInfo(node);
        if(! filterInfo.matches(FilterInfo.SHOW_AS_MATCHED)) {
            filterInfo.add(FilterInfo.SHOW_AS_MATCHED);
            if(! filterInfo.matches(FilterInfo.SHOW_AS_MATCHED_ANCESTOR))
                showAncestors(node);
            if(! filterInfo.matches(FilterInfo.SHOW_AS_MATCHED_DESCENDANT))
                showDescendants(node);
        }
    }

    private void showAncestors(NodeModel node) {
        NodeModel parent = node.getParentNode();
        if(parent == null)
            return;
        FilterInfo filterInfo = getFilterInfo(parent);
        if(! filterInfo.matches(FilterInfo.SHOW_AS_MATCHED_ANCESTOR)) {
            filterInfo.add(FilterInfo.SHOW_AS_MATCHED_ANCESTOR);
            showAncestors(parent);
        }
    }

    private void showDescendants(NodeModel node) {
        for (NodeModel child : children(node)) {
            FilterInfo filterInfo = getFilterInfo(child);
            filterInfo.add(FilterInfo.SHOW_AS_MATCHED_DESCENDANT);
            showDescendants(child);
        }
    }
}
