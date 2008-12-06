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
package org.freeplane.controller.views;

import java.awt.Color;
import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serializable;

import javax.swing.JColorChooser;
import javax.swing.JDialog;

public class ColorTracker implements ActionListener, Serializable {
	static class Closer extends WindowAdapter implements Serializable {
		@Override
		public void windowClosing(final WindowEvent e) {
			final Window w = e.getWindow();
			w.hide();
		}
	}

	static class DisposeOnClose extends ComponentAdapter implements
	        Serializable {
		@Override
		public void componentHidden(final ComponentEvent e) {
			final Window w = (Window) e.getComponent();
			w.dispose();
		}
	}

	static JColorChooser colorChooser = new JColorChooser();

	/** Static JColorChooser to have the recent colors feature. */
	static public JColorChooser getCommonJColorChooser() {
		return ColorTracker.colorChooser;
	}

	public static Color showCommonJColorChooserDialog(
	                                                  final Component component,
	                                                  final String title,
	                                                  final Color initialColor)
	        throws HeadlessException {
		final JColorChooser pane = ColorTracker.getCommonJColorChooser();
		pane.setColor(initialColor);
		final ColorTracker ok = new ColorTracker(pane);
		final JDialog dialog = JColorChooser.createDialog(component, title,
		    true, pane, ok, null);
		dialog.addWindowListener(new Closer());
		dialog.addComponentListener(new DisposeOnClose());
		dialog.show();
		return ok.getColor();
	}

	final private JColorChooser chooser;
	private Color color;

	ColorTracker(final JColorChooser c) {
		chooser = c;
	}

	public void actionPerformed(final ActionEvent e) {
		color = chooser.getColor();
	}

	Color getColor() {
		return color;
	}
}
