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
package org.freeplane.addins.mindmapmode.encrypt;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.freeplane.controller.ActionDescriptor;
import org.freeplane.controller.Controller;
import org.freeplane.controller.FreeMindAction;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.map.tree.view.MapView;
import org.freeplane.modes.ModeController;
import org.freeplane.modes.mindmapmode.EncryptionModel;
import org.freeplane.modes.mindmapmode.MModeController;
import org.freeplane.ui.dialogs.EnterPasswordDialog;

@ActionDescriptor(
	tooltip="accessories/plugins/EnterPassword.properties_documentation", //
	name="accessories/plugins/EnterPassword.properties_name", //
	iconPath="accessories/plugins/icons/unlock.png", //
	locations={"/menu_bar/extras/first/nodes/crypto"}
)
public class EnterPassword extends FreeMindAction {
	public EnterPassword() {
		super();
	}

	/**
	 */
	private void doPasswordCheckAndDecryptNode(final EncryptionModel encNode) {
		while (true) {
			final EnterPasswordDialog pwdDialog = new EnterPasswordDialog(
			    Controller.getController().getViewController().getJFrame(),
			    false);
			pwdDialog.setModal(true);
			pwdDialog.setVisible(true);
			if (pwdDialog.getResult() == EnterPasswordDialog.CANCEL) {
				return;
			}
			if (!encNode.decrypt(pwdDialog.getPassword())) {
				JOptionPane
				    .showMessageDialog(
				        Controller.getController().getViewController()
				            .getContentPane(),
				        getModeController()
				            .getText(
				                "accessories/plugins/EncryptNode.properties_wrong_password"),
				        "Freemind", JOptionPane.ERROR_MESSAGE);
			}
			else {
				return;
			}
		}
	}
    public void actionPerformed(ActionEvent e) {
    	NodeModel node = getModeController().getSelectedNode();
    	toggleCryptState(node);
    	getModeController().getMapController().nodeRefresh(node);
    	return;
	}


	/**
	 */
	private void toggleCryptState(final NodeModel node) {
		final MModeController mindMapController = getMModeController();
		final EncryptionModel encNode = node.getEncryptionModel();
		if (encNode != null) {
			if (encNode.isAccessible()) {
				node.setFolded(true);
				mindMapController.getMapController().nodeStructureChanged(node);
				encNode.setAccessible(false);
			}
			else {
				doPasswordCheckAndDecryptNode(encNode);
				mindMapController.getMapController().nodeStructureChanged(node);
			}
			final MapView mapView = mindMapController.getMapView();
			mapView.selectAsTheOnlyOneSelected(mapView.getNodeView(node));
		}
		else {
			JOptionPane
			    .showMessageDialog(
			        Controller.getController().getViewController()
			            .getContentPane(),
			        mindMapController
			            .getText("accessories/plugins/EncryptNode.properties_insert_encrypted_node_first"),
			        "Freemind", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	@Override
	public boolean isEnabled() {
		boolean isEncryptedNode = false;
		final ModeController modeController = getModeController();
		if (modeController.getSelectedNode() != null) {
			final EncryptionModel enode = modeController.getSelectedNode()
			    .getEncryptionModel();
			if (enode != null) {
				isEncryptedNode = true;
			}
		}
		return isEncryptedNode;
	}


}
