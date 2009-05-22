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
import org.freeplane.core.modecontroller.INodeSelectionListener;
import org.freeplane.core.modecontroller.MapController;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.EncryptionModel;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ActionLocationDescriptor;
import org.freeplane.core.ui.components.EnterPasswordDialog;

@ActionLocationDescriptor(locations = { "/menu_bar/extras/first/nodes/crypto" })
public class EnterPassword extends AFreeplaneAction  implements INodeSelectionListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public EnterPassword(final ModeController modeController) {
		super("EnterPassword", modeController.getController());
		modeController.getMapController().addNodeSelectionListener(this);
	}

	public void actionPerformed(final ActionEvent e) {
		final NodeModel node = getModeController().getMapController().getSelectedNode();
		toggleCryptState(node);
	}

	/**
	 * @param e 
	 */
	private boolean doPasswordCheckAndDecryptNode(final EncryptionModel encNode) {
		while (true) {
			final EnterPasswordDialog pwdDialog = new EnterPasswordDialog(getController().getViewController()
			    .getFrame(), false);
			pwdDialog.setModal(true);
			pwdDialog.setVisible(true);
			if (pwdDialog.getResult() == EnterPasswordDialog.CANCEL) {
				return false;
			}
			final StringBuffer password = pwdDialog.getPassword();
			if (!encNode.decrypt(getModeController().getMapController(), new SingleDesEncrypter(password))) {
				final Controller controller = getController();
				JOptionPane.showMessageDialog(controller.getViewController().getContentPane(), ResourceBundles
				    .getText("accessories/plugins/EncryptNode.properties_wrong_password"), "Freeplane",
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
		final EnterPasswordDialog pwdDialog = new EnterPasswordDialog(getController().getViewController().getFrame(),
		    true);
		pwdDialog.setModal(true);
		pwdDialog.show();
		if (pwdDialog.getResult() == EnterPasswordDialog.CANCEL) {
			return null;
		}
		final StringBuffer password = pwdDialog.getPassword();
		return password;
	}

	/**
	 * @param e 
	 */
	private void toggleCryptState(final NodeModel node) {
		final ModeController mindMapController = getModeController();
		final EncryptionModel encNode = EncryptionModel.getModel(node);
		if (encNode != null) {
			if (encNode.isAccessible()) {
				encNode.setAccessible(false);
				encNode.getEncryptedContent(mindMapController.getMapController());
				node.setFolded(true);
			}
			else {
				if (doPasswordCheckAndDecryptNode(encNode)) {
					node.setFolded(false);
				}
			}
			final Controller controller = getController();
			final IMapSelection selection = controller.getSelection();
			selection.selectAsTheOnlyOneSelected(node);
		}
		else {
			encrypt(node);
		}
		mindMapController.getMapController().nodeRefresh(node);
	}
	public boolean canBeEnabled() {
		final ModeController modeController = getModeController();
		if (modeController == null) {
			return false;
		}
		boolean isEncryptedNode = false;
		boolean isOpened = false;
		final MapController mapController = modeController.getMapController();
		final NodeModel selectedNode = mapController.getSelectedNode();
		if (selectedNode != null) {
			if(! selectedNode.getMap().isReadOnly()){
				return true;
			}
			final EncryptionModel enode = EncryptionModel.getModel(selectedNode);
			if (enode != null) {
				isEncryptedNode = true;
				isOpened = enode.isAccessible();
			}
		}
		return (isEncryptedNode && !isOpened);
	}
	public void onDeselect(final NodeModel node) {
		setEnabled(false);
	}

	public void onSelect(final NodeModel node) {
		setEnabled(canBeEnabled());
	}
}
