/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2012 Dimitry
 *
 *  This file author is Dimitry
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
package org.freeplane.features.mode.mindmapmode;

import java.awt.event.ActionEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.swing.JOptionPane;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.IUserInputListenerFactory;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;

/**
 * @author Dimitry Polivaev
 * 01.05.2012
 */
class SaveAcceleratorPresetsAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SaveAcceleratorPresetsAction() {
		super("SaveAcceleratorPresetsAction");
	}

	public void actionPerformed(final ActionEvent e) {
		final String keyset = JOptionPane.showInputDialog(TextUtils.getText("enter_keyset_name"));
		if (keyset == null || keyset.equals("")) {
			return;
		}
		final File acceleratorsUserDirectory = LoadAcceleratorPresetsAction.getAcceleratorsUserDirectory();
		final File keysetFile = new File(acceleratorsUserDirectory, keyset + ".properties");
		if (keysetFile.exists()) {
			final int confirm = JOptionPane.showConfirmDialog(UITools.getMenuComponent(), TextUtils
			    .getText("overwrite_keyset_question"), "Freeplane", JOptionPane.YES_NO_OPTION);
			if (confirm != JOptionPane.YES_OPTION) {
				return;
			}
		}
		try {
			acceleratorsUserDirectory.mkdirs();
			final OutputStream output = new BufferedOutputStream(new FileOutputStream(keysetFile));
			final IUserInputListenerFactory userInputListenerFactory = Controller.getCurrentModeController()
					.getUserInputListenerFactory();
			ResourceController.getResourceController().getAcceleratorManager().storeAcceleratorPreset(output);
			output.close();
			userInputListenerFactory.rebuildMenus("load_accelerator_presets");
		}
		catch (final IOException e1) {
			UITools.errorMessage(TextUtils.getText("can_not_save_key_set"));
		}
	}
}