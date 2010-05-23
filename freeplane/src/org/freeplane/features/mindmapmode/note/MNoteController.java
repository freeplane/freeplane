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
package org.freeplane.features.mindmapmode.note;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.html.StyleSheet;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.IMapSelection;
import org.freeplane.core.modecontroller.MapController;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.undo.IActor;
import org.freeplane.core.util.LogTool;
import org.freeplane.features.common.note.NoteController;
import org.freeplane.features.common.note.NoteModel;
import org.freeplane.features.mindmapmode.ortho.SpellCheckerController;
import org.freeplane.features.mindmapmode.text.MTextController;

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
			final ModeController modeController = getModeController();
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
	public static final String RESOURCES_USE_SPLIT_PANE = "use_split_pane";
	/**
	 * Indicates, whether or not the main panel has to be refreshed with new
	 * content. The typical content will be empty, so this state is saved here.
	 */
	private boolean mLastContentEmpty = true;
	private JSplitPane mSplitPane = null;
	private NoteManager noteManager;
	private SHTMLPanel noteViewerComponent;
	private Integer positionToRecover = null;

	/**
	 * @param modeController
	 */
	public MNoteController(final ModeController modeController) {
		super(modeController);
		modeController.addAction(new SelectNoteAction(this, getModeController()));
		modeController.addAction(new ShowHideNoteAction(this, getModeController()));
		modeController.addAction(new SetNoteWindowPosition(modeController.getController(), "top"));
		modeController.addAction(new SetNoteWindowPosition(modeController.getController(), "left"));
		modeController.addAction(new SetNoteWindowPosition(modeController.getController(), "right"));
		modeController.addAction(new SetNoteWindowPosition(modeController.getController(), "bottom"));
		modeController.addAction(new RemoveNoteAction(this, getModeController()));
	}

	SHTMLPanel getHtmlEditorPanel() {
		if (htmlEditorPanel != null) {
			return htmlEditorPanel;
		}
		htmlEditorPanel = MTextController.createSHTMLPanel();
		htmlEditorPanel.setMinimumSize(new Dimension(100, 100));
		final SHTMLEditorPane editorPane = (SHTMLEditorPane) htmlEditorPanel.getEditorPane();

		for (InputMap inputMap = editorPane.getInputMap(); inputMap != null; inputMap = inputMap.getParent()){
			inputMap.remove(KeyStroke.getKeyStroke("ctrl pressed T"));
			inputMap.remove(KeyStroke.getKeyStroke("ctrl shift pressed T"));
			inputMap.remove(KeyStroke.getKeyStroke("ctrl pressed SPACE"));
		}
		
		editorPane.addFocusListener(new FocusListener() {
			private SpellCheckerController spellCheckerController = null;

			public void focusLost(final FocusEvent e) {
				spellCheckerController.enableAutoSpell(editorPane, false);
			}

			public void focusGained(final FocusEvent e) {
				initSpellChecker();
				spellCheckerController.enableAutoSpell(editorPane, true);
			}

			private void initSpellChecker() {
				if (spellCheckerController != null) {
					return;
				}
				spellCheckerController = SpellCheckerController.getController(getModeController());
				spellCheckerController.addSpellCheckerMenu(editorPane.getPopup());
				spellCheckerController.enableShortKey(editorPane, true);
			}
		});
		return htmlEditorPanel;
	}

	SHTMLPanel getNoteViewerComponent() {
		return noteViewerComponent;
	}

	Integer getPositionToRecover() {
		return positionToRecover;
	}

	JSplitPane getSplitPane() {
		return mSplitPane;
	}

	JSplitPane getSplitPaneToScreen() {
		JSplitPane splitPane;
		splitPane = getSplitPane();
		if (splitPane == null) {
			showNotesPanel(true);
			splitPane = getSplitPane();
			ResourceController.getResourceController().setProperty(MNoteController.RESOURCES_USE_SPLIT_PANE, "true");
		}
		return splitPane;
	}

	void hideNotesPanel() {
		noteManager.saveNote();
		noteViewerComponent.setVisible(false);
		getModeController().getController().getViewController().removeSplitPane();
		mSplitPane = null;
	}

	boolean isLastContentEmpty() {
		return mLastContentEmpty;
	}

	@Override
	protected void onWrite(final MapModel map) {
		final ModeController modeController = getModeController();
		final Controller controller = modeController.getController();
		final IMapSelection selection = controller.getSelection();
		if (selection == null) {
			return;
		}
		final NodeModel selected = selection.getSelected();
		noteManager.saveNote(selected);
	}

	void setLastContentEmpty(final boolean mLastContentEmpty) {
		this.mLastContentEmpty = mLastContentEmpty;
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
					note.setNoteText(text);
					node.addExtension(note);
				}
				else {
					if (null != node.getExtension(NoteModel.class)) {
						node.removeExtension(NoteModel.class);
					}
				}
				setStateIcon(node, enabled);
				getModeController().getMapController().nodeChanged(node);
				noteManager.updateEditor();
			}

			public void undo() {
				setText(oldText);
			}
		};
		(getModeController()).execute(actor, node.getMap());
	}

	void setPositionToRecover(final Integer sPositionToRecover) {
		positionToRecover = sPositionToRecover;
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
		if (ResourceController.getResourceController().getBooleanProperty(
		    MNoteController.RESOURCES_USE_DEFAULT_FONT_FOR_NOTES_TOO)) {
			// set default font for notes:
			final Font defaultFont = ResourceController.getResourceController().getDefaultFont();
			String rule = "body {";
			rule += "font-family: " + defaultFont.getFamily() + ";";
			rule += "font-size: " + defaultFont.getSize() + "pt;";
			rule += "}\n";
			if (ResourceController.getResourceController().getBooleanProperty(
			    MNoteController.RESOURCES_USE_MARGIN_TOP_ZERO_FOR_NOTES)) {
				/* this is used for paragraph spacing. I put it here, too, as
				 * the tooltip display uses the same spacing. But it is to be discussed.
				 * fc, 23.3.2009.
				 */
				rule += "p {";
				rule += "margin-top:0;";
				rule += "}\n";
			}
			final StyleSheet styleSheet = noteViewerComponent.getDocument().getStyleSheet();
			styleSheet.removeStyle("body");
			styleSheet.removeStyle("p");
			styleSheet.addRule(rule);
			// done setting default font.
		}
		noteViewerComponent.setOpenHyperlinkHandler(new ActionListener() {
			public void actionPerformed(final ActionEvent pE) {
				try {
					getModeController().getController().getViewController()
					    .openDocument(new URL(pE.getActionCommand()));
				}
				catch (final Exception e) {
					LogTool.severe(e);
				}
			}
		});
		noteViewerComponent.setVisible(true);
		mSplitPane = getModeController().getController().getViewController().insertComponentIntoSplitPane(southPanel);
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

	boolean isEditing() {
		return SwingUtilities.isDescendingFrom(KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner(),
		    noteViewerComponent);
	}

	void setFocusToMap() {
		if (getPositionToRecover() != null) {
			mSplitPane.setDividerLocation(getPositionToRecover().intValue());
			setPositionToRecover(null);
		}
		final Controller controller = getModeController().getController();
		final NodeModel node = controller.getSelection().getSelected();
		controller.getViewController().getComponent(node).requestFocus();
	}

	public void shutdownController() {
		getModeController().getMapController().removeNodeSelectionListener(noteManager);
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
		final ModeController modeController = getModeController();
		noteManager = new NoteManager(this);
		if (shouldUseSplitPane()) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					showNotesPanel(false);
				}
			});
		}
		modeController.getMapController().addNodeSelectionListener(noteManager);
		noteManager.mNoteDocumentListener = new NoteDocumentListener();
	}
}
