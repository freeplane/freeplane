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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.embed.swing.SwingNode;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
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

	public interface IOptionPanelFeedback {
		void writeProperties(Properties props);
	}

	final static private String OPTION_PANEL_RESOURCE_PREFIX = "OptionPanel.";
	static final String PREFERENCE_STORAGE_PROPERTY = "OptionPanel_Window_Properties";
	private ArrayList<ArrayList<IPropertyControl>> controls;
	final private IOptionPanelFeedback feedback;
	private Tab selectedTab;
	private JDialog topDialog;

	public OptionPanel(final JDialog d, final IOptionPanelFeedback feedback) {
		super();
		topDialog = d;
		this.feedback = feedback;
		new OptionPanelBuilder();
	}

	public void buildPanel(final DefaultMutableTreeNode controlsTree) {
		initControls(controlsTree);
		buildCentralPanel();
		buildButtonBar();
	}

	private void buildCentralPanel() {
		JFXPanel centralPanel = new JFXPanel();
		Platform.runLater(new Runnable() {
			public void run() {
				initFX(centralPanel);
			}
		});
		centralPanel.setLayout(new GridLayout(1, 1));
		topDialog.getContentPane().add(centralPanel, BorderLayout.CENTER);
	}

	private void initFX(JFXPanel panel) {
		Scene scene = createScene();
		panel.setScene(scene);
	}

	private Scene createScene() {
		TabPane tabPane = new TabPane();
		tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
		for (ArrayList<IPropertyControl> tabGroup : controls) {
			SwingNode swingNode = new SwingNode();
			createSwingNode(tabGroup, swingNode);
			// First element in tabGroup will always be a TabProperty; see initControls method
			String tabName = TextUtils.getOptionalText(((TabProperty) tabGroup.get(0)).getLabel());
			Tab newTab = buildTab(tabName, swingNode);
			tabPane.getTabs().add(newTab);
		}
		handleTabSelection(tabPane);
		handleTabChangeListener(tabPane);
		return new Scene(tabPane);
	}

	private Tab buildTab(String tabName, SwingNode swingNode) {
		Tab newTab = new Tab();
		newTab.setText(tabName);
		ScrollPane scrollPane = new ScrollPane();
		scrollPane.setContent(swingNode);
		scrollPane.setFitToWidth(true);
		scrollPane.setFitToHeight(true);
		newTab.setContent(scrollPane);
		return newTab;
	}

	private void createSwingNode(ArrayList<IPropertyControl> tabGroup, SwingNode swingNode) {
		SwingUtilities.invokeLater(() -> {
			FormLayout bottomLayout = new FormLayout(tabGroup.get(0).getDescription(), "");
			final DefaultFormBuilder bottomBuilder = new DefaultFormBuilder(bottomLayout);
			bottomBuilder.setDefaultDialogBorder();
			for (IPropertyControl control : tabGroup) {
				layoutControlOnPanel(bottomBuilder, control);
			}
			swingNode.setContent(bottomBuilder.getPanel());
		});
    }

	private void layoutControlOnPanel(final DefaultFormBuilder bottomBuilder, IPropertyControl control) {
	    if (control instanceof TabProperty) {
	    	return;
	    }
	    control.layout(bottomBuilder);
    }

	private void handleTabSelection(TabPane tabPane) {
		if (selectedTab != null) {
			SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
			for (Tab tab : tabPane.getTabs()) {
				if (tab.getText().equals(selectedTab.getText())) {
					selectionModel.select(tab);
				}
			}
		}
	}

	private void handleTabChangeListener(TabPane tabPane) {
		tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
			public void changed(ObservableValue<? extends Tab> arg0, Tab oldValue, Tab newValue) {
				selectedTab = newValue;
			}
		});
	}

	private void buildButtonBar() {
	    final JButton cancelButton = buildCancelButton();
		final JButton okButton = buildOkButton();
		handleAttachmentToDialog(cancelButton, okButton);
    }

	private JButton buildCancelButton() {
	    final JButton cancelButton = new JButton();
		MenuBuilder.setLabelAndMnemonic(cancelButton, TextUtils.getRawText("cancel"));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				closeWindow();
			}
		});
	    return cancelButton;
    }

	private JButton buildOkButton() {
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
	    return okButton;
    }

	private void handleAttachmentToDialog(final JButton cancelButton, final JButton okButton) {
	    topDialog.getRootPane().setDefaultButton(okButton);
		topDialog.getContentPane().add(ButtonBarFactory.buildOKCancelBar(cancelButton, okButton), BorderLayout.SOUTH);
    }

	private boolean validate() {
		final Properties properties = getOptionProperties();
		final ValidationResult result = new ValidationResult();
		for (final IValidator validator : Controller.getCurrentController().getOptionValidators()) {
			result.add(validator.validate(properties));
		}
		if (!result.isValid()) {
			UITools.errorMessage(formatErrors(OPTION_PANEL_RESOURCE_PREFIX + "validation_error", result.getErrors()));
			LogUtils.severe(result.toString());
		}
		else if (result.hasWarnings()) {
			UITools.informationMessage(formatErrors(OPTION_PANEL_RESOURCE_PREFIX + "validation_warning",
			    result.getWarnings()));
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
	private void initControls(final DefaultMutableTreeNode controlsTree) {
		controls = new ArrayList<>();
		ArrayList<IPropertyControl> tabGroup = null;
		for (final Enumeration<DefaultMutableTreeNode> i = controlsTree.preorderEnumeration(); i.hasMoreElements();) {
			final IPropertyControlCreator creator = (IPropertyControlCreator) i.nextElement().getUserObject();
			if (creator == null) {
				continue;
			}
			final IPropertyControl control = creator.createControl();
			if (control instanceof TabProperty) {
				tabGroup = new ArrayList<>();
				tabGroup.add(control);
				controls.add(tabGroup);
			}
			else {
				tabGroup.add(control);
			}
		}
	}

	public void closeWindow() {
		final OptionPanelWindowConfigurationStorage storage = new OptionPanelWindowConfigurationStorage();
		storage.setPanel(OPTION_PANEL_RESOURCE_PREFIX + selectedTab.getText());
		storage.storeDialogPositions(topDialog, OptionPanel.PREFERENCE_STORAGE_PROPERTY);
		topDialog.setVisible(false);
		topDialog.dispose();
		Platform.setImplicitExit(false); // Without this line, the JFXPanel does not show on future openings
	}

	private Properties getOptionProperties() {
		final Properties p = new Properties();
		for (final ArrayList<IPropertyControl> tabGroup : controls) {
			for (final IPropertyControl control : tabGroup) {
				if (control instanceof PropertyBean) {
					final PropertyBean bean = (PropertyBean) control;
					final String value = bean.getValue();
					if (value != null) {
						p.setProperty(bean.getName(), value);
					}
				}
			}
		}
		return p;
	}

	public void setProperties() {
		for (final ArrayList<IPropertyControl> tabGroup : controls) {
			for (final IPropertyControl control : tabGroup) {
				if (control instanceof PropertyBean) {
					final PropertyBean bean = (PropertyBean) control;
					final String name = bean.getName();
					final String value = ResourceController.getResourceController().getProperty(name);
					bean.setValue(value);
				}
			}
		}
	}

	void setSelectedPanel(final String panelName) {
		if (panelName.startsWith(OPTION_PANEL_RESOURCE_PREFIX)) {
			String panelNameWithoutPrefix = panelName.substring(OPTION_PANEL_RESOURCE_PREFIX.length());
			selectedTab = new Tab(panelNameWithoutPrefix);
		}
	}
}
