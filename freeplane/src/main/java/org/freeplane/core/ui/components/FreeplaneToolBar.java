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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.Compat;
import org.freeplane.features.icon.factory.IconFactory;

/**
 * @author Stefan Zechmeister
 */
public class FreeplaneToolBar extends JToolBar {
	private static final int TOOLBAR_BUTTON_SIZE_1D = IconFactory.DEFAULT_UI_ICON_HEIGTH.toBaseUnitsRounded() + 2;
	private static final Dimension DEFAULT_TOOLBAR_BUTTON_SIZE = new Dimension(TOOLBAR_BUTTON_SIZE_1D, TOOLBAR_BUTTON_SIZE_1D);
	protected static Insets nullInsets = new Insets(0, 0, 0, 0);
	
	private static final GridBagConstraints separatorConstraints = new GridBagConstraints();
	
	static {
		separatorConstraints.gridy = 0;
		separatorConstraints.fill = GridBagConstraints.VERTICAL;
		separatorConstraints.gridheight = GridBagConstraints.REMAINDER;
	}

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
		if(orientation == SwingConstants.HORIZONTAL) {
			GridBagLayout gridBagLayout = new GridBagLayout();
			super.setLayout(gridBagLayout);
		} else
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
	
	

	@Override
	public void addSeparator() {
		if(getOrientation() == SwingConstants.VERTICAL) {
			super.addSeparator();
		}
		else {
			JToolBar.Separator s = new JToolBar.Separator();
			add(s, separatorConstraints);
		}
	}



	protected void configureComponent(final Component comp) {
		if (!(comp instanceof AbstractButton)) {
			return;
		}
		final AbstractButton abstractButton = (AbstractButton) comp;
		configureToolbarButton(abstractButton);
	}



	public static AbstractButton createButton(AFreeplaneAction action) {
		AbstractButton button = new JButton(action);
		if (action.isSelectable()) {
			button = new JAutoToggleButton(action);
		}
		else {
			button = new JButton(action);
		}
	    configureToolbarButton(button);
	    return button;
	}



	public static void configureToolbarButton(AbstractButton abstractButton) {
		configureToolbarButtonText(abstractButton);
		configureToolbarButtonSize(abstractButton);
	}

	private static void configureToolbarButtonText(final AbstractButton abstractButton) {
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



	public static AbstractButton createButton(AFreeplaneAction action,
			ButtonModel hideMatchingNodes) {
		JAutoToggleButton button = new JAutoToggleButton(action, hideMatchingNodes);
		configureToolbarButton(button);
	    return button;
	}
}
