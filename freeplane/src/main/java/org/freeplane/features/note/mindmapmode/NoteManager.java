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
import java.awt.Font;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.text.html.StyleSheet;

import org.freeplane.api.TextWritingDirection;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
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
import org.freeplane.features.text.TextController;
import org.freeplane.main.application.ApplicationResourceController;

import com.lightdev.app.shtm.SHTMLEditorPane;

class NoteManager implements INodeSelectionListener, IMapSelectionListener, IMapLifeCycleListener {
	private static final class NodeLinkTarget {
		private final NodeModel node;
		private final String path;

		private NodeLinkTarget(NodeModel node, String path) {
			this.node = node;
			this.path = path;
		}

		@Override
		public String toString() { return path + node.getText(); }
	}
    private static final String NOTE_FOLLOWS_SELECTION_PROPERTY = "noteFollowsSelection";
    private static final String LAST_NOTE_URL_PROPERTY = "lastNoteUrl";
    private static final String LAST_NOTE_NODE_PROPERTY = "lastNoteNode";
	private boolean ignoreEditorUpdate;
	private NodeModel node;
    private WeakReference<NodeModel> lastShownNoteNode = new WeakReference<>(null);
	/**
	 *
	 */
	final MNoteController noteController;
	private String selectedTabName = NoteModel.DEFAULT_TAB_NAME;
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
            rememberShownNoteNode(node);
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
        final Font noteFont = noteStyleAccessor.getNoteFont();
        if(noteFont != null)
        	notePanel.setFont(noteFont.deriveFont(noteFont.getSize2D() * UITools.FONT_SCALE_FACTOR));
        notePanel.setForeground(noteForeground);
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
        if (ignoreEditorUpdate) {
            return;
        }
		final NoteModel noteModel = this.node != null ? NoteModel.getNote(this.node) : null;
		if (noteModel != null && noteModel.getTab(selectedTabName) == null)
			selectedTabName = NoteModel.DEFAULT_TAB_NAME;
		final NoteModel.Tab selectedTab = noteModel != null ? noteModel.getTab(selectedTabName) : null;
		final String note = selectedTab != null ? selectedTab.getText() : null;
		notePanel.setTabs(noteModel == null ? java.util.Collections.emptyList() : noteModel.getTabs(), selectedTabName);
		if (note != null) {
			try {
			    TextController textController = TextController.getController();
				final Object transformedContent = textController.getTransformedObject(node, selectedTab != null ? selectedTab : noteModel, note, notePanel);
				Icon icon = textController.getIcon(transformedContent);
				if(icon != null)
					notePanel.setViewedImage(icon, noteStyleAccessor.getHorizontalAlignment(), noteBackground);
				else if (transformedContent == note) {
					notePanel.removeDocumentListener();
					String noteContentType = noteController.getNoteContentType(node);
					String editedContent = TextController.isHtmlContentType(noteContentType) ? HtmlUtils.textToHTML(note) : note;
					notePanel.setEditedContent(editedContent, bodyCssRule, noteStyleSheet, noteForeground, noteBackground);
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

    void restoreStartupNote() {
        if (noteFollowsSelection || noteController.getNotePanel() == null) {
            return;
        }
        NodeModel startupNoteTarget = loadStartupNoteTarget();
        node = startupNoteTarget;
        rememberShownNoteNode(startupNoteTarget);
        updateEditor();
    }

    void saveShutdownNoteTarget() {
        saveFrozenNoteTarget(resolveShutdownNoteTarget(noteFollowsSelection, lastShownNoteNode));
    }

	void saveNote(String text) {
        boolean isHtml = HtmlUtils.isHtml(text);
        boolean editorContentEmpty = isHtml && HtmlUtils.isEmpty(text)
                || ! isHtml && text.trim().isEmpty();
        Controller.getCurrentModeController().getMapController().removeNodeSelectionListener(this);
        try {
            ignoreEditorUpdate = true;
            if (editorContentEmpty) {
				noteController.setNoteText(node, selectedTabName, null);
            }
            else {
				final NoteModel currentNote = NoteModel.getNote(node);
				final NoteModel.Tab currentTab = currentNote == null ? null : currentNote.getTab(selectedTabName);
				final String oldText = currentTab == null ? null : currentTab.getText();
				if (null == oldText)
					noteController.setNoteText(node, selectedTabName, text);
                else if(isHtml){
                    final String oldTextWithoutHead = NotePanel.HEAD.matcher(oldText).replaceFirst("");
                    if (!oldTextWithoutHead.trim().equals(text.trim()))
						noteController.setNoteText(node, selectedTabName, text);
                }
                else
					noteController.setNoteText(node, selectedTabName, text);
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
            rememberShownNoteNode(selectedNode);
        }
        else {
            if (node == null) {
                node = selectedNode();
            }
            rememberShownNoteNode(node);
        }
    }

    private ApplicationResourceController applicationResourceController() {
        return (ApplicationResourceController) ResourceController.getResourceController();
    }

    private void clearSavedFrozenNoteTarget() {
        ApplicationResourceController resourceController = applicationResourceController();
        resourceController.setProperty(LAST_NOTE_URL_PROPERTY, "");
        resourceController.setProperty(LAST_NOTE_NODE_PROPERTY, "");
    }

    static NodeModel resolveSavedFrozenNoteTarget(String savedMapUrl, String savedNodeId, Iterable<MapModel> openMaps) {
        if (savedMapUrl == null || savedMapUrl.isEmpty() || savedNodeId == null || savedNodeId.isEmpty()) {
            return null;
        }
        for (MapModel map : openMaps) {
            String mapUrl = mapUrl(map);
            if (savedMapUrl.equals(mapUrl)) {
                return map.getNodeForID(savedNodeId);
            }
        }
        return null;
    }

    static NodeModel resolveStartupNoteTarget(String savedMapUrl, String savedNodeId, Iterable<MapModel> openMaps,
            NodeModel selectedNode) {
        NodeModel savedTarget = resolveSavedFrozenNoteTarget(savedMapUrl, savedNodeId, openMaps);
        return savedTarget != null ? savedTarget : selectedNode;
    }

    static NodeModel resolveShutdownNoteTarget(boolean noteFollowsSelection, WeakReference<NodeModel> lastShownNoteNode) {
        return noteFollowsSelection || lastShownNoteNode == null ? null : lastShownNoteNode.get();
    }

    private NodeModel loadStartupNoteTarget() {
        ApplicationResourceController resourceController = applicationResourceController();
        return resolveStartupNoteTarget(resourceController.getProperty(LAST_NOTE_URL_PROPERTY, null),
            resourceController.getProperty(LAST_NOTE_NODE_PROPERTY, null),
            Controller.getCurrentController().getMapViewManager().getMaps().values(), selectedNode());
    }

    private void saveFrozenNoteTarget(NodeModel noteTarget) {
        if (noteTarget == null) {
            clearSavedFrozenNoteTarget();
            return;
        }
        String mapUrl = mapUrl(noteTarget.getMap());
        if (mapUrl == null) {
            clearSavedFrozenNoteTarget();
            return;
        }
        ApplicationResourceController resourceController = applicationResourceController();
        resourceController.setProperty(LAST_NOTE_URL_PROPERTY, mapUrl);
        resourceController.setProperty(LAST_NOTE_NODE_PROPERTY, noteTarget.getID());
    }

    private static String mapUrl(MapModel map) {
        return map != null && map.getURL() != null ? map.getURL().toString() : null;
    }

    private void rememberShownNoteNode(NodeModel noteTarget) {
        lastShownNoteNode = new WeakReference<>(noteTarget);
    }

    private NodeModel selectedNode() {
        IMapSelection selection = Controller.getCurrentController().getSelection();
        return selection != null ? selection.getSelected() : null;
    }



	TextWritingDirection getNoteTextDirection() {
		if(node ==  null)
			return TextWritingDirection.DEFAULT;
		else
			return Controller.getCurrentModeController()
					.getExtension(NodeStyleController.class)
					.getTextWritingDirection(node);
	}

	void selectTab(String tabName) {
		if (tabName.equals(selectedTabName)) return;
		saveNote();
		selectedTabName = tabName;
		updateEditor();
	}

	void addTab() {
		if (node == null) return;
		saveNote();
		NoteModel note = NoteModel.getNote(node);
		NoteModel updated = note == null ? new NoteModel() : note.copy();
		updated.ensureTabs();
		int index = updated.getTabs().size() + 1;
		String name = "note" + index;
		while (updated.getTab(name) != null) name = "note" + (++index);
		updated.addTab(name);
		noteController.setNoteTabs(node, updated, "addNoteTab");
		selectedTabName = name;
		updateEditor();
	}

	void renameTab(String oldName, String newName) {
		if (node == null || newName == null || newName.trim().isEmpty()) return;
		newName = newName.trim();
		NoteModel note = NoteModel.getNote(node);
		if (note == null || note.getTab(newName) != null) return;
		NoteModel updated = note.copy();
		NoteModel.Tab tab = updated.getTab(oldName);
		if (tab == null) return;
		tab.setName(newName);
		noteController.setNoteTabs(node, updated, "renameNoteTab");
		selectedTabName = newName;
		updateEditor();
	}

	void closeTab(String tabName) {
		if (node == null) return;
		saveNote();
		NoteModel note = NoteModel.getNote(node);
		if (note == null) return;
		NoteModel updated = note.copy();
		updated.ensureTabs();
		updated.removeTab(tabName);
		noteController.setNoteTabs(node, updated, "removeNoteTab");
		selectedTabName = NoteModel.DEFAULT_TAB_NAME;
		updateEditor();
	}

	void insertLink(SHTMLEditorPane editorPane, boolean linkToNoteTab) {
		if (node == null || editorPane.getSelectionStart() == editorPane.getSelectionEnd()) return;
		List<NodeLinkTarget> nodes = new ArrayList<>();
		collectNodes(node.getMap().getRootNode(), "", nodes);
		NodeLinkTarget selectedTarget = (NodeLinkTarget) JOptionPane.showInputDialog(noteController.getNotePanel(),
				"Link selected text to", "Insert note link", JOptionPane.PLAIN_MESSAGE, null,
				nodes.toArray(), nodes.get(0));
		if (selectedTarget == null) return;
		NodeModel target = selectedTarget.node;
		if (!linkToNoteTab) {
			editorPane.setLink(null, "#" + target.getID(), null);
			return;
		}
		NoteModel targetNote = NoteModel.getNote(target);
		List<String> destinations = new ArrayList<>();
		if (targetNote == null || !targetNote.hasTabs()) destinations.add(NoteModel.DEFAULT_TAB_NAME);
		else for (NoteModel.Tab tab : targetNote.getTabs()) destinations.add(tab.getName());
		String destination = (String) JOptionPane.showInputDialog(noteController.getNotePanel(),
				"Link to note tab", "Insert note link", JOptionPane.PLAIN_MESSAGE, null,
				destinations.toArray(), destinations.get(0));
		if (destination == null) return;
		String href = "note:#" + target.getID() + "/" + destination;
		editorPane.setLink(null, href, null);
	}

	void removeLink(SHTMLEditorPane editorPane) {
		editorPane.setLink(null, null, null);
	}

	private void collectNodes(NodeModel current, String path, List<NodeLinkTarget> nodes) {
		nodes.add(new NodeLinkTarget(current, path));
		for (NodeModel child : current.getChildren()) collectNodes(child, path + "  ", nodes);
	}

	boolean openNoteLink(String link) {
		if (!link.startsWith("note:#")) return false;
		int slash = link.indexOf('/', "note:#".length());
		String nodeId = slash < 0 ? link.substring("note:#".length()) : link.substring("note:#".length(), slash);
		String tabName = slash < 0 ? NoteModel.DEFAULT_TAB_NAME : link.substring(slash + 1);
		NodeModel target = node == null ? null : node.getMap().getNodeForID(nodeId);
		if (target == null) return false;
		saveNote();
		selectedTabName = tabName;
		Controller.getCurrentModeController().getMapController().select(target);
		node = target;
		updateEditor();
		return true;
	}
}
