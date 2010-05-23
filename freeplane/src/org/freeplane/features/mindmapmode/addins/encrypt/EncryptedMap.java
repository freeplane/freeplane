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
package org.freeplane.features.mindmapmode.addins.encrypt;

import java.awt.event.ActionEvent;

import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.EncryptionModel;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ActionLocationDescriptor;
import org.freeplane.core.ui.components.EnterPasswordDialog;
import org.freeplane.features.common.addins.encrypt.SingleDesEncrypter;

@ActionLocationDescriptor(locations = { "/menu_bar/file/open" })
public class EncryptedMap extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public EncryptedMap(final ModeController modeController) {
		super("EncryptedMap", modeController.getController());
	}

	public void actionPerformed(final ActionEvent e) {
		newEncryptedMap();
	}

	/**
	 * @param e 
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

	/**
	 * @param e 
	 *
	 */
	private void newEncryptedMap() {
		final StringBuilder password = getUsersPassword();
		if (password == null) {
			return;
		}
		final ModeController newModeController = getModeController();
		final NodeModel node = new NodeModel(ResourceBundles
		    .getText("accessories/plugins/EncryptNode.properties_select_me"), null);
		final EncryptionModel encryptedMindMapNode = new EncryptionModel(node);
		encryptedMindMapNode.setEncrypter(new SingleDesEncrypter(password));
		node.addExtension(encryptedMindMapNode);
		newModeController.getMapController().newMap(node);
		encryptedMindMapNode.updateIcon();
	}
	
	@Override
	public void afterMapChange(final Object newMap) {
	}
}
