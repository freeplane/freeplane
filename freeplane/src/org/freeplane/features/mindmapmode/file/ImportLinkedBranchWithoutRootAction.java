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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ListIterator;

import javax.swing.JOptionPane;

import org.freeplane.core.Compat;
import org.freeplane.core.controller.Controller;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.url.UrlManager;
import org.freeplane.features.common.clipboard.ClipboardController;
import org.freeplane.features.common.link.NodeLinks;
import org.freeplane.features.mindmapmode.MMapController;
import org.freeplane.features.mindmapmode.clipboard.MClipboardController;

/**
 * This is exactly the opposite of exportBranch.
 */
class ImportLinkedBranchWithoutRootAction extends AFreeplaneAction {
	public ImportLinkedBranchWithoutRootAction(final Controller controller) {
		super(controller, "import_linked_branch_without_root");
	}

	public void actionPerformed(final ActionEvent e) {
		final MapModel map = getController().getMap();
		final NodeModel selected = getModeController().getMapController().getSelectedNode();
		if (selected == null || NodeLinks.getLink(selected) == null) {
			JOptionPane.showMessageDialog(getController().getViewController().getMapView(), getModeController()
			    .getText("import_linked_branch_no_link"));
			return;
		}
		URL absolute = null;
		try {
			final String relative = NodeLinks.getLink(selected);
			absolute = UrlManager.isAbsolutePath(relative) ? Compat.fileToUrl(new File(relative)) : new URL(
			    Compat.fileToUrl(map.getFile()), relative);
		}
		catch (final MalformedURLException ex) {
			JOptionPane.showMessageDialog(getController().getViewController().getMapView(),
			    "Couldn't create valid URL.");
			return;
		}
		try {
			final NodeModel node = ((MMapController) getModeController().getMapController()).loadTree(map, new File(
			    absolute.getFile()));
			for (final ListIterator i = node.getModeController().getMapController().childrenUnfolded(node); i.hasNext();) {
				final NodeModel importNode = (NodeModel) i.next();
				((MClipboardController) ClipboardController.getController(getController().getModeController())).paste(
				    importNode, selected);
			}
		}
		catch (final Exception ex) {
			UrlManager.getController(getModeController()).handleLoadingException(ex);
		}
	}
}
