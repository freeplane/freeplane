/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
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
package org.freeplane.features.browsemode;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.JLabel;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.frame.IMapTitleChangeListener;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.ui.IMenuContributor;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.components.PersistentEditableComboBox;


public class BToolbarContributor implements IMenuContributor, IMapTitleChangeListener {
	private static final String BROWSE_URL_STORAGE_KEY = "browse_url_storage";
	final private ModeController modeController;
	private PersistentEditableComboBox urlfield = null;

	public BToolbarContributor(final ModeController controller) {
		modeController = controller;
		urlfield = new PersistentEditableComboBox(BROWSE_URL_STORAGE_KEY);
		urlfield.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				final String urlText = urlfield.getText();
				if ("".equals(urlText) || e.getActionCommand().equals("comboBoxEdited")) {
					return;
				}
				try {
					modeController.getMapController().newMap(new URL(urlText));
				}
				catch (final Exception e1) {
					org.freeplane.core.util.Tools.logException(e1);
					Controller.getController().errorMessage(e1);
				}
			}
		});
	}

	public void setMapTitle(final MapModel model, final String newMapTitle) {
		if (model == null) {
			return;
		}
		if (model.getModeController() != modeController) {
			return;
		}
		final URL url = model.getURL();
		if (url == null) {
			return;
		}
		setURLField(url.toString());
	}

	private void setURLField(final String text) {
		urlfield.setText(text);
	}

	public void updateMenus(final MenuBuilder builder) {
		builder.addComponent("/main_toolbar", new JLabel("URL:"), MenuBuilder.AS_CHILD);
		builder.addComponent("/main_toolbar", urlfield, MenuBuilder.AS_CHILD);
	}
}
