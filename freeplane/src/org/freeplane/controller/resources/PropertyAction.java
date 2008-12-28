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
package org.freeplane.controller.resources;

import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;
import javax.swing.tree.DefaultMutableTreeNode;

import org.freeplane.controller.Controller;
import org.freeplane.controller.FreeplaneAction;
import org.freeplane.controller.resources.ui.OptionPanel;
import org.freeplane.controller.resources.ui.OptionPanel.IOptionPanelFeedback;
import org.freeplane.main.Tools;

/**
 * @author foltin
 */
public class PropertyAction extends FreeplaneAction {
	private final DefaultMutableTreeNode controls;

	/**
	 * @param controls 
	 *
	 */
	public PropertyAction(final DefaultMutableTreeNode controls) {
		super("property_dialog");
		this.controls = controls;
	}

	public void actionPerformed(final ActionEvent arg0) {
		final JDialog dialog = new JDialog(Controller.getController().getViewController()
		    .getJFrame(), true /* modal */);
		dialog.setResizable(true);
		dialog.setUndecorated(false);
		final OptionPanel options = new OptionPanel(dialog, new IOptionPanelFeedback() {
			public void writeProperties(final Properties props) {
				final Vector sortedKeys = new Vector();
				sortedKeys.addAll(props.keySet());
				Collections.sort(sortedKeys);
				boolean propertiesChanged = false;
				for (final Iterator i = sortedKeys.iterator(); i.hasNext();) {
					final String key = (String) i.next();
					final String newProperty = props.getProperty(key);
					propertiesChanged = propertiesChanged
					        || !newProperty.equals(Controller.getResourceController().getProperty(
					            key));
					Controller.getResourceController().setProperty(key, newProperty);
				}
				if (propertiesChanged) {
					JOptionPane.showMessageDialog(null, Controller
					    .getText("option_changes_may_require_restart"));
					Controller.getResourceController().saveProperties();
				}
			}
		});
		options.buildPanel(controls);
		options.setProperties();
		dialog.setTitle("Freemind Properties");
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent event) {
				options.closeWindow();
			}
		});
		final Action action = new AbstractAction() {
			public void actionPerformed(final ActionEvent arg0) {
				options.closeWindow();
			}
		};
		Tools.addEscapeActionToDialog(dialog, action);
		dialog.setVisible(true);
	}
}
