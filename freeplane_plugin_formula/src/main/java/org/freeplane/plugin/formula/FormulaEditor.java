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
import javax.swing.SwingUtilities;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.explorer.MapExplorerController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.text.mindmapmode.EditNodeDialog;
import org.freeplane.plugin.script.ExecuteScriptException;
import org.freeplane.plugin.script.FormulaUtils;
import org.freeplane.view.swing.ui.mindmapmode.GlassPaneManager;
import org.freeplane.view.swing.ui.mindmapmode.INodeSelector;

import de.sciss.syntaxpane.SyntaxDocument;
import de.sciss.syntaxpane.Token;
import de.sciss.syntaxpane.TokenType;

/**
 * @author Dimitry Polivaev
 * Nov 20, 2010
 */
class FormulaEditor extends EditNodeDialog implements INodeSelector {

	private static final String PASSED_WIDTH_PROPERTY = "formulaDialog.passed.width";
    private static final String PASSED_HEIGHT_PROPERTY = "formulaDialog.passed.height";
    private static final String FAILED_WIDTH_PROPERTY = "formulaDialog.failed.width";
    private static final String FAILED_HEIGHT_PROPERTY = "formulaDialog.failed.height";

	static enum EvaluationStatus{

		PASSED(PASSED_WIDTH_PROPERTY, PASSED_HEIGHT_PROPERTY),
		FAILED(FAILED_WIDTH_PROPERTY, FAILED_HEIGHT_PROPERTY);


		public final String heightPropertyName;
		public final String widthPropertyName;
		private EvaluationStatus(String widthPropertyName, String heightPropertyName) {
			this.heightPropertyName = heightPropertyName;
			this.widthPropertyName = widthPropertyName;
		}

	}

	static final String GROOVY_EDITOR_FONT = "groovy_editor_font";
	static final String GROOVY_EDITOR_FONT_SIZE = "groovy_editor_font_size";

	private JEditorPane textEditor;
	private MapExplorerController mapExplorer;
	private EvaluationStatus evaluationStatus;

	FormulaEditor(MapExplorerController mapExplorer, NodeModel nodeModel, KeyEvent firstEvent, IEditControl editControl,
                          boolean enableSplit, JEditorPane textEditor) {
	    super(nodeModel, firstEvent, true, editControl, enableSplit, textEditor);
		this.mapExplorer = mapExplorer;
	    this.textEditor = textEditor;
	    this.evaluationStatus = EvaluationStatus.PASSED;
    }

	@Override
    public void show(RootPaneContainer frame) {
	    textEditor.addAncestorListener(new GlassPaneManager(frame.getRootPane(), this));
	    super.show(frame);
    }

	@Override
	protected void configureDialog(JDialog dialog) {
		addPreviewPane(dialog);
		dialog.setModal(false);
	}

	private void addPreviewPane(JDialog dialog) {
		String content = getText();
		try {
			FormulaUtils.evalIfScript(getNode(), content);
			evaluationStatus = EvaluationStatus.PASSED;
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
			UITools.setScrollbarIncrement(scrollPane);
			final Rectangle availableScreenBounds = UITools.getAvailableScreenBounds(UITools.getCurrentRootComponent());
			final Dimension maximumSize = new Dimension(availableScreenBounds.width * 3 / 4, Integer.MAX_VALUE);
			final Dimension preferredSize = scrollPane.getPreferredSize();
			preferredSize.width = Math.min(preferredSize.width, maximumSize.width);
			preferredSize.height = 0;
			scrollPane.setPreferredSize(preferredSize);
			final Box resisablePreview = Box.createHorizontalBox();
			dialog.add(resisablePreview, BorderLayout.EAST);
			evaluationStatus = EvaluationStatus.FAILED;
		}
	}

	@Override
	public void nodeSelected(final NodeModel node) {
		final String replacement;
		if(isCaretInsideStringToken())
			replacement = mapExplorer.getNodeReferenceSuggestion(node);
		else
			replacement = createReference(node);
		replaceSelectedText(replacement);
    }

    private void replaceSelectedText(final String replacement) {
        textEditor.replaceSelection(replacement);
	    textEditor.requestFocus();
	    SwingUtilities.getWindowAncestor(textEditor).toFront();
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
		replaceSelectedText(replacement);
	}

	private boolean isCaretInsideStringToken() {
		final int caretPosition = textEditor.getCaretPosition();
		SyntaxDocument document =  (SyntaxDocument) textEditor.getDocument();
		final Token token = document.getTokenAt(caretPosition);
		final boolean caretInsideStringToken = TokenType.isString(token);
		return caretInsideStringToken;
	}

    @Override
	protected void saveDialogSize(final JDialog dialog) {
        ResourceController resourceController = ResourceController.getResourceController();
        resourceController.setProperty(evaluationStatus.widthPropertyName, dialog.getWidth());
        resourceController.setProperty(evaluationStatus.heightPropertyName, dialog.getHeight());
    }

	@Override
	protected void restoreDialogSize(final JDialog dialog) {
        Dimension preferredSize = dialog.getPreferredSize();
        ResourceController resourceController = ResourceController.getResourceController();
        preferredSize.width = Math.max(preferredSize.width, resourceController.getIntProperty(evaluationStatus.widthPropertyName, 0));
        preferredSize.height = Math.max(preferredSize.height, resourceController.getIntProperty(evaluationStatus.heightPropertyName, 0));
        dialog.setPreferredSize(preferredSize);
    }

}