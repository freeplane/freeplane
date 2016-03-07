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
package org.freeplane.features.time;

import java.util.Date;

import org.freeplane.core.resources.TranslatedObject;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.format.FormattedDate;
import org.freeplane.n3.nanoxml.XMLElement;

public abstract class TimeCondition extends ASelectableCondition {
	static final String DATE = "DATE";
	static final String FILTER_CREATED_AFTER = "filter_created_after";
	static final String FILTER_CREATED_BEFORE = "filter_created_before";
	static final String FILTER_MODIFIED_AFTER = "filter_modified_after";
	static final String FILTER_MODIFIED_BEFORE = "filter_modified_before";

	public static ASelectableCondition create(final TranslatedObject simpleCond, final FormattedDate date) {
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

	final private FormattedDate date;
	public TimeCondition(final FormattedDate date) {
		this.date = date;
	}

	abstract protected String createDescription();

	public Date getDate() {
		return date;
	}

	abstract protected String getName();


	public void fillXML(final XMLElement child) {
		child.setAttribute(DATE, Long.toString(getDate().getTime()));
	}
}
