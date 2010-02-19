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

import java.util.Set;

import javax.swing.JComponent;

import org.freeplane.core.filter.condition.ConditionFactory;
import org.freeplane.core.filter.condition.ISelectableCondition;
import org.freeplane.core.model.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 * Mar 7, 2009
 */
public abstract class ConnectorLabelCondition implements ISelectableCondition {
	static final String TEXT = "TEXT";
	static final String IGNORE_CASE = "IGNORE_CASE";
	private String description;
	final private String text;
	private JComponent renderer;
	final private boolean ignoreCase;

	protected boolean ignoreCase() {
    	return ignoreCase;
    }

	public ConnectorLabelCondition(final String text, boolean ignoreCase) {
		super();
		this.ignoreCase = ignoreCase;
		this.text = ignoreCase ? text.toLowerCase() : text;
	}

	public String getText() {
    	return text;
    }

	abstract protected boolean checkLink(final ConnectorModel connector);

	public boolean checkNode(final NodeModel node) {
		final NodeLinks nodeLinks = NodeLinks.getModel(node);
		if(nodeLinks != null){
			for(LinkModel l:nodeLinks.getLinks()){
				if(! (l instanceof ConnectorModel)){
					continue;
				}
				if(checkLink((ConnectorModel) l)){
					return true;
				}
			}
		}
		if(! node.hasID()){
			return false;
		}
		final MapLinks mapLinks = MapLinks.getLinks(node.getMap());
		if(mapLinks == null){
			return false;
		}
		final Set<LinkModel> targetLinks = mapLinks.get(node.getID());
		if(targetLinks == null){
			return false;
		}
		for(LinkModel l:targetLinks){
			if(! (l instanceof ConnectorModel)){
				continue;
			}
			if(checkLink((ConnectorModel) l)){
				return true;
			}
		}
		return false;
	}

	abstract protected String createDesctiption();

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
		child.setAttribute(TEXT, text);
		child.setAttribute(IGNORE_CASE, Boolean.toString(ignoreCase));
		element.addChild(child);
	}
}
