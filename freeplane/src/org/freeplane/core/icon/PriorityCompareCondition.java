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

import java.util.List;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.freeplane.core.filter.condition.CompareConditionAdapter;
import org.freeplane.core.filter.condition.ISelectableCondition;
import org.freeplane.core.filter.condition.JCondition;
import org.freeplane.core.icon.factory.IconStoreFactory;
import org.freeplane.core.io.xml.TreeXmlReader;
import org.freeplane.core.io.xml.TreeXmlWriter;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.n3.nanoxml.XMLElement;

public class PriorityCompareCondition extends CompareConditionAdapter {
	static final String COMPARATION_RESULT = "COMPARATION_RESULT";
	static final String NAME = "priority_compare_condition";
	static final String SUCCEED = "SUCCEED";
	static final String VALUE = "VALUE";
	private static final IconStore STORE = IconStoreFactory.create();

	static ISelectableCondition load(final XMLElement element) {
		return new PriorityCompareCondition(element.getAttribute(PriorityCompareCondition.VALUE, null), Integer
		    .parseInt(element.getAttribute(PriorityCompareCondition.COMPARATION_RESULT, null)), TreeXmlReader
		    .xmlToBoolean(element.getAttribute(PriorityCompareCondition.SUCCEED, null)));
	}

	final private int comparationResult;
	final private boolean succeed;

	PriorityCompareCondition(final String value, final int comparationResult, final boolean succeed) {
		super(value, false);
		this.comparationResult = comparationResult;
		this.succeed = succeed;
		final JCondition renderer = new JCondition();
		final String string = toString();
		final JLabel label = new JLabel(string.substring(0, string.length() - 3));
		label.setIcon(STORE.getMindIcon(getIconName()).getIcon());
		label.setHorizontalTextPosition(SwingConstants.LEFT);
		renderer.add(label);
		setListCellRendererComponent(renderer);
	}

	public boolean checkNode(final NodeModel node) {
		final List<MindIcon> icons = node.getIcons();
		for (final MindIcon icon : icons) {
			final String iconName = icon.getFileName();
			if (iconName.length() != 10) {
				continue;
			}
			if (!iconName.startsWith("full-")) {
				continue;
			}
			if (iconName.charAt(5) < '0' || iconName.charAt(5) > '9') {
				continue;
			}
			if (succeed == (compareTo(iconName.substring(5, 6)) == comparationResult)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected String createDesctiption() {
		final String priorityCondition = ResourceBundles.getText(PriorityConditionController.FILTER_PRIORITY);
		return super.createDescription(priorityCondition, comparationResult, succeed);
	}

	private String getIconName() {
		return "full-" + getConditionValue().toString();
	}

	public void toXml(final XMLElement element) {
		final XMLElement child = new XMLElement();
		child.setName(PriorityCompareCondition.NAME);
		super.attributesToXml(child);
		child.setAttribute(PriorityCompareCondition.COMPARATION_RESULT, Integer.toString(comparationResult));
		child.setAttribute(PriorityCompareCondition.SUCCEED, TreeXmlWriter.BooleanToXml(succeed));
		element.addChild(child);
	}
}
