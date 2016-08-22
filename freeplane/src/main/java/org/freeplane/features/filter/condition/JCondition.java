/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
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
package org.freeplane.features.filter.condition;

import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicPanelUI;

/**
 * @author Dimitry Polivaev
 */
public class JCondition extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 */
	public JCondition() {
		super();
		setUI(BasicPanelUI.createUI(this));
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
	}

	@Override
	public Component add(Component comp, int index) {
		comp.setForeground(null);
		comp.setBackground(null);
		return super.add(comp, index);
	}

	@Override
	public Component add(Component comp) {
		comp.setForeground(null);
		comp.setBackground(null);
		return super.add(comp);
	}
}
