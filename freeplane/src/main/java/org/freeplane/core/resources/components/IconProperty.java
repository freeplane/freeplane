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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import org.freeplane.core.ui.components.IconSelectionPopupDialog;
import org.freeplane.features.icon.MindIcon;

import com.jgoodies.forms.builder.DefaultFormBuilder;

public class IconProperty extends PropertyBean implements IPropertyControl, ActionListener {
	private MindIcon mActualIcon;
	private final JButton mButton;
	/**
	 * Of IconInformation s.
	 */
	private final List<MindIcon> mIcons;

	public IconProperty(final String name, final List<MindIcon> icons) {
		super(name);
		mIcons = icons;
		mButton = new JButton();
		mButton.addActionListener(this);
	}

	public void actionPerformed(final ActionEvent e) {
		final List<MindIcon> icons = new ArrayList<MindIcon>();
		final List<String> descriptions = new ArrayList<String>();
		for (final MindIcon icon : mIcons) {
			icons.add(icon);
			descriptions.add(icon.getTranslatedDescription());
		}
		final IconSelectionPopupDialog dialog = new IconSelectionPopupDialog(JOptionPane
		    .getFrameForComponent((Component) e.getSource()), icons);
		dialog.setLocationRelativeTo(JOptionPane.getFrameForComponent((Component) e.getSource()));
		dialog.setModal(true);
		dialog.setVisible(true);
		final int result = dialog.getResult();
		if (result >= 0) {
			final MindIcon icon = mIcons.get(result);
			setValue(icon.getName());
			firePropertyChangeEvent();
		}
	}

	@Override
	public String getValue() {
		return mActualIcon.getName();
	}

	public void appendToForm(final DefaultFormBuilder builder) {
		appendToForm(builder, mButton);
	}

	public void setEnabled(final boolean pEnabled) {
		mButton.setEnabled(pEnabled);
	}

	private void setIcon(final MindIcon actualIcon) {
		mButton.setIcon(actualIcon.getIcon());
		mButton.setToolTipText(actualIcon.getTranslatedDescription());
	}

	@Override
	public void setValue(final String value) {
		for (final MindIcon icon : mIcons) {
			if (icon.getName().equals(value)) {
				mActualIcon = icon;
				setIcon(mActualIcon);
				return;
			}
		}
		throw new NoSuchElementException();
	}

	public MindIcon getIcon() {
		return mActualIcon;
	}
}
