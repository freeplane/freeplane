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
package org.freeplane.ui.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;

import org.freeplane.controller.Controller;

public class PersistentEditableComboBox extends JComboBox {
	private ActionListener actionListener = null;
	final private String pStorageKey;
	private boolean sendExternalEvents = true;

	public PersistentEditableComboBox(final String storageKey) {
		pStorageKey = storageKey;
		setEditable(true);
		addUrl("", false);
		final String storedUrls = Controller.getResourceController().getProperty(pStorageKey);
		if (storedUrls != null) {
			final String[] array = storedUrls.split("\t");
			for (int i = 0; i < array.length; i++) {
				final String string = array[i];
				addUrl(string, false);
			}
		}
		setSelectedIndex(0);
		super.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				addUrl(getText(), false);
				if (sendExternalEvents && actionListener != null) {
					actionListener.actionPerformed(arg0);
				}
			}
		});
	}

	@Override
	public void addActionListener(final ActionListener arg0) {
		actionListener = arg0;
	}

	private boolean addUrl(final String selectedItem, final boolean calledFromSetText) {
		for (int i = 0; i < getModel().getSize(); i++) {
			final String element = (String) getModel().getElementAt(i);
			if (element.equals(selectedItem)) {
				if (calledFromSetText) {
					setSelectedIndex(i);
				}
				return false;
			}
		}
		addItem(selectedItem);
		setSelectedIndex(getModel().getSize() - 1);
		if (calledFromSetText) {
			final StringBuffer resultBuffer = new StringBuffer();
			for (int i = 0; i < getModel().getSize(); i++) {
				final String element = (String) getModel().getElementAt(i);
				resultBuffer.append(element);
				resultBuffer.append("\t");
			}
			Controller.getResourceController().setProperty(pStorageKey, resultBuffer.toString());
		}
		return true;
	};

	public String getText() {
		return getSelectedItem().toString();
	}

	public void setText(final String text) {
		sendExternalEvents = false;
		addUrl(text, true);
		sendExternalEvents = true;
	}
}
