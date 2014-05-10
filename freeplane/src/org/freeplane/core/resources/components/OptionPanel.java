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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.commons.lang.StringUtils;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.components.IValidator.ValidationResult;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;

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

	public OptionPanel(final JDialog topDialog, final IOptionPanelFeedback feedback) {
		super();
		this.topDialog = topDialog;
		this.feedback = feedback;
		new OptionPanelBuilder();
	}

	public void buildPanel(final DefaultMutableTreeNode controlsTree) {
		initControls(controlsTree);
		JFXPanel topPanel = new JFXPanel();
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				initFX(topPanel);
			}
		});
		topPanel.setLayout(new GridLayout(1, 1));
		topDialog.getContentPane().add(topPanel, BorderLayout.CENTER);
	}

	private void initFX(JFXPanel panel) {
		Scene scene = createScene();
		panel.setScene(scene);
	}

	private Scene createScene() {
		BorderPane pane = new BorderPane();
		pane.setCenter(buildCentralPanel());
		pane.setBottom(buildButtonBar());
		return new Scene(pane);
	}

	private StackPane buildCentralPanel() {
		TabPane tabPane = buildTabPane();
		BorderPane borderPane = buildProgressPane();
		StackPane stackPane = buildStackPane(tabPane, borderPane);
		asynchronouslyLoadTabPane(borderPane, stackPane);
		return stackPane;
	}

	private TabPane buildTabPane() {
		TabPane tabPane = new TabPane();
		handleTabSelection(tabPane);
		handleTabChangeListener(tabPane);
		tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
		return tabPane;
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

	/**
	 * In the future, this BorderPane would display a progress bar in the center 
	 * as the loading of the form will take some time.
	 */
	private BorderPane buildProgressPane() {
		BorderPane borderPane = new BorderPane();
		return borderPane;
	}

	private StackPane buildStackPane(TabPane tabPane, BorderPane borderPane) {
		StackPane stackPane = new StackPane();
		stackPane.getChildren().addAll(tabPane, borderPane);
		return stackPane;
	}

	private void asynchronouslyLoadTabPane(BorderPane borderPane, StackPane stackPane) {
		OptionPanelTabPaneLoader task = new OptionPanelTabPaneLoader(controls, stackPane);
		final Thread thread = new Thread(task, "OptionPanelTabPaneLoader");
		thread.start();
		task.setOnSucceeded(workerStateEvent -> {
			Platform.runLater(new Runnable() {
				public void run() {
					stackPane.getChildren().remove(borderPane);
				}
			});
		});
	}

	private HBox buildButtonBar() {
		final Button cancelButton = buildCancelButton();
		final Button okButton = buildOkButton();
		final HBox buttonBar = buildButtonBarHBox(cancelButton, okButton);
		return buttonBar;
	}

	private Button buildCancelButton() {
		final Button cancelButton = new Button();
		//		MenuBuilder.setLabelAndMnemonic(cancelButton, TextUtils.getRawText("cancel"));
		cancelButton.setText(TextUtils.getRawText("cancel").replace("&", "_"));
		cancelButton.setMnemonicParsing(true);
		cancelButton.setOnAction(actionEvent -> {
			closeWindow();
		});
		return cancelButton;
	}

	private Button buildOkButton() {
		final Button okButton = new Button();
		//		MenuBuilder.setLabelAndMnemonic(okButton, TextUtils.getRawText("ok"));
		okButton.setText(TextUtils.getRawText("ok").replace("&", "_"));
		okButton.setMnemonicParsing(true);
		okButton.setOnAction(actionEvent -> {
			SwingUtilities.invokeLater(() -> {
				if (validate()) {
					closeWindow();
					feedback.writeProperties(getOptionProperties());
				}
			});
		});
		return okButton;
	}

	private HBox buildButtonBarHBox(final Button cancelButton, final Button okButton) {
		HBox hbox = new HBox();
		hbox.setAlignment(Pos.CENTER_RIGHT);
		hbox.setPadding(new Insets(5, 5, 5, 5));
		hbox.setSpacing(10);
		hbox.getChildren().addAll(cancelButton, okButton);
		return hbox;
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
