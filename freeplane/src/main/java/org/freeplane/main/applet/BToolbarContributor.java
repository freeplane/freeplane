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
package org.freeplane.main.applet;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.JLabel;

import org.freeplane.core.ui.components.PersistentEditableComboBox;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.IMapViewChangeListener;
import org.freeplane.features.ui.IMapViewManager;

class BToolbarContributor implements EntryVisitor, IMapViewChangeListener {
	private static final String BROWSE_URL_STORAGE_KEY = "browse_url_storage";
// 	final private ModeController modeController;
	private PersistentEditableComboBox urlfield = null;

	public BToolbarContributor() {
		urlfield = new PersistentEditableComboBox(BROWSE_URL_STORAGE_KEY, 20);
		urlfield.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				final String urlText = urlfield.getText();
				if ("".equals(urlText) || e.getActionCommand().equals("comboBoxEdited")) {
					return;
				}
				try {
					Controller.getCurrentModeController().getMapController().newMap(new URL(urlText));
				}
				catch (final Exception e1) {
					LogUtils.warn(e1);
				}
			}
		});
	}

	public void afterViewChange(final Component oldView, final Component newView) {
		if (newView == null) {
			return;
		}
		final IMapViewManager mapViewManager = Controller.getCurrentController().getMapViewManager();
		mapViewManager.getModeController(newView);
		final MapModel map = mapViewManager.getModel(newView);
		final URL url = map.getURL();
		if (url == null) {
			return;
		}
		setURLField(url.toString());
	}

	public void afterViewClose(final Component oldView) {
	}

	public void afterViewCreated(final Component mapView) {
	}

	public void beforeViewChange(final Component oldView, final Component newView) {
	}

	private void setURLField(final String text) {
		urlfield.setText(text);
	}

	@Override
	public void visit(Entry target) {
		final EntryAccessor entryAccessor = new EntryAccessor();
		final Entry label = new Entry();
		target.addChild(label);
		entryAccessor.setComponent(label, new JLabel("URL:"));
		final Entry field = new Entry();
		target.addChild(field);
		entryAccessor.setComponent(field, urlfield);
	}

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		return true;
	}
}
