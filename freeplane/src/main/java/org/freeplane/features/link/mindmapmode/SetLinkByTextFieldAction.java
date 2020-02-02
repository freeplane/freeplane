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
package org.freeplane.features.link.mindmapmode;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.net.URI;
import java.net.URISyntaxException;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.clipboard.ClipboardAccessor;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

class SetLinkByTextFieldAction extends AFreeplaneAction {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public SetLinkByTextFieldAction() {
		super("SetLinkByTextFieldAction");
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		final ModeController modeController = Controller.getCurrentModeController();
		final NodeModel selectedNode = modeController.getMapController().getSelectedNode();
		String linkAsString = NodeLinks.getLinkAsString(selectedNode);
		if(Compat.isWindowsOS() && linkAsString != null && linkAsString.startsWith("smb:")){
			final URI link = NodeLinks.getValidLink(selectedNode);
			linkAsString = Compat.smbUri2unc(link);
		}
		if(linkAsString == null || "".equals(linkAsString)){
			linkAsString = "http://";
			// if clipboard contains a valid uri use it
			Transferable t = ClipboardAccessor.getController().getClipboardContents();
			if (t != null && t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				try {
					final String plainTextFromClipboard = t.getTransferData(DataFlavor.stringFlavor).toString().trim();
					new URI(plainTextFromClipboard);
					linkAsString = plainTextFromClipboard;
				}
				catch (final Exception ex) {
				}
			}
		}
		final String inputValue = UITools.showInputDialog(
		    Controller.getCurrentController().getSelection().getSelected(), TextUtils.getText("edit_link_manually"), linkAsString);
		if (inputValue != null && ! inputValue.matches("\\w+://")) {
			final MLinkController linkController = (MLinkController) MLinkController.getController();
			if (inputValue.equals("")) {
				linkController.setLink(selectedNode, (URI) null, LinkController.LINK_ABSOLUTE);
				return;
			}
			try {
				final URI link = LinkController.createURI(inputValue.trim());
				linkController.setLink(selectedNode, link, LinkController.LINK_ABSOLUTE);
			}
			catch (final URISyntaxException e1) {
				LogUtils.warn(e1);
				UITools.errorMessage(TextUtils.format("invalid_uri", inputValue));
				return;
			}
		}
	}
}
