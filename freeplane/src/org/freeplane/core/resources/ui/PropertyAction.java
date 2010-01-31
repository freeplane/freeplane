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
package org.freeplane.core.resources.ui;

import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;
import javax.swing.tree.DefaultMutableTreeNode;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.ui.OptionPanel.IOptionPanelFeedback;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;

/**
 * @author foltin
 */
public class PropertyAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final DefaultMutableTreeNode controls;

	/**
	 * @param controls 
	 *
	 */
	public PropertyAction(final Controller controller, final DefaultMutableTreeNode controls) {
		super("PropertyAction", controller);
		this.controls = controls;
	}

	public void actionPerformed(final ActionEvent arg0) {
		final JDialog dialog = new JDialog(getController().getViewController().getFrame(), true /* modal */);
		dialog.setResizable(true);
		dialog.setUndecorated(false);
		final OptionPanel options = new OptionPanel(dialog, new IOptionPanelFeedback() {
			public void writeProperties(final Properties props) {
				boolean propertiesChanged = false;
				for (Object keyObject : props.keySet()) {
					String key = keyObject.toString();
					final String newProperty = props.getProperty(key);
					propertiesChanged = propertiesChanged
					        || !newProperty.equals(ResourceController.getResourceController().getProperty(key));
					ResourceController.getResourceController().setProperty(key, newProperty);
				}
				if (propertiesChanged) {
					JOptionPane.showMessageDialog(UITools.getFrame(), ResourceBundles
					    .getText("option_changes_may_require_restart"));
					ResourceController.getResourceController().saveProperties(getController());
				}
			}
		});
		final String marshalled = ResourceController.getResourceController().getProperty(OptionPanel.PREFERENCE_STORAGE_PROPERTY);
		final OptionPanelWindowConfigurationStorage storage = OptionPanelWindowConfigurationStorage.decorateDialog(marshalled, dialog);
		if (storage != null) {
			options.setSelectedPanel(storage.getPanel());
		}
		options.buildPanel(controls);
		options.setProperties();
		dialog.setTitle("Freeplane Properties");
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent event) {
				options.closeWindow();
			}
		});
		final Action action = new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(final ActionEvent arg0) {
				options.closeWindow();
			}
		};
		UITools.addEscapeActionToDialog(dialog, action);
		if(storage == null){
			dialog.pack();
		}
		dialog.setVisible(true);
	}
}
