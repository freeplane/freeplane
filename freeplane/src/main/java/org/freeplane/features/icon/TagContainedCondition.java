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

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.freeplane.core.ui.components.TagIcon;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.filter.condition.ConditionFactory;
import org.freeplane.features.filter.condition.JCondition;
import org.freeplane.features.map.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;

public class TagContainedCondition extends ASelectableCondition {
	static final String TAG = "TAG";
	static final String NAME = "tag_contained_condition";

	static ASelectableCondition load(final XMLElement element) {
		return new TagContainedCondition(new Tag(element.getAttribute(TagContainedCondition.TAG, "")));
	}

	final private Tag tag;

	public TagContainedCondition(final Tag tag) {
		this.tag = tag;
	}

	@Override
    public boolean checkNode(final NodeModel node) {
		return Tags.getTags(node).indexOf(tag) >= 0;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing
	 * .JList, java.lang.Object, int, boolean, boolean)
	 */
	@Override
    public JComponent createRendererComponent() {
		final JCondition component = new JCondition();
		final String text = TextUtils.getText("filter_tag") + ' ' + TextUtils.getText("filter_contains") + ' ';
		component.add(ConditionFactory.createConditionLabel(text));
		JLabel icon = new JLabel(new TagIcon(tag.getContent(), UITools.getUIFont()));
		component.add(icon);
		return component;
	}

	@Override
    public void fillXML(final XMLElement child) {
		child.setAttribute(TagContainedCondition.TAG, tag.getContent());
	}

	@Override
    protected String createDescription() {
		return TextUtils.getText("filter_tag") + " \"" + tag.getContent() + "\"";
    }

	@Override
    protected String getName() {
	    return NAME;
    }
}
