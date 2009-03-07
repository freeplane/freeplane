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

import javax.swing.JComponent;

import org.freeplane.core.filter.condition.ConditionFactory;
import org.freeplane.core.filter.condition.ICondition;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.NamedObject;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 * Mar 7, 2009
 */
public abstract class HyperLinkCondition implements ICondition{
	public HyperLinkCondition(String hyperlink) {
	    super();
	    this.hyperlink = hyperlink;
    }

	final private String hyperlink;

	private JComponent renderer;

	private String description; 

	public boolean checkNode(NodeModel node) {
	    final NodeLinks model = NodeLinks.getModel(node);
	    if(model == null){
	    	return false;
	    }
	    final String nodeLink = model.getHyperLink();
	    if(nodeLink == null){
	    	return false;
	    }
	    return checkLink(nodeLink);
    }

	abstract protected boolean checkLink(final String nodeLink) ;
	public JComponent getListCellRendererComponent() {
		if (renderer == null) {
			renderer = ConditionFactory.createCellRendererComponent(toString());
		}
		return renderer;
	}


	abstract protected String createDesctiption();

	@Override
	public String toString() {
		if (description == null) {
			description = createDesctiption();
		}
		return description;
	}

	public String getHyperlink() {
	    return hyperlink;
    }
	
	abstract String getName();

	public void toXml(final XMLElement element) {
		final XMLElement child = new XMLElement();
		child.setName(getName());
		child.setAttribute("text", hyperlink);
		element.addChild(child);
	}
}

