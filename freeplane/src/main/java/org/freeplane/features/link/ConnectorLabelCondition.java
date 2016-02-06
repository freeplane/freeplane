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

import java.util.Set;

import org.freeplane.features.filter.StringMatchingStrategy;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.map.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 * Mar 7, 2009
 */
public abstract class ConnectorLabelCondition extends ASelectableCondition {
	static final String TEXT = "TEXT";
	static final String MATCH_CASE = "MATCH_CASE";
    static final String MATCH_APPROXIMATELY = "MATCH_APPROXIMATELY";
	final private String text;
	final private boolean matchCase;
	final private boolean matchApproximately;
	final private StringMatchingStrategy stringMatchingStrategy;

	protected boolean matchCase() {
		return matchCase;
	}
	
	protected boolean matchApproximately() {
		return matchApproximately;
	}
	
	protected StringMatchingStrategy getStringMatchingStrategy()
	{
		return stringMatchingStrategy;
	}

	public ConnectorLabelCondition(final String text, final boolean matchCase,
			final boolean matchApproximately) {
		super();
		this.matchCase = matchCase;
		//this.text = matchCase ? text : text.toLowerCase();
		this.text = text;
		this.matchApproximately = matchApproximately;
		stringMatchingStrategy = matchApproximately ? StringMatchingStrategy.DEFAULT_APPROXIMATE_STRING_MATCHING_STRATEGY :
			StringMatchingStrategy.EXACT_STRING_MATCHING_STRATEGY;
	}

	public String getText() {
		return text;
	}

	abstract protected boolean checkLink(final ConnectorModel connector);

	public boolean checkNode(final NodeModel node) {
		final NodeLinks nodeLinks = NodeLinks.getLinkExtension(node);
		if (nodeLinks != null) {
			for (final NodeLinkModel l : nodeLinks.getLinks()) {
				if (!(l instanceof ConnectorModel)) {
					continue;
				}
				if (checkLink((ConnectorModel) l)) {
					return true;
				}
			}
		}
		if (!node.hasID()) {
			return false;
		}
		final MapLinks mapLinks = MapLinks.getLinks(node.getMap());
		if (mapLinks == null) {
			return false;
		}
		final Set<NodeLinkModel> targetLinks = mapLinks.get(node.getID());
		if (targetLinks == null) {
			return false;
		}
		for (final NodeLinkModel l : targetLinks) {
			if (!(l instanceof ConnectorModel)) {
				continue;
			}
			if (checkLink((ConnectorModel) l)) {
				return true;
			}
		}
		return false;
	}

	abstract protected String createDescription();

	protected abstract String getName();

	protected void fillXML(final XMLElement child) {
		child.setAttribute(TEXT, text);
		child.setAttribute(MATCH_CASE, Boolean.toString(matchCase));
		child.setAttribute(MATCH_APPROXIMATELY, Boolean.toString(matchApproximately));
	}
}
