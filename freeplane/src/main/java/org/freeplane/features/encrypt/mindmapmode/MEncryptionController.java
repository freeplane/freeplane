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
package org.freeplane.features.encrypt.mindmapmode;

import org.freeplane.core.undo.IActor;
import org.freeplane.features.encrypt.EncryptionController;
import org.freeplane.features.encrypt.PasswordStrategy;
import org.freeplane.features.map.EncryptionModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

/**
 * @author Dimitry Polivaev
 * Feb 13, 2011
 */
public class MEncryptionController extends EncryptionController {
	public MEncryptionController(ModeController modeController) {
	    super(modeController);
    }

	public static void install(MEncryptionController encryptionController){
		EncryptionController.install(encryptionController);
		final ModeController modeController = Controller.getCurrentModeController();
		final RemoveEncryption removeEncryptionAction = new RemoveEncryption(encryptionController);
		modeController.addAction(removeEncryptionAction);
		final EncryptedMap encryptedMapAction = new EncryptedMap();
		modeController.addAction(encryptedMapAction);
	}
	
	public void removeEncryption(final NodeModel node, final PasswordStrategy passwordStrategy) {
		final EncryptionModel encryptedMindMapNode = EncryptionModel.getModel(node);
		if (encryptedMindMapNode == null) {
			return;
		}
		if(! encryptedMindMapNode.isAccessible())
			toggleLock(node, passwordStrategy);
		if(! encryptedMindMapNode.isAccessible())
			return;
		final IActor actor = new IActor() {
			@Override
			public boolean isReadonly() {
				return true;
			}
			public void act() {
				node.removeExtension(encryptedMindMapNode);
				Controller.getCurrentModeController().getMapController().nodeChanged(node);
			}

			public String getDescription() {
				return "removeEncryption";
			}

			public void undo() {
				node.addExtension(encryptedMindMapNode);
				Controller.getCurrentModeController().getMapController().nodeChanged(node);
			}
		};
		Controller.getCurrentModeController().execute(actor, node.getMap());
		
    }

}
