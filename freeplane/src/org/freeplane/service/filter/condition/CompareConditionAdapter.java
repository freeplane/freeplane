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
package org.freeplane.service.filter.condition;

import org.freeplane.controller.Freeplane;
import org.freeplane.io.xml.n3.nanoxml.XMLElement;
import org.freeplane.main.Tools;

abstract class CompareConditionAdapter extends NodeCondition {
	static final String IGNORE_CASE = "ignore_case";
	static final String VALUE = "value";
	final private String conditionValue;
	final private boolean ignoreCase;

	CompareConditionAdapter(final String value, final boolean ignoreCase) {
		super();
		conditionValue = value;
		this.ignoreCase = ignoreCase;
	}

	@Override
	public void attributesToXml(final XMLElement child) {
		super.attributesToXml(child);
		child.setAttribute(CompareConditionAdapter.VALUE, conditionValue);
		child.setAttribute(CompareConditionAdapter.IGNORE_CASE, Tools
		    .BooleanToXml(ignoreCase));
	}

	protected int compareTo(final String nodeValue)
	        throws NumberFormatException {
		try {
			final int i2 = Integer.parseInt(conditionValue);
			final int i1 = Integer.parseInt(nodeValue);
			return i1 < i2 ? -1 : (i1 == i2 ? 0 : 1);
		}
		catch (final NumberFormatException fne) {
		};
		double d2;
		try {
			d2 = Double.parseDouble(conditionValue);
		}
		catch (final NumberFormatException fne) {
			return ignoreCase ? nodeValue.compareToIgnoreCase(conditionValue)
			        : nodeValue.compareTo(conditionValue);
		};
		final double d1 = Double.parseDouble(nodeValue);
		return Double.compare(d1, d2);
	}

	public String createDescription(final String attribute,
	                                final int comparationResult,
	                                final boolean succeed) {
		String simpleCondition;
		switch (comparationResult) {
			case -1:
				simpleCondition = succeed ? ConditionFactory.FILTER_LT
				        : ConditionFactory.FILTER_GE;
				break;
			case 0:
				simpleCondition = Freeplane
				    .getText(succeed ? ConditionFactory.FILTER_IS_EQUAL_TO
				            : ConditionFactory.FILTER_IS_NOT_EQUAL_TO);
				break;
			case 1:
				simpleCondition = succeed ? ConditionFactory.FILTER_GT
				        : ConditionFactory.FILTER_LE;
				break;
			default:
				throw new IllegalArgumentException();
		}
		return ConditionFactory.createDescription(attribute, simpleCondition,
		    conditionValue, ignoreCase);
	}
}
