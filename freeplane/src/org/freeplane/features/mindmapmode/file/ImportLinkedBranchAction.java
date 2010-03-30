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
import java.net.URI;
import java.net.URL;

import javax.swing.JOptionPane;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.frame.ViewController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.common.link.LinkController;
import org.freeplane.features.common.link.NodeLinks;
import org.freeplane.features.common.map.MapModel;
import org.freeplane.features.common.map.ModeController;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.url.UrlManager;
import org.freeplane.features.mindmapmode.link.MLinkController;
import org.freeplane.features.mindmapmode.map.MMapController;

class ImportLinkedBranchAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ImportLinkedBranchAction(final Controller controller) {
		super("ImportLinkedBranchAction", controller);
	}

	public void actionPerformed(final ActionEvent e) {
		final MapModel map = getController().getMap();
		final ModeController modeController = getModeController();
		final NodeModel selected = modeController.getMapController().getSelectedNode();
		final ViewController viewController = getController().getViewController();
		if (selected == null || NodeLinks.getLink(selected) == null) {
			JOptionPane.showMessageDialog((viewController.getMapView()), TextUtils
			    .getText("import_linked_branch_no_link"));
			return;
		}
		try {
			final URI uri = NodeLinks.getLink(selected);
			final File file = uri.isAbsolute() && !uri.isOpaque() ? new File(uri) : new File(new URL(map.getURL(), uri
			    .getPath()).getFile());
			final NodeModel node = ((MMapController) modeController.getMapController()).loadTree(map, file);
			((MMapController) modeController.getMapController()).insertNode(node, selected);
			((MLinkController) LinkController.getController(modeController)).setLink(selected, (URI) null, false);
			((MLinkController) LinkController.getController(modeController)).setLink(node, (URI) null, false);
		}
		catch (final MalformedURLException ex) {
			UITools.errorMessage("Couldn't create valid URL for:" + map.getFile());
			LogUtils.warn(ex);
			return;
		}
		catch (final Exception ex) {
			UrlManager.getController(modeController).handleLoadingException(ex);
		}
	}
}
