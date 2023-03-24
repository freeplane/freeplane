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
package org.freeplane.core.resources.components;

import java.awt.Dialog.ModalityType;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

import org.freeplane.core.ui.components.PopupDialog;
import org.freeplane.core.ui.components.UITools;

import com.jgoodies.forms.builder.DefaultFormBuilder;

public class ButtonPanelProperty extends PropertyBean implements IPropertyControl {

    protected final JButton startButton;
	protected final ButtonSelectorPanel buttons;
	public ButtonPanelProperty(final String name, ButtonSelectorPanel buttons) {
	    super(name);
	    this.buttons = buttons;
	    buttons.setCallback(this::firePropertyChangeEvent);
        startButton = new JButton();
        startButton.setDefaultCapable(false);
        startButton.addActionListener(this::showButtonPanel);
	}

 	@Override
	public String getValue() {
		return buttons.getValue();
	}

	@Override
	public JComponent getValueComponent() {
		return startButton;
	}

	@Override
    public void appendToForm(final DefaultFormBuilder builder) {
	    appendToForm(builder, startButton);
	}

	public Vector<String> getPossibleValues() {
		return buttons.getPossibleValues();
	}

	@Override
    public void setEnabled(final boolean pEnabled) {
	    buttons.setEnabled(pEnabled);
		super.setEnabled(pEnabled);
	}

	@Override
	public void setValue(final String value) {
	    buttons.setValue(value);
		JToggleButton selectedButton = buttons.getSelectedButton();
		startButton.setIcon(selectedButton.getIcon());
		startButton.setToolTipText(selectedButton.getToolTipText());
	}

    private void showButtonPanel(ActionEvent e) {
        Window owner = SwingUtilities.getWindowAncestor(startButton);
        final JDialog dialog = new JDialog(owner, ModalityType.MODELESS);
        dialog.setResizable(false);
        dialog.setUndecorated(true);
        dialog.getRootPane().applyComponentOrientation(owner.getComponentOrientation());
        dialog.getContentPane().add(buttons.getButtonPanel());
        PopupDialog.closeWhenOwnerIsFocused(dialog);
        PopupDialog.closeOnEscape(dialog);
        Point eventLocation = new Point(0, startButton.getHeight());
        SwingUtilities.convertPointToScreen(eventLocation, startButton);
        dialog.pack();
        UITools.setBounds(dialog, eventLocation.x, eventLocation.y,
                dialog.getWidth(), dialog.getHeight());

        JToggleButton selectedButton = buttons.getSelectedButton();
        selectedButton.requestFocusInWindow();
        dialog.setVisible(true);

    }
}
