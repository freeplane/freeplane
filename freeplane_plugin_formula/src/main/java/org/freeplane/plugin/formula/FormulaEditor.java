/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2010 dimitry
 *
 *  This file author is dimitry
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
package org.freeplane.plugin.formula;

import java.awt.event.KeyEvent;

import javax.swing.JEditorPane;
import javax.swing.RootPaneContainer;

import org.freeplane.features.explorer.mindmapmode.MapExplorerController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.text.mindmapmode.EditNodeDialog;
import org.freeplane.view.swing.ui.mindmapmode.GlassPaneManager;
import org.freeplane.view.swing.ui.mindmapmode.INodeSelector;

import jsyntaxpane.SyntaxDocument;
import jsyntaxpane.Token;
import jsyntaxpane.TokenType;

/**
 * @author Dimitry Polivaev
 * Nov 20, 2010
 */
class FormulaEditor extends EditNodeDialog implements INodeSelector {

	static final String GROOVY_EDITOR_FONT = "groovy_editor_font";
	static final String GROOVY_EDITOR_FONT_SIZE = "groovy_editor_font_size";

	private JEditorPane textEditor;
	private MapExplorerController mapExplorer;

	FormulaEditor(MapExplorerController mapExplorer, NodeModel nodeModel, String text, KeyEvent firstEvent, IEditControl editControl,
                          boolean enableSplit, JEditorPane textEditor) {
	    super(nodeModel, text, firstEvent, editControl, enableSplit, textEditor);
		this.mapExplorer = mapExplorer;
	    super.setModal(false);
	    this.textEditor = textEditor;
    }

	@Override
    public void show(RootPaneContainer frame) {
	    textEditor.addAncestorListener(new GlassPaneManager(frame.getRootPane(), this));
	    super.show(frame);
    }

	@Override
	public void nodeSelected(final NodeModel node) {
		final int caretPosition = textEditor.getCaretPosition();
		SyntaxDocument document =  (SyntaxDocument) textEditor.getDocument();
		final Token token = document.getTokenAt(caretPosition);
		final String replacement;
		if(TokenType.isString(token))
			replacement = mapExplorer.getNodeReferenceSuggestion(node);
		else
			replacement = node.getID();
		textEditor.replaceSelection(replacement);
	    textEditor.requestFocus();
    }
}