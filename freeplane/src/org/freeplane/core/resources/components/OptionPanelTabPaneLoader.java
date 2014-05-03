/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2014 home
 *
 *  This file author is home
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

import java.util.ArrayList;

import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import org.freeplane.core.util.TextUtils;

public final class OptionPanelTabPaneLoader extends Task<Void> {
	private ArrayList<ArrayList<IPropertyControl>> controls;
	private StackPane stackPane;
	int count = 0;

	OptionPanelTabPaneLoader(ArrayList<ArrayList<IPropertyControl>> controls, StackPane stackPane) {
		this.controls = controls;
		this.stackPane = stackPane;
	}

	@Override
	protected Void call() throws Exception {
		for (ArrayList<IPropertyControl> tabGroup : controls) {
			GridPane formPane = new GridPane();
			createSwingNode(tabGroup, formPane);
			// First element in tabGroup should always be a TabProperty
			String tabName = TextUtils.getOptionalText(((TabProperty) tabGroup.get(0)).getLabel());
			Tab newTab = buildTab(tabName, formPane);
			TabPane tabPane = (TabPane) stackPane.getChildren().get(0);
			tabPane.getTabs().add(newTab);
		}
		return null;
	}

	private void createSwingNode(ArrayList<IPropertyControl> tabGroup, GridPane formPane) {
		for (IPropertyControl control : tabGroup) {
			formPane.addColumn(0, new Label(control.getName()), new TextField());
		}
		//		SwingUtilities.invokeLater(new Runnable() {
		//			public void run() {
		//				FormLayout bottomLayout = new FormLayout(tabGroup.get(0).getDescription(), "");
		//				final DefaultFormBuilder bottomBuilder = new DefaultFormBuilder(bottomLayout);
		//				bottomBuilder.setDefaultDialogBorder();
		//				for (IPropertyControl control : tabGroup) {
		//					layoutControlOnPanel(bottomBuilder, control);
		//				}
		//				swingNode.setContent(bottomBuilder.getPanel());
		//			}
		//		});
	}

	//	private void layoutControlOnPanel(final DefaultFormBuilder bottomBuilder, IPropertyControl control) {
	//		if (control instanceof TabProperty) {
	//			return;
	//		}
	//		control.layout(bottomBuilder);
	//	}

	private Tab buildTab(String tabName, GridPane formPane) {
		Tab newTab = new Tab();
		newTab.setText(tabName);
		ScrollPane scrollPane = new ScrollPane();
		scrollPane.setContent(formPane);
		scrollPane.setFitToWidth(true);
		scrollPane.setFitToHeight(true);
		newTab.setContent(scrollPane);
		return newTab;
	}

}
