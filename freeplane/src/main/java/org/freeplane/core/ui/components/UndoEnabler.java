package org.freeplane.core.ui.components;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import org.freeplane.core.util.TextUtils;

public class UndoEnabler {
    public static final String UNDO_ACTION = "Undo";
    public static final String REDO_ACTION = "Redo";

    public static void addUndoRedoFunctionality(JTextComponent textfield) {
		final UndoManager undo = new UndoManager();
		Document doc = textfield.getDocument();

		// Listen for undo and redo events
		doc.addUndoableEditListener(new UndoableEditListener() {
		    public void undoableEditHappened(UndoableEditEvent evt) {
		        undo.addEdit(evt.getEdit());
		    }
		});

		// Create an undo action and add it to the text component
		AbstractAction undoAction = new AbstractAction(TextUtils.getText("UndoAction.text")) {
		    public void actionPerformed(ActionEvent evt) {
		        try {
		            if (undo.canUndo()) {
		                undo.undo();
		            }
		        } catch (CannotUndoException e) {
		        }
		    }
		};
		undoAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control Z"));
		textfield.getActionMap().put(UNDO_ACTION, undoAction);

		// Bind the undo action to ctl-Z
		textfield.getInputMap().put(KeyStroke.getKeyStroke("control Z"), UNDO_ACTION);

		// Create a redo action and add it to the text component
		AbstractAction redoAction = new AbstractAction(TextUtils.getText("RedoAction.text")) {
		    public void actionPerformed(ActionEvent evt) {
		        try {
		            if (undo.canRedo()) {
		                undo.redo();
		            }
		        } catch (CannotRedoException e) {
		        }
		    }
		};
		redoAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control Y"));
		textfield.getActionMap().put(REDO_ACTION, redoAction);

		// Bind the redo action to ctl-Y
		textfield.getInputMap().put(KeyStroke.getKeyStroke("control Y"), REDO_ACTION);
	}
}