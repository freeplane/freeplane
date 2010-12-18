package org.freeplane.plugin.formula;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.lang.reflect.Method;

import javax.swing.JEditorPane;
import javax.swing.text.EditorKit;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.ui.components.JRestrictedSizeScrollPane;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.text.ITextTransformer;
import org.freeplane.features.mindmapmode.text.EditNodeBase;
import org.freeplane.features.mindmapmode.text.EditNodeBase.IEditControl;
import org.freeplane.features.mindmapmode.text.EditNodeDialog;
import org.freeplane.plugin.script.ExecuteScriptException;
import org.freeplane.plugin.script.FormulaUtils;
import org.freeplane.plugin.script.NodeIdHighLighter;

class FormulaTextTransformer implements ITextTransformer {
	FormulaTextTransformer() {
	}

	public String transformText(final String text, final NodeModel nodeModel) {
		if (text == null) {
			return text;
		}
		final String plainText = HtmlUtils.htmlToPlain(text);
		if (!FormulaUtils.containsFormula(plainText)) {
			return text;
		}
		// starting a new ScriptContext in evalIfScript
		final Object result = FormulaUtils.evalIfScript(nodeModel, null, plainText);
		if (result == null) {
			throw new ExecuteScriptException("got null result from evaluating " + nodeModel.getID() + ", text='"
			        + plainText.substring(1) + "'");
		}
		return result.toString();
	}

	public EditNodeBase createEditNodeBase(NodeModel nodeModel, String text, IEditControl editControl,
	                                       KeyEvent firstEvent, boolean isNewNode, boolean editLong) {
		if (firstEvent != null) {
			if (firstEvent.getKeyChar() == '=') {
				text = "=";
			}
			else {
				return null;
			}
		}
		if (text.startsWith("=")) {
			JEditorPane textEditor = new JEditorPane();
			final JRestrictedSizeScrollPane scrollPane = new JRestrictedSizeScrollPane(textEditor);
			scrollPane.setMinimumSize(new Dimension(0, 60));
			final EditNodeDialog editNodeDialog = new FormulaEditor(nodeModel, text, firstEvent, editControl, false,
			    textEditor);
			editNodeDialog.setTitle(TextUtils.getText("formula_editor"));
			textEditor.setContentType("text/groovy");
//			try {
//				final EditorKit editorKit = textEditor.getEditorKit();
//				final Method method = editorKit.getClass().getMethod("installComponent", JEditorPane.class,
//				    String.class);
//				method.invoke(editorKit, textEditor, NodeIdHighLighter.class.getName());
//			}
//			catch (Exception e) {
//				e.printStackTrace();
//			}
			try {
				final EditorKit editorKit = textEditor.getEditorKit();
				final Class<?> controllerClass = editorKit.getClass().getClassLoader().loadClass(Controller.class.getName());
				System.err.println("hi, loaded Controller from: " + controllerClass.getClassLoader());
				final Method setCurrentController = controllerClass.getMethod(
				    "setCurrentController", controllerClass);
				setCurrentController.invoke(null, Controller.getCurrentController());
//				final Method method = editorKit.getClass().getMethod("installComponent", JEditorPane.class,
//				    String.class);
//				method.invoke(editorKit, textEditor, NodeIdHighLighter.class.getName());
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			return editNodeDialog;
		}
		return null;
	}
}
