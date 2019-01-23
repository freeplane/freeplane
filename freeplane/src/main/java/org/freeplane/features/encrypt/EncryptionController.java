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
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.undo.IActor;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.icon.IStateIconProvider;
import org.freeplane.features.icon.IconController;
import org.freeplane.features.icon.IconStore;
import org.freeplane.features.icon.UIIcon;
import org.freeplane.features.icon.factory.IconStoreFactory;
import org.freeplane.features.map.EncryptionModel;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapWriter;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

/**
 * @author Dimitry Polivaev
 * Feb 13, 2011
 */
public class EncryptionController implements IExtension {
	private static final IconStore STORE = IconStoreFactory.ICON_STORE;
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
			@Override
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

			@Override
			public boolean mustIncludeInIconRegistry() {
				return true;
			}
		});
    }

	public void toggleLock(final NodeModel node, PasswordStrategy passwordStrategy) {
		final EncryptionModel encryptionModel = EncryptionModel.getModel(node);
		if (encryptionModel != null) {
			final boolean wasFolded = node.isFolded();
			final boolean wasAccessible = encryptionModel.isAccessible();
			if (!wasAccessible && !doPasswordCheckAndDecryptNode(node, encryptionModel, passwordStrategy))
					return;
			final boolean becomesFolded = wasAccessible;
			final boolean becomesAccessible = ! wasAccessible;
			Controller.getCurrentController().getSelection().selectAsTheOnlyOneSelected(node);
			final MapWriter mapWriter = Controller.getCurrentModeController().getMapController().getMapWriter();
			final IActor actor = new IActor() {
				@Override
				public void act() {
					if(becomesAccessible) {
						encryptionModel.unlock();
					}
					else {
						encryptionModel.lock(mapWriter);
					}
					if (becomesFolded != wasFolded) {
						node.setFolded(becomesFolded);
					}
					fireEncryptionChangedEvent(node);
				}

				@Override
				public String getDescription() {
					return "toggleCryptState";
				}

				@Override
				public void undo() {
					if(wasAccessible) {
						encryptionModel.unlock();
					}
					else {
						encryptionModel.lock(mapWriter);
					}
					if(becomesFolded != wasFolded)
						node.setFolded(wasFolded);
					fireEncryptionChangedEvent(node);
				}
			};
			Controller.getCurrentModeController().execute(actor, node.getMap());
		}
		else {
			encrypt(node, passwordStrategy);
		}
	}

	private boolean doPasswordCheckAndDecryptNode(NodeModel node, final EncryptionModel encryptionModel, PasswordStrategy passwordStrategy) {
		final StringBuilder password = passwordStrategy.getPassword(node);
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

    private boolean decrypt(final EncryptionModel encryptionModel, final StringBuilder password) {
        final MapController mapController = Controller.getCurrentModeController().getMapController();
        return encryptionModel.decrypt(mapController, new SingleDesEncrypter(password));
    }

	private void encrypt(final NodeModel node, PasswordStrategy passwordStrategy) {
		if(node.allClones().size() > 1) {
			UITools.errorMessage(TextUtils.getText("can_not_encrypt_cloned_node"));
			return;
		}

		final StringBuilder password = passwordStrategy.getPasswordWithConfirmation(node);
		if (passwordStrategy.isCancelled()) {
			return;
		}
		final EncryptionModel encryptionModel = new EncryptionModel(node, new SingleDesEncrypter(password));
		final IActor actor = new IActor() {
			@Override
			public void act() {
				node.addExtension(encryptionModel);
				fireEncryptionChangedEvent(node);
			}

			@Override
			public String getDescription() {
				return "encrypt";
			}

			@Override
			public void undo() {
				node.removeExtension(encryptionModel);
				fireEncryptionChangedEvent(node);
			}
		};
		Controller.getCurrentModeController().execute(actor, node.getMap());
	}


	private void fireEncryptionChangedEvent(final NodeModel node) {
		Controller.getCurrentModeController().getMapController().nodeRefresh(node, EncryptionModel.class, null, null);
	}
}
