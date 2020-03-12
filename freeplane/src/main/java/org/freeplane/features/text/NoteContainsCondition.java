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
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.StringMatchingStrategy;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.filter.condition.ConditionFactory;
import org.freeplane.features.filter.condition.StringConditionAdapter;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.note.NoteModel;
import org.freeplane.n3.nanoxml.XMLElement;

public class NoteContainsCondition extends StringConditionAdapter {
    static final String IGNORE_CASE_NAME = "note_contains_condition";
	static final String MATCH_CASE_NAME = "match_case_note_contains_condition";
	static final String VALUE = "VALUE";

    static ASelectableCondition loadMatchCase(final XMLElement element) {
        return new NoteContainsCondition(element.getAttribute(NoteContainsCondition.VALUE, null), true,
                Boolean.valueOf(element.getAttribute(NoteContainsCondition.MATCH_APPROXIMATELY, null)));
    }

    static ASelectableCondition loadIgnoreCase(final XMLElement element) {
        return new NoteContainsCondition(element.getAttribute(NoteContainsCondition.VALUE, null), false,
                Boolean.valueOf(element.getAttribute(NoteContainsCondition.MATCH_APPROXIMATELY, null)));
    }

	final private String value;
	final StringMatchingStrategy stringMatchingStrategy;

	NoteContainsCondition(final String value, final boolean matchCase, final boolean matchApproximately) {
		super(matchCase, matchApproximately);
		this.value = value;
		this.stringMatchingStrategy = matchApproximately ? StringMatchingStrategy.DEFAULT_APPROXIMATE_STRING_MATCHING_STRATEGY :
			StringMatchingStrategy.EXACT_STRING_MATCHING_STRATEGY;
	}

	public boolean checkNode(final NodeModel node) {
		final String text = getText(node);
		if (text == null) {
			return false;
		}
		return stringMatchingStrategy.matches(normalizedValue(), normalize(text), true);
	}

	@Override
	protected String createDescription() {
		return createDescription(true);
	}

	protected String createDescription(final boolean matchCase) {
		final String nodeCondition = TextUtils.getText(TextController.FILTER_NOTE);
		final String simpleCondition = TextUtils.getText(ConditionFactory.FILTER_CONTAINS);
		return ConditionFactory.createDescription(nodeCondition, simpleCondition, value, matchCase, matchApproximately);
	}

	protected String getText(final NodeModel node) {
		final String noteText = NoteModel.getNoteText(node);
		return noteText == null ? null : HtmlUtils.htmlToPlain(noteText);
	}

	public void fillXML(final XMLElement child) {
		super.fillXML(child);
		child.setAttribute(NoteContainsCondition.VALUE, value);
	}

	@Override
    protected String getName() {
	    return matchCase ?  MATCH_CASE_NAME : IGNORE_CASE_NAME;
    }

    @Override
    protected Object conditionValue() {
        return value;
    }
}