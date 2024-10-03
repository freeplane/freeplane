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

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;

import org.freeplane.core.resources.TranslatedObject;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 */
public class ConditionFactory {
    public enum ConditionOperator {
        FILTER_CONTAINS("filter_contains", "⋯", "⋯"),
        FILTER_CONTAINS_WORDWISE("filter_contains_wordwise", "⋅", "⋅"),
        FILTER_DOES_NOT_EXIST("filter_does_not_exist", "!"),
        FILTER_EXIST("filter_exist", ""),
        FILTER_GE(">=", "≥"),
        FILTER_GT(">", ">"),
        FILTER_IS_EQUAL_TO("filter_is_equal_to", "="),
        FILTER_STARTS_WITH("filter_starts_with", "", "*"),
        FILTER_IS_NOT_EQUAL_TO("filter_is_not_equal_to", "≠"),
        FILTER_LE("<=", "≤"),
        FILTER_LT("<", "<"),
        FILTER_REGEXP("filter_regexp_matches", "/", "/"),
        EMPTY("", "", "", "");

        private final String persistedValue;
        private final String operator;
        private final String openingValueDelimiter;
        private final String closingValueDelimiter;

        private ConditionOperator(String persistedValue, String operator) {
            this(persistedValue, operator, "", "");
        }
        private ConditionOperator(String persistedValue,
                String openingValueDelimiter, String closingValueDelimiter) {
            this(persistedValue, " ", openingValueDelimiter, closingValueDelimiter);
        }
        private ConditionOperator(String persistedValue, String operator,
                String openingValueDelimiter, String closingValueDelimiter) {
            this.persistedValue = persistedValue;
            this.operator = operator;
            this.openingValueDelimiter = openingValueDelimiter;
            this.closingValueDelimiter = closingValueDelimiter;
        }

        public String getPersistedValue() {
            return persistedValue;
        }
        public String getOperator() {
            return operator;
        }
        public String getOpeningValueDelimiter() {
            return openingValueDelimiter;
        }
        public String getClosingValueDelimiter() {
            return closingValueDelimiter;
        }
    }

    public enum ConditionOption {
        FILTER_MATCH_CASE("filter_match_case", "Aa"),
        FILTER_IGNORE_DIACRITICS("filter_ignore_diacritics", "Ã"),
        FILTER_MATCH_APPROX("filter_match_approximately", "≈");


        private final String persistedValue;
        private final String displayedOption;



        private ConditionOption(String persistedValue, String displayedOption) {
            this.persistedValue = persistedValue;
            this.displayedOption = displayedOption;
        }

        String getPersistedValue() {
            return persistedValue;
        }

        public String getDisplayedOption() {
            return displayedOption;
        }


    }
    public static final String FILTER_CONTAINS = ConditionOperator.FILTER_CONTAINS.getPersistedValue();
    public static final String FILTER_CONTAINS_WORDWISE = ConditionOperator.FILTER_CONTAINS_WORDWISE.getPersistedValue();
	public static final String FILTER_DOES_NOT_EXIST = ConditionOperator.FILTER_DOES_NOT_EXIST.getPersistedValue();
	public static final String FILTER_EXIST = ConditionOperator.FILTER_EXIST.getPersistedValue();
	public static final String FILTER_GE = ConditionOperator.FILTER_GE.getPersistedValue();
	public static final String FILTER_GT = ConditionOperator.FILTER_GT.getPersistedValue();
	public static final String FILTER_IS_EQUAL_TO = ConditionOperator.FILTER_IS_EQUAL_TO.getPersistedValue();
	public static final String FILTER_STARTS_WITH = ConditionOperator.FILTER_STARTS_WITH.getPersistedValue();
	public static final String FILTER_IS_NOT_EQUAL_TO = ConditionOperator.FILTER_IS_NOT_EQUAL_TO.getPersistedValue();
	public static final String FILTER_LE = ConditionOperator.FILTER_LE.getPersistedValue();
	public static final String FILTER_LT = ConditionOperator.FILTER_LT.getPersistedValue();
	public static final String FILTER_REGEXP = ConditionOperator.FILTER_REGEXP.getPersistedValue();

	public static final String FILTER_MATCH_CASE = ConditionOption.FILTER_MATCH_CASE.getPersistedValue();
	public static final String FILTER_MATCH_APPROX = ConditionOption.FILTER_MATCH_APPROX.getPersistedValue();
	public static final String FILTER_IGNORE_DIACRITICS = ConditionOption.FILTER_IGNORE_DIACRITICS.getPersistedValue();


	private static final DecoratedConditionFactory DECORATED_CONDITION_FACTORY = new DecoratedConditionFactory();

	final private SortedMap<Integer, IElementaryConditionController> conditionControllers;

	public ConditionFactory() {
		conditionControllers = new TreeMap<Integer, IElementaryConditionController>();
	}

	public void addConditionController(final int position, final IElementaryConditionController controller) {
		final IElementaryConditionController old = conditionControllers.put(new Integer(position), controller);
		assert old == null;
	}

	public Iterator<IElementaryConditionController> conditionIterator() {
		final Iterator<IElementaryConditionController> iterator = conditionControllers.values().iterator();
		return iterator;
	}

	public ASelectableCondition createCondition(final Object selectedItem, final TranslatedObject simpleCond,
	                                            final Object value, final boolean matchCase,
	                                            final boolean matchApproximately, boolean ignoreDiacritics) {
		return getConditionController(selectedItem).createCondition(selectedItem, simpleCond, value, matchCase,
				matchApproximately, ignoreDiacritics);
	}

	public IElementaryConditionController getConditionController(final Object item) {
		final Iterator<IElementaryConditionController> iterator = conditionIterator();
		while (iterator.hasNext()) {
			final IElementaryConditionController next = iterator.next();
			if (next.canHandle(item)) {
				return next;
			}
		}
		throw new NoSuchElementException();
	}

	public ASelectableCondition loadCondition(final XMLElement element) {
		final ASelectableCondition condition = loadAnonymousCondition(element);
		if(condition != null){
		    final String userName = element.getAttribute("user_name", null);
		    condition.setUserName(userName);
		}
		return condition;
	}

	private ASelectableCondition loadAnonymousCondition(final XMLElement element) {
	    ASelectableCondition decoratorCondition = DECORATED_CONDITION_FACTORY.createRelativeCondition(this, element);
	    if (decoratorCondition != null) {
			return decoratorCondition;
		}
		if (element.getName().equalsIgnoreCase(ConjunctConditions.NAME)) {
			return ConjunctConditions.load(this, element);
		}
		if (element.getName().equalsIgnoreCase(DisjunctConditions.NAME)) {
			return DisjunctConditions.load(this, element);
		}
		final Iterator<IElementaryConditionController> conditionIterator = conditionIterator();
		while (conditionIterator.hasNext()) {
			final ASelectableCondition condition = conditionIterator.next().loadCondition(element);
			if (condition != null) {
				return condition;
			}
		}
		return null;
    }
}
