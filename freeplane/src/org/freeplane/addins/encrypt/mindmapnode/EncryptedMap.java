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

import org.freeplane.core.controller.Controller;
import org.freeplane.core.map.INodeSelectionListener;
import org.freeplane.core.map.ModeController;
import org.freeplane.core.map.NodeModel;
import org.freeplane.core.ui.ActionDescriptor;
import org.freeplane.core.ui.FreeplaneAction;
import org.freeplane.core.ui.components.EnterPasswordDialog;
import org.freeplane.modes.mindmapmode.EncryptionModel;
import org.freeplane.view.swing.map.NodeView;

@ActionDescriptor(name = "accessories/plugins/NewEncryptedMap.properties_name", //
tooltip = "accessories/plugins/NewEncryptedMap.properties_documentation", //
iconPath = "accessories/plugins/icons/lock.png", //
locations = { "/menu_bar/file/open" })
public class EncryptedMap extends FreeplaneAction implements INodeSelectionListener {
	public EncryptedMap(final ModeController modeController) {
		super();
		modeController.addNodeSelectionListener(this);
	}

	public void actionPerformed(final ActionEvent e) {
		newEncryptedMap();
	}

	public boolean canBeEnabled() {
		boolean isEncryptedNode = false;
		boolean isOpened = false;
		final ModeController modeController = getModeController();
		if (modeController == null) {
			return false;
		}
		if (modeController.getSelectedNode() != null) {
			final EncryptionModel enode = EncryptionModel.getModel(modeController.getSelectedNode());
			if (enode != null) {
				isEncryptedNode = true;
				isOpened = enode.isAccessible();
			}
		}
		return (!isEncryptedNode || isOpened);
	}

	/**
	 */
	private StringBuffer getUsersPassword() {
		final EnterPasswordDialog pwdDialog = new EnterPasswordDialog(Controller.getController()
		    .getViewController().getJFrame(), true);
		pwdDialog.setModal(true);
		pwdDialog.show();
		if (pwdDialog.getResult() == EnterPasswordDialog.CANCEL) {
			return null;
		}
		final StringBuffer password = pwdDialog.getPassword();
		return password;
	}

	/**
	 *
	 */
	private void newEncryptedMap() {
		final StringBuffer password = getUsersPassword();
		if (password == null) {
			return;
		}
		final ModeController newModeController = getMModeController();
		final NodeModel node = new NodeModel(Controller
		    .getText("accessories/plugins/EncryptNode.properties_select_me"), null);
		final EncryptionModel encryptedMindMapNode = new EncryptionModel(node);
		encryptedMindMapNode.setPassword(password);
		node.addExtension(encryptedMindMapNode);
		newModeController.getMapController().newMap(node);
	}

	public void onDeselect(final NodeView node) {
		setEnabled(false);
	}

	public void onSelect(final NodeView node) {
		setEnabled(canBeEnabled());
	}
}
