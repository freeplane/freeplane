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
import org.freeplane.core.undo.IActor;

@ActionLocationDescriptor(locations = { "/menu_bar/extras/first/nodes/crypto" })
public class EnterPassword extends AFreeplaneAction implements INodeSelectionListener {
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
			if (modeController.canEdit()) {
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
			final StringBuilder password = pwdDialog.getPassword();
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
		final StringBuilder password = getUsersPassword();
		if (password == null) {
			return;
		}
		final EncryptionModel encryptedMindMapNode = new EncryptionModel(node);
		encryptedMindMapNode.setEncrypter(new SingleDesEncrypter(password));
		final IActor actor = new IActor() {
			public void act() {
				node.addExtension(encryptedMindMapNode);
				encryptedMindMapNode.updateIcon();
				getModeController().getMapController().nodeChanged(node);
			}

			public String getDescription() {
				return "encrypt";
			}

			public void undo() {
				node.removeExtension(encryptedMindMapNode);
				node.removeStateIcons("decrypted");
				getModeController().getMapController().nodeChanged(node);
			}
		};
		getModeController().execute(actor, node.getMap());
	}

	/**
	 */
	private StringBuilder getUsersPassword() {
		final EnterPasswordDialog pwdDialog = new EnterPasswordDialog(getController().getViewController().getFrame(),
		    true);
		pwdDialog.setModal(true);
		pwdDialog.show();
		if (pwdDialog.getResult() == EnterPasswordDialog.CANCEL) {
			return null;
		}
		final StringBuilder password = pwdDialog.getPassword();
		return password;
	}

	public void onDeselect(final NodeModel node) {
		setEnabled(false);
	}

	public void onSelect(final NodeModel node) {
		setEnabled(canBeEnabled());
	}

	/**
	 * @param e 
	 */
	private void toggleCryptState(final NodeModel node) {
		final ModeController mindMapController = getModeController();
		final EncryptionModel encNode = EncryptionModel.getModel(node);
		if (encNode != null) {
			final boolean wasAccessible = encNode.isAccessible();
			final boolean wasFolded = node.isFolded();
			if (wasAccessible) {
				encNode.setAccessible(false);
				encNode.getEncryptedContent(mindMapController.getMapController());
				node.setFolded(true);
			}
			else {
				if (doPasswordCheckAndDecryptNode(encNode)) {
					node.setFolded(false);
				}
				else {
					return;
				}
			}
			final Controller controller = getController();
			final IMapSelection selection = controller.getSelection();
			selection.selectAsTheOnlyOneSelected(node);
			final IActor actor = new IActor() {
				public void act() {
					encNode.setAccessible(!wasAccessible);
					if (wasAccessible) {
						node.setFolded(true);
					}
					encNode.updateIcon();
					mindMapController.getMapController().nodeRefresh(node);
				}

				public String getDescription() {
					return "toggleCryptState";
				}

				public void undo() {
					encNode.setAccessible(wasAccessible);
					if (wasAccessible) {
						node.setFolded(wasFolded);
					}
					encNode.updateIcon();
					mindMapController.getMapController().nodeRefresh(node);
				}
			};
			getModeController().execute(actor, node.getMap());
		}
		else {
			encrypt(node);
		}
	}
}
