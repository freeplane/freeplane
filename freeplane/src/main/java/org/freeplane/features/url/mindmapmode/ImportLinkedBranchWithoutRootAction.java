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
package org.freeplane.features.url.mindmapmode;

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URI;
import java.net.URL;
import javax.swing.JOptionPane;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.url.UrlManager;

/**
 * This is exactly the opposite of exportBranch.
 */
class ImportLinkedBranchWithoutRootAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ImportLinkedBranchWithoutRootAction() {
		super("ImportLinkedBranchWithoutRootAction");
	}

	public void actionPerformed(final ActionEvent e) {
		final MapModel map = Controller.getCurrentController().getMap();
		final ModeController modeController = Controller.getCurrentModeController();
		final NodeModel selected = modeController.getMapController().getSelectedNode();
		if (selected == null || NodeLinks.getLink(selected) == null) {
			JOptionPane.showMessageDialog(Controller.getCurrentController().getMapViewManager().getMapViewComponent(), TextUtils
			    .getText("import_linked_branch_no_link"));
			return;
		}
		try {
			final URI uri = NodeLinks.getLink(selected);
			final URL url = map.getURL();
			final File file = uri.isAbsolute() && !uri.isOpaque() ? new File(uri) : new File(new URL(url, uri
			    .getPath()).getFile());
			final NodeModel node = ((MFileManager) UrlManager.getController()).loadTree(map, file);
			map.setURL(url);
			MapController r = Controller.getCurrentModeController().getMapController();
			for (final NodeModel child : node.getChildren()) {
				child.setParent(null);
				((MMapController) modeController.getMapController()).insertNode(child, selected);
			}
			((MLinkController) LinkController.getController()).setLink(selected, (URI) null, LinkController.LINK_ABSOLUTE);
		}
		catch (final Exception ex) {
			UrlManager.getController().handleLoadingException(ex);
		}
	}
}
