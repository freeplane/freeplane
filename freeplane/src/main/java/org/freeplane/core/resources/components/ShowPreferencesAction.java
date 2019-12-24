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
package org.freeplane.core.resources.components;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.tree.DefaultMutableTreeNode;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.components.OptionPanel.IOptionPanelFeedback;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.menubuilders.generic.UserRole;
import org.freeplane.core.util.TextUtils;

/**
 * @author foltin
 */
public class ShowPreferencesAction extends AFreeplaneAction {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private final DefaultMutableTreeNode controls;

	/**
	 * @param controls
	 *
	 */
	public ShowPreferencesAction( final DefaultMutableTreeNode controls) {
		super("ShowPreferencesAction");
		this.controls = controls;
	}

	public void actionPerformed(final ActionEvent e) {
		JDialog dialog = null;
		if(e != null){
			final Object source = e.getSource();
			if(source instanceof Component){
				final Window window = SwingUtilities.getWindowAncestor((Component) source);
				dialog = createDialog(window);
			}
		}
		if(dialog == null){
			dialog= createDialog((Window) UITools.getMenuComponent());
		}
		dialog.setResizable(true);
		dialog.setUndecorated(false);
		final OptionPanel options = new OptionPanel(dialog, new IOptionPanelFeedback() {
			public void writeProperties(final Properties props) {
				boolean propertiesChanged = false;
				for (final Object keyObject : props.keySet()) {
					final String key = keyObject.toString();
					final String newProperty = props.getProperty(key);
					propertiesChanged = propertiesChanged
					        || !newProperty.equals(ResourceController.getResourceController().getProperty(key));
					ResourceController.getResourceController().setProperty(key, newProperty);
				}
				if (propertiesChanged) {
					JOptionPane.showMessageDialog(UITools.getMenuComponent(), TextUtils
					    .getText("option_changes_may_require_restart"));
					ResourceController.getResourceController().saveProperties();
				}
			}
		});
		final String marshalled = ResourceController.getResourceController().getProperty(
		    OptionPanel.PREFERENCE_STORAGE_PROPERTY);
		final OptionPanelWindowConfigurationStorage storage = OptionPanelWindowConfigurationStorage.decorateDialog(
		    marshalled, dialog);
		final String actionCommand = e != null ?  e.getActionCommand() : null;
		if(actionCommand != null && actionCommand.startsWith(OptionPanel.OPTION_PANEL_RESOURCE_PREFIX))
			options.setSelectedPanel(actionCommand);
		else if (storage != null) {
			options.setSelectedPanel(storage.getPanel());
		}
		options.buildPanel(controls);
		options.setProperties();
		final String title = TextUtils.getText("ShowPreferencesAction.dialog");
		dialog.setTitle(title);
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
		if (storage == null) {
			UITools.setBounds(dialog, -1, -1, dialog.getPreferredSize().width + 50, -1);
		}
		dialog.setVisible(true);
	}

	private JDialog createDialog(final Window window) {
		if(window instanceof Dialog){
			return new JDialog((Dialog)window, true /* modal */);
		}
		else if(window instanceof Frame){
			return new JDialog((Frame)window, true /* modal */);
		}
		else
			return null;
	}

	@Override
    public void afterMapChange(UserRole userRole, boolean isMapSelected) {
    }
}
