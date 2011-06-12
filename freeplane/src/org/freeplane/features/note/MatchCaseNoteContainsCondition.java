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
package org.freeplane.features.note;

import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.filter.condition.ConditionFactory;
import org.freeplane.features.map.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;

public class MatchCaseNoteContainsCondition extends ASelectableCondition {
	static final String NAME = "match_case_note_contains_condition";
	static final String VALUE = "VALUE";

	static ASelectableCondition load(final XMLElement element) {
		return new MatchCaseNoteContainsCondition(element.getAttribute(MatchCaseNoteContainsCondition.VALUE, null));
	}

	final private String value;

	MatchCaseNoteContainsCondition(final String value) {
		super();
		this.value = value;
	}

	public boolean checkNode(final NodeModel node) {
		final String text = getText(node);
		if (text == null) {
			return false;
		}
		return text.indexOf(value) > -1;
	}

	@Override
	protected String createDescription() {
		return createDescription(true);
	}

	protected String createDescription(final boolean matchCase) {
		final String nodeCondition = TextUtils.getText(NoteConditionController.FILTER_NOTE);
		final String simpleCondition = TextUtils.getText(ConditionFactory.FILTER_CONTAINS);
		return ConditionFactory.createDescription(nodeCondition, simpleCondition, value, matchCase);
	}

	protected String getText(final NodeModel node) {
		final String noteText = NoteModel.getNoteText(node);
		return noteText == null ? null : HtmlUtils.htmlToPlain(noteText);
	}

	public void fillXML(final XMLElement child) {
		super.fillXML(child);
		child.setAttribute(MatchCaseNoteContainsCondition.VALUE, value);
	}

	@Override
    protected String getName() {
	    return NAME;
    }
}