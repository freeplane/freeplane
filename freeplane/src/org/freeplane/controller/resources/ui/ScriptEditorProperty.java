/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
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
package org.freeplane.controller.resources.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;

import org.freeplane.core.extension.IExtension;
import org.freeplane.map.text.HtmlTools;
import org.freeplane.modes.mindmapmode.MModeController;

import com.jgoodies.forms.builder.DefaultFormBuilder;

public class ScriptEditorProperty extends PropertyBean implements IPropertyControl, ActionListener {
	public interface IScriptEditorStarter extends IExtension {
		String startEditor(String scriptInput);
	}

	JButton mButton;
	final JPopupMenu menu = new JPopupMenu();
	final private MModeController mMindMapController;
	String script;

	/**
	 */
	public ScriptEditorProperty(final String name, final MModeController pMindMapController) {
		super(name);
		mMindMapController = pMindMapController;
		mButton = new JButton();
		mButton.addActionListener(this);
		script = "";
	}

	public void actionPerformed(final ActionEvent arg0) {
		final IScriptEditorStarter plugin = (IScriptEditorStarter) mMindMapController
		    .getExtension(IScriptEditorStarter.class);
		if (plugin != null) {
			final IScriptEditorStarter starter = plugin;
			final String resultScript = starter.startEditor(script);
			if (resultScript != null) {
				script = resultScript;
				firePropertyChangeEvent();
			}
		}
	}

	@Override
	public String getValue() {
		return HtmlTools.unicodeToHTMLUnicodeEntity(HtmlTools.toXMLEscapedText(script));
	}

	public void layout(final DefaultFormBuilder builder) {
		final JLabel label = builder.append(OptionString.getText(getLabel()), mButton);
		label.setToolTipText(OptionString.getText(getDescription()));
	}

	public void setEnabled(final boolean pEnabled) {
		mButton.setEnabled(pEnabled);
	}

	/**
	 */
	private void setScriptValue(String result) {
		if (result == null) {
			result = "";
		}
		script = HtmlTools.toXMLUnescapedText(HtmlTools.unescapeHTMLUnicodeEntity(result));
		mButton.setText(script);
	}

	@Override
	public void setValue(final String value) {
		setScriptValue(value);
	}
}
