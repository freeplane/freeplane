/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  this file is modified by Dimitry Polivaev in 2008.
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

import java.awt.Component;
import java.awt.Insets;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

/**
 * @author Stefan Zechmeister
 */
public class FreeplaneToolBar extends JToolBar {
	private static Insets nullInsets = new Insets(0, 0, 0, 0);
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	public FreeplaneToolBar() {
		this("", SwingConstants.HORIZONTAL);
	}

	/**
	 */
	public FreeplaneToolBar(final int arg0) {
		this("", arg0);
	}

	/**
	 */
	public FreeplaneToolBar(final String arg0) {
		this(arg0, SwingConstants.HORIZONTAL);
	}

	/**
	 */
	public FreeplaneToolBar(final String arg0, final int arg1) {
		super(arg0, arg1);
		this.setMargin(FreeplaneToolBar.nullInsets);
		setFloatable(false);
		setRollover(true);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JToolBar#add(javax.swing.Action)
	 */
	@Override
	public JButton add(final Action arg0) {
		final String actionName = (String) arg0.getValue(Action.NAME);
		arg0.putValue(Action.SHORT_DESCRIPTION, actionName);
		final JButton returnValue = super.add(arg0);
		returnValue.setName(actionName);
		returnValue.setText("");
		return returnValue;
	}

	@Override
	public Component add(final Component comp) {
		super.add(comp);
		configureComponent(comp);
		return comp;
	}

	@Override
	public Component add(final Component comp, final int index) {
		super.add(comp, index);
		configureComponent(comp);
		return comp;
	}

	@Override
	public void add(final Component comp, final Object constraints) {
		super.add(comp, constraints);
		configureComponent(comp);
	}

	@Override
	public void add(final Component comp, final Object constraints, final int index) {
		super.add(comp, constraints, index);
		configureComponent(comp);
	}

	@Override
	public Component add(final String name, final Component comp) {
		super.add(name, comp);
		configureComponent(comp);
		return comp;
	}

	private void configureComponent(final Component comp) {
		comp.setFocusable(false);
		if (!(comp instanceof AbstractButton)) {
			return;
		}
		final AbstractButton abstractButton = (AbstractButton) comp;
		abstractButton.setMargin(FreeplaneToolBar.nullInsets);
		if (null != abstractButton.getIcon()){
			String text = abstractButton.getText();
			String toolTipText = abstractButton.getToolTipText();
			if(text != null){
				if(toolTipText == null){
					abstractButton.setToolTipText(text);
				}
				abstractButton.setText(null);
			}
		}
		if (System.getProperty("os.name").startsWith("Mac OS")) {
			abstractButton.setBorderPainted(false);
		}
		abstractButton.setContentAreaFilled(false);
	}
}
