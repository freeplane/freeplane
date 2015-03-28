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

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.undo.IActor;
import org.freeplane.features.icon.IStateIconProvider;
import org.freeplane.features.icon.IconController;
import org.freeplane.features.icon.IconStore;
import org.freeplane.features.icon.UIIcon;
import org.freeplane.features.icon.factory.IconStoreFactory;
import org.freeplane.features.map.EncryptionModel;
import org.freeplane.features.map.MapController;
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
				final EncryptionModel encryptionModel = EncryptionModel.getModel(node);
				if (encryptionModel != null) {
					if(encryptionModel.isAccessible())
						return decryptedIcon;
					else
						return encryptedIcon;
				}
				return null;
			}
		});
    }

	public void toggleCryptState(final NodeModel node, PasswordStrategy passwordStrategy) {
		final EncryptionModel encryptionModel = EncryptionModel.getModel(node);
		if (encryptionModel != null) {
			final boolean wasAccessible = encryptionModel.isAccessible();
			final boolean wasFolded = node.isFolded();
			if (wasAccessible) {
				encryptionModel.setAccessible(false);
				encryptionModel.getEncryptedContent(Controller.getCurrentModeController().getMapController());
				node.setFolded(true);
			}
			else {
				if (doPasswordCheckAndDecryptNode(encryptionModel, passwordStrategy)) {
					node.setFolded(false);
				}
				else {
					return;
				}
			}
			Controller.getCurrentController().getSelection().selectAsTheOnlyOneSelected(node);
			final IActor actor = new IActor() {
				public void act() {
					encryptionModel.setAccessible(!wasAccessible);
					if (wasAccessible) {
						node.setFolded(true);
					}
					Controller.getCurrentModeController().getMapController().nodeRefresh(node);
				}

				public String getDescription() {
					return "toggleCryptState";
				}

				public void undo() {
					encryptionModel.setAccessible(wasAccessible);
					if (wasAccessible) {
						node.setFolded(wasFolded);
					}
					Controller.getCurrentModeController().getMapController().nodeRefresh(node);
				}
			};
			Controller.getCurrentModeController().execute(actor, node.getMap());
		}
		else {
			encrypt(node, passwordStrategy);
		}
	}

	private boolean doPasswordCheckAndDecryptNode(final EncryptionModel encryptionModel, PasswordStrategy passwordStrategy) {
		while (true) {
			final StringBuilder password = passwordStrategy.getPassword();
			if (passwordStrategy.isCancelled())
			    return false;
			if (!decrypt(encryptionModel, password)) {
				passwordStrategy.onWrongPassword();
				return false;
			}
			else {
				return true;
			}
		}
	}

    private boolean decrypt(final EncryptionModel encryptionModel, final StringBuilder password) {
        final MapController mapController = Controller.getCurrentModeController().getMapController();
        return encryptionModel.decrypt(mapController, new SingleDesEncrypter(password));
    }

	private void encrypt(final NodeModel node, PasswordStrategy passwordStrategy) {
		final StringBuilder password = passwordStrategy.getPasswordWithConfirmation();
		if (passwordStrategy.isCancelled()) {
			return;
		}
		final EncryptionModel encryptionModel = new EncryptionModel(node);
		encryptionModel.setEncrypter(new SingleDesEncrypter(password));
		final IActor actor = new IActor() {
			public void act() {
				node.addExtension(encryptionModel);
				Controller.getCurrentModeController().getMapController().nodeChanged(node);
			}

			public String getDescription() {
				return "encrypt";
			}

			public void undo() {
				node.removeExtension(encryptionModel);
				Controller.getCurrentModeController().getMapController().nodeChanged(node);
			}
		};
		Controller.getCurrentModeController().execute(actor, node.getMap());
	}
}
