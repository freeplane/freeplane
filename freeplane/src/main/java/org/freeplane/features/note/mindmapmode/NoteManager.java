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
import java.awt.ComponentOrientation;

import javax.swing.Icon;
import javax.swing.SwingUtilities;
import javax.swing.text.html.StyleSheet;

import org.freeplane.api.TextWritingDirection;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.IMapLifeCycleListener;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.IMapSelectionListener;
import org.freeplane.features.map.INodeChangeListener;
import org.freeplane.features.map.INodeSelectionListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.note.NoteModel;
import org.freeplane.features.note.NoteStyleAccessor;
import org.freeplane.features.styles.MapStyle;
import org.freeplane.features.styles.LogicalStyleController.StyleOption;
import org.freeplane.features.text.TextController;

class NoteManager implements INodeSelectionListener, IMapSelectionListener, IMapLifeCycleListener {
    private static final String NOTE_FOLLOWS_SELECTION_PROPERTY = "noteFollowsSelection";
	private boolean ignoreEditorUpdate;
	private NodeModel node;
	/**
	 *
	 */
	final MNoteController noteController;
    private boolean noteFollowsSelection;

	public NoteManager(final MNoteController noteController) {
		this.noteController = noteController;
		ResourceController resourceController = ResourceController.getResourceController();
        resourceController.addPropertyChangeListener(this::propertyChanged);
        noteFollowsSelection = resourceController.getBooleanProperty(NOTE_FOLLOWS_SELECTION_PROPERTY);
        final MapController mapController = noteController.getModeController().getMapController();
		mapController.addNodeChangeListener(new INodeChangeListener() {

            @Override
            public void nodeChanged(NodeChangeEvent event) {
                if(event.getNode().equals(node)) {
					if (NodeModel.NOTE_TEXT.equals(event.getProperty()))
						updateEditor();
					else if (TextWritingDirection.class.equals(event.getProperty())) {
						final NotePanel notePanel = noteController.getNotePanel();
						if(notePanel != null)
							notePanel.setComponentOrientation(getNoteTextDirection().componentOrientation);
					}
				}
            }
        });

		mapController.addMapChangeListener(new IMapChangeListener() {
		    @Override
			public void mapChanged(final MapChangeEvent event) {
		    	final Object property = event.getProperty();
		    	if (node != null
		    			&& node.getMap() == event.getMap() && property.equals(MapStyle.MAP_STYLES)) {
		    		final NotePanel notePanel = noteController.getNotePanel();
		    		if(notePanel != null)
		    			notePanel.setComponentOrientation(getNoteTextDirection().componentOrientation);
		    	}
		    }

		});
	}

	@Override
    public void onRemove(MapModel map) {
        if(! noteFollowsSelection && node != null && node.getMap() == map) {
            stopEditing();
            updateEditor();
        }
    }

    @Override
	public void onDeselect(final NodeModel node) {
	    if(noteFollowsSelection)
	        stopEditing();
	}

    private void stopEditing() {
        final NotePanel notePanel = noteController.getNotePanel();
		if (notePanel == null) {
			return;
		}
		notePanel.removeDocumentListener();
		saveNote(node);
        notePanel.stopEditing();
		this.node = null;
    }

	@Override
	public void onSelect(final NodeModel node) {
	    if(noteFollowsSelection) {
	        this.node = node;
	        updateEditor();
	    }
	}


	void updateEditor() {
		final NotePanel notePanel = noteController.getNotePanel();
		if (notePanel == null) {
			return;
		}
        // set default font for notes:
        final ModeController modeController = Controller.getCurrentModeController();
        final NoteStyleAccessor noteStyleAccessor = new NoteStyleAccessor(modeController, node, 1f, false);
        String noteCssRule = noteStyleAccessor.getNoteCSSStyle();
        Color noteForeground = noteStyleAccessor.getNoteForeground();
        Color noteBackground = noteStyleAccessor.getNoteBackground();
        final ComponentOrientation componentOrientation = getNoteTextDirection().componentOrientation;
        notePanel.setComponentOrientation(componentOrientation);
        StringBuilder bodyCssBuilder = new StringBuilder( "body {").append(noteCssRule).append("}\n");
        if (ResourceController.getResourceController().getBooleanProperty(
            MNoteController.RESOURCES_USE_MARGIN_TOP_ZERO_FOR_NOTES)) {
            bodyCssBuilder.append("p {margin-top:0;}\n");
        }

        String bodyCssRule = bodyCssBuilder.toString();
        StyleSheet noteStyleSheet = noteStyleAccessor.getNoteStyleSheet();
		if(node == null) {
		    notePanel.setViewedContent("", bodyCssRule, noteStyleSheet, noteForeground, noteBackground);
			return;
		}
		final String note = this.node != null ? NoteModel.getNoteText(this.node) : null;
		if (note != null) {
			try {
			    TextController textController = TextController.getController();
				final Object transformedContent = textController.getTransformedObject(node, NoteModel.getNote(node), note);
				Icon icon = textController.getIcon(transformedContent);
				if(icon != null)
					notePanel.setViewedImage(icon, noteStyleAccessor.getHorizontalAlignment());
				else if (transformedContent == note) {
					if (ignoreEditorUpdate) {
						return;
					}
					notePanel.removeDocumentListener();
					notePanel.setEditedContent(note, bodyCssRule, noteStyleSheet, noteForeground, noteBackground);
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							notePanel.installDocumentListener();
						}
					});
				}
				else
					notePanel.setViewedContent(transformedContent.toString(), bodyCssRule, noteStyleSheet, noteForeground, noteBackground);
			}
			catch (Throwable e) {
				LogUtils.warn(e.getMessage());
				notePanel.setViewedContent(TextUtils.format("MainView.errorUpdateText", note, e.getLocalizedMessage()),
				        bodyCssRule, noteStyleSheet, noteForeground, noteBackground);
			}
		} else {
			String noteContentType = noteController.getNoteContentType(node);
			if (TextController.isHtmlContentType(noteContentType))
					notePanel.setEditedContent("", bodyCssRule, noteStyleSheet, noteForeground, noteBackground);
			else
				notePanel.setViewedContent("", bodyCssRule, noteStyleSheet, noteForeground, noteBackground);
		}
        notePanel.updateBaseUrl(node.getMap().getURL());
	}

	@Override
	public void afterMapChange(MapModel oldMap, MapModel newMap) {
		if(newMap == null) {
		    if(node != null) {
		        saveNote();
		        node = null;
		    }
			final NotePanel notePanel = noteController.getNotePanel();
			if(notePanel != null)
				notePanel.removeViewedContent();
		}
	}

   void saveNote(final NodeModel savedNode) {
        if (node == null || node.getMap() != savedNode.getMap()) {
            return;
        }
        saveNote();
    }

    void saveNote() {
        if (node == null) {
            return;
        }
        final NotePanel notePanel = noteController.getNotePanel();
        if (notePanel != null) {
            notePanel.saveNote();
        }
    }


	NodeModel getNode() {
		return node;
	}

    void saveNote(String text) {
        boolean isHtml = HtmlUtils.isHtml(text);
        boolean editorContentEmpty = isHtml && HtmlUtils.isEmpty(text)
                || ! isHtml && text.trim().isEmpty();
        Controller.getCurrentModeController().getMapController().removeNodeSelectionListener(this);
        try {
            ignoreEditorUpdate = true;
            if (editorContentEmpty) {
                noteController.setNoteText(node, null);
            }
            else {
                final String oldText = noteController.getNoteText(node);
                if (null == oldText)
                    noteController.setNoteText(node, text);
                else if(isHtml){
                    final String oldTextWithoutHead = NotePanel.HEAD.matcher(oldText).replaceFirst("");
                    if (!oldTextWithoutHead.trim().equals(text.trim()))
                        noteController.setNoteText(node, text);
                }
                else
                    noteController.setNoteText(node, text);
            }
        }
        finally {
            ignoreEditorUpdate = false;
        }
        Controller.getCurrentModeController().getMapController().addNodeSelectionListener(this);
    }

    private void propertyChanged(String propertyName, String newValue, @SuppressWarnings("unused") String oldValue) {
        if(! NOTE_FOLLOWS_SELECTION_PROPERTY.equals(propertyName))
            return;
        noteFollowsSelection = Boolean.parseBoolean(newValue);
        if(noteFollowsSelection) {
            IMapSelection selection = Controller.getCurrentController().getSelection();
            NodeModel selectedNode = selection != null ? selection.getSelected() : null;
            if(node != selectedNode) {
                node = selectedNode;
                updateEditor();
            }
        }
    }



	TextWritingDirection getNoteTextDirection() {
		if(node ==  null)
			return TextWritingDirection.DEFAULT;
		else
			return Controller.getCurrentModeController()
					.getExtension(NodeStyleController.class)
					.getTextWritingDirection(node, StyleOption.FOR_UNSELECTED_NODE);
	}
}
