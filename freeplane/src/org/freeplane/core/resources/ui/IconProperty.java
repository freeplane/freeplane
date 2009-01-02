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
package org.freeplane.core.resources.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.map.MindIcon;
import org.freeplane.core.ui.components.IconSelectionPopupDialog;

import com.jgoodies.forms.builder.DefaultFormBuilder;

public class IconProperty extends PropertyBean implements IPropertyControl, ActionListener {
	private MindIcon mActualIcon = null;
	JButton mButton;
	/**
	 * Of IconInformation s.
	 */
	final private Vector mIcons;

	public IconProperty(final String name, final Vector icons) {
		super(name);
		mIcons = icons;
		mButton = new JButton();
		mButton.addActionListener(this);
	}

	public void actionPerformed(final ActionEvent arg0) {
		final Vector icons = new Vector();
		final Vector descriptions = new Vector();
		for (final Iterator iter = mIcons.iterator(); iter.hasNext();) {
			final MindIcon icon = (MindIcon) iter.next();
			icons.add(icon);
			descriptions.add(icon.getDescription());
		}
		final IconSelectionPopupDialog dialog = new IconSelectionPopupDialog(Controller
		    .getController().getViewController().getJFrame(), icons);
		dialog.setLocationRelativeTo(Controller.getController().getViewController().getJFrame());
		dialog.setModal(true);
		dialog.setVisible(true);
		final int result = dialog.getResult();
		if (result >= 0) {
			final MindIcon icon = (MindIcon) mIcons.get(result);
			setValue(icon.getName());
			firePropertyChangeEvent();
		}
	}

	@Override
	public String getValue() {
		return mActualIcon.getName();
	}

	public void layout(final DefaultFormBuilder builder) {
		final JLabel label = builder.append(OptionString.getText(getLabel()), mButton);
		label.setToolTipText(OptionString.getText(getDescription()));
	}

	public void setEnabled(final boolean pEnabled) {
		mButton.setEnabled(pEnabled);
	}

	private void setIcon(final MindIcon actualIcon) {
		mButton.setIcon(actualIcon.getIcon());
		mButton.setToolTipText(actualIcon.getDescription());
	}

	@Override
	public void setValue(final String value) {
		for (final Iterator iter = mIcons.iterator(); iter.hasNext();) {
			final MindIcon icon = (MindIcon) iter.next();
			if (icon.getName().equals(value)) {
				mActualIcon = icon;
				setIcon(mActualIcon);
			}
		}
	}
}
