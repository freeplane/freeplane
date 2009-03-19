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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.enums.ResourceControllerProperties;
import org.freeplane.core.modecontroller.MapController;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.undo.IUndoableActor;
import org.freeplane.features.common.note.NoteController;
import org.freeplane.features.common.note.NoteModel;
import org.freeplane.features.mindmapmode.ortho.SpellCheckerController;

import com.lightdev.app.shtm.SHTMLEditorPane;
import com.lightdev.app.shtm.SHTMLPanel;
import com.lightdev.app.shtm.TextResources;

/**
 * @author Dimitry Polivaev
 */
public class MNoteController extends NoteController {
	private class JumpToMapAction extends AbstractAction {
		/**
		 * @param noteController
		 */
		JumpToMapAction() {
		}

		public void actionPerformed(final ActionEvent e) {
			if (getPositionToRecover() != null) {
				mSplitPane.setDividerLocation(getPositionToRecover().intValue());
				setPositionToRecover(null);
			}
			final Controller controller = getModeController().getController();
			final NodeModel node = controller.getSelection().getSelected();
			controller.getViewController().getComponent(node).requestFocus();
		}
	}

	final class NoteDocumentListener implements DocumentListener {
		public void changedUpdate(final DocumentEvent arg0) {
			docEvent();
		}

		private void docEvent() {
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
		modeController.addAction("selectNoteAction", new SelectNoteAction(this, getModeController()));
		modeController.addAction("showHideNoteAction", new ShowHideNoteAction(this, getModeController()));
		modeController.addAction("note_window_location.top", new SetNoteWindowPosition(modeController.getController(),
		    "top"));
		modeController.addAction("note_window_location.left", new SetNoteWindowPosition(modeController.getController(),
		    "left"));
		modeController.addAction("note_window_location.right", new SetNoteWindowPosition(
		    modeController.getController(), "right"));
		modeController.addAction("note_window_location.bottom", new SetNoteWindowPosition(modeController
		    .getController(), "bottom"));
		modeController.addAction("removeNoteAction", new RemoveNoteAction(this, getModeController()));
	}

	SHTMLPanel getHtmlEditorPanel() {
		if (htmlEditorPanel == null) {
			SHTMLPanel.setResources(new TextResources() {
				public String getString(String pKey) {
					pKey = "simplyhtml." + pKey;
					String resourceString = ResourceController.getResourceController().getText(pKey, null);
					if (resourceString == null) {
						resourceString = ResourceController.getResourceController().getProperty(pKey);
					}
					return resourceString;
				}
			});
			htmlEditorPanel = SHTMLPanel.createSHTMLPanel();
			htmlEditorPanel.setMinimumSize(new Dimension(100, 100));
			final SHTMLEditorPane editorPane = (SHTMLEditorPane) htmlEditorPanel.getEditorPane();
			final SpellCheckerController spellCheckerController = SpellCheckerController
			    .getController(getModeController());
			spellCheckerController.enableAutoSpell(editorPane);
			spellCheckerController.addSpellCheckerMenu(editorPane.getPopup());
			spellCheckerController.enableShortKey(editorPane);
		}
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
			showNotesPanel();
			splitPane = getSplitPane();
			ResourceController.getResourceController().setProperty(
			    ResourceControllerProperties.RESOURCES_USE_SPLIT_PANE, "true");
		}
		return splitPane;
	}

	void hideNotesPanel() {
		noteViewerComponent.setVisible(false);
		getModeController().getController().getViewController().removeSplitPane();
		mSplitPane = null;
	}

	boolean isLastContentEmpty() {
		return mLastContentEmpty;
	}

	public boolean isSelected() {
		return getSplitPane() != null;
	}

	@Override
	protected void onWrite(final NodeModel node) {
		noteManager.onWrite(node);
	}

	void setLastContentEmpty(final boolean mLastContentEmpty) {
		this.mLastContentEmpty = mLastContentEmpty;
	}

	public void setNoteText(final NodeModel node, final String newText) {
		final String oldText = getNoteText(node);
		if (oldText == newText || null != oldText && oldText.equals(newText)) {
			return;
		}
		final IUndoableActor actor = new IUndoableActor() {
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
		(getModeController()).execute(actor);
	}

	void setPositionToRecover(final Integer sPositionToRecover) {
		positionToRecover = sPositionToRecover;
	}

	private boolean shouldUseSplitPane() {
		return "true".equals(ResourceController.getResourceController().getProperty(
		    ResourceControllerProperties.RESOURCES_USE_SPLIT_PANE));
	}

	public void showNotesPanel() {
		final SouthPanel southPanel = new SouthPanel();
		southPanel.add(noteViewerComponent, BorderLayout.CENTER);
		noteViewerComponent.setVisible(true);
		mSplitPane = getModeController().getController().getViewController().insertComponentIntoSplitPane(southPanel);
		southPanel.revalidate();
	}

	public void shutdownController() {
		getModeController().getMapController().removeNodeSelectionListener(noteManager);
		noteViewerComponent.getActionMap().remove("jumpToMapAction");
		if (noteViewerComponent != null && shouldUseSplitPane()) {
			hideNotesPanel();
			noteViewerComponent = null;
		}
	}

	public void startupController() {
		final ModeController modeController = getModeController();
		noteManager = new NoteManager(this);
		noteViewerComponent = getHtmlEditorPanel();
		final Action jumpToMapAction = new JumpToMapAction();
		final String keystroke = ResourceController.getResourceController().getAdjustableProperty(
		    "keystroke_accessories/plugins/NodeNote_jumpto.keystroke.alt_N");
		noteViewerComponent.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
		    KeyStroke.getKeyStroke(keystroke), "jumpToMapAction");
		noteViewerComponent.getActionMap().put("jumpToMapAction", jumpToMapAction);
		if (shouldUseSplitPane()) {
			showNotesPanel();
		}
		modeController.getMapController().addNodeSelectionListener(noteManager);
		noteManager.mNoteDocumentListener = new NoteDocumentListener();
	}
}
