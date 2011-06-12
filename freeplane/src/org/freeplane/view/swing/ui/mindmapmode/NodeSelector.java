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
package org.freeplane.view.swing.ui.mindmapmode;

import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;

/**
 * @author Dimitry Polivaev
 * Mar 3, 2011
 */
public class NodeSelector implements INodeSelector {
	private NodeModel node = null;
	private JDialog dialog;
	NodeModel getNode() {
    	return node;
    }
	public void nodeSelected(NodeModel node) {
		this.node = node;
		dialog.setVisible(false);
	}
	public void show(final Component component, final INodeSelector externalSelector) {
		node = null;
		dialog = UITools.createCancelDialog(component, TextUtils.getText("node_selector"), TextUtils.getText("node_selector_message"));
		UITools.setDialogLocationRelativeTo(dialog, component);
		dialog.getRootPane().addAncestorListener(new GlassPaneManager(SwingUtilities.getRootPane(component), this));
		dialog.addComponentListener(new ComponentAdapter() {
			@Override
            public void componentHidden(ComponentEvent e) {
				externalSelector.nodeSelected(node);
            }
		});
		dialog.setVisible(true);
	}
}
