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
import org.freeplane.core.util.TextUtils;

/**
 * @author foltin
 */
public class PropertyAction extends AFreeplaneAction {

	private static final long serialVersionUID = 1L;
	private final DefaultMutableTreeNode controls;

	public PropertyAction( final DefaultMutableTreeNode controls) {
		super("PropertyAction");
		this.controls = controls;
	}

	public void actionPerformed(final ActionEvent event) {
		JDialog dialog = null;
		dialog = createNewDialog(event, dialog);
		final OptionPanel options = buildOptionPanel(dialog);
		final String marshalled = ResourceController.getResourceController().getProperty(
		    OptionPanel.PREFERENCE_STORAGE_PROPERTY);
		final OptionPanelWindowConfigurationStorage storage = OptionPanelWindowConfigurationStorage.decorateDialog(
		    marshalled, dialog);
		setSelectedPanel(options, storage);
		options.buildPanel(controls);
		options.setProperties();
		addDialogListeners(dialog, options);
		setDialogProperties(dialog, storage);
	}

	private JDialog createNewDialog(final ActionEvent e, JDialog dialog) {
		if (e != null) {
			final Object source = e.getSource();
			if (source instanceof Component) {
				final Window window = SwingUtilities.getWindowAncestor((Component) source);
				if (window instanceof Dialog) {
					dialog = new JDialog((Dialog) window, true /* modal */);
				}
				else if (window instanceof Frame) {
					dialog = new JDialog((Frame) window, true /* modal */);
				}
			}
		}
		if (dialog == null)
			dialog = new JDialog(UITools.getFrame(), true /* modal */);
		return dialog;
    }

	private OptionPanel buildOptionPanel(JDialog dialog) {
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
					JOptionPane.showMessageDialog(UITools.getFrame(), TextUtils
					    .getText("option_changes_may_require_restart"));
					ResourceController.getResourceController().saveProperties();
				}
			}
		});
	    return options;
    }

	private void setSelectedPanel(final OptionPanel options, final OptionPanelWindowConfigurationStorage storage) {
		if (storage != null) {
			String storagePanelName = storage.getPanel();
			options.setSelectedPanel(storagePanelName);
		}
    }

	private void addDialogListeners(JDialog dialog, final OptionPanel options) {
	    dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent event) {
				options.closeWindow();
			}
		});
		final Action action = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(final ActionEvent arg0) {
				options.closeWindow();
			}
		};
		UITools.addEscapeActionToDialog(dialog, action);
    }

	private void setDialogProperties(JDialog dialog, final OptionPanelWindowConfigurationStorage storage) {
		setDialogTitle(dialog);
		setDialogBounds(dialog, storage);
		dialog.setResizable(true);
		dialog.setUndecorated(false);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
	}

	private void setDialogTitle(JDialog dialog) {
		final String title = TextUtils.getText("PropertyAction.dialog");
		dialog.setTitle(title);
	}

	private void setDialogBounds(JDialog dialog, final OptionPanelWindowConfigurationStorage storage) {
		if (storage == null) {
			UITools.setBounds(dialog, -1, -1, dialog.getPreferredSize().width + 50, -1);
		}
	}

	@Override
    public void afterMapChange(final Object newMap) {
    }
}
