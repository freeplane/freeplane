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

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;

import org.freeplane.core.resources.FpStringUtils;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.BlindIcon;

import com.jgoodies.forms.builder.DefaultFormBuilder;

// TODO ARCH rladstaetter 28.02.2009 three way logic tends to be an architecture problem, remove
@Deprecated
public class ThreeCheckBoxProperty extends PropertyBean implements IPropertyControl {
	@Deprecated
	public static final String DON_T_TOUCH_VALUE = "don_t_touch";
	@Deprecated
	protected static final int DON_T_TOUCH_VALUE_INT = 2;
	@Deprecated
	static public final String FALSE_VALUE = "false";
	@Deprecated
	protected static final int FALSE_VALUE_INT = 1;
	private static final ImageIcon MINUS_IMAGE = new ImageIcon(ResourceController.getResourceController().getResource(
	    "/images/edit_remove.png"));
	private static final Icon NO_IMAGE = new BlindIcon(15);
	private static final ImageIcon PLUS_IMAGE = new ImageIcon(ResourceController.getResourceController().getResource(
	    "/images/edit_add.png"));
	@Deprecated
	static public final String TRUE_VALUE = "true";
	@Deprecated
	protected static final int TRUE_VALUE_INT = 0;
	JButton mButton = new JButton();
	protected String mDontTouchValue = "don_t_touch";
	protected String mFalseValue = "false";
	protected String mTrueValue = "true";
	int state = 0;

	/**
	 */
	public ThreeCheckBoxProperty(final String name) {
		super(name);
		mButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				setState((getState() + 1) % 3);
				firePropertyChangeEvent();
			}
		});
	}

	private int getState() {
		return state;
	}

	@Override
	public String getValue() {
		switch (state) {
			case TRUE_VALUE_INT:
				return mTrueValue;
			case FALSE_VALUE_INT:
				return mFalseValue;
			case DON_T_TOUCH_VALUE_INT:
				return mDontTouchValue;
		}
		return null;
	}

	public void layout(final DefaultFormBuilder builder) {
		final JLabel label = builder.append(FpStringUtils.getOptionalText(getLabel()), mButton);
		label.setToolTipText(FpStringUtils.getOptionalText(getDescription()));
	}

	public void setEnabled(final boolean pEnabled) {
		mButton.setEnabled(pEnabled);
	}

	/**
	 *
	 */
	protected void setState(final int newState) {
		state = newState;
		Icon[] icons;
		icons = new Icon[3];
		icons[ThreeCheckBoxProperty.TRUE_VALUE_INT] = ThreeCheckBoxProperty.PLUS_IMAGE;
		icons[ThreeCheckBoxProperty.FALSE_VALUE_INT] = ThreeCheckBoxProperty.MINUS_IMAGE;
		icons[ThreeCheckBoxProperty.DON_T_TOUCH_VALUE_INT] = ThreeCheckBoxProperty.NO_IMAGE;
		mButton.setIcon(icons[state]);
	}

	@Override
	public void setValue(final String value) {
		if (value == null
		        || !(value.toLowerCase().equals(mTrueValue) || value.toLowerCase().equals(mFalseValue) || value
		            .toLowerCase().equals(mDontTouchValue))) {
			throw new IllegalArgumentException("Cannot set a boolean to " + value);
		}
		setState(transformString(value));
	}

	private int transformString(final String string) {
		if (string == null) {
			return ThreeCheckBoxProperty.DON_T_TOUCH_VALUE_INT;
		}
		if (string.toLowerCase().equals(mTrueValue)) {
			return ThreeCheckBoxProperty.TRUE_VALUE_INT;
		}
		if (string.toLowerCase().equals(mFalseValue)) {
			return ThreeCheckBoxProperty.FALSE_VALUE_INT;
		}
		return ThreeCheckBoxProperty.DON_T_TOUCH_VALUE_INT;
	}
}
