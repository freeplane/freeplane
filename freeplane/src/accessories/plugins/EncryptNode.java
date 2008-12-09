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
package accessories.plugins;

import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.freeplane.controller.Controller;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.map.tree.view.MapView;
import org.freeplane.modes.ModeController;
import org.freeplane.modes.mindmapmode.EncryptionModel;
import org.freeplane.modes.mindmapmode.MModeController;
import org.freeplane.modes.mindmapmode.NodeHookAction;
import org.freeplane.ui.IMenuItemEnabledListener;
import org.freeplane.ui.dialogs.EnterPasswordDialog;

import deprecated.freemind.extensions.IHookRegistration;
import deprecated.freemind.modes.mindmapmode.hooks.MindMapNodeHookAdapter;

/**
 * @author foltin
 */
public class EncryptNode extends MindMapNodeHookAdapter {
	/**
	 * Enables the encrypt/decrypt menu item only if the map/node is encrypted.
	 *
	 * @author foltin
	 */
	public static class Registration implements IHookRegistration,
	        IMenuItemEnabledListener {
		final private ModeController controller;
		private boolean enabled = false;

		public Registration(final ModeController controller) {
			this.controller = controller;
		}

		public void deRegister() {
			enabled = false;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * freemind.controller.MenuItemEnabledListener#isEnabled(javax.swing
		 * .JMenuItem, javax.swing.Action)
		 */
		public boolean isEnabled(final JMenuItem item, final Action action) {
			final String hookName = ((NodeHookAction) action).getHookName();
			if (!enabled) {
				return false;
			}
			boolean isEncryptedNode = false;
			boolean isOpened = false;
			if (controller.getSelectedNode() != null) {
				final EncryptionModel enode = controller.getSelectedNode()
				    .getEncryptionModel();
				if (enode != null) {
					isEncryptedNode = true;
					isOpened = enode.isAccessible();
				}
			}
			if (hookName.equals("accessories/plugins/EnterPassword.properties")) {
				return isEncryptedNode;
			}
			else {
				/*
				 * you can insert an encrypted node, if the current selected
				 * node is not encrypted, or if it is opened.
				 */
				return (!isEncryptedNode || isOpened);
			}
		}

		public void register() {
			enabled = true;
		}
	}

	/**
	 *
	 */
	public EncryptNode() {
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
				        getMindMapController()
				            .getText(
				                "accessories/plugins/EncryptNode.properties_wrong_password"),
				        "Freemind", JOptionPane.ERROR_MESSAGE);
			}
			else {
				return;
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
		getMindMapController();
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

	@Override
	public void invoke(final NodeModel node) {
		super.invoke(node);
		final String actionType = getResourceString("action");
		if (actionType.equals("encrypt")) {
			encrypt(node);
			getController().getMapController().nodeRefresh(node);
			return;
		}
		else if (actionType.equals("toggleCryptState")) {
			toggleCryptState(node);
			getController().getMapController().nodeRefresh(node);
			return;
		}
		else if (actionType.equals("encrypted_map")) {
			newEncryptedMap();
			return;
		}
		else {
			throw new IllegalArgumentException("Unknown action type:"
			        + actionType);
		}
	}

	/**
	 *
	 */
	private void newEncryptedMap() {
		final StringBuffer password = getUsersPassword();
		if (password == null) {
			return;
		}
		final ModeController newModeController = getMindMapController();
		final NodeModel node = new NodeModel(getMindMapController().getText(
		    "accessories/plugins/EncryptNode.properties_select_me"), null);
		final EncryptionModel encryptedMindMapNode = new EncryptionModel(node);
		encryptedMindMapNode.setPassword(password);
		node.addExtension(encryptedMindMapNode);
		newModeController.getMapController().newMap(node);
	}

	/**
	 */
	private void toggleCryptState(final NodeModel node) {
		final MModeController mindMapController = getMindMapController();
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
}
