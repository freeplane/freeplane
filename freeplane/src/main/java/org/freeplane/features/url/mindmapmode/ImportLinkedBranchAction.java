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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import javax.swing.JOptionPane;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.PersistentNodeHook;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.features.url.UrlManager;

class ImportLinkedBranchAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ImportLinkedBranchAction() {
		super("ImportLinkedBranchAction");
	}

	public void actionPerformed(final ActionEvent e) {
		final MapModel map = Controller.getCurrentController().getMap();
		final ModeController modeController = Controller.getCurrentModeController();
		final NodeModel selected = modeController.getMapController().getSelectedNode();
		final IMapViewManager viewController = Controller.getCurrentController().getMapViewManager();
		if (selected == null || NodeLinks.getLink(selected) == null) {
			JOptionPane.showMessageDialog((viewController.getMapViewComponent()), TextUtils
			    .getText("import_linked_branch_no_link"));
			return;
		}
		final URI uri = NodeLinks.getLink(selected);
		try {
			final File file = uri.isAbsolute() && !uri.isOpaque() ? new File(uri) : new File(new URL(map.getURL(), uri
			    .getPath()).getFile());
			final NodeModel node = ((MFileManager) UrlManager.getController()).loadTree(map, file);
			PersistentNodeHook.removeMapExtensions(node);
			((MMapController) modeController.getMapController()).insertNode(node, selected);
			((MLinkController) LinkController.getController()).setLink(selected, (URI) null, LinkController.LINK_ABSOLUTE);
			((MLinkController) LinkController.getController()).setLink(node, (URI) null, LinkController.LINK_ABSOLUTE);
		}
		catch (final MalformedURLException ex) {
			UITools.errorMessage(TextUtils.format("invalid_url_msg", uri.toString()));
			LogUtils.warn(ex);
			return;
		}
		catch (final IllegalArgumentException ex) {
			UITools.errorMessage(TextUtils.format("invalid_file_msg", uri.toString()));
			LogUtils.warn(ex);
			return;
		}
		catch (final Exception ex) {
			UrlManager.getController().handleLoadingException(ex);
		}
	}
}
