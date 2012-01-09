/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2011 dimitry
 *
 *  This file author is dimitry
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
package org.freeplane.features.encrypt;

import javax.swing.JOptionPane;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.ui.components.EnterPasswordDialog;
import org.freeplane.core.undo.IActor;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.icon.IStateIconProvider;
import org.freeplane.features.icon.IconController;
import org.freeplane.features.icon.IconStore;
import org.freeplane.features.icon.UIIcon;
import org.freeplane.features.icon.factory.IconStoreFactory;
import org.freeplane.features.map.EncryptionModel;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

/**
 * @author Dimitry Polivaev
 * Feb 13, 2011
 */
public class EncryptionController implements IExtension {
	private static final IconStore STORE = IconStoreFactory.create();
	private static UIIcon decryptedIcon = STORE.getUIIcon("unlock.png");
	private static UIIcon encryptedIcon = STORE.getUIIcon("lock.png");
	
	public static void install(EncryptionController encryptionController){
		final ModeController modeController = Controller.getCurrentModeController();
		modeController.addExtension(EncryptionController.class, encryptionController);
		final EnterPassword pwdAction = new EnterPassword(encryptionController);
		modeController.addAction(pwdAction);
	}
	
	
	public EncryptionController(final ModeController modeController) {
		registerStateIconProvider(modeController);
    }


	private void registerStateIconProvider(final ModeController modeController) {
	    IconController.getController(modeController).addStateIconProvider(new IStateIconProvider() {
			public UIIcon getStateIcon(NodeModel node) {
				final EncryptionModel encNode = EncryptionModel.getModel(node);
				if (encNode != null) {
					if(encNode.isAccessible())
						return decryptedIcon;
					else
						return encryptedIcon;
				}
				return null;
			}
		});
    }
	/**
	 * @param e 
	 */
	public void toggleCryptState(final NodeModel node) {
		final ModeController mindMapController = Controller.getCurrentModeController();
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
			final Controller controller = Controller.getCurrentController();
			final IMapSelection selection = controller.getSelection();
			selection.selectAsTheOnlyOneSelected(node);
			final IActor actor = new IActor() {
				public void act() {
					encNode.setAccessible(!wasAccessible);
					if (wasAccessible) {
						node.setFolded(true);
					}
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
					mindMapController.getMapController().nodeRefresh(node);
				}
			};
			Controller.getCurrentModeController().execute(actor, node.getMap());
		}
		else {
			encrypt(node);
		}
	}
	/**
	 * @param e 
	 */
	private boolean doPasswordCheckAndDecryptNode(final EncryptionModel encNode) {
		while (true) {
			final EnterPasswordDialog pwdDialog = new EnterPasswordDialog(Controller.getCurrentController().getViewController()
			    .getFrame(), false);
			pwdDialog.setModal(true);
			pwdDialog.setVisible(true);
			if (pwdDialog.getResult() == EnterPasswordDialog.CANCEL) {
				return false;
			}
			final StringBuilder password = pwdDialog.getPassword();
			if (!encNode.decrypt(Controller.getCurrentModeController().getMapController(), new SingleDesEncrypter(password))) {
				final Controller controller = Controller.getCurrentController();
				JOptionPane.showMessageDialog(controller.getViewController().getContentPane(), TextUtils
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
				Controller.getCurrentModeController().getMapController().nodeChanged(node);
			}

			public String getDescription() {
				return "encrypt";
			}

			public void undo() {
				node.removeExtension(encryptedMindMapNode);
				Controller.getCurrentModeController().getMapController().nodeChanged(node);
			}
		};
		Controller.getCurrentModeController().execute(actor, node.getMap());
	}

	/**
	 */
	private StringBuilder getUsersPassword() {
		final EnterPasswordDialog pwdDialog = new EnterPasswordDialog(Controller.getCurrentController().getViewController().getFrame(),
		    true);
		pwdDialog.setModal(true);
		pwdDialog.show();
		if (pwdDialog.getResult() == EnterPasswordDialog.CANCEL) {
			return null;
		}
		final StringBuilder password = pwdDialog.getPassword();
		return password;
	}
	
}
