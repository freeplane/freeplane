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
package org.freeplane.features.common.addins.encrypt;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.IMapSelection;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.EncryptionModel;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.ui.ActionDescriptor;
import org.freeplane.core.ui.FreeplaneAction;
import org.freeplane.core.ui.components.EnterPasswordDialog;

@ActionDescriptor(tooltip = "accessories/plugins/EnterPassword.properties_documentation", //
name = "accessories/plugins/EnterPassword.properties_name", //
iconPath = "accessories/plugins/icons/unlock.png", //
locations = { "/menu_bar/extras/first/nodes/crypto" })
public class EnterPassword extends FreeplaneAction {
	public EnterPassword(final ModeController modeController) {
		super();
	}

	public void actionPerformed(final ActionEvent e) {
		final NodeModel node = getModeController().getMapController().getSelectedNode();
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
			final StringBuffer password = pwdDialog.getPassword();
			if (!encNode.decrypt(new SingleDesEncrypter(password))) {
				JOptionPane.showMessageDialog(Controller.getController().getViewController()
				    .getContentPane(), getModeController().getText(
				    "accessories/plugins/EncryptNode.properties_wrong_password"), "Freeplane",
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
		encryptedMindMapNode.setEncrypter(new SingleDesEncrypter(password));
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
		final ModeController mindMapController = getModeController();
		final EncryptionModel encNode = EncryptionModel.getModel(node);
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
			final IMapSelection selection = Controller.getController().getSelection();
			selection.selectAsTheOnlyOneSelected(node);
		}
		else {
			encrypt(node);
		}
		mindMapController.getMapController().nodeRefresh(node);
	}
}
