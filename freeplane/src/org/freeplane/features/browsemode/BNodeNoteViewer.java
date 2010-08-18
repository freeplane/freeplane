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

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.controller.INodeSelectionListener;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.common.icon.UIIcon;
import org.freeplane.features.common.icon.factory.IconStoreFactory;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.note.NoteController;
import org.freeplane.features.common.note.NoteModel;

/**
 * @author foltin
 */
public class BNodeNoteViewer implements INodeSelectionListener {
	private static UIIcon noteIcon = null;
// // 	final private Controller controller;
	private JScrollPane noteScrollPane;
	private JLabel noteViewer;

	public BNodeNoteViewer() {
//		this.controller = controller;
	}

	private JComponent createNoteViewerComponent() {
		if (noteViewer == null) {
			noteViewer = new JLabel();
			noteViewer.setBackground(Color.WHITE);
			noteViewer.setVerticalAlignment(SwingConstants.TOP);
			noteViewer.setOpaque(true);
			noteScrollPane = new JScrollPane(noteViewer) {
				private static final long serialVersionUID = -4923850893346946687L;

				@Override
				public Dimension getPreferredSize() {
					final Dimension appletSize = getRootPane().getSize();
					final int height;
					if(appletSize.height < 300){
						height = appletSize.height / 3;
					}
					else if (appletSize.height < 2100){
						height = 100 + (appletSize.height - 300) / 5;
					}
					else{
						height = 600;
					}
					return new Dimension(appletSize.width, height);
				}
			};
			UITools.setScrollbarIncrement((JScrollPane) noteScrollPane);
			UITools.addScrollbarIncrementPropertyListener((JScrollPane) noteScrollPane);
		}
		noteScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		noteScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		return noteScrollPane;
	}

	public void onDeselect(final NodeModel pNode) {
		if(noteViewer != null){
			noteViewer.setText("");
		}
	}

	public void onSelect(final NodeModel pNode) {
		final String noteText = NoteModel.getNoteText(pNode);
		if (noteText != null && !noteText.equals("")) {
			if(noteViewer == null){
				Controller.getCurrentController().getViewController().insertComponentIntoSplitPane(createNoteViewerComponent());
			}
			noteViewer.setText(noteText);
		}
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
