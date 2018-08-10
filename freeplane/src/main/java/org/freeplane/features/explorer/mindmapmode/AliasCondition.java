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
package org.freeplane.features.explorer.mindmapmode;

import java.net.URI;

import org.freeplane.features.attribute.Attribute;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.map.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 * Mar 7, 2009
 */
public abstract class AliasCondition extends ASelectableCondition {
	static final String TEXT = "TEXT";
	final private String alias;
	public AliasCondition(final String alias) {
		super();
		this.alias = alias;
	}

	abstract protected boolean checkAlias(final String alias);

	public boolean checkNode(final NodeModel node) {
		final String alias = NodeAlias.getAlias(node);
		return !alias.isEmpty() && checkAlias(alias);
	}

	public String getAlias() {
		return alias;
	}
	
	protected void fillXML(XMLElement element){
		element.setAttribute(TEXT, alias);
	}
}
