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
package org.freeplane.features.filter.condition;

import java.awt.FontMetrics;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.swing.Icon;

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;

public class SelectedViewSnapshotCondition extends ASelectableCondition {
	private static final String NAME = "selected_view_snapshot";
	private static String defaultDescription;
	private final String description;

	HashSet<NodeModel> selectedNodes;

    private static String defaultDescription() {
        if (SelectedViewSnapshotCondition.defaultDescription == null) {
            SelectedViewSnapshotCondition.defaultDescription = TextUtils.getText("filter_selected_node_view_snapshot");
        }
        return SelectedViewSnapshotCondition.defaultDescription;
    }

    public SelectedViewSnapshotCondition(Collection<? extends NodeModel> selectedNodes) {
        this(selectedNodes, defaultDescription());
    }

    public SelectedViewSnapshotCondition(Collection<? extends NodeModel> selectedNodes, String description) {
		super();
        this.description = description;
		this.selectedNodes = new HashSet<NodeModel>();
		this.selectedNodes.addAll(selectedNodes);
	}

	public boolean checkNode(final NodeModel node) {
		return selectedNodes.contains(node);
	}

	@Override
    protected String createDescription() {
		return description;
    }

    @Override
    protected List<Icon> createRenderedIcons(FontMetrics fontMetrics) {
        return createRenderedIconsFromDescription(fontMetrics);
    }

	@Override
    protected String getName() {
	    return NAME;
   }

    @Override
    public boolean canBePersisted() {
        return false;
    }

}
