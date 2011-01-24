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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;

import org.freeplane.core.io.xml.TreeXmlWriter;
import org.freeplane.core.util.TextUtils;
import org.freeplane.n3.nanoxml.XMLElement;

abstract public class CompareConditionAdapter extends ASelectableCondition {
	public static final String MATCH_CASE = "MATCH_CASE";
	public static final String VALUE = "VALUE";
	public static final DateFormat shortDateFormat;
	static {
		shortDateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
		shortDateFormat.setLenient(true);
	}
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
		final ParsePosition parsePosition = new ParsePosition(0);
		conditionValue = shortDateFormat.parse(value, parsePosition);
		if(parsePosition.getErrorIndex() == -1 && parsePosition.getIndex() == value.length())
			return;
		conditionValue = value;
	}

	@Override
	public void fillXML(final XMLElement child) {
		super.fillXML(child);
		child.setAttribute(CompareConditionAdapter.VALUE, valueAsString());
		child.setAttribute(CompareConditionAdapter.MATCH_CASE, TreeXmlWriter.BooleanToXml(matchCase));
	}

	protected int signOfcompareTo(final String nodeValue) throws NumberFormatException {
		return Integer.signum(compareTo(nodeValue));
	}

	private int compareTo(final String nodeValue) {
	    if (conditionValue instanceof Integer) {
			try {
				final Integer value = Integer.valueOf(nodeValue);
				return compareTo(value);
			}
			catch (final NumberFormatException fne) {
			};
		}
		if (conditionValue instanceof Number) {
			try {
				final Double value = Double.valueOf(nodeValue);
				return compareTo(value);
			}
			catch (final NumberFormatException fne) {
			};
		}
		if (conditionValue instanceof Date) {
			final ParsePosition parsePosition = new ParsePosition(0);
			final Date value =  shortDateFormat.parse(nodeValue, parsePosition);
			if(parsePosition.getErrorIndex() == -1 && parsePosition.getIndex() == nodeValue.length())
				return compareTo(value);;
		}
		return matchCase ? nodeValue.compareTo(valueAsString()) : nodeValue
		    .compareToIgnoreCase(valueAsString());
    }

	protected int compareTo(final Double value) {
	    return value.compareTo(((Number) conditionValue).doubleValue());
    }

	protected int compareTo(final Integer value) {
	    return value.compareTo((Integer) conditionValue);
    }

	protected int compareTo(final Date value) {
	    return value.compareTo((Date) conditionValue);
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
		return ConditionFactory.createDescription(attribute, simpleCondition, valueAsString(), matchCase);
	}

	private String valueAsString() {
		if(conditionValue instanceof Date)
			return shortDateFormat.format((Date)conditionValue);
	    return conditionValue.toString();
    }

	public Comparable<?> getConditionValue() {
		return conditionValue;
	}
}
