/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2010 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is created by Stefan Ott in 2011.
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
package org.freeplane.features.attribute.mindmapmode;

import java.awt.event.ActionEvent;
import java.util.Collection;

import org.freeplane.core.ui.AMultipleNodeAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.features.attribute.AttributeController;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

@EnabledAction(checkOnNodeChange = true)
public class RemoveAllAttributesAction extends AMultipleNodeAction {
	/**
	 * @author Stefan Ott
	 *
	 * This action removes all attribute of a node
	 */
	private static final long serialVersionUID = 1L;

	public RemoveAllAttributesAction() {
		super("attributes_RemoveAllAttributesAction");
	};

	@Override
	public void actionPerformed(final ActionEvent e, final NodeModel node) {
		final AttributeUtilities atrUtil = new AttributeUtilities();
		if (atrUtil.hasAttributes(node)) {
			final NodeAttributeTableModel natm = NodeAttributeTableModel.getModel(node);
			final int j = natm.getRowCount();
			for (int i = 0; i < j; i++) {
				AttributeController.getController().performRemoveRow(node, natm, 0);
			}
		}
	}

	@Override
	public void setEnabled() {
		boolean enable = false;
		final AttributeUtilities atrUtil = new AttributeUtilities();
		final Collection<NodeModel> nodes = Controller.getCurrentModeController().getMapController().getSelectedNodes();
		for (final NodeModel node : nodes) {
			if (node != null && atrUtil.hasAttributes(node)) {
				enable = true;
				break;
			}
		}
		setEnabled(enable);
	}
}
