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
package org.freeplane.features.mindmapmode.file;

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ListIterator;

import javax.swing.JOptionPane;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.url.UrlManager;
import org.freeplane.features.common.link.LinkController;
import org.freeplane.features.common.link.NodeLinks;
import org.freeplane.features.mindmapmode.MMapController;
import org.freeplane.features.mindmapmode.link.MLinkController;

/**
 * This is exactly the opposite of exportBranch.
 */
class ImportLinkedBranchWithoutRootAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ImportLinkedBranchWithoutRootAction(final Controller controller) {
		super("ImportLinkedBranchWithoutRootAction", controller);
	}

	public void actionPerformed(final ActionEvent e) {
		final MapModel map = getController().getMap();
		final ModeController modeController = getModeController();
		final NodeModel selected = modeController.getMapController().getSelectedNode();
		if (selected == null || NodeLinks.getLink(selected) == null) {
			JOptionPane.showMessageDialog(getController().getViewController().getMapView(), ResourceBundles
			    .getText("import_linked_branch_no_link"));
			return;
		}
		try {
			final URI uri = NodeLinks.getLink(selected);
			final File file = uri.isAbsolute() && !uri.isOpaque() ? new File(uri) : new File(new URL(map.getURL(), uri
			    .getPath()).getFile());
			final NodeModel node = ((MMapController) modeController.getMapController()).loadTree(map, file);
			for (final ListIterator i = modeController.getMapController().childrenUnfolded(node); i.hasNext();) {
				final NodeModel importNode = (NodeModel) i.next();
				((MMapController) modeController.getMapController()).insertNode(importNode, selected);
			}
			((MLinkController) LinkController.getController(modeController)).setLink(selected, (URI) null, false);
		}
		catch (final Exception ex) {
			UrlManager.getController(modeController).handleLoadingException(ex);
		}
	}
}
