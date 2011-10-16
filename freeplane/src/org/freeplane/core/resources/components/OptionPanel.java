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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.commons.lang.StringUtils;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.components.IValidator.ValidationResult;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;

public class OptionPanel {
	final private class ChangeTabAction implements ActionListener {
		final private CardLayout cardLayout;
		final private JPanel centralPanel;
		final private String tabName;

		private ChangeTabAction(final CardLayout cardLayout, final JPanel centralPanel, final String tabName) {
			super();
			this.cardLayout = cardLayout;
			this.centralPanel = centralPanel;
			this.tabName = tabName;
		}

		public void actionPerformed(final ActionEvent arg0) {
			cardLayout.show(centralPanel, tabName);
			for (final JButton button : tabButtonMap.values()) {
				button.setForeground(null);
			}
			getTabButton(tabName).setForeground(OptionPanel.MARKED_BUTTON_COLOR);
			selectedPanel = tabName;
		}
	}

	public interface IOptionPanelFeedback {
		void writeProperties(Properties props);
	}

	private static final Color MARKED_BUTTON_COLOR = Color.BLUE;
	static final String PREFERENCE_STORAGE_PROPERTY = "OptionPanel_Window_Properties";
	private Vector<IPropertyControl> controls;
	final private IOptionPanelFeedback feedback;
	private String selectedPanel;
	final private HashMap<String, ChangeTabAction> tabActionMap = new HashMap<String, ChangeTabAction>();
	final private HashMap<String, JButton> tabButtonMap = new HashMap<String, JButton>();
	final private JDialog topDialog;

	/**
	 * @throws IOException
	 */
	public OptionPanel(final JDialog d, final IOptionPanelFeedback feedback) {
		super();
		topDialog = d;
		this.feedback = feedback;
		new OptionPanelBuilder();
	}

	public void buildPanel(final DefaultMutableTreeNode controlsTree) {
		final FormLayout leftLayout = new FormLayout("max(80dlu;p):grow", "");
		final DefaultFormBuilder leftBuilder = new DefaultFormBuilder(leftLayout);
		final CardLayout cardLayout = new VariableSizeCardLayout();
		final JPanel rightStack = new JPanel(cardLayout);
		FormLayout rightLayout = null;
		DefaultFormBuilder rightBuilder = null;
		String lastTabName = null;
		initControls(controlsTree);
		final Iterator<IPropertyControl> iterator = controls.iterator();
		while (iterator.hasNext()) {
			final IPropertyControl control = iterator.next();
			if (control instanceof TabProperty) {
				final TabProperty newTab = (TabProperty) control;
				if (rightBuilder != null) {
					rightStack.add(rightBuilder.getPanel(), lastTabName);
				}
				rightLayout = new FormLayout(newTab.getDescription(), "");
				rightBuilder = new DefaultFormBuilder(rightLayout);
				rightBuilder.setDefaultDialogBorder();
				lastTabName = newTab.getLabel();
				final JButton tabButton = new JButton(TextUtils.getOptionalText(lastTabName));
				final ChangeTabAction changeTabAction = new ChangeTabAction(cardLayout, rightStack, lastTabName);
				tabButton.addActionListener(changeTabAction);
				registerTabButton(tabButton, lastTabName, changeTabAction);
				leftBuilder.append(tabButton);
			}
			else {
				control.layout(rightBuilder);
			}
		}
		rightStack.add(rightBuilder.getPanel(), lastTabName);
		if (selectedPanel != null && tabActionMap.containsKey(selectedPanel)) {
			(tabActionMap.get(selectedPanel)).actionPerformed(null);
		}
		final JScrollPane rightComponent = new JScrollPane(rightStack);
		UITools.setScrollbarIncrement(rightComponent);
		final JSplitPane centralPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftBuilder.getPanel(),
		    rightComponent);
		topDialog.getContentPane().add(centralPanel, BorderLayout.CENTER);
		final JButton cancelButton = new JButton();
		MenuBuilder.setLabelAndMnemonic(cancelButton, TextUtils.getRawText("cancel"));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				closeWindow();
			}
		});
		final JButton okButton = new JButton();
		MenuBuilder.setLabelAndMnemonic(okButton, TextUtils.getRawText("ok"));
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				if (validate()) {
					closeWindow();
					feedback.writeProperties(getOptionProperties());
				}
			}
		});
		topDialog.getRootPane().setDefaultButton(okButton);
		topDialog.getContentPane().add(ButtonBarFactory.buildOKCancelBar(cancelButton, okButton), BorderLayout.SOUTH);
	}
	
	private boolean validate() {
		final Properties properties = getOptionProperties();
		final ValidationResult result = new ValidationResult();
		for (IValidator validator : Controller.getCurrentController().getOptionValidators()) {
			result.add(validator.validate(properties));
		}
		if (!result.isValid()) {
			UITools.errorMessage(formatErrors("OptionPanel.validation_error", result.getErrors()));
			LogUtils.severe(result.toString());
		}
		else if (result.hasWarnings()) {
			UITools.informationMessage(formatErrors("OptionPanel.validation_warning", result.getWarnings()));
			LogUtils.warn(result.toString());
		}
		return result.isValid();
	}

	private String formatErrors(String key, ArrayList<String> errors) {
		// TextUtils.format() xml escapes the format arguments - we don't want that
		final MessageFormat formatter = new MessageFormat(TextUtils.getText(key));
		return formatter.format(new Object[] { StringUtils.join(errors.iterator(), "<br>") });
	}

	@SuppressWarnings("unchecked")
    private void initControls(final DefaultMutableTreeNode controlsTree) {
	    controls = new Vector<IPropertyControl>();
		for (final Enumeration<DefaultMutableTreeNode> i = controlsTree.preorderEnumeration(); i.hasMoreElements();) {
			final IPropertyControlCreator creator = (IPropertyControlCreator) i.nextElement().getUserObject();
			if (creator == null) {
				continue;
			}
			final IPropertyControl control = creator.createControl();
			controls.add(control);
		}
    }

	public void closeWindow() {
		final OptionPanelWindowConfigurationStorage storage = new OptionPanelWindowConfigurationStorage();
		storage.setPanel(selectedPanel);
		storage.storeDialogPositions(topDialog, OptionPanel.PREFERENCE_STORAGE_PROPERTY);
		topDialog.setVisible(false);
		topDialog.dispose();
	}

	private Properties getOptionProperties() {
		final Properties p = new Properties();
		for (final IPropertyControl control : controls) {
			if (control instanceof PropertyBean) {
				final PropertyBean bean = (PropertyBean) control;
				final String value = bean.getValue();
				if (value != null) {
					p.setProperty(bean.getName(), value);
				}
			}
		}
		return p;
	}

	private JButton getTabButton(final String name) {
		return tabButtonMap.get(name);
	}

	private void registerTabButton(final JButton tabButton, final String name, final ChangeTabAction changeTabAction) {
		tabButtonMap.put(name, tabButton);
		tabActionMap.put(name, changeTabAction);
		if (selectedPanel == null) {
			selectedPanel = name;
		}
	}

	public void setProperties() {
		for (final IPropertyControl control : controls) {
			if (control instanceof PropertyBean) {
				final PropertyBean bean = (PropertyBean) control;
				final String name = bean.getName();
				final String value = ResourceController.getResourceController().getProperty(name);
				bean.setValue(value);
			}
		}
	}

	void setSelectedPanel(final String panel) {
		selectedPanel = panel;
	}
}
