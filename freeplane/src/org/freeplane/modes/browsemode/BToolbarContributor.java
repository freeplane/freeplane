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
package org.freeplane.modes.browsemode;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.JLabel;

import org.freeplane.controller.Controller;
import org.freeplane.controller.views.IMapTitleChangeListener;
import org.freeplane.map.tree.MapModel;
import org.freeplane.map.tree.view.MapView;
import org.freeplane.modes.IMenuContributor;
import org.freeplane.modes.ModeController;
import org.freeplane.ui.MenuBuilder;
import org.freeplane.ui.components.PersistentEditableComboBox;

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
					org.freeplane.main.Tools.logException(e1);
					Controller.getController().errorMessage(e1);
				}
			}
		});
	}

	public void setMapTitle(final String newMapTitle, final MapView mapView, final MapModel model) {
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
