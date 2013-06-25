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
package org.freeplane.core.ui.components;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

/**
 * @author Dimitry Polivaev
 */
public class JAutoScrollBarPane extends JScrollPane {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 */
	public JAutoScrollBarPane(final Component view) {
		super(view, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	}

	@Override
	public void doLayout() {
		super.doLayout();
		final Insets insets = getInsets();
		final int insetHeight = insets.top + insets.bottom;
		final Dimension prefSize = getViewport().getPreferredSize();
		int height = getHeight() - insetHeight;
		if (getHorizontalScrollBar().isVisible()) {
			height -= getHorizontalScrollBar().getHeight();
		}
		final boolean isVsbNeeded = height < prefSize.height;
		boolean layoutAgain = false;
		if (isVsbNeeded && getVerticalScrollBarPolicy() == ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER) {
			setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			layoutAgain = true;
		}
		else if (!isVsbNeeded && getVerticalScrollBarPolicy() == ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS) {
			setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
			layoutAgain = true;
		}
		if (layoutAgain) {
			super.doLayout();
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					((JComponent) getParent()).revalidate();
				}
			});
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.JComponent#getPreferredSize()
	 */
	@Override
	public Dimension getPreferredSize() {
		if (!isValid()) {
			doLayout();
		}
		return super.getPreferredSize();
	}
}
