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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.net.URI;

import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.html.StyleSheet;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.undo.IActor;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.note.NoteController;
import org.freeplane.features.note.NoteModel;
import org.freeplane.features.spellchecker.mindmapmode.SpellCheckerController;
import org.freeplane.features.styles.MapStyle;
import org.freeplane.features.styles.SetBooleanMapPropertyAction;
import org.freeplane.features.text.mindmapmode.FreeplaneToSHTMLPropertyChangeAdapter;
import org.freeplane.features.text.mindmapmode.MTextController;
import com.lightdev.app.shtm.SHTMLEditorPane;
import com.lightdev.app.shtm.SHTMLPanel;

/**
 * @author Dimitry Polivaev
 */
public class MNoteController extends NoteController {
	final class NoteDocumentListener implements DocumentListener {
		public void changedUpdate(final DocumentEvent arg0) {
			docEvent();
		}

		private void docEvent() {
			final Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
			if (focusOwner == null || !SwingUtilities.isDescendingFrom(focusOwner, htmlEditorPanel)) {
				return;
			}
			final ModeController modeController = Controller.getCurrentModeController();
			final MapController mapController = modeController.getMapController();
			final MapModel map = modeController.getController().getMap();
			mapController.setSaved(map, false);
		}

		public void insertUpdate(final DocumentEvent arg0) {
			docEvent();
		}

		public void removeUpdate(final DocumentEvent arg0) {
			docEvent();
		}
	}

	private static class SouthPanel extends JPanel {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		public SouthPanel() {
			super(new BorderLayout());
			setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
		}

		@Override
		protected boolean processKeyBinding(final KeyStroke ks, final KeyEvent e, final int condition,
		                                    final boolean pressed) {
			return super.processKeyBinding(ks, e, condition, pressed) || e.getKeyChar() == KeyEvent.VK_SPACE
			        || e.getKeyChar() == KeyEvent.VK_ALT;
		}
	}

	private static SHTMLPanel htmlEditorPanel;
	public static final String RESOURCES_REMOVE_NOTES_WITHOUT_QUESTION = "remove_notes_without_question";
	public static final String RESOURCES_USE_DEFAULT_FONT_FOR_NOTES_TOO = "resources_use_default_font_for_notes_too";
	public static final String RESOURCES_USE_MARGIN_TOP_ZERO_FOR_NOTES = "resources_use_margin_top_zero_for_notes";
	static final String RESOURCES_USE_SPLIT_PANE = "use_split_pane";
	private final NoteManager noteManager;
	private SHTMLPanel noteViewerComponent;

	/**
	 * @param modeController
	 */
	public MNoteController(ModeController modeController) {
		super();
		noteManager = new NoteManager(this);
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

	SHTMLPanel getHtmlEditorPanel() {
		if (htmlEditorPanel != null) {
			return htmlEditorPanel;
		}
		htmlEditorPanel = MTextController.getController().createSHTMLPanel(NoteModel.EDITING_PURPOSE);

		// make sure that SHTML gets notified of relevant config changes!
	   	ResourceController.getResourceController().addPropertyChangeListener(
    			new FreeplaneToSHTMLPropertyChangeAdapter(htmlEditorPanel));

		htmlEditorPanel.setMinimumSize(new Dimension(100, 100));
		final SHTMLEditorPane editorPane = (SHTMLEditorPane) htmlEditorPanel.getEditorPane();

		for (InputMap inputMap = editorPane.getInputMap(); inputMap != null; inputMap = inputMap.getParent()){
			inputMap.remove(KeyStroke.getKeyStroke("ctrl pressed T"));
			inputMap.remove(KeyStroke.getKeyStroke("ctrl shift pressed T"));
			inputMap.remove(KeyStroke.getKeyStroke("ctrl pressed SPACE"));
		}

		editorPane.addFocusListener(new FocusListener() {
			private SpellCheckerController spellCheckerController = null;
			private boolean enabled = false;
			public void focusLost(final FocusEvent e) {
				if(! e.isTemporary()){
					spellCheckerController.enableAutoSpell(editorPane, false);
					enabled = false;
				}
			}

			public void focusGained(final FocusEvent e) {
				if(! enabled){
					initSpellChecker();
					spellCheckerController.enableAutoSpell(editorPane, true);
					enabled = true;
				}
			}

			private void initSpellChecker() {
				if (spellCheckerController != null) {
					return;
				}
				spellCheckerController = SpellCheckerController.getController();
				spellCheckerController.addSpellCheckerMenu(editorPane.getPopup());
				spellCheckerController.enableShortKey(editorPane, true);
			}
		});
		return htmlEditorPanel;
	}

	SHTMLPanel getNoteViewerComponent() {
		return noteViewerComponent;
	}

	void hideNotesPanel() {
		noteManager.saveNote();
		noteViewerComponent.setVisible(false);
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
		final String oldText = getNoteText(node);
		if (oldText == newText || null != oldText && oldText.equals(newText)) {
			return;
		}
		final IActor actor = new IActor() {
			public void act() {
				setText(newText);
			}

			public String getDescription() {
				return "setNoteText";
			}

			private void setText(final String text) {
				final boolean enabled = !(text == null || text.equals(""));
				if (enabled) {
					final NoteModel note = NoteModel.createNote(node);
					note.setHtml(text);
					node.addExtension(note);
				}
				else {
					if (null != node.getExtension(NoteModel.class)) {
						node.removeExtension(NoteModel.class);
					}
				}
				Controller.getCurrentModeController().getMapController().nodeChanged(node, NodeModel.NOTE_TEXT, oldText, text);
				if(noteManager != null)
					noteManager.updateEditor();
			}

			public void undo() {
				setText(oldText);
			}
		};
		Controller.getCurrentModeController().execute(actor, node.getMap());
	}

	private boolean shouldUseSplitPane() {
		return "true".equals(ResourceController.getResourceController().getProperty(
		    MNoteController.RESOURCES_USE_SPLIT_PANE));
	}

	void showNotesPanel(final boolean requestFocus) {
		if (noteViewerComponent == null) {
			noteViewerComponent = getHtmlEditorPanel();
			noteManager.updateEditor();
		}
		final SouthPanel southPanel = new SouthPanel();
		southPanel.add(noteViewerComponent, BorderLayout.CENTER);
//		setDefaultFont();
		noteViewerComponent.setOpenHyperlinkHandler(new ActionListener() {
			public void actionPerformed(final ActionEvent pE) {
				try {
					String uriText = pE.getActionCommand();
					LinkController.getController().loadURI(noteManager.getNode(), new URI(uriText));
				}
				catch (final Exception e) {
					LogUtils.severe(e);
				}
			}
		});
		noteViewerComponent.setVisible(true);
		ResourceController.getResourceController().setProperty(MNoteController.RESOURCES_USE_SPLIT_PANE, "true");
		Controller.getCurrentModeController().getController().getViewController().insertComponentIntoSplitPane(southPanel);
		if (requestFocus) {
			KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					final SHTMLPanel htmlEditorPanel = getHtmlEditorPanel();
					htmlEditorPanel.getMostRecentFocusOwner().requestFocus();
					if (ResourceController.getResourceController().getBooleanProperty("goto_note_end_on_edit")) {
						final JEditorPane editorPane = htmlEditorPanel.getEditorPane();
						editorPane.setCaretPosition(editorPane.getDocument().getLength());
					}
				}
			});
		}
		southPanel.revalidate();
	}

	void setDefaultStyle(NodeModel node) {
	    final StyleSheet styleSheet = noteViewerComponent.getDocument().getStyleSheet();
	    styleSheet.removeStyle("body");
	    styleSheet.removeStyle("p");
	    // set default font for notes:
	    final ModeController modeController = Controller.getCurrentModeController();
	    String noteCssRule = getNoteCSSStyle(modeController, node, 1f, false);
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
        return focusOwner != null && noteViewerComponent != null && SwingUtilities.isDescendingFrom(focusOwner, noteViewerComponent);
	}

	void setFocusToMap() {
		final Controller controller = Controller.getCurrentModeController().getController();
		final NodeModel node = controller.getSelection().getSelected();
		controller.getMapViewManager().getComponent(node).requestFocusInWindow();
	}

	public void shutdownController() {
		Controller.getCurrentModeController().getMapController().removeNodeSelectionListener(noteManager);
		Controller.getCurrentController().getMapViewManager().removeMapSelectionListener(noteManager);
		if (noteViewerComponent == null) {
			return;
		}
		noteViewerComponent.getActionMap().remove("jumpToMapAction");
		if (shouldUseSplitPane()) {
			hideNotesPanel();
			noteViewerComponent = null;
		}
	}

	public void startupController() {
		final ModeController modeController = Controller.getCurrentModeController();
		if (shouldUseSplitPane()) {
			showNotesPanel(false);
		}
		modeController.getMapController().addNodeSelectionListener(noteManager);
		Controller.getCurrentController().getMapViewManager().addMapSelectionListener(noteManager);
		noteManager.mNoteDocumentListener = new NoteDocumentListener();
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
}
