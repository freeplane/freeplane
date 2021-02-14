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
package org.freeplane.view.swing.features.time.mindmapmode;

import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.format.FormattedDate;
import org.freeplane.n3.nanoxml.XMLElement;

abstract class ReminderCondition extends ASelectableCondition {
	static final String DATE = "DATE";
	static final String FILTER_REMINDER_AFTER = "filter_reminder_after";
	static final String FILTER_REMINDER_BEFORE = "filter_reminder_before";

	final private FormattedDate date;
	ReminderCondition(final FormattedDate date) {
		this.date = date;
	}

	abstract protected String createDescription();

	public FormattedDate getDate() {
		return date;
	}

	abstract protected String getName();


	public void fillXML(final XMLElement child) {
		child.setAttribute(DATE, Long.toString(getDate().getTime()));
	}
}
