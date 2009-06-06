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
package org.freeplane.features.common.note;

import org.freeplane.core.filter.condition.ICondition;
import org.freeplane.core.model.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;

class IgnoreCaseNoteContainsCondition extends NoteContainsCondition {
	static final String NAME = "ignore_case_note_contains_condition";
	static final String VALUE = "VALUE";

	static ICondition load(final XMLElement element) {
		return new IgnoreCaseNoteContainsCondition(element.getAttribute(IgnoreCaseNoteContainsCondition.VALUE, null));
	}

	IgnoreCaseNoteContainsCondition(final String value) {
		super(value.toLowerCase());
	}

	@Override
	protected String createDesctiption() {
		return createDesctiption(true);
	}

	public void toXml(final XMLElement element) {
		toXml(element, NAME);
	}

	@Override
    protected String getText(NodeModel node) {
	    final String text = super.getText(node);
		return text == null ? null : text.toLowerCase();
    }
	
	
}
