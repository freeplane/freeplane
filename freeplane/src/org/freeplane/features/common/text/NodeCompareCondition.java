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
package org.freeplane.features.common.text;

import org.freeplane.core.filter.condition.CompareConditionAdapter;
import org.freeplane.core.filter.condition.ISelectableCondition;
import org.freeplane.core.io.xml.TreeXmlReader;
import org.freeplane.core.io.xml.TreeXmlWriter;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.n3.nanoxml.XMLElement;

public class NodeCompareCondition extends CompareConditionAdapter {
	static final String COMPARATION_RESULT = "COMPARATION_RESULT";
	static final String NAME = "node_compare_condition";
	static final String SUCCEED = "SUCCEED";
	static final String VALUE = "VALUE";

	static ISelectableCondition load(final XMLElement element) {
		return new NodeCompareCondition(element.getAttribute(NodeCompareCondition.VALUE, null), TreeXmlReader
		    .xmlToBoolean(element.getAttribute(CompareConditionAdapter.IGNORE_CASE, null)), Integer.parseInt(element
		    .getAttribute(NodeCompareCondition.COMPARATION_RESULT, null)), TreeXmlReader.xmlToBoolean(element
		    .getAttribute(NodeCompareCondition.SUCCEED, null)));
	}

	final private int comparationResult;
	final private boolean succeed;

	NodeCompareCondition(final String value, final boolean ignoreCase, final int comparationResult,
	                     final boolean succeed) {
		super(value, ignoreCase);
		this.comparationResult = comparationResult;
		this.succeed = succeed;
	}

	public boolean checkNode(final NodeModel node) {
		final String text = node.getPlainTextContent();
		return checkText(text);
	}

	private boolean checkText(final String plainTextContent) {
		try {
			return succeed == (compareTo(plainTextContent) == comparationResult);
		}
		catch (final NumberFormatException e) {
			return false;
		}
	}

	@Override
	protected String createDesctiption() {
		final String nodeCondition = ResourceBundles.getText(NodeConditionController.FILTER_NODE);
		return super.createDescription(nodeCondition, comparationResult, succeed);
	}

	public void toXml(final XMLElement element) {
		final XMLElement child = new XMLElement();
		child.setName(NodeCompareCondition.NAME);
		super.attributesToXml(child);
		child.setAttribute(NodeCompareCondition.COMPARATION_RESULT, Integer.toString(comparationResult));
		child.setAttribute(NodeCompareCondition.SUCCEED, TreeXmlWriter.BooleanToXml(succeed));
		element.addChild(child);
	}
}
