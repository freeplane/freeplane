/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
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
package org.freeplane.map.cloud.mindmapmode;

import java.awt.Color;
import java.awt.event.ActionEvent;

import javax.swing.JMenuItem;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.freeplane.controller.views.ColorTracker;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.modes.mindmapmode.MModeController;

import deprecated.freemind.modes.mindmapmode.actions.undo.MultipleNodeAction;

class CloudColorAction extends MultipleNodeAction implements PopupMenuListener {
	private Color actionColor;

	public CloudColorAction(final MModeController controller) {
		super(controller, "cloud_color", "images/Colors24.gif");
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		Color selectedColor = null;
		MModeController controller;
		{
			controller = getMModeController();
			final NodeModel selected = controller.getSelectedNode();
			if (selected.getCloud() != null) {
				selectedColor = selected.getModeController()
				    .getCloudController().getColor(selected);
			}
		}
		actionColor = ColorTracker.showCommonJColorChooserDialog(controller
		    .getMapView().getSelected(), controller
		    .getText("choose_cloud_color"), selectedColor);
		if (actionColor == null) {
			return;
		}
		super.actionPerformed(e);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freemind.modes.mindmapmode.actions.MultipleNodeAction#actionPerformed
	 * (freemind.modes.NodeModel)
	 */
	@Override
	protected void actionPerformed(final ActionEvent e, final NodeModel node) {
		final MCloudController cloudController = (MCloudController) node
		    .getModeController().getCloudController();
		cloudController.setColor(node, actionColor);
	}

	/**
	 *
	 */
	private boolean isCloudEnabled() {
		final NodeModel selected = getMModeController().getSelectedNode();
		return selected != null && selected.getCloud() != null;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * javax.swing.event.PopupMenuListener#popupMenuCanceled(javax.swing.event
	 * .PopupMenuEvent)
	 */
	public void popupMenuCanceled(final PopupMenuEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * javax.swing.event.PopupMenuListener#popupMenuWillBecomeInvisible(javax
	 * .swing.event.PopupMenuEvent)
	 */
	public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * javax.swing.event.PopupMenuListener#popupMenuWillBecomeVisible(javax.
	 * swing.event.PopupMenuEvent)
	 */
	public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
		final JMenuItem item = (JMenuItem) e.getSource();
		item.setEnabled(isCloudEnabled());
	}
}
