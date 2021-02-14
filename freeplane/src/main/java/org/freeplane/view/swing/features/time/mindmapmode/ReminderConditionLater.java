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

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.filter.condition.ConditionFactory;
import org.freeplane.features.map.NodeModel;

/**
 * @author Dimitry Polivaev
 * Mar 5, 2009
 */
class ReminderConditionLater extends ASelectableCondition {
	static final String NAME = "reminder_condition_later";
	static final String FILTER_REMINDER_LATER = "filter_reminder_later";

	public boolean checkNode(final NodeModel node) {
		final ReminderExtension reminder = ReminderExtension.getExtension(node);
		if(reminder == null)
			return false;
		final long reminderTime = reminder.getRemindUserAt();
		final long currentTimeMillis = System.currentTimeMillis();
		final boolean later = reminderTime >= currentTimeMillis;
		return later;
	}

	@Override
	protected String createDescription() {
		final String reminder = TextUtils.getText(ReminderConditionController.FILTER_REMINDER);
		final String before = TextUtils.getText(FILTER_REMINDER_LATER);
		return ConditionFactory.createDescription(reminder, before, null);
	}

	@Override
    protected
	String getName() {
		return NAME;
	}
}
