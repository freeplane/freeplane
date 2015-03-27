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
package org.freeplane.features.text;

import org.freeplane.core.util.HtmlUtils;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.note.NoteModel;
import org.freeplane.n3.nanoxml.XMLElement;

public class NoteContainsCondition extends MatchCaseNoteContainsCondition {
	static final String NAME = "note_contains_condition";
	static final String VALUE = "VALUE";

	static ASelectableCondition load(final XMLElement element) {
		return new NoteContainsCondition(element.getAttribute(NoteContainsCondition.VALUE, null),
				Boolean.valueOf(element.getAttribute(MatchCaseNoteContainsCondition.MATCH_APPROXIMATELY, null)));
	}
	
	protected boolean matchCase()
	{
		return false;
	}

	NoteContainsCondition(final String value, final boolean matchApproximately) {
		super(value.toLowerCase(), matchApproximately);
	}

	@Override
	protected String createDescription() {
		return createDescription(false);
	}

	@Override
	protected String getText(final NodeModel node) {
		final String noteText = NoteModel.getNoteText(node);
		return noteText == null ? null : HtmlUtils.htmlToPlain(noteText).toLowerCase();
	}
}
