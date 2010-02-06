/**
 * 
 */
package org.freeplane.plugin.script.proxy;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.freeplane.core.icon.IconController;
import org.freeplane.core.icon.MindIcon;
import org.freeplane.core.icon.factory.IconStoreFactory;
import org.freeplane.core.model.NodeModel;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.features.mindmapmode.icon.MIconController;

class IconsProxy extends AbstractProxy<NodeModel> implements Proxy.Icons {
	IconsProxy(final NodeModel delegate, final MModeController modeController) {
		super(delegate, modeController);
	}

	public void addIcon(final String name) {
		getIconController().addIcon(getDelegate(),
				IconStoreFactory.create().getMindIcon(name));

	}

	private int findIcon(final String iconID) {
		final List<MindIcon> icons = getDelegate().getIcons();
		final int i = 0;
		for (final MindIcon icon : icons) {
			if (icon.getName().equals(iconID)) {
				return i;
			}
		}
		return -1;
	}

	MIconController getIconController() {
		return (MIconController) IconController
				.getController(getModeController());
	}

	public List<String> getIcons() {
		final List<MindIcon> icons = getDelegate().getIcons();
		final int size = icons.size();
		if (size == 0) {
			return Collections.emptyList();
		}
		final List<String> list = new Vector<String>(size);
		for (final MindIcon icon : icons) {
			list.add(icon.getName());
		}
		return list;
	}

	public boolean removeIcon(final String iconID) {
		final int index = findIcon(iconID);
		if (index == -1) {
			return false;
		}
		getIconController().removeIcon(getDelegate(), index);
		return true;
	}
}