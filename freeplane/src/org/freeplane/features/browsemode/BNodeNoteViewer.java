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
package org.freeplane.features.browsemode;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JApplet;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.icon.UIIcon;
import org.freeplane.core.icon.factory.IconStoreFactory;
import org.freeplane.core.modecontroller.INodeSelectionListener;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.HtmlTools;
import org.freeplane.features.common.note.NoteController;
import org.freeplane.features.common.note.NoteModel;

/**
 * @author foltin
 */
public class BNodeNoteViewer implements INodeSelectionListener {
	private static UIIcon noteIcon = null;
	final private Controller controller;
	private JComponent noteScrollPane;
	private JLabel noteViewer;

	public BNodeNoteViewer(final Controller controller) {
		this.controller = controller;
	}

	protected JComponent getNoteViewerComponent() {
		if (noteViewer == null) {
			noteViewer = new JLabel();
			noteViewer.setBackground(Color.WHITE);
			noteViewer.setVerticalAlignment(SwingConstants.TOP);
			noteViewer.setOpaque(true);
			noteScrollPane = new JScrollPane(noteViewer) {
				private static final long serialVersionUID = -4923850893346946687L;

				@Override
				public Dimension getPreferredSize() {
					final JApplet applet = (JApplet) SwingUtilities.getAncestorOfClass(JApplet.class, noteScrollPane);
					final Dimension appletSize = applet.getContentPane().getSize();
					return new Dimension(appletSize.width, Math.min(appletSize.height / 3, 200));
				}
			};
			UITools.setScrollbarIncrement((JScrollPane) noteScrollPane);
			UITools.addScrollbarIncrementPropertyListener((JScrollPane) noteScrollPane);
		}
		return noteScrollPane;
	}

	public void onDeselect(final NodeModel pNode) {
		controller.getViewController().removeSplitPane();
	}

	public void onSelect(final NodeModel pNode) {
		final String noteText = NoteModel.getNoteText(pNode);
		if (noteText != null && !noteText.equals("")) {
			controller.getViewController().insertComponentIntoSplitPane(getNoteViewerComponent());
			noteViewer.setText(noteText);
			try {
				noteViewer.setText(noteText);
			}
			catch (final Exception ex) {
				setTextWithExceptionInfo(noteText, ex);
			}
		}
	}

	private void setTextWithExceptionInfo(final String text, final Exception ex) {
		final String string = HtmlTools.combineTextWithExceptionInfo(text, ex);
		noteViewer.setText(string);
	}

	public void onUpdate(final NodeModel pNode) {
		setStateIcon(pNode, true);
	}

	/** Copied from NodeNoteRegistration. */
	protected void setStateIcon(final NodeModel node, final boolean enabled) {
		if (BNodeNoteViewer.noteIcon == null) {
			BNodeNoteViewer.noteIcon = IconStoreFactory.create().getUIIcon("knotes.png");
		}
		node.setStateIcon(NoteController.NODE_NOTE_ICON, (enabled) ? BNodeNoteViewer.noteIcon : null, true);
	}
}
