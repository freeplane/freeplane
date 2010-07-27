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


import org.freeplane.features.common.filter.condition.ASelectableCondition;
import org.freeplane.features.common.filter.condition.ISelectableCondition;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 * Mar 7, 2009
 */
public abstract class HyperLinkCondition extends ASelectableCondition implements ISelectableCondition {
	static final String TEXT = "TEXT";
	final private String hyperlink;
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

	public String getHyperlink() {
		return hyperlink;
	}
	
	protected void fillXML(XMLElement element){
		element.setAttribute(TEXT, hyperlink);
	}
}
