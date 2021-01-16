/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is created by Dimitry Polivaev in 2008.
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
package org.freeplane.features.note.mindmapmode;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.KeyboardFocusManager;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.html.StyleSheet;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.undo.IActor;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.note.NoteController;
import org.freeplane.features.note.NoteModel;
import org.freeplane.features.note.NoteStyleAccessor;
import org.freeplane.features.styles.MapStyle;
import org.freeplane.features.styles.SetBooleanMapPropertyAction;
import org.freeplane.features.text.RichTextModel;

/**
 * @author Dimitry Polivaev
 */
public class MNoteController extends NoteController {
	final class NoteDocumentListener implements DocumentListener {
		@Override
		public void changedUpdate(final DocumentEvent arg0) {
			docEvent();
		}

		private void docEvent() {
			final Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
			if (focusOwner == null || !SwingUtilities.isDescendingFrom(focusOwner, notePanel)) {
				return;
			}
			final ModeController modeController = Controller.getCurrentModeController();
			final MapController mapController = modeController.getMapController();
			final MapModel map = modeController.getController().getMap();
			if(map != null) {
				mapController.setSaved(map, false);
			}
		}

		@Override
		public void insertUpdate(final DocumentEvent arg0) {
			docEvent();
		}

		@Override
		public void removeUpdate(final DocumentEvent arg0) {
			docEvent();
		}
	}

	public static final String RESOURCES_REMOVE_NOTES_WITHOUT_QUESTION = "remove_notes_without_question";
	public static final String RESOURCES_USE_DEFAULT_FONT_FOR_NOTES_TOO = "resources_use_default_font_for_notes_too";
	public static final String RESOURCES_USE_MARGIN_TOP_ZERO_FOR_NOTES = "resources_use_margin_top_zero_for_notes";
	static final String RESOURCES_USE_SPLIT_PANE = "use_split_pane";
	private static NotePanel notePanel;

	public static MNoteController getController() {
	    return (MNoteController) NoteController.getController();
	}


	
	private final NoteManager noteManager;
    private final Set<String> noteContentTypes;

	/**
	 * @param modeController
	 */
	public MNoteController(ModeController modeController) {
		super();
		noteManager = new NoteManager(this);
        noteContentTypes = new LinkedHashSet<>();
        noteContentTypes.add(RichTextModel.DEFAULT_CONTENT_TYPE);
		createActions(modeController);
	}

	private void createActions(ModeController modeController) {
	    modeController.addAction(new SelectNoteAction(this));
		modeController.addAction(new ShowHideNoteAction(this));
		modeController.addAction(new EditNoteInDialogAction());
		modeController.addAction(new SetNoteWindowPosition("top"));
		modeController.addAction(new SetNoteWindowPosition( "left"));
		modeController.addAction(new SetNoteWindowPosition("right"));
		modeController.addAction(new SetNoteWindowPosition("bottom"));
		modeController.addAction(new RemoveNoteAction(this));
		modeController.addAction(new SetBooleanMapPropertyAction(SHOW_NOTE_ICONS));
    }

    public boolean addNoteContentType(String e) {
        return noteContentTypes.add(e);
    }
    
    public String[] getNoteContentTypes() {
        return noteContentTypes.stream().toArray(String[]::new);
    }

	void hideNotesPanel() {
		noteManager.saveNote();
		notePanel.setVisible(false);
		Controller.getCurrentModeController().getController().getViewController().removeSplitPane();
		ResourceController.getResourceController().setProperty(MNoteController.RESOURCES_USE_SPLIT_PANE, "false");
	}

	@Override
	protected void onWrite(final MapModel map) {
		final ModeController modeController = Controller.getCurrentModeController();
		final Controller controller = modeController.getController();
		final IMapSelection selection = controller.getSelection();
		if (selection == null) {
			return;
		}
		final NodeModel selected = selection.getSelected();
		noteManager.saveNote(selected);
	}

	public void setNoteText(final NodeModel node, final String newText) {
        if("".equals(newText)) {
            setNoteText(node, null);
            return;
        }
            
        final String oldText = NoteModel.getNoteText(node);
        if (oldText == newText || null != oldText && oldText.equals(newText)) {
            return;
        }
        
        NoteModel oldNote = NoteModel.getNote(node);
        NoteModel newNote= oldNote == null ? new NoteModel() :  oldNote.copy();
        newNote.setText(newText);
        
        setNote(node, oldNote, newNote, "setNoteText");
	}


    public void setNoteContentType(final NodeModel node, final String newContentType) {
        final String oldContentType = NoteModel.getNoteContentType(node);
        if (oldContentType == newContentType || null != oldContentType && oldContentType.equals(newContentType)) {
            return;
        }

        NoteModel oldNote = NoteModel.getNote(node);
        NoteModel newNote= oldNote == null ? new NoteModel() :  oldNote.copy();
        newNote.setContentType(newContentType);

        setNote(node, oldNote, newNote, "setNoteContentType");
    }
    
    private void setNote(final NodeModel node, NoteModel oldNote, NoteModel newNote, String description) {
        final IActor actor = new IActor() {
            @Override
            public void act() {
                setNote(newNote);
                Controller.getCurrentModeController().getMapController()
                    .nodeChanged(node, NodeModel.NOTE_TEXT, oldNote, newNote);
            }

            @Override
            public String getDescription() {
                return description;
            }

            @Override
            public void undo() {
                setNote(oldNote);
                Controller.getCurrentModeController().getMapController()
                    .nodeChanged(node, NodeModel.NOTE_TEXT, newNote, oldNote);
            }
            private void setNote(final NoteModel note) {
                if(note.isEmpty()) {
                    node.removeExtension(NoteModel.class);
                }
                else
                    node.putExtension(note);
				if(noteManager != null)
					noteManager.updateEditor();
			}
		};
		Controller.getCurrentModeController().execute(actor, node.getMap());
    }



	private boolean shouldUseSplitPane() {
		return "true".equals(ResourceController.getResourceController().getProperty(
		    MNoteController.RESOURCES_USE_SPLIT_PANE));
	}

	void showNotesPanel(final boolean requestFocus) {
		if (notePanel == null) {
			notePanel = new NotePanel(noteManager, new NoteDocumentListener());
			noteManager.updateEditor();
		}
		ResourceController.getResourceController().setProperty(MNoteController.RESOURCES_USE_SPLIT_PANE, "true");
		Controller.getCurrentModeController().getController().getViewController().insertComponentIntoSplitPane(notePanel);
		notePanel.setVisible(true);
		if (requestFocus) {
			KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					notePanel.requestFocusInWindow();
				}
			});
		}
		notePanel.revalidate();
	}

	void setDefaultStyle(NodeModel node) {
	    final StyleSheet styleSheet = notePanel.getStyleSheet();
	    styleSheet.removeStyle("body");
	    styleSheet.removeStyle("p");
	    // set default font for notes:
	    final ModeController modeController = Controller.getCurrentModeController();
	    final NoteStyleAccessor noteStyleAccessor = new NoteStyleAccessor(modeController, node, 1f, false);
		String noteCssRule = noteStyleAccessor.getNoteCSSStyle();
		Color noteForeground = noteStyleAccessor.getNoteForeground();
		notePanel.updateCaretColor(noteForeground);
		String bodyRule = new StringBuilder( "body {").append(noteCssRule).append("}\n").toString();
		styleSheet.addRule(bodyRule);
	    if (ResourceController.getResourceController().getBooleanProperty(
	        MNoteController.RESOURCES_USE_MARGIN_TOP_ZERO_FOR_NOTES)) {
			/* this is used for paragraph spacing. I put it here, too, as
			 * the tooltip display uses the same spacing. But it is to be discussed.
			 * fc, 23.3.2009.
			 */
			String paragraphtRule = "p {margin-top:0;}\n";
			styleSheet.addRule(paragraphtRule);
		}
	}

	boolean isEditing() {
		final Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        return focusOwner != null && notePanel != null && SwingUtilities.isDescendingFrom(focusOwner, notePanel);
	}

	void setFocusToMap() {
		final Controller controller = Controller.getCurrentModeController().getController();
		final NodeModel node = controller.getSelection().getSelected();
		controller.getMapViewManager().getComponent(node).requestFocusInWindow();
	}

	public void shutdownController() {
		Controller.getCurrentModeController().getMapController().removeNodeSelectionListener(noteManager);
		Controller.getCurrentController().getMapViewManager().removeMapSelectionListener(noteManager);
		if (notePanel == null) {
			return;
		}
		notePanel.getActionMap().remove("jumpToMapAction");
		if (shouldUseSplitPane()) {
			hideNotesPanel();
			notePanel = null;
		}
	}

	public void startupController() {
		final ModeController modeController = Controller.getCurrentModeController();
		if (shouldUseSplitPane()) {
			showNotesPanel(false);
		}
		modeController.getMapController().addNodeSelectionListener(noteManager);
		Controller.getCurrentController().getMapViewManager().addMapSelectionListener(noteManager);
	}

	boolean isNoteEditorShowing() {
		return ResourceController.getResourceController().getBooleanProperty(
		    MNoteController.RESOURCES_USE_SPLIT_PANE);
	}

	public void setShowNotesInMap(final MapModel model, final boolean show) {
		MapStyle.getController().setProperty(model, SHOW_NOTES_IN_MAP, Boolean.toString(show));
	}

	public void editNoteInDialog(final NodeModel nodeModel) {
		new NoteDialogStarter().editNoteInDialog(nodeModel);
	}

	NotePanel getNotePanel() {
		return notePanel;
	}
}
