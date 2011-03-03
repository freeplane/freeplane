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

import java.util.Date;

import org.freeplane.core.io.xml.TreeXmlWriter;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.FreeplaneDate;
import org.freeplane.core.util.TextUtils;
import org.freeplane.n3.nanoxml.XMLElement;

abstract public class CompareConditionAdapter extends ASelectableCondition {
	public static final String MATCH_CASE = "MATCH_CASE";
	public static final String VALUE = "VALUE";
	private Comparable<?> conditionValue;
	final private boolean matchCase;
	private int comparisonResult;
	private boolean error;

	protected CompareConditionAdapter(final String value, final boolean matchCase) {
		super();
		this.matchCase = matchCase;
		final ResourceController resourceController = ResourceController.getResourceController();
		if(resourceController.getBooleanProperty("compare_as_number") && TextUtils.isNumber(value)) {
			Number number = TextUtils.toNumber(value);
			if(number instanceof Comparable<?>){
				conditionValue = (Comparable<?>) number;
			}
			return;
		}
		if(resourceController.getBooleanProperty("compare_as_date") ){
			final Date date = FreeplaneDate.toDate(value);
			if(date != null){
				conditionValue = date;
				return;
			}
		}
		conditionValue = value;
	}

	protected CompareConditionAdapter(final Double value) {
		super();
		this.matchCase = false;
		conditionValue = value;
	}

	protected CompareConditionAdapter(final Long value) {
		super();
		this.matchCase = false;
		conditionValue = value;
	}

	@Override
	public void fillXML(final XMLElement child) {
		super.fillXML(child);
		child.setAttribute(CompareConditionAdapter.VALUE, valueAsString());
		child.setAttribute(CompareConditionAdapter.MATCH_CASE, TreeXmlWriter.BooleanToXml(matchCase));
	}

	protected void compareTo(Object nodeContent, final String nodeText) throws NumberFormatException {
		error = false;
		comparisonResult = Integer.signum(compareToData(nodeContent, nodeText));
	}

	private int compareToData(Object content, final String text) {
		if (conditionValue instanceof Number) {
			try {
				Number number = TextUtils.toNumber(text); 
				if(number instanceof Long)
					return compareTo((Long)number);
				if(number instanceof Double)
					return compareTo((Double)number);
			}
			catch (final NumberFormatException fne) {
			};
			error = true;
			return 0;
		}
		if (conditionValue instanceof Date) {
			if(content instanceof Date){
				return ((Date) content).compareTo((Date)content);
			}
			final Date date = FreeplaneDate.toDate(text);
			if(date != null)
				return compareTo(date);
			error = true;
			return 0;
		}
		return matchCase ? text.compareTo(valueAsString()) : text
		    .compareToIgnoreCase(valueAsString());
    }

	protected int getComparisonResult() {
    	return comparisonResult;
    }

	protected boolean isComparisonOK() {
    	return ! error;
    }

	private int compareTo(final Double value) {
	    return value.compareTo(((Number) conditionValue).doubleValue());
    }

	protected int compareTo(final Long value) {
	    return value.compareTo((Long) conditionValue);
    }

	private int compareTo(final Date value) {
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
		return ConditionFactory.createDescription(attribute, simpleCondition, valueDescription(), matchCase);
	}

	private String valueAsString() {
		if (conditionValue instanceof Date)
			return FreeplaneDate.toStringISO((Date) conditionValue);
		return conditionValue.toString();
	}

	private String valueDescription() {
		return conditionValue.toString();
	}

	public Comparable<?> getConditionValue() {
		return conditionValue;
	}
}
