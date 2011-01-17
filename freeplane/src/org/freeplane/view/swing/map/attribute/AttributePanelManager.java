/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2011 dimitry
 *
 *  This file author is dimitry
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
package org.freeplane.view.swing.map.attribute;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.controller.INodeSelectionListener;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.common.attribute.AttributeController;
import org.freeplane.features.common.map.ModeController;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.view.swing.map.NodeView;

/**
 * @author Dimitry Polivaev
 * Jan 9, 2011
 */
public class AttributePanelManager{
	final private JPanel tablePanel;
	private ModeController modeController;
	private class TableCreator implements INodeSelectionListener{

		private AttributeView attributeView;

		public void onDeselect(NodeModel node) {
			removeOldView();
        }

		private void removeOldView() {
	        if(attributeView != null){
				tablePanel.remove(0);
				tablePanel.revalidate();
				tablePanel.repaint();
				attributeView.viewRemoved();
				attributeView = null;
			}
        }

		public void onSelect(NodeModel node) {
			removeOldView();
			final NodeView nodeView = (NodeView) Controller.getCurrentController().getViewController().getSelectedComponent();
			if(nodeView == null)
				return;
			AttributeController.getController(modeController).createAttributeTableModel(node);
			attributeView = new AttributeView(nodeView, false);
			JComponent c  = attributeView.getContainer();
			tablePanel.add(c);
			tablePanel.revalidate();
			tablePanel.repaint();
        }

	}
	public AttributePanelManager(final ModeController modeController){
		this.modeController = modeController;
		tablePanel = new JPanel();
		modeController.getMapController().addNodeSelectionListener(new TableCreator());
	}
	public JPanel getTablePanel() {
    	return tablePanel;
    }
}
