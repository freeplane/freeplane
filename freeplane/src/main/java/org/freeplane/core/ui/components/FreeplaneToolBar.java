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
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import org.freeplane.core.util.Compat;
import org.freeplane.features.icon.factory.IconFactory;

/**
 * @author Stefan Zechmeister
 */
public class FreeplaneToolBar extends JToolBar {
	private static final int TOOLBAR_BUTTON_SIZE_1D = IconFactory.DEFAULT_UI_ICON_HEIGTH.toBaseUnitsRounded() + 2;
	private static final Dimension DEFAULT_TOOLBAR_BUTTON_SIZE = new Dimension(TOOLBAR_BUTTON_SIZE_1D, TOOLBAR_BUTTON_SIZE_1D);
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
			@Override
			public void ancestorResized(final HierarchyEvent e) {
				revalidate();
				repaint();
			}

			@Override
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
		configureToolbarButton(abstractButton);
	}



	public static void configureToolbarButton(AbstractButton abstractButton) {
		configureToolbarButtonText(abstractButton);
		configureToolbarButtonSize(abstractButton);
	}

	private static void configureToolbarButtonText(final AbstractButton abstractButton) {
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
	}

	private static void configureToolbarButtonSize(final AbstractButton abstractButton) {
		if (Compat.isMacOsX()) {
			abstractButton.putClientProperty("JButton.buttonType", "segmentedGradient");
			abstractButton.putClientProperty("JButton.segmentPosition", "middle");
			final Dimension buttonSize = DEFAULT_TOOLBAR_BUTTON_SIZE;
			abstractButton.setPreferredSize(buttonSize);
			abstractButton.setFocusPainted(false);
		}
		abstractButton.setFocusable(false);
		abstractButton.setMargin(FreeplaneToolBar.nullInsets);
	}
}
