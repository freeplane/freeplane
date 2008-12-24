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
package org.freeplane.controller.filter.condition;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.freeplane.controller.Controller;
import org.freeplane.controller.resources.NamedObject;
import org.freeplane.io.xml.n3.nanoxml.XMLElement;
import org.freeplane.map.attribute.filter.AttributeConditionController;
import org.freeplane.map.icon.filter.IconConditionController;
import org.freeplane.map.text.filter.NodeConditionController;

/**
 * @author Dimitry Polivaev
 */
public class ConditionFactory {
	public static final String FILTER_CONTAINS = "filter_contains";
	public static final String FILTER_DOES_NOT_EXIST = "filter_does_not_exist";
	public static final String FILTER_EXIST = "filter_exist";
	public static final String FILTER_GE = ">=";
	public static final String FILTER_GT = ">";
	public static final String FILTER_IGNORE_CASE = "filter_ignore_case";
	public static final String FILTER_IS_EQUAL_TO = "filter_is_equal_to";
	public static final String FILTER_IS_NOT_EQUAL_TO = "filter_is_not_equal_to";
	public static final String FILTER_LE = "<=";
	public static final String FILTER_LT = "<";

	static public JComponent createCellRendererComponent(final String description) {
		final JCondition component = new JCondition();
		final JLabel label = new JLabel(description);
		component.add(label);
		return component;
	}

	public static String createDescription(final String attribute, final String simpleCondition,
	                                       final String value, final boolean ignoreCase) {
		final String description = attribute
		        + " "
		        + simpleCondition
		        + (value != null ? " \"" + value + "\"" : "")
		        + (ignoreCase && value != null ? ", "
		                + Controller.getText(ConditionFactory.FILTER_IGNORE_CASE) : "");
		return description;
	}

	final private SortedMap<Integer, IElementaryConditionController> conditionControllers;

	/**
	 *
	 */
	public ConditionFactory() {
		conditionControllers = new TreeMap<Integer, IElementaryConditionController>();
		addConditionController(0, new NodeConditionController());
		addConditionController(1, new IconConditionController());
		addConditionController(2, new AttributeConditionController());
	}

	public void addConditionController(final int position,
	                                   final IElementaryConditionController controller) {
		final IElementaryConditionController old = conditionControllers.put(new Integer(position),
		    controller);
		assert old == null;
	}

	public Iterator<IElementaryConditionController> conditionIterator() {
		final Iterator<IElementaryConditionController> iterator = conditionControllers.values()
		    .iterator();
		return iterator;
	}

	public ICondition createCondition(final Object selectedItem, final NamedObject simpleCond,
	                                  final Object value, final boolean ignoreCase) {
		return getConditionController(selectedItem).createCondition(selectedItem, simpleCond,
		    value, ignoreCase);
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

	public ICondition loadCondition(final XMLElement element) {
		if (element.getName().equalsIgnoreCase(ConditionNotSatisfiedDecorator.NAME)) {
			return ConditionNotSatisfiedDecorator.load(element);
		}
		if (element.getName().equalsIgnoreCase(ConjunctConditions.NAME)) {
			return ConjunctConditions.load(element);
		}
		if (element.getName().equalsIgnoreCase(DisjunctConditions.NAME)) {
			return DisjunctConditions.load(element);
		}
		final Iterator<IElementaryConditionController> conditionIterator = conditionIterator();
		while (conditionIterator.hasNext()) {
			final ICondition condition = conditionIterator.next().loadCondition(element);
			if (condition != null) {
				return condition;
			}
		}
		return null;
	}

	public IElementaryConditionController removeConditionController(
	                                                                final int position,
	                                                                final IElementaryConditionController controller) {
		final IElementaryConditionController old = conditionControllers
		    .remove(new Integer(position));
		return old;
	}
}
