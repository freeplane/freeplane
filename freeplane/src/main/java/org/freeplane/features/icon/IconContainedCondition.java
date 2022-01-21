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
package org.freeplane.features.icon;

import java.awt.Color;
import java.util.Collection;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.filter.condition.ConditionFactory;
import org.freeplane.features.filter.condition.JCondition;
import org.freeplane.features.icon.factory.IconStoreFactory;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.styles.LogicalStyleController.StyleOption;
import org.freeplane.n3.nanoxml.XMLElement;

public class IconContainedCondition extends ASelectableCondition {
	private static final IconStore STORE = IconStoreFactory.ICON_STORE;
	static final String ICON = "ICON";
	static final String NAME = "icon_contained_condition";

	static ASelectableCondition load(final XMLElement element) {
		return new IconContainedCondition(element.getAttribute(IconContainedCondition.ICON, null));
	}

	final private String iconName;

	public IconContainedCondition(final String iconName) {
		this.iconName = iconName;
	}

	public boolean checkNode(final NodeModel node) {
		return iconFirstIndex(node) != -1
		        || isStateIconContained(node)
		        || isLinkIconContained(node);
	}

	private boolean isLinkIconContained(NodeModel node) {
		return LinkController.getController().containsLinkDecorationIcon(node, iconName);
	}

	private int iconFirstIndex(final NodeModel node) {
		final Collection<NamedIcon> icons = IconController.getController().getIcons(node, StyleOption.FOR_UNSELECTED_NODE);
		int i = 0;
		for (NamedIcon nextIcon : icons) {
			if (iconName.equals(nextIcon.getName())) {
				return i;
			}
			i++;
		}
		return -1;
	}

	private boolean isStateIconContained(final NodeModel node) {
		final Collection<UIIcon> stateIcons = IconController.getController().getStateIcons(node);
		return stateIcons.stream().map(UIIcon::getName).anyMatch(iconName::equals);
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
	public JComponent createRendererComponent() {
		final JCondition component = new JCondition();
		final String text = TextUtils.getText("filter_icon") + ' ' + TextUtils.getText("filter_contains") + ' ';
		component.add(ConditionFactory.createConditionLabel(text));
		JLabel icon = ConditionFactory.createConditionLabel(STORE.getUIIcon(getIconName()));
		component.add(icon);
		icon.setBackground(Color.WHITE);
		icon.setOpaque(true);
		return component;
	}

	public void fillXML(final XMLElement child) {
		child.setAttribute(IconContainedCondition.ICON, iconName);
	}

	@Override
    protected String createDescription() {
		return TextUtils.getText("filter_icon") + " \"" + getIconName() + "\"";
    }

	@Override
    protected String getName() {
	    return NAME;
    }
}
