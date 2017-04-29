package org.freeplane.core.ui;

import javax.swing.JComboBox;

import org.freeplane.core.resources.ResourceController;

public class MenuSplitterConfiguration {
	public static final String MAX_MENU_ITEM_COUNT_KEY = "max_menu_item_count";
	public static final int MAX_MENU_ITEM_COUNT = ResourceController.getResourceController().getIntProperty(
	    MAX_MENU_ITEM_COUNT_KEY);
	public static void setMaximumRowCount(JComboBox c){
		c.setMaximumRowCount(MAX_MENU_ITEM_COUNT);
	}
}