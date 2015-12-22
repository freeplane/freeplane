package org.freeplane.core.ui;

import org.freeplane.core.resources.ResourceController;

public class MenuSplitterConfiguration {
	public static final String MAX_MENU_ITEM_COUNT_KEY = "max_menu_item_count";
	public static final int MAX_MENU_ITEM_COUNT = ResourceController.getResourceController().getIntProperty(
	    MAX_MENU_ITEM_COUNT_KEY);
}