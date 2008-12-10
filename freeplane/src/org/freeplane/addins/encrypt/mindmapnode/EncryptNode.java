/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
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
package org.freeplane.addins.encrypt.mindmapnode;

import java.awt.event.ActionEvent;

import org.freeplane.controller.ActionDescriptor;
import org.freeplane.controller.Controller;
import org.freeplane.controller.FreeMindAction;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.map.tree.view.NodeView;
import org.freeplane.modes.INodeSelectionListener;
import org.freeplane.modes.ModeController;
import org.freeplane.modes.mindmapmode.EncryptionModel;
import org.freeplane.ui.dialogs.EnterPasswordDialog;

@ActionDescriptor(name = "accessories/plugins/EncryptNode.properties_name", //
tooltip = "accessories/plugins/EncryptNode.properties_documentation", //
iconPath = "accessories/plugins/icons/lock.png", //
locations = { "/menu_bar/extras/first/nodes/crypto" })
public class EncryptNode extends FreeMindAction implements
        INodeSelectionListener {
	public EncryptNode(final ModeController modeController) {
		super();
		modeController.addNodeSelectionListener(this);
	}

	public void actionPerformed(final ActionEvent e) {
		final NodeModel node = getModeController().getSelectedNode();
		encrypt(node);
		getModeController().getMapController().nodeRefresh(node);
	}

	public boolean canBeEnabled() {
		boolean isEncryptedNode = false;
		boolean isOpened = false;
		final ModeController modeController = getModeController();
		if (modeController == null) {
			return false;
		}
		if (modeController.getSelectedNode() != null) {
			final EncryptionModel enode = modeController.getSelectedNode()
			    .getEncryptionModel();
			if (enode != null) {
				isEncryptedNode = true;
				isOpened = enode.isAccessible();
			}
		}
		return (!isEncryptedNode || isOpened);
	}

	/**
	 */
	private void encrypt(final NodeModel node) {
		final StringBuffer password = getUsersPassword();
		if (password == null) {
			return;
		}
		final EncryptionModel encryptedMindMapNode = new EncryptionModel(node);
		encryptedMindMapNode.setPassword(password);
		node.addExtension(encryptedMindMapNode);
	}

	/**
	 */
	private StringBuffer getUsersPassword() {
		final EnterPasswordDialog pwdDialog = new EnterPasswordDialog(
		    Controller.getController().getViewController().getJFrame(), true);
		pwdDialog.setModal(true);
		pwdDialog.show();
		if (pwdDialog.getResult() == EnterPasswordDialog.CANCEL) {
			return null;
		}
		final StringBuffer password = pwdDialog.getPassword();
		return password;
	}

	public void onDeselect(final NodeView node) {
		setEnabled(false);
	}

	public void onSelect(final NodeView node) {
		setEnabled(canBeEnabled());
	}
}
