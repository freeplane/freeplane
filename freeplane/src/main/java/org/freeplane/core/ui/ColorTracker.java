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
package org.freeplane.core.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.HeadlessException;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serializable;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

public class ColorTracker implements ActionListener, Serializable {
	static class Closer extends WindowAdapter implements Serializable {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void windowClosing(final WindowEvent e) {
			e.getWindow().setVisible(false);
			e.getWindow().dispose();
		}
	}

	static class DisposeOnClose extends ComponentAdapter implements Serializable {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void componentHidden(final ComponentEvent e) {
			final Window w = (Window) e.getComponent();
			w.dispose();
		}
	}

	static JColorChooser colorChooser = new JColorChooser();
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/** Static JColorChooser to have the recent color^^s feature. */
	static public JColorChooser getCommonJColorChooser() {
		return ColorTracker.colorChooser;
	}

	public static Color showCommonJColorChooserDialog(final Component component, final String title,
	                                                  final Color initialColor, final Color defaultColor) {
		final JColorChooser pane = ColorTracker.getCommonJColorChooser();
		pane.setColor(initialColor != null ? initialColor : (defaultColor != null ? defaultColor : Color.WHITE));
		final ColorTracker ok = new ColorTracker(pane);
		final JDialog dialog = JColorChooser.createDialog(component, title, true, pane, ok, null);
		final Container container = (Container) dialog.getContentPane().getComponent(1);
		if(defaultColor != null){
			final JButton defaultBtn = new JButton(TextUtils.getText("reset_to_default"));
			defaultBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					dialog.dispose();
					ok.setColor(defaultColor);
				}
			});
			container.add(defaultBtn);
		}
		dialog.addWindowListener(new Closer());
		dialog.addComponentListener(new DisposeOnClose());
		dialog.pack();
		UITools.setDialogLocationRelativeTo(dialog, component);
		dialog.setVisible(true);
		return ok.getColor();
	}

	public static Color showCommonJColorChooserDialog( final NodeModel nodeModel,
	                                                  final String title, final Color initialColor, Color defaultColor)
	        throws HeadlessException {
		Controller controller = Controller.getCurrentController();
		final Component component = controller.getMapViewManager().getComponent(nodeModel);
		return ColorTracker.showCommonJColorChooserDialog(component, title, initialColor, defaultColor);
	}

	final private JColorChooser chooser;
	private Color color;

	ColorTracker(final JColorChooser c) {
		chooser = c;
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		color = chooser.getColor();
	}

	Color getColor() {
		return color;
	}

	protected void setColor(final Color color) {
		this.color = color;
	}
}
