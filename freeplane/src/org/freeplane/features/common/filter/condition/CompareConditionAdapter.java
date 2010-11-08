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
package org.freeplane.features.common.filter.condition;

import org.freeplane.core.io.xml.TreeXmlWriter;
import org.freeplane.core.util.TextUtils;
import org.freeplane.n3.nanoxml.XMLElement;

abstract public class CompareConditionAdapter extends ASelectableCondition {
	public static final String MATCH_CASE = "MATCH_CASE";
	public static final String VALUE = "VALUE";
	private Comparable<?> conditionValue;
	final private boolean matchCase;

	protected CompareConditionAdapter(final String value, final boolean matchCase) {
		super();
		this.matchCase = matchCase;
		try {
			conditionValue = Integer.valueOf(value);
			return;
		}
		catch (final NumberFormatException fne) {
		};
		try {
			conditionValue = Double.valueOf(value);
			return;
		}
		catch (final NumberFormatException fne) {
		};
		conditionValue = value;
	}

	@Override
	public void fillXML(final XMLElement child) {
		super.fillXML(child);
		child.setAttribute(CompareConditionAdapter.VALUE, conditionValue.toString());
		child.setAttribute(CompareConditionAdapter.MATCH_CASE, TreeXmlWriter.BooleanToXml(matchCase));
	}

	protected int compareTo(final String nodeValue) throws NumberFormatException {
		if (conditionValue instanceof Integer) {
			try {
				final Integer value = Integer.valueOf(nodeValue);
				return compareTo(value);
			}
			catch (final NumberFormatException fne) {
			};
			final Double value = Double.valueOf(nodeValue);
			return compareTo(value);
		}
		else if (conditionValue instanceof Double) {
			final Double value = Double.valueOf(nodeValue);
			return compareTo(value);
		}
		return matchCase ? nodeValue.compareTo(conditionValue.toString()) : nodeValue
		    .compareToIgnoreCase(conditionValue.toString());
	}

	protected int compareTo(final Double value) {
	    return value.compareTo(new Double((Integer) conditionValue));
    }

	protected int compareTo(final Integer value) {
	    return value.compareTo((Integer) conditionValue);
    }

	public String createDescription(final String attribute, final int comparationResult, final boolean succeed) {
		String simpleCondition;
		switch (comparationResult) {
			case -1:
				simpleCondition = succeed ? ConditionFactory.FILTER_LT : ConditionFactory.FILTER_GE;
				break;
			case 0:
				simpleCondition = TextUtils.getText(succeed ? ConditionFactory.FILTER_IS_EQUAL_TO
				        : ConditionFactory.FILTER_IS_NOT_EQUAL_TO);
				break;
			case 1:
				simpleCondition = succeed ? ConditionFactory.FILTER_GT : ConditionFactory.FILTER_LE;
				break;
			default:
				throw new IllegalArgumentException();
		}
		return ConditionFactory.createDescription(attribute, simpleCondition, conditionValue.toString(), matchCase);
	}

	public Comparable<?> getConditionValue() {
		return conditionValue;
	}
}
