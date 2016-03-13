package org.freeplane.core.ui;

import javax.swing.JMenuItem;

public class MenuItemMnemonicHolder extends ButtonNameMnemonicHolder {

	final private JMenuItem menuItem;


	public MenuItemMnemonicHolder(JMenuItem menuItem) {
		super(menuItem);
		this.menuItem = menuItem;
	}

	@Override
	public boolean hasAccelerator() {
		return menuItem.getAccelerator() != null;
	}

}