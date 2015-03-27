/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2003 -2013 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
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
import java.awt.GridLayout;
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
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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
import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class OptionPanel {
	public interface IOptionPanelFeedback {
		void writeProperties(Properties props);
	}

	final static private String OPTION_PANEL_RESOURCE_PREFIX = "OptionPanel.";
	static final String PREFERENCE_STORAGE_PROPERTY = "OptionPanel_Window_Properties";
	private Vector<IPropertyControl> controls;
	final private IOptionPanelFeedback feedback;
	final private HashMap<String, Integer> tabStringToIndexMap = new HashMap<String, Integer>();
	final private HashMap<Integer, String> tabIndexToStringMap = new HashMap<Integer, String>();
	private String selectedPanel;
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
	
	/**
	 * Builds and returns a right aligned button bar with the given buttons.
	 *
	 * @param buttons  an array of buttons to add
	 * @return a right aligned button bar with the given buttons
	 */
	public static JPanel buildRightAlignedBar(JButton[] buttons) {
//        ButtonBarBuilder2 builder = new ButtonBarBuilder2();
		ButtonBarBuilder builder = new ButtonBarBuilder();
		builder.addGlue();
		builder.addButton(buttons);
		return builder.getPanel();
	}
	
	
	/**
	 * Builds and returns a button bar with OK and Cancel.
	 *
	 * @param ok		the OK button
	 * @param cancel	the Cancel button
	 * @return a panel that contains the button(s)
	 */
	public static JPanel buildOKCancelBar(
			JButton ok, JButton cancel) {
		return buildRightAlignedBar(new JButton[] {ok, cancel});
	}

	
	
	/**
	 * This method builds the preferences panel.
	 * A list of IPropertyControl is iterated through and
	 * if the IPropertyControl is an instance of TabProperty,
	 * it creates a new "tab" that can be clicked to reveal the appropriate panel.
	 * If the previous selected tab was saved on close,
	 * the appropriate tab is reopened.
	 *
	 * @param controlsTree  This is the data that needs to be built
	 */
	public void buildPanel(final DefaultMutableTreeNode controlsTree) {
		final JPanel centralPanel = new JPanel();
		centralPanel.setLayout(new GridLayout(1, 1));
		final JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		FormLayout bottomLayout = null;
		DefaultFormBuilder bottomBuilder = null;
		initControls(controlsTree);
		final Iterator<IPropertyControl> iterator = controls.iterator();
		int tabIndex = 0;
		while (iterator.hasNext()) {
			final IPropertyControl control = iterator.next();
			if (control instanceof TabProperty) {
				final TabProperty newTab = (TabProperty) control;
				bottomLayout = new FormLayout(newTab.getName(), "");
				bottomBuilder = new DefaultFormBuilder(bottomLayout);
				bottomBuilder.setDefaultDialogBorder();
				final JScrollPane bottomComponent = new JScrollPane(bottomBuilder.getPanel());
				UITools.setScrollbarIncrement(bottomComponent);
				final String tabName = TextUtils.getOptionalText(newTab.getLabel());
				tabStringToIndexMap.put(tabName, tabIndex);
				tabIndexToStringMap.put(tabIndex, tabName);
				tabbedPane.addTab(tabName, bottomComponent);
				tabIndex++;
			}
			else {
				control.layout(bottomBuilder);
			}
		}
		tabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(final ChangeEvent event) {
				final JTabbedPane c = (JTabbedPane) event.getSource();
				selectedPanel = tabIndexToStringMap.get(c.getSelectedIndex());
			}
		});
		centralPanel.add(tabbedPane);
		if (selectedPanel != null && tabStringToIndexMap.containsKey(selectedPanel)) {
			// Without the containsKey call the loading of the tab "behaviour"/"behavior" gives a nullpointer exception
			tabbedPane.setSelectedIndex(tabStringToIndexMap.get(selectedPanel));
		}
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
		topDialog.getContentPane().add(buildOKCancelBar(cancelButton, okButton), BorderLayout.SOUTH);
	}

	private boolean validate() {
		final Properties properties = getOptionProperties();
		final ValidationResult result = new ValidationResult();
		for (final IValidator validator : Controller.getCurrentController().getOptionValidators()) {
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

	private String formatErrors(final String key, final ArrayList<String> errors) {
		// TextUtils.format() xml escapes the format arguments - we don't want that
		final MessageFormat formatter = new MessageFormat(TextUtils.getText(key));
		return formatter.format(new Object[] { StringUtils.join(errors.iterator(), "<br>") });
	}

	@SuppressWarnings("unchecked")
	/**
	 * This is where the controls are added to the "controls" IProperty Vector
	 * @param controlsTree This is the tree that gets built
	 */
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
		storage.setPanel(OPTION_PANEL_RESOURCE_PREFIX + selectedPanel);
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
		if (panel.startsWith(OPTION_PANEL_RESOURCE_PREFIX)) {
			selectedPanel = panel.substring(OPTION_PANEL_RESOURCE_PREFIX.length());
		}
	}
}
