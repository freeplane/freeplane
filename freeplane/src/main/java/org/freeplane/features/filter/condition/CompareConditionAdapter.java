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
package org.freeplane.features.filter.condition;

import java.util.Date;

import org.freeplane.core.io.xml.TreeXmlWriter;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.TextUtils;
import org.freeplane.core.util.TypeReference;
import org.freeplane.features.filter.StringMatchingStrategy;
import org.freeplane.features.format.FormattedDate;
import org.freeplane.features.format.FormattedNumber;
import org.freeplane.features.format.IFormattedObject;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * Adapter for Conditions which compare values (</<=/>/>=/=/!=)
 * 
 * @author ?
 *
 */
abstract public class CompareConditionAdapter extends ASelectableCondition {
	public static final String OBJECT = "OBJECT";
	public static final String MATCH_CASE = "MATCH_CASE";
	public static final String MATCH_APPROXIMATELY = "MATCH_APPROXIMATELY";
	public static final String VALUE = "VALUE";
	private Comparable<?> conditionValue;
	final private boolean matchCase;
	final protected boolean matchApproximately;
	final StringMatchingStrategy stringMatchingStrategy;
	private int comparisonResult;
	private boolean error;
	
	abstract public boolean isEqualityCondition();

	@SuppressWarnings("deprecation")
	protected CompareConditionAdapter(final Object value, final boolean matchCase, final boolean matchApproximately) {
		super();
		this.matchCase = matchCase;
		this.matchApproximately = matchApproximately;
		stringMatchingStrategy = matchApproximately ? StringMatchingStrategy.DEFAULT_APPROXIMATE_STRING_MATCHING_STRATEGY :
			StringMatchingStrategy.EXACT_STRING_MATCHING_STRATEGY;
		final ResourceController resourceController = ResourceController.getResourceController();
		if(value instanceof String && resourceController.getBooleanProperty("compare_as_number") && TextUtils.isNumber((String) value)) {
			Number number = TextUtils.toNumber((String) value);
			if(number instanceof Comparable<?>){
				conditionValue = (Comparable<?>) number;
			}
			return;
		}
		if(value instanceof FormattedNumber){
		    conditionValue = (FormattedNumber)value;
		    return;
		}
		    
		if(value instanceof FormattedDate){
			final FormattedDate date = (FormattedDate) value;
			if(date.containsTime() || 
					date.getHours() == 0 && date.getMinutes() == 0 && date.getSeconds() == 0) {
				conditionValue = date;
            }
            else{
                final Date reducedDate = new Date(date.getYear(), date.getMonth(), date.getDate());
	 	            conditionValue = new FormattedDate(reducedDate.getTime(), date.getDateFormat());
			}
			return;
		}
		conditionValue = value.toString();
		
	}

	protected CompareConditionAdapter(final Double value) {
		super();
		this.matchCase = false;
		this.matchApproximately = false;
		conditionValue = value;
		stringMatchingStrategy = matchApproximately ? StringMatchingStrategy.DEFAULT_APPROXIMATE_STRING_MATCHING_STRATEGY :
			StringMatchingStrategy.EXACT_STRING_MATCHING_STRATEGY;
	}

	protected CompareConditionAdapter(final Long value) {
		super();
		this.matchCase = false;
		this.matchApproximately = false;
		conditionValue = value;
		stringMatchingStrategy = matchApproximately ? StringMatchingStrategy.DEFAULT_APPROXIMATE_STRING_MATCHING_STRATEGY :
			StringMatchingStrategy.EXACT_STRING_MATCHING_STRATEGY;
	}

	@Override
	public void fillXML(final XMLElement child) {
		super.fillXML(child);
		if(conditionValue instanceof IFormattedObject){
			child.setAttribute(OBJECT, TypeReference.toSpec(conditionValue));
		}
		else
			child.setAttribute(CompareConditionAdapter.VALUE, conditionValue.toString());
		child.setAttribute(CompareConditionAdapter.MATCH_CASE, TreeXmlWriter.BooleanToXml(matchCase));
		child.setAttribute(CompareConditionAdapter.MATCH_APPROXIMATELY, TreeXmlWriter.BooleanToXml(matchApproximately));
	}

	protected void compareTo(final Object transformedContent){
		error = false;
		comparisonResult = Integer.signum(compareToData(transformedContent));
	}

	private int compareToData(final Object transformedContent) {
	    if (conditionValue instanceof FormattedNumber && transformedContent instanceof Number){
	        return -((FormattedNumber)conditionValue).compareTo((Number)transformedContent);
	    }
		if (conditionValue instanceof Number && transformedContent instanceof String) {
			try {
				Number number = TextUtils.toNumber((String)transformedContent); 
		        if (conditionValue instanceof FormattedNumber){
		            return -((FormattedNumber)conditionValue).compareTo(number);
		        }
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
		if (conditionValue instanceof FormattedDate) {
			if (transformedContent instanceof Date) {
				return compareTo((Date)transformedContent);
			}
			error = true;
			return 0;
		}
		
		final String valueAsString = conditionValue.toString();
		final String text = transformedContent.toString();
		if (isEqualityCondition())
		{
			return stringMatchingStrategy.matches(valueAsString, text, false, matchCase) ? 0 : -1;
		}
		else
		{
			return matchCase ? text.compareTo(valueAsString) : text.compareToIgnoreCase(valueAsString);
		}
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

	@SuppressWarnings("deprecation")
    private int compareTo(final Date value) {
		if (((FormattedDate) conditionValue).containsTime() || (value.getHours() == 0 && value.getMinutes() == 0 && value.getSeconds() == 0))
			return value.compareTo((Date) conditionValue);
		return new Date(value.getYear(), value.getMonth(), value.getDate()).compareTo((Date) conditionValue);
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
		return ConditionFactory.createDescription(attribute, simpleCondition, valueDescription(), matchCase, matchApproximately);
	}

	private String valueDescription() {
		return conditionValue.toString();
	}

	public Comparable<?> getConditionValue() {
		return conditionValue;
	}
}
