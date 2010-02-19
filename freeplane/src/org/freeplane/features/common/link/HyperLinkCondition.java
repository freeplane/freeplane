/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry Polivaev
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
package org.freeplane.features.common.link;

import java.net.URI;

import javax.swing.JComponent;

import org.freeplane.core.filter.condition.ConditionFactory;
import org.freeplane.core.filter.condition.ISelectableCondition;
import org.freeplane.core.model.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 * Mar 7, 2009
 */
public abstract class HyperLinkCondition implements ISelectableCondition {
	static final String TEXT = "TEXT";
	private String description;
	final private String hyperlink;
	private JComponent renderer;

	public HyperLinkCondition(final String hyperlink) {
		super();
		this.hyperlink = hyperlink;
	}

	abstract protected boolean checkLink(final URI nodeLink);

	public boolean checkNode(final NodeModel node) {
		final URI nodeLink = NodeLinks.getValidLink(node);
		if (nodeLink == null) {
			return false;
		}
		return checkLink(nodeLink);
	}

	abstract protected String createDesctiption();

	public String getHyperlink() {
		return hyperlink;
	}

	public JComponent getListCellRendererComponent() {
		if (renderer == null) {
			renderer = ConditionFactory.createCellRendererComponent(toString());
		}
		return renderer;
	}

	abstract String getName();

	@Override
	public String toString() {
		if (description == null) {
			description = createDesctiption();
		}
		return description;
	}

	public void toXml(final XMLElement element) {
		final XMLElement child = new XMLElement();
		child.setName(getName());
		child.setAttribute(TEXT, hyperlink);
		element.addChild(child);
	}
}
