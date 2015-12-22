/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file author is to be reworked.
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

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;

public class VariableSizeCardLayout extends CardLayout {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public VariableSizeCardLayout() {
		super();
	}

	public VariableSizeCardLayout(final int hgap, final int vgap) {
		super(hgap, vgap);
	}

	/**
	 * Determines the preferred size of the container argument using this card
	 * layout.
	 *
	 * @param parent
	 *            the parent container in which to do the layout
	 * @return the preferred dimensions to lay out the subcomponents of the
	 *         specified container
	 * @see java.awt.Container#getPreferredSize
	 * @see java.awt.CardLayout#minimumLayoutSize
	 */
	@Override
	public Dimension preferredLayoutSize(final Container parent) {
		synchronized (parent.getTreeLock()) {
			final Insets insets = parent.getInsets();
			final int ncomponents = parent.getComponentCount();
			int w = 0;
			int h = 0;
			for (int i = 0; i < ncomponents; i++) {
				final Component comp = parent.getComponent(i);
				if (comp.isVisible()) {
					final Dimension d = comp.getPreferredSize();
					if (d.width > w) {
						w = d.width;
					}
					if (d.height > h) {
						h = d.height;
					}
				}
			}
			return new Dimension(insets.left + insets.right + w + getHgap() * 2, insets.top + insets.bottom + h
			        + getVgap() * 2);
		}
	}
}
