/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
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
package org.freeplane.core.icon;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.freeplane.core.filter.condition.ICondition;
import org.freeplane.core.filter.condition.JCondition;
import org.freeplane.core.icon.factory.IconStoreFactory;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.n3.nanoxml.XMLElement;

class IconContainedCondition implements ICondition {
	
	private static final IconStore STORE = IconStoreFactory.create();
	
	static final String ICON = "ICON";
	static final String NAME = "icon_contained_condition";

	static private int iconFirstIndex(ModeController modeController, final NodeModel node, final String iconName) {		
		final List<MindIcon> icons = IconController.getIcons(modeController, node);
		for (final ListIterator<MindIcon> i = icons.listIterator(); i.hasNext();) {
			final MindIcon nextIcon = i.next();
			if (iconName.equals(nextIcon.getName())) {
				return i.previousIndex();
			}
		}
		return -1;
	}

	static private int iconLastIndex(ModeController modeController, final NodeModel node, final String iconName) {
		final List<MindIcon> icons = IconController.getIcons(modeController, node);
		final ListIterator<MindIcon> i = icons.listIterator(icons.size());
		while (i.hasPrevious()) {
			final MindIcon nextIcon = i.previous();
			if (iconName.equals(nextIcon.getName())) {
				return i.nextIndex();
			}
		}
		return -1;
	}

	private static boolean isStateIconContained(final NodeModel node, final String iconName) {
		final Set<String> stateIcons = node.getStateIcons().keySet();
		for (final Iterator<String> stateIcon = stateIcons.iterator(); stateIcon.hasNext();) {
			final String nextIcon = stateIcon.next();
			if (iconName.equals(nextIcon)) {
				return true;
			}
		}
		return false;
	}

	static ICondition load(final XMLElement element) {
		return new IconContainedCondition(element.getAttribute(IconContainedCondition.ICON, null));
	}

	final private String iconName;

	public IconContainedCondition(final String iconName) {
		this.iconName = iconName;
	}

	public boolean checkNode(ModeController modeController, final NodeModel node) {
		return IconContainedCondition.iconFirstIndex(modeController, node, iconName) != -1
		        || IconContainedCondition.isStateIconContained(node, iconName);
	}

	private String getIconName() {
		return iconName;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing
	 * .JList, java.lang.Object, int, boolean, boolean)
	 */
	public JComponent getListCellRendererComponent() {
		final JCondition component = new JCondition();
		final String text = ResourceBundles.getText("filter_icon") + ' ' + ResourceBundles.getText("filter_contains")
		        + ' ';
		component.add(new JLabel(text));
		component.add(new JLabel(STORE.getMindIcon(getIconName()).getIcon()));
		return component;
	}

	public void toXml(final XMLElement element) {
		final XMLElement child = new XMLElement();
		child.setName(IconContainedCondition.NAME);
		child.setAttribute(IconContainedCondition.ICON, iconName);
		element.addChild(child);
	}
}
