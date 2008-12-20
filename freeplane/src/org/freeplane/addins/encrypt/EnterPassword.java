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
package org.freeplane.addins.encrypt;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.freeplane.controller.ActionDescriptor;
import org.freeplane.controller.Controller;
import org.freeplane.controller.FreeplaneAction;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.map.tree.view.MapView;
import org.freeplane.modes.ModeController;
import org.freeplane.modes.mindmapmode.EncryptionModel;
import org.freeplane.modes.mindmapmode.MModeController;
import org.freeplane.ui.dialogs.EnterPasswordDialog;

@ActionDescriptor(tooltip = "accessories/plugins/EnterPassword.properties_documentation", //
name = "accessories/plugins/EnterPassword.properties_name", //
iconPath = "accessories/plugins/icons/unlock.png", //
locations = { "/menu_bar/extras/first/nodes/crypto" })
public class EnterPassword extends FreeplaneAction {
	public EnterPassword(final ModeController modeController) {
		super();
	}

	public void actionPerformed(final ActionEvent e) {
		final NodeModel node = getModeController().getSelectedNode();
		toggleCryptState(node);
	}

	/**
	 */
	private boolean doPasswordCheckAndDecryptNode(final EncryptionModel encNode) {
		while (true) {
			final EnterPasswordDialog pwdDialog = new EnterPasswordDialog(Controller
			    .getController().getViewController().getJFrame(), false);
			pwdDialog.setModal(true);
			pwdDialog.setVisible(true);
			if (pwdDialog.getResult() == EnterPasswordDialog.CANCEL) {
				return false;
			}
			if (!encNode.decrypt(pwdDialog.getPassword())) {
				JOptionPane.showMessageDialog(Controller.getController().getViewController()
				    .getContentPane(), getModeController().getText(
				    "accessories/plugins/EncryptNode.properties_wrong_password"), "Freemind",
				    JOptionPane.ERROR_MESSAGE);
				return false;
			}
			else {
				return true;
			}
		}
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
	 */
	private void toggleCryptState(final NodeModel node) {
		final MModeController mindMapController = getMModeController();
		final EncryptionModel encNode = node.getEncryptionModel();
		if (encNode != null) {
			if (encNode.isAccessible()) {
				node.setFolded(true);
				encNode.setAccessible(false);
				mindMapController.getMapController().nodeStructureChanged(node);
			}
			else {
				if (doPasswordCheckAndDecryptNode(encNode)) {
					node.setFolded(false);
					mindMapController.getMapController().nodeStructureChanged(node);
				}
			}
			final MapView mapView = mindMapController.getMapView();
			mapView.selectAsTheOnlyOneSelected(mapView.getNodeView(node));
		}
		else {
			encrypt(node);
		}
		mindMapController.getMapController().nodeRefresh(node);
	}
}
