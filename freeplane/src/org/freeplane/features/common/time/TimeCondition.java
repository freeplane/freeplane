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
package org.freeplane.features.common.time;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JComponent;

import org.freeplane.core.filter.condition.ConditionFactory;
import org.freeplane.core.filter.condition.ICondition;
import org.freeplane.core.icon.MindIcon;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.NamedObject;
import org.freeplane.n3.nanoxml.XMLElement;

abstract class TimeCondition implements ICondition {
	static final String DATE = "DATE";
	static final String FILTER_CREATED_AFTER = "filter_created_after";
	static final String FILTER_CREATED_BEFORE = "filter_created_before";
	static final String FILTER_MODIFIED_AFTER = "filter_modified_after";
	static final String FILTER_MODIFIED_BEFORE = "filter_modified_before";

	public static ICondition create(final NamedObject simpleCond, final Date date) {
		if (simpleCond.objectEquals(TimeCondition.FILTER_MODIFIED_AFTER)) {
			return new TimeConditionModifiedAfter(date);
		}
		if (simpleCond.objectEquals(TimeCondition.FILTER_MODIFIED_BEFORE)) {
			return new TimeConditionModifiedBefore(date);
		}
		if (simpleCond.objectEquals(TimeCondition.FILTER_CREATED_AFTER)) {
			return new TimeConditionCreatedAfter(date);
		}
		if (simpleCond.objectEquals(TimeCondition.FILTER_CREATED_BEFORE)) {
			return new TimeConditionCreatedBefore(date);
		}
		return null;
	}

	public static String format(final Date date) {
		return DateFormat.getDateInstance(DateFormat.MEDIUM).format(date);
	}

	final private Date date;
	private String description;
	private JComponent renderer;

	public TimeCondition(final Date date) {
		this.date = date;
	}

	abstract protected String createDesctiption();

	public Date getDate() {
		return date;
	}

	public JComponent getListCellRendererComponent() {
		if (renderer == null) {
			renderer = ConditionFactory.createCellRendererComponent(toString());
		}
		return renderer;
	}

	abstract String getName();

	@Override
	public String toString() {
		if (description == null) {
			description = createDesctiption();
		}
		return description;
	}

	public void toXml(final XMLElement element) {
		final XMLElement child = new XMLElement();
		child.setName(getName());
		child.setAttribute(DATE, Long.toString(getDate().getTime()));
		element.addChild(child);
	}
}
