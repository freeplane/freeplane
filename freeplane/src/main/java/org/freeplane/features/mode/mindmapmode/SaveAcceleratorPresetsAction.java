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

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.IUserInputListenerFactory;
import org.freeplane.core.ui.components.FocusRequestor;
import org.freeplane.core.ui.components.InfoArea;
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
	    final File acceleratorsUserDirectory = LoadAcceleratorPresetsAction.getAcceleratorsUserDirectory();
	    String keyset = inputPresetName(acceleratorsUserDirectory);
		if (keyset == null || keyset.equals("")) {
			return;
		}
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
            final IUserInputListenerFactory userInputListenerFactory = Controller.getCurrentModeController()
                    .getUserInputListenerFactory();
			try (final OutputStream output = new BufferedOutputStream(new FileOutputStream(keysetFile))) {
	            ResourceController.getResourceController().getAcceleratorManager().storeAcceleratorPreset(output);
			}
			userInputListenerFactory.rebuildMenus("load_accelerator_presets");
		}
		catch (final IOException e1) {
			UITools.errorMessage(TextUtils.getText("can_not_save_key_set"));
		}
	}

    private String inputPresetName(final File acceleratorsUserDirectory) {
        JTextArea info= new InfoArea();
        info.setColumns(40);
        info.setLineWrap(true);
        info.setWrapStyleWord(false);
        info.setFont(info.getFont().deriveFont(Font.ITALIC));
        info.setAlignmentX(Component.CENTER_ALIGNMENT);
        info.setText(TextUtils.format("loadHotKeysHelp", acceleratorsUserDirectory.getAbsolutePath()));

        JTextField inputField = new JTextField(40);
        inputField.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        Box dialogComponents = Box.createVerticalBox();
        dialogComponents.add(inputField);
        dialogComponents.add(info);
        
        
        FocusRequestor.requestFocus(inputField);
        int result = JOptionPane.showConfirmDialog(UITools.getMenuComponent(), dialogComponents, 
                TextUtils.getText("enter_keyset_name"), 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        String presetName = result == JOptionPane.OK_OPTION ? inputField.getText() : "";
        return presetName;
    }
}