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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

/**
 * @author Stefan Zechmeister
 */
public class FreeplaneToolBar extends JToolBar {
	protected static Insets nullInsets = new Insets(0, 0, 0, 0);
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 */
	public FreeplaneToolBar(final String name, final int orientation) {
		super(name, orientation);
		this.setMargin(FreeplaneToolBar.nullInsets);
		setFloatable(false);
		setRollover(true);
		if(orientation == SwingConstants.HORIZONTAL)
			super.setLayout(ToolbarLayout.horizontal());
		else
			super.setLayout(ToolbarLayout.vertical());
		addHierarchyBoundsListener(new HierarchyBoundsListener() {
			public void ancestorResized(final HierarchyEvent e) {
				revalidate();
				repaint();
			}

			public void ancestorMoved(final HierarchyEvent e) {
			}
		});
	}
	
	

	@Override
    public void setLayout(LayoutManager mgr) {
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

	protected void configureComponent(final Component comp) {
		if (!(comp instanceof AbstractButton)) {
			return;
		}
		final AbstractButton abstractButton = (AbstractButton) comp;
		final String actionName = (String) abstractButton.getAction().getValue(Action.NAME);
		abstractButton.setName(actionName);
		if (null != abstractButton.getIcon()) {
			final String text = abstractButton.getText();
			final String toolTipText = abstractButton.getToolTipText();
			if (text != null) {
				if (toolTipText == null) {
					abstractButton.setToolTipText(text);
				}
				abstractButton.setText(null);
			}
		}
		if (System.getProperty("os.name").equals("Mac OS X")) {
			abstractButton.putClientProperty("JButton.buttonType", "segmented");
			abstractButton.putClientProperty("JButton.segmentPosition", "middle");
			final Dimension buttonSize = new Dimension(22, 22);
			abstractButton.setPreferredSize(buttonSize);
			abstractButton.setFocusPainted(false);
		}
		abstractButton.setFocusable(false);
		abstractButton.setMargin(FreeplaneToolBar.nullInsets);
	}
}
