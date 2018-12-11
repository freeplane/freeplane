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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.RootPaneContainer;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.components.resizer.JResizer.Direction;
import org.freeplane.features.explorer.MapExplorerController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.text.mindmapmode.EditNodeDialog;
import org.freeplane.plugin.script.ExecuteScriptException;
import org.freeplane.plugin.script.FormulaUtils;
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
	protected void configureDialog(JDialog dialog) {
		addPreviewPane(dialog);
	}

	private void addPreviewPane(JDialog dialog) {
		String content = getText();
		try {
			FormulaUtils.evalIfScript(getNode(), content);
		}
		catch (ExecuteScriptException e) {
			final StringWriter out = new StringWriter();
			try(PrintWriter writer = new PrintWriter(out)) {
				e.printStackTrace(writer);
			}
			final JTextArea exceptionView = new JTextArea(out.toString());
			exceptionView.setBackground(Color.LIGHT_GRAY);
			exceptionView.setForeground(Color.RED.darker());
			final Font font = textEditor.getFont();
			exceptionView.setFont(font.deriveFont(font.getSize2D() * 0.8f));
			exceptionView.setEditable(false);
			final JScrollPane scrollPane = new JScrollPane(exceptionView, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			final Rectangle availableScreenBounds = UITools.getAvailableScreenBounds(UITools.getCurrentRootComponent());
			final Dimension maximumSize = new Dimension(availableScreenBounds.width * 3 / 4, Integer.MAX_VALUE);
			final Dimension preferredSize = scrollPane.getPreferredSize();
			preferredSize.width = Math.min(preferredSize.width, maximumSize.width);
			scrollPane.setPreferredSize(preferredSize);
			final Box resisablePreview = Direction.RIGHT.createBox(scrollPane);
			dialog.add(resisablePreview, BorderLayout.EAST);
		}
	}

	@Override
	public void nodeSelected(final NodeModel node) {
		final String replacement;
		if(isCaretInsideStringToken())
			replacement = mapExplorer.getNodeReferenceSuggestion(node);
		else
			replacement = createReference(node);
		textEditor.replaceSelection(replacement);
	    textEditor.requestFocus();
    }

	private String createReference(final NodeModel node) {
		if(node == getNode())
			return "node";
		else if(! mapExplorer.isGlobal(node))
			return node.getID();
		final String alias = mapExplorer.getAlias(node);
		if(alias.isEmpty())
			return node.getID();
		else
			return "at(':~" + alias + "')";
	}

	@Override
	public void tableRowSelected(NodeModel node, String rowName) {
		if(isCaretInsideStringToken())
			return;
		final String replacement = createReference(node) + "['" + rowName + "']";
		textEditor.replaceSelection(replacement);
	    textEditor.requestFocus();
	}

	private boolean isCaretInsideStringToken() {
		final int caretPosition = textEditor.getCaretPosition();
		SyntaxDocument document =  (SyntaxDocument) textEditor.getDocument();
		final Token token = document.getTokenAt(caretPosition);
		final boolean caretInsideStringToken = TokenType.isString(token);
		return caretInsideStringToken;
	}


}