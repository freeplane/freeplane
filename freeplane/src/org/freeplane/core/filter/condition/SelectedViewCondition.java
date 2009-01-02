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

import javax.swing.JComponent;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.io.XMLElement;
import org.freeplane.core.map.NodeModel;
import org.freeplane.view.swing.map.NodeView;

public class SelectedViewCondition implements ICondition {
	private static ICondition condition;
	private static String description;
	private static JComponent renderer;

	public static ICondition CreateCondition() {
		if (SelectedViewCondition.condition == null) {
			SelectedViewCondition.condition = new SelectedViewCondition();
		}
		return SelectedViewCondition.condition;
	}

	public SelectedViewCondition() {
		super();
	}

	public boolean checkNode(final NodeModel node) {
		Controller.getController();
		final NodeView viewer = Controller.getModeController().getNodeView(node);
		return viewer != null && viewer.isSelected();
	}

	public JComponent getListCellRendererComponent() {
		if (SelectedViewCondition.renderer == null) {
			SelectedViewCondition.renderer = ConditionFactory
			    .createCellRendererComponent(SelectedViewCondition.description);
		}
		return SelectedViewCondition.renderer;
	}

	@Override
	public String toString() {
		if (SelectedViewCondition.description == null) {
			SelectedViewCondition.description = Controller.getText("filter_selected_node_view");
		}
		return SelectedViewCondition.description;
	}

	public void toXml(final XMLElement element) {
	}
}
