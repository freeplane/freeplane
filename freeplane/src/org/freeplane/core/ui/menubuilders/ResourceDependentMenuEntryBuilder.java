package org.freeplane.core.ui.menubuilders;

import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JMenu;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.LabelAndMnemonicSetter;
import org.freeplane.core.util.TextUtils;

class ResourceDependentMenuEntryBuilder implements MenuEntryBuilder {
	@Override
	public JMenu createMenuEntry(Entry entry) {
		JMenu menu = new JMenu();
		String name = entry.getName();
		final String iconResource = ResourceController.getResourceController().getProperty(name + ".icon", null);
		LabelAndMnemonicSetter.setLabelAndMnemonic(menu, TextUtils.getRawText(name));
		if(iconResource != null){
			final URL url = ResourceController.getResourceController().getResource(iconResource);
			menu.setIcon(new ImageIcon(url));
		}
		return menu;
	}
}