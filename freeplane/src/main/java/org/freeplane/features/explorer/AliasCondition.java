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
package org.freeplane.features.explorer;

import org.freeplane.features.filter.condition.StringConditionAdapter;
import org.freeplane.features.map.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 * Mar 7, 2009
 */
public abstract class AliasCondition extends StringConditionAdapter {
	static final String TEXT = "TEXT";
	final private String alias;
	public AliasCondition(final String alias, boolean matchCase, boolean matchAproximately, boolean ignoreDiacritics) {
		super(matchCase, matchAproximately, ignoreDiacritics);
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
	
    @Override
    protected Object conditionValue() {
        return alias;
    }

	
	protected void fillXML(XMLElement element){
	    super.fillXML(element);
		element.setAttribute(TEXT, alias);
	}
}
