/**
 * 
 */
package org.freeplane.plugin.script.proxy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.freeplane.features.icon.IconController;
import org.freeplane.features.icon.MindIcon;
import org.freeplane.features.icon.factory.IconStoreFactory;
import org.freeplane.features.icon.mindmapmode.MIconController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.script.ScriptContext;

class IconsProxy extends AbstractProxy<NodeModel> implements Proxy.Icons {
	IconsProxy(final NodeModel delegate, final ScriptContext scriptContext) {
		super(delegate, scriptContext);
	}

	public void add(final String name) {
		getIconController().addIcon(getDelegate(), IconStoreFactory.create().getMindIcon(name));
	}

	@Deprecated
	public void addIcon(final String name) {
		add(name);
	}

	private int findIcon(final String iconID) {
		final List<MindIcon> icons = getDelegate().getIcons();
		for (int i = 0; i < icons.size(); i++) {
			if (icons.get(i).getName().equals(iconID)) {
				return i;
			}
		}
		return -1;
	}

	private MIconController getIconController() {
		return (MIconController) IconController.getController();
	}

	public List<String> getIcons() {
		final List<MindIcon> icons = getDelegate().getIcons();
		final int size = icons.size();
		if (size == 0) {
			return Collections.emptyList();
		}
		final ArrayList<String> list = new ArrayList<String>(size);
		for (final MindIcon icon : icons) {
			list.add(icon.getName());
		}
		return list;
	}

	public boolean remove(final String iconID) {
		final int index = findIcon(iconID);
		if (index == -1) {
			return false;
		}
		getIconController().removeIcon(getDelegate(), index);
		return true;
	}

	@Deprecated
	public boolean removeIcon(final String iconID) {
		return remove(iconID);
	}
}
