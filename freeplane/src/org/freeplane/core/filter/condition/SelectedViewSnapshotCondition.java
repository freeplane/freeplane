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
package org.freeplane.core.filter.condition;

import java.util.HashSet;

import javax.swing.JComponent;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.n3.nanoxml.XMLElement;

public class SelectedViewSnapshotCondition implements ISelectableCondition {
	private static String description;
	private static JComponent renderer;

	public static ISelectableCondition CreateCondition(final Controller controller) {
		return new SelectedViewSnapshotCondition(controller);
	}

	HashSet<NodeModel> selectedNodes;

	public SelectedViewSnapshotCondition(final Controller controller) {
		super();
		selectedNodes = new HashSet<NodeModel>();
		selectedNodes.addAll(controller.getSelection().getSelection());
	}

	public boolean checkNode(final NodeModel node) {
		return selectedNodes.contains(node);
	}

	public JComponent getListCellRendererComponent() {
		if (SelectedViewSnapshotCondition.renderer == null) {
			SelectedViewSnapshotCondition.renderer = ConditionFactory.createCellRendererComponent(toString());
		}
		return SelectedViewSnapshotCondition.renderer;
	}

	@Override
	public String toString() {
		if (SelectedViewSnapshotCondition.description == null) {
			SelectedViewSnapshotCondition.description = ResourceBundles.getText("filter_selected_node_view_snapshot");
		}
		return SelectedViewSnapshotCondition.description;
	}

	public void toXml(final XMLElement element) {
	}
}
