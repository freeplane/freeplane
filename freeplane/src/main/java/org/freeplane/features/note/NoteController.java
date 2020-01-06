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
package org.freeplane.features.note;

import java.awt.Component;

import javax.swing.Icon;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.icon.IStateIconProvider;
import org.freeplane.features.icon.IconController;
import org.freeplane.features.icon.UIIcon;
import org.freeplane.features.icon.factory.IconStoreFactory;
import org.freeplane.features.map.ITooltipProvider;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.styles.MapStyle;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.features.text.TextController;
import org.freeplane.view.swing.map.MainView;

/**
 * @author Dimitry Polivaev
 */
public class NoteController implements IExtension {
	/**
	 *
	 */
	public static final String NODE_NOTE_ICON = "accessories.plugins.NodeNoteIcon";
	private static final UIIcon noteIcon= IconStoreFactory.ICON_STORE.getUIIcon("knotes.svg");
	public static final Icon bwNoteIcon = IconStoreFactory.ICON_STORE.getUIIcon("note_black_and_transp.svg").getIcon();
	public static final String bwNoteIconUrl = "freeplaneresource:/images/note_black_and_transp.png";

	public static final String SHOW_NOTE_ICONS = "show_note_icons";
	private static final Integer NOTE_TOOLTIP = 9;
	public static final String SHOW_NOTES_IN_MAP = "show_notes_in_map";
	protected static final String SHOW_NOTE_ICON_IN_TOOLTIP = "show_note_icon_in_tooltip";

	public static NoteController getController() {
		final ModeController modeController = Controller.getCurrentModeController();
		return getController(modeController);
	}

	public static NoteController getController(ModeController modeController) {
		return modeController.getExtension(NoteController.class);
    }

	public static void install( final NoteController noteController) {
		final ModeController modeController = Controller.getCurrentModeController();
		modeController.addExtension(NoteController.class, noteController);
	}

 	final private ModeController modeController;

	public NoteController() {
		super();
		final ModeController modeController = Controller.getCurrentModeController();
		this.modeController = modeController;
		modeController.getMapController().getReadManager().addElementHandler("richcontent", new NoteBuilder());
		final NoteWriter noteWriter = new NoteWriter(this);
		final WriteManager writeManager = modeController.getMapController().getWriteManager();
		writeManager.addAttributeWriter("map", noteWriter);
		writeManager.addExtensionElementWriter(NoteModel.class, noteWriter);
		registerNoteTooltipProvider(modeController);
		registerStateIconProvider();
	}

	public final String getNoteText(final NodeModel node) {
		final NoteModel extension = node.getExtension(NoteModel.class);
		return extension != null ? extension.getHtml() : null;
	}

	public final String getXmlNoteText(final NodeModel node) {
		final NoteModel extension = node.getExtension(NoteModel.class);
		return extension != null ? extension.getXml() : null;
	}

	/**
	 * @param data.node
	 */
	protected void onWrite(final MapModel map) {
	}

	private void registerNoteTooltipProvider(ModeController modeController) {
		modeController.addToolTipProvider(NOTE_TOOLTIP, new ITooltipProvider() {
			@Override
			public String getTooltip(final ModeController modeController, NodeModel node, Component view){
				return getTooltip(modeController, node, (MainView)view);
			}
			private String getTooltip(final ModeController modeController, NodeModel node, MainView view) {
				if(showNotesInMap(node.getMap()) && ! TextController.getController(modeController).isMinimized(node)){
					return null;
				}
				final String noteText = NoteModel.getNoteText(node);
				if (noteText == null)
					return null;
				float zoom = view.getNodeView().getMap().getZoom();
				final String rule = new NoteStyleAccessor(modeController, node, zoom, true).getNoteCSSStyle();
				final StringBuilder tooltipBodyBegin = new StringBuilder("<body><div style=\"");
				tooltipBodyBegin.append(rule);
				tooltipBodyBegin.append("\">");
				if(ResourceController.getResourceController().getBooleanProperty(SHOW_NOTE_ICON_IN_TOOLTIP)) {
					tooltipBodyBegin.append("<img src =\"");
					tooltipBodyBegin.append(bwNoteIconUrl);
					tooltipBodyBegin.append("\">");
				}
				final String tooltipText = noteText.replaceFirst("<body>",
					tooltipBodyBegin.toString()).replaceFirst("</body>", "</div></body>");
				return tooltipText;
			}
		});
	}

	private void registerStateIconProvider() {
		IconController.getController().addStateIconProvider(new IStateIconProvider() {
			@Override
			public UIIcon getStateIcon(NodeModel node) {
				boolean showIcon;
				if(NoteModel.getNote(node) != null){
					final String showNoteIcon = MapStyle.getController(modeController).getPropertySetDefault(node.getMap(), SHOW_NOTE_ICONS);
					showIcon = Boolean.parseBoolean(showNoteIcon);
					if(showIcon)
						return noteIcon;
				}
				return null;
			}

			@Override
			public boolean mustIncludeInIconRegistry() {
				return true;
			}
		});
    }

	public boolean showNotesInMap(MapModel model) {
		final String property = MapStyleModel.getExtension(model).getProperty(NoteController.SHOW_NOTES_IN_MAP);
		return Boolean.parseBoolean(property);
	}
}
