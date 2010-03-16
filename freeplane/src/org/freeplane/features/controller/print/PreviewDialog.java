/*
 * Preview Dialog - A Preview Dialog for your Swing Applications Copyright (C)
 * 2003 Jens Kaiser. Created by Dimitry Polivaev. Written by: 2003 Jens Kaiser
 * <jens.kaiser@web.de> This program is free software; you can redistribute it
 * and/or modify it under the terms of the GNU Library General Public License as
 * published by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Library General Public License for more details. You should have
 * received a copy of the GNU Library General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 675 Mass Ave,
 * Cambridge, MA 02139, USA.
 */
package org.freeplane.features.controller.print;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.Printable;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.FreeplaneToolBar;
import org.freeplane.core.ui.components.UITools;

class PreviewDialog extends JDialog implements ActionListener {
	final private static double DEFAULT_ZOOM_FACTOR_STEP = 0.1;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final private JLabel pageNumber;
	protected Printable view;

	public PreviewDialog(final PrintController printController, final String title, final Component c) {
		super(JOptionPane.getFrameForComponent(c), title, true);
		view = (Printable) c;
		final Preview preview = new Preview(printController, view, 1);
		final JScrollPane scrollPane = new JScrollPane(preview);
		UITools.setScrollbarIncrement(scrollPane);
		getContentPane().add(scrollPane, "Center");
		final JToolBar toolbar = new FreeplaneToolBar("preview_toolbar", SwingConstants.HORIZONTAL);
		getContentPane().add(toolbar, "North");
		pageNumber = new JLabel("- 1 -");
		final JButton button = getButton("Back24.gif", new BrowseAction(preview, pageNumber, -1));
		toolbar.add(button);
		pageNumber.setPreferredSize(button.getPreferredSize());
		pageNumber.setHorizontalAlignment(SwingConstants.CENTER);
		toolbar.add(pageNumber);
		toolbar.add(getButton("Forward24.gif", new BrowseAction(preview, pageNumber, 1)));
		toolbar.add(new JToolBar.Separator());
		toolbar.add(getButton("ZoomIn24.png", new ZoomAction(preview, PreviewDialog.DEFAULT_ZOOM_FACTOR_STEP)));
		toolbar.add(getButton("ZoomOut24.png", new ZoomAction(preview, -PreviewDialog.DEFAULT_ZOOM_FACTOR_STEP)));
		toolbar.add(new JToolBar.Separator());
		final JPanel buttons = new JPanel();
		buttons.setLayout(new FlowLayout(FlowLayout.RIGHT));
		final JButton ok = new JButton("OK");
		ok.addActionListener(this);
		buttons.add(ok);
		getContentPane().add(buttons, "South");
		UITools.addEscapeActionToDialog(this);
	}

	public void actionPerformed(final ActionEvent e) {
		dispose();
	}

	private JButton getButton(final String iconName, final AbstractAction action) {
		return getButton(null, iconName, action);
	}

	private JButton getButton(final String name, final String iconName, final AbstractAction action) {
		JButton result = null;
		ImageIcon icon = null;
		final URL imageURL = ResourceController.getResourceController().getResource("/images/" + iconName);
		if (imageURL != null) {
			icon = new ImageIcon(imageURL);
		}
		if (action != null) {
			if (icon != null) {
				action.putValue(Action.SMALL_ICON, new ImageIcon(imageURL));
			}
			if (name != null) {
				action.putValue(Action.NAME, name);
			}
			result = new JButton(action);
		}
		else {
			result = new JButton(name, icon);
		}
		return result;
	}
}
