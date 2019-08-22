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
package org.freeplane.plugin.script.filter;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;

import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;

import org.freeplane.core.resources.TranslatedObject;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.filter.condition.IElementaryConditionController;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.script.ScriptComboBoxEditor;
import org.freeplane.plugin.script.ScriptRenderer;


/**
 * @author Dimitry Polivaev
 * 21.12.2008
 */
public class ScriptConditionController implements IElementaryConditionController {
	static final String FILTER_SCRIPT = "filter_script";
	private final ComboBoxEditor editor;
	private final ListCellRenderer renderer;
	private final ComboBoxModel values;

	public ScriptConditionController() {
		super();
		if(! GraphicsEnvironment.isHeadless()) {
			editor = new ScriptComboBoxEditor();
			renderer = new ScriptRenderer();
			values = new DefaultComboBoxModel();
			Component showEditorBtn = editor.getEditorComponent();
			final Dimension preferredSize = showEditorBtn.getPreferredSize();
			preferredSize.width = 100;
			showEditorBtn.setPreferredSize(preferredSize);
		}
		else {
			editor = null;
			renderer = null;
			values = null;
		}

	}

	public boolean canEditValues(final Object property, final TranslatedObject simpleCond) {
		return true;
	}

	public boolean canHandle(final Object selectedItem) {
		if (!(selectedItem instanceof TranslatedObject)) {
			return false;
		}
		final TranslatedObject namedObject = (TranslatedObject) selectedItem;
		return namedObject.objectEquals(ScriptConditionController.FILTER_SCRIPT);
	}

	public boolean canSelectValues(final Object property, final TranslatedObject simpleCond) {
		return true;
	}

	public ASelectableCondition createCondition(final Object selectedItem, final TranslatedObject simpleCond,
	                                            final Object value, final boolean matchCase,
	                                            final boolean matchApproximately) {
		if(value == null)
			return null;
		final String string = (String) value;
		if("".equals(string))
			return null;
		return new ScriptCondition(string);
	}

	public ComboBoxModel getConditionsForProperty(final Object property) {
		return new DefaultComboBoxModel(getScriptConditionNames());
	}

	public ListModel getFilteredProperties() {
		final DefaultListModel list = new DefaultListModel();
		list.addElement(TextUtils.createTranslatedString(FILTER_SCRIPT));
		return list;
	}

	public Object[] getScriptConditionNames() {
		return new TranslatedObject[] { new TranslatedObject(ScriptCondition.NAME, " ")};
	}

	public ComboBoxEditor getValueEditor(Object selectedProperty, TranslatedObject selectedCondition) {
		return editor;
	}

	public ComboBoxModel getValuesForProperty(final Object selectedItem, TranslatedObject simpleCond) {
		values.setSelectedItem("");
		return values;
	}

	public boolean isCaseDependent(final Object property, final TranslatedObject simpleCond) {
		return false;
	}

	public boolean supportsApproximateMatching(final Object property, final TranslatedObject simpleCond) {
		return false;
	}

	public ASelectableCondition loadCondition(final XMLElement element) {
		try {
			if (element.getName().equalsIgnoreCase(ScriptCondition.NAME)) {
			    return ScriptCondition.load(element);
			}
		}
		catch (final Exception e) {
			LogUtils.severe(e);
		}
		return null;
	}

	public ListCellRenderer getValueRenderer(Object selectedProperty, TranslatedObject selectedCondition) {
	    return renderer;
    }
}
