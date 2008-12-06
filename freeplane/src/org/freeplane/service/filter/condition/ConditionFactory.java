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

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.freeplane.controller.Freeplane;
import org.freeplane.controller.resources.NamedObject;
import org.freeplane.io.xml.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 */
public class ConditionFactory {
	static final String FILTER_CONTAINS = "filter_contains";
	static final String FILTER_DOES_NOT_EXIST = "filter_does_not_exist";
	static final String FILTER_EXIST = "filter_exist";
	static final String FILTER_GE = ">=";
	static final String FILTER_GT = ">";
	static final String FILTER_ICON = "filter_icon";
	static final String FILTER_IGNORE_CASE = "filter_ignore_case";
	static final String FILTER_IS_EQUAL_TO = "filter_is_equal_to";
	static final String FILTER_IS_NOT_EQUAL_TO = "filter_is_not_equal_to";
	static final String FILTER_LE = "<=";
	static final String FILTER_LT = "<";
	static final String FILTER_NODE = "filter_node";

	static public JComponent createCellRendererComponent(
	                                                     final String description) {
		final JCondition component = new JCondition();
		final JLabel label = new JLabel(description);
		component.add(label);
		return component;
	}

	static String createDescription(final String attribute,
	                                final String simpleCondition,
	                                final String value, final boolean ignoreCase) {
		final String description = attribute
		        + " "
		        + simpleCondition
		        + (value != null ? " \"" + value + "\"" : "")
		        + (ignoreCase && value != null ? ", "
		                + Freeplane.getController().getResourceController()
		                    .getResourceString(
		                        ConditionFactory.FILTER_IGNORE_CASE) : "");
		return description;
	}

	/**
	 *
	 */
	public ConditionFactory() {
	}

	public ICondition createAttributeCondition(
	                                           final String attribute,
	                                           final NamedObject simpleCondition,
	                                           final String value,
	                                           final boolean ignoreCase) {
		if (simpleCondition.equals(ConditionFactory.FILTER_EXIST)) {
			return new AttributeExistsCondition(attribute);
		}
		if (simpleCondition.equals(ConditionFactory.FILTER_DOES_NOT_EXIST)) {
			return new AttributeNotExistsCondition(attribute);
		}
		if (ignoreCase) {
			if (simpleCondition.equals(ConditionFactory.FILTER_IS_EQUAL_TO)) {
				return new AttributeCompareCondition(attribute, value, true, 0,
				    true);
			}
			if (simpleCondition.equals(ConditionFactory.FILTER_IS_NOT_EQUAL_TO)) {
				return new AttributeCompareCondition(attribute, value, true, 0,
				    false);
			}
			if (simpleCondition.equals(ConditionFactory.FILTER_GT)) {
				return new AttributeCompareCondition(attribute, value, true, 1,
				    true);
			}
			if (simpleCondition.equals(ConditionFactory.FILTER_GE)) {
				return new AttributeCompareCondition(attribute, value, true,
				    -1, false);
			}
			if (simpleCondition.equals(ConditionFactory.FILTER_LT)) {
				return new AttributeCompareCondition(attribute, value, true,
				    -1, true);
			}
			if (simpleCondition.equals(ConditionFactory.FILTER_LE)) {
				return new AttributeCompareCondition(attribute, value, true, 1,
				    false);
			}
		}
		else {
			if (simpleCondition.equals(ConditionFactory.FILTER_IS_EQUAL_TO)) {
				return new AttributeCompareCondition(attribute, value, false,
				    0, true);
			}
			if (simpleCondition.equals(ConditionFactory.FILTER_IS_NOT_EQUAL_TO)) {
				return new AttributeCompareCondition(attribute, value, false,
				    0, false);
			}
			if (simpleCondition.equals(ConditionFactory.FILTER_GT)) {
				return new AttributeCompareCondition(attribute, value, false,
				    1, true);
			}
			if (simpleCondition.equals(ConditionFactory.FILTER_GE)) {
				return new AttributeCompareCondition(attribute, value, false,
				    -1, false);
			}
			if (simpleCondition.equals(ConditionFactory.FILTER_LT)) {
				return new AttributeCompareCondition(attribute, value, false,
				    -1, true);
			}
			if (simpleCondition.equals(ConditionFactory.FILTER_LE)) {
				return new AttributeCompareCondition(attribute, value, false,
				    1, false);
			}
		}
		return null;
	}

	public ICondition createCondition(final NamedObject attribute,
	                                  final NamedObject simpleCondition,
	                                  final String value,
	                                  final boolean ignoreCase) {
		if (attribute.equals(ConditionFactory.FILTER_ICON)
		        && simpleCondition.equals(ConditionFactory.FILTER_CONTAINS)) {
			return new IconContainedCondition(value);
		}
		if (attribute.equals(ConditionFactory.FILTER_NODE)) {
			return createNodeCondition(simpleCondition, value, ignoreCase);
		}
		return null;
	}

	protected ICondition createNodeCondition(final NamedObject simpleCondition,
	                                         final String value,
	                                         final boolean ignoreCase) {
		if (ignoreCase) {
			if (simpleCondition.equals(ConditionFactory.FILTER_CONTAINS)) {
				if (value.equals("")) {
					return null;
				}
				return new IgnoreCaseNodeContainsCondition(value);
			}
			if (simpleCondition.equals(ConditionFactory.FILTER_IS_EQUAL_TO)) {
				return new NodeCompareCondition(value, true, 0, true);
			}
			if (simpleCondition.equals(ConditionFactory.FILTER_IS_NOT_EQUAL_TO)) {
				return new NodeCompareCondition(value, true, 0, false);
			}
			if (simpleCondition.equals(ConditionFactory.FILTER_GT)) {
				return new NodeCompareCondition(value, true, 1, true);
			}
			if (simpleCondition.equals(ConditionFactory.FILTER_GE)) {
				return new NodeCompareCondition(value, true, -1, false);
			}
			if (simpleCondition.equals(ConditionFactory.FILTER_LT)) {
				return new NodeCompareCondition(value, true, -1, true);
			}
			if (simpleCondition.equals(ConditionFactory.FILTER_LE)) {
				return new NodeCompareCondition(value, true, 1, false);
			}
		}
		else {
			if (simpleCondition.equals(ConditionFactory.FILTER_CONTAINS)) {
				if (value.equals("")) {
					return null;
				}
				return new NodeContainsCondition(value);
			}
			if (simpleCondition.equals(ConditionFactory.FILTER_IS_EQUAL_TO)) {
				return new NodeCompareCondition(value, false, 0, true);
			}
			if (simpleCondition.equals(ConditionFactory.FILTER_IS_NOT_EQUAL_TO)) {
				return new NodeCompareCondition(value, false, 0, false);
			}
			if (simpleCondition.equals(ConditionFactory.FILTER_GT)) {
				return new NodeCompareCondition(value, false, 1, true);
			}
			if (simpleCondition.equals(ConditionFactory.FILTER_GE)) {
				return new NodeCompareCondition(value, false, -1, false);
			}
			if (simpleCondition.equals(ConditionFactory.FILTER_LT)) {
				return new NodeCompareCondition(value, false, -1, true);
			}
			if (simpleCondition.equals(ConditionFactory.FILTER_LE)) {
				return new NodeCompareCondition(value, false, 1, false);
			}
		}
		return null;
	}

	public NamedObject[] getAttributeConditionNames() {
		return new NamedObject[] {
		        Freeplane.getController().getResourceController()
		            .createTranslatedString(ConditionFactory.FILTER_EXIST),
		        Freeplane.getController().getResourceController()
		            .createTranslatedString(
		                ConditionFactory.FILTER_DOES_NOT_EXIST),
		        Freeplane
		            .getController()
		            .getResourceController()
		            .createTranslatedString(ConditionFactory.FILTER_IS_EQUAL_TO),
		        Freeplane.getController().getResourceController()
		            .createTranslatedString(
		                ConditionFactory.FILTER_IS_NOT_EQUAL_TO),
		        NamedObject.literal(ConditionFactory.FILTER_GT),
		        NamedObject.literal(ConditionFactory.FILTER_GE),
		        NamedObject.literal(ConditionFactory.FILTER_LE),
		        NamedObject.literal(ConditionFactory.FILTER_LT), };
	}

	public Object[] getIconConditionNames() {
		return new NamedObject[] { Freeplane.getController()
		    .getResourceController().createTranslatedString(
		        ConditionFactory.FILTER_CONTAINS), };
	}

	public NamedObject[] getNodeConditionNames() {
		return new NamedObject[] {
		        Freeplane.getController().getResourceController()
		            .createTranslatedString(ConditionFactory.FILTER_CONTAINS),
		        Freeplane
		            .getController()
		            .getResourceController()
		            .createTranslatedString(ConditionFactory.FILTER_IS_EQUAL_TO),
		        Freeplane.getController().getResourceController()
		            .createTranslatedString(
		                ConditionFactory.FILTER_IS_NOT_EQUAL_TO),
		        NamedObject.literal(ConditionFactory.FILTER_GT),
		        NamedObject.literal(ConditionFactory.FILTER_GE),
		        NamedObject.literal(ConditionFactory.FILTER_LE),
		        NamedObject.literal(ConditionFactory.FILTER_LT), };
	}

	public ICondition loadCondition(final XMLElement element) {
		if (element.getName().equalsIgnoreCase(NodeContainsCondition.NAME)) {
			return NodeContainsCondition.load(element);
		}
		if (element.getName().equalsIgnoreCase(
		    IgnoreCaseNodeContainsCondition.NAME)) {
			return IgnoreCaseNodeContainsCondition.load(element);
		}
		if (element.getName().equalsIgnoreCase(NodeCompareCondition.NAME)) {
			return NodeCompareCondition.load(element);
		}
		if (element.getName().equalsIgnoreCase(AttributeCompareCondition.NAME)) {
			return AttributeCompareCondition.load(element);
		}
		if (element.getName().equalsIgnoreCase(AttributeExistsCondition.NAME)) {
			return AttributeExistsCondition.load(element);
		}
		if (element.getName()
		    .equalsIgnoreCase(AttributeNotExistsCondition.NAME)) {
			return AttributeNotExistsCondition.load(element);
		}
		if (element.getName().equalsIgnoreCase(IconContainedCondition.NAME)) {
			return IconContainedCondition.load(element);
		}
		if (element.getName().equalsIgnoreCase(
		    ConditionNotSatisfiedDecorator.NAME)) {
			return ConditionNotSatisfiedDecorator.load(element);
		}
		if (element.getName().equalsIgnoreCase(ConjunctConditions.NAME)) {
			return ConjunctConditions.load(element);
		}
		if (element.getName().equalsIgnoreCase(DisjunctConditions.NAME)) {
			return DisjunctConditions.load(element);
		}
		return null;
	}
}
