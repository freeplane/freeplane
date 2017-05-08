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
package org.freeplane.features.clipboard.mindmapmode;

import java.awt.Component;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.clipboard.ClipboardController;
import org.freeplane.features.clipboard.mindmapmode.MClipboardController.IDataFlavorHandler;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

class SelectedPasteAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SelectedPasteAction() {
		super("SelectedPasteAction");
	}

	public void actionPerformed(final ActionEvent e) {
		final MClipboardController clipboardController = (MClipboardController) ClipboardController
		    .getController();
		final Collection<IDataFlavorHandler> flavorHandlers = clipboardController.getFlavorHandlers();
		if (flavorHandlers.isEmpty()) {
			return;
		}
		final JPanel options = createPane(flavorHandlers);
		if (JOptionPane.CANCEL_OPTION == JOptionPane.showConfirmDialog((Component) e.getSource(), options, 
				(String) getValue(Action.NAME), JOptionPane.OK_CANCEL_OPTION)) {
			return;
		}
		final NodeModel parent = Controller.getCurrentController().getSelection().getSelected();
		final Transferable clipboardContents = clipboardController.getClipboardContents();
		clipboardController.paste(clipboardContents, selectedHandler, parent, false, parent.isNewChildLeft());
		selectedHandler = null;
	}

	private IDataFlavorHandler selectedHandler;

	private JPanel createPane(final Collection<IDataFlavorHandler> flavorHandlers) {
		final ButtonGroup group = new ButtonGroup();
		final JRadioButton[] buttons = new JRadioButton[flavorHandlers.size()];
		int i = 0;
		selectedHandler = null;
		for (final IDataFlavorHandler handler : flavorHandlers) {
			final JRadioButton radioButton = new JRadioButton(TextUtils.getText(handler.getClass().getSimpleName()));
			group.add(radioButton);
			if (selectedHandler == null) {
				selectedHandler = handler;
				radioButton.setSelected(true);
			}
			radioButton.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					selectedHandler = handler;
				}
			});
			buttons[i++] = radioButton;
		}
		return createPane(buttons);
	}

	private JPanel createPane(final JRadioButton[] radioButtons) {
		final int numChoices = radioButtons.length;
		final JPanel box = new JPanel();
		box.setLayout(new BoxLayout(box, BoxLayout.PAGE_AXIS));
		for (int i = 0; i < numChoices; i++) {
			box.add(radioButtons[i]);
		}
		return box;
	}
}
