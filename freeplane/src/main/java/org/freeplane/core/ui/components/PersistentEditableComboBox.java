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
package org.freeplane.core.ui.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import org.freeplane.core.resources.ResourceController;

public class PersistentEditableComboBox extends JComboBox {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ActionListener actionListener = null;
	final private String pStorageKey;
	private boolean sendExternalEvents = true;

	public PersistentEditableComboBox(final String storageKey, final int maximumRowCount) {
		pStorageKey = storageKey;
		setEditable(true);
		setMaximumRowCount(maximumRowCount);
		final String storedUrls = ResourceController.getResourceController().getProperty(pStorageKey);
		if (storedUrls != null) {
			final String[] array = storedUrls.split("\t");
			for (int i = 0; i < array.length; i++) {
				if (i == maximumRowCount) {
					break;
				}
				final String string = array[i];
				addUrl(string);
			}
		}
		setSelectedItem("");
		super.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				final String text = getText();
				if (text == null) {
					return;
				}
				addUrl(text);
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

	private void addUrl(final String selectedItem) {
		final DefaultComboBoxModel model = (DefaultComboBoxModel) getModel();
		for (int i = 0; i < model.getSize(); i++) {
			final String element = (String) model.getElementAt(i);
			if (element.equals(selectedItem)) {
				model.removeElementAt(i);
				break;
			}
		}
		model.insertElementAt(selectedItem, 0);
		setSelectedIndex(0);
		final StringBuilder resultBuffer = new StringBuilder();
		for (int i = 0; i < model.getSize(); i++) {
			final String element = (String) model.getElementAt(i);
			resultBuffer.append(element);
			resultBuffer.append("\t");
		}
		ResourceController.getResourceController().setProperty(pStorageKey, resultBuffer.toString());
	};

	public String getText() {
		final Object selectedItem = getSelectedItem();
		return selectedItem == null ? null : selectedItem.toString();
	}

	public void setText(final String text) {
		sendExternalEvents = false;
		addUrl(text);
		sendExternalEvents = true;
	}
}
