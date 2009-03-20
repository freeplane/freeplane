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

import javax.swing.JOptionPane;

import org.freeplane.core.Compat;
import org.freeplane.core.controller.Controller;
import org.freeplane.core.frame.ViewController;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.FreeplaneResourceBundle;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.IFreeplaneAction;
import org.freeplane.core.url.UrlManager;
import org.freeplane.core.util.LogTool;
import org.freeplane.features.common.link.NodeLinks;
import org.freeplane.features.mindmapmode.MMapController;

class ImportLinkedBranchAction extends AFreeplaneAction implements IFreeplaneAction {
	private static final long serialVersionUID = -6730969236999381143L;

	public ImportLinkedBranchAction(final Controller controller) {
		super(controller, "import_linked_branch");
	}

	public void actionPerformed(final ActionEvent e) {
		final MapModel map = getController().getMap();
		final ModeController modeController = getModeController();
		final NodeModel selected = modeController.getMapController().getSelectedNode();
		final ViewController viewController = getController().getViewController();
		if (selected == null || NodeLinks.getLink(selected) == null) {
			JOptionPane.showMessageDialog((viewController.getMapView()), FreeplaneResourceBundle
			    .getText("import_linked_branch_no_link"));
			return;
		}
		URL absolute = null;
		try {
			final String relative = NodeLinks.getLink(selected);
			absolute = UrlManager.isAbsolutePath(relative) ? Compat.fileToUrl(new File(relative)) : new URL(Compat
			    .fileToUrl(map.getFile()), relative);
		}
		catch (final MalformedURLException ex) {
			JOptionPane
			    .showMessageDialog(viewController.getMapView(), "Couldn't create valid URL for:" + map.getFile());
			LogTool.logException(ex);
			return;
		}
		try {
			final NodeModel node = ((MMapController) modeController.getMapController()).loadTree(map, new File(absolute
			    .getFile()));
			((MMapController) modeController.getMapController()).insertNode(node, selected);
		}
		catch (final Exception ex) {
			UrlManager.getController(modeController).handleLoadingException(ex);
		}
	}

	public String getName() {
		return "importLinkedBranch";
	}
}
