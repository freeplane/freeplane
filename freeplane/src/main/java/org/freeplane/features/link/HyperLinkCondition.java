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
package org.freeplane.features.link;

import java.net.URI;

import org.freeplane.features.attribute.Attribute;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.filter.condition.StringConditionAdapter;
import org.freeplane.features.map.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 * Mar 7, 2009
 */
public abstract class HyperLinkCondition extends StringConditionAdapter {
	static final String TEXT = "TEXT";
	final private String hyperlink;
	public HyperLinkCondition(final String hyperlink, final boolean matchCase, final boolean matchApproximately, boolean ignoreDiacritics) {
		super(matchCase, matchApproximately, ignoreDiacritics);
		this.hyperlink = hyperlink;
	}

	abstract protected boolean checkLink(final URI nodeLink);

	public boolean checkNode(final NodeModel node) {
		final URI nodeLink = NodeLinks.getValidLink(node);
		if (nodeLink != null && checkLink(nodeLink))
			return true;
		final NodeAttributeTableModel attributes = NodeAttributeTableModel.getModel(node);
		if(attributes == null){
			return false;
		}
		final int rowCount = attributes.getRowCount();
		for(int i = 0; i < rowCount; i++){
			final Attribute attribute = attributes.getAttribute(i);
			final Object value = attribute.getValue();
			if (value instanceof URI && checkLink((URI)value))
				return true;
		}
		return false;
	}

	public String getHyperlink() {
		return hyperlink;
	}
	
	@Override
    protected Object conditionValue() {
	    return hyperlink;
    }

    protected void fillXML(XMLElement element){
        super.fillXML(element);
		element.setAttribute(TEXT, hyperlink);
	}
}
