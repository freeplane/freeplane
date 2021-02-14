/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Dimitry Polivaev
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

import java.util.Date;

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.condition.ConditionFactory;
import org.freeplane.features.format.FormattedDate;
import org.freeplane.features.map.NodeModel;

/**
 * @author Dimitry Polivaev
 * Mar 5, 2009
 */
class ReminderConditionAfter extends ReminderCondition {
	static final String NAME = "reminder_condition_after";

	ReminderConditionAfter(final FormattedDate date) {
		super(date);
	}

	public boolean checkNode(final NodeModel node) {
		final ReminderExtension reminder = ReminderExtension.getExtension(node);
		if(reminder == null)
			return false;
		final long reminderTime = reminder.getRemindUserAt();
		final Date filterDate = getDate();
		final boolean before = reminderTime > filterDate.getTime();
		return before;
	}

	@Override
	protected String createDescription() {
		final String filterTime = TextUtils.getText(ReminderConditionController.FILTER_REMINDER);
		final String before = TextUtils.getText(FILTER_REMINDER_AFTER);
		final String dateAsString = getDate().toString();
		return ConditionFactory.createDescription(filterTime, before, dateAsString);
	}

	@Override
    protected
	String getName() {
		return NAME;
	}
}
