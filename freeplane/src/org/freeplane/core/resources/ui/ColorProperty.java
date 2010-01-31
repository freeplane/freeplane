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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.plaf.basic.BasicButtonUI;

import org.freeplane.core.frame.ColorTracker;
import org.freeplane.core.resources.FpStringUtils;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.ColorUtils;

import com.jgoodies.forms.builder.DefaultFormBuilder;

// TODO rladstaetter 28.02.2009 remove: a container for color properties is not necessary. if really need be, one can use a generic container (see Triple<A,B,C>); consider using FpColor
@Deprecated
public class ColorProperty extends PropertyBean implements IPropertyControl, ActionListener {
	Color color;
	final private String defaultColor;
	JButton mButton;
	final JPopupMenu menu = new JPopupMenu();

	/**
	 * @param defaultColor
	 * @param pTranslator
	 */
	public ColorProperty(final String name, final String defaultColor) {
		super(name);
		this.defaultColor = defaultColor;
		mButton = new JButton() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			{
				setUI(BasicButtonUI.createUI(this));
			}
		};
		mButton.addActionListener(this);
		color = Color.BLACK;
	}

	public void actionPerformed(final ActionEvent arg0) {
		final Color result = ColorTracker.showCommonJColorChooserDialog(mButton.getRootPane(), FpStringUtils
		    .getOptionalText(getLabel()), getColorValue());
		setColorValue(result);
		firePropertyChangeEvent();
	}

	/**
	 */
	private Color getColorValue() {
		return color;
	}

	@Override
	public String getValue() {
		return ColorUtils.colorToString(getColorValue());
	}

	public void layout(final DefaultFormBuilder builder) {
		final JLabel label = builder.append(FpStringUtils.getOptionalText(getLabel()), mButton);
		label.setToolTipText(FpStringUtils.getOptionalText(getDescription()));
		final JMenuItem item = new JMenuItem(FpStringUtils.getOptionalText("ColorProperty.ResetColor"));
		item.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				setValue(defaultColor);
			}
		});
		menu.add(item);
		mButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(final MouseEvent evt) {
				if (evt.isPopupTrigger()) {
					menu.show(evt.getComponent(), evt.getX(), evt.getY());
				}
			}

			@Override
			public void mouseReleased(final MouseEvent evt) {
				if (evt.isPopupTrigger()) {
					menu.show(evt.getComponent(), evt.getX(), evt.getY());
				}
			}
		});
	}

	/**
	 */
	private void setColorValue(Color input) {
		color = input;
		if (input == null) {
			input = Color.WHITE;
		}
		mButton.setBackground(input);
		Color textColor = UITools.getTextColorForBackground(input);
		mButton.setForeground(textColor);
		mButton.setText(ColorUtils.colorToString(input));
	}

	public void setEnabled(final boolean pEnabled) {
		mButton.setEnabled(pEnabled);
	}

	@Override
	public void setValue(final String value) {
		setColorValue(ColorUtils.stringToColor(value));
	}
}
