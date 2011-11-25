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
import java.awt.Font;
import java.net.URL;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.icon.UIIcon;
import org.freeplane.features.icon.factory.IconStoreFactory;
import org.freeplane.features.map.ITooltipProvider;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.features.text.TextController;

/**
 * @author Dimitry Polivaev
 */
public class NoteController implements IExtension {
	private static boolean firstRun = true;
	/**
	 *
	 */
	public static final String NODE_NOTE_ICON = "accessories.plugins.NodeNoteIcon";
	private static UIIcon noteIcon;
	public static URL bwNoteIconUrl;
	public static final String RESOURCES_DON_T_SHOW_NOTE_ICONS = "don_t_show_note_icons";
	private static final Integer NODE_TOOLTIP = 9;
	public static final String SHOW_NOTES_IN_MAP = "show_notes_in_map";

	public static NoteController getController() {
		final ModeController modeController = Controller.getCurrentModeController();
		return getController(modeController);
	}

	public static NoteController getController(ModeController modeController) {
		return (NoteController) modeController.getExtension(NoteController.class);
    }
	
	public static void install() {
		FilterController.getCurrentFilterController().getConditionFactory().addConditionController(6,
		    new NoteConditionController());
	}

	public static void install( final NoteController noteController) {
		final ModeController modeController = Controller.getCurrentModeController();
		modeController.addExtension(NoteController.class, noteController);
		if (firstRun) {
			noteIcon = IconStoreFactory.create().getUIIcon("knotes.png");
			bwNoteIconUrl = ResourceController.getResourceController().getResource("/images/note_black_and_transp.png");
			firstRun = false;
		}
	}

// 	final private ModeController modeController;

	public NoteController() {
		super();
//		this.modeController = modeController;
		final ModeController modeController = Controller.getCurrentModeController();
		modeController.getMapController().getReadManager().addElementHandler("richcontent", new NoteBuilder(this));
		final NoteWriter noteWriter = new NoteWriter(this);
		final WriteManager writeManager = modeController.getMapController().getWriteManager();
		writeManager.addAttributeWriter("map", noteWriter);
		writeManager.addExtensionElementWriter(NoteModel.class, noteWriter);
		registerNoteTooltip(modeController);
	}

	public final String getNoteText(final NodeModel node) {
		final NoteModel extension = (NoteModel) node.getExtension(NoteModel.class);
		return extension != null ? extension.getHtml() : null;
	}

	public final String getXmlNoteText(final NodeModel node) {
		final NoteModel extension = (NoteModel) node.getExtension(NoteModel.class);
		return extension != null ? extension.getXml() : null;
	}

	/**
	 * @param node
	 */
	protected void onWrite(final MapModel map) {
	}

	protected void setStateIcon(final NodeModel node, final boolean enabled) {
		boolean showIcon = enabled;
		if (ResourceController.getResourceController().getBooleanProperty(
			NoteController.RESOURCES_DON_T_SHOW_NOTE_ICONS)) {
			showIcon = false;
		}
		node.setStateIcon(NoteController.NODE_NOTE_ICON, (showIcon) ? noteIcon : null, true);
	}

	private void registerNoteTooltip(ModeController modeController) {
		modeController.addToolTipProvider(NODE_TOOLTIP, new ITooltipProvider() {
			public String getTooltip(ModeController modeController, NodeModel node, Component view) {
				if(showNotesInMap(node.getMap()) && ! TextController.getController(modeController).isMinimized(node)){
					return null;
				}
				final String noteText = NoteModel.getNoteText(node);
				if (noteText == null)
					return null;
				final StringBuilder rule = new StringBuilder();
				// set default font for notes:
				final NodeStyleController style = (NodeStyleController) Controller.getCurrentModeController().getExtension(
				    NodeStyleController.class);
				MapModel map = modeController.getController().getMap();
				if(map != null){
				    final Font defaultFont;
				    defaultFont = style.getDefaultFont(map, MapStyleModel.NOTE_STYLE);
				    rule.append("font-family: " + defaultFont.getFamily() + ";");
				    rule.append("font-size: " + defaultFont.getSize() + "pt;");
	                if (defaultFont.isItalic()) {
	                    rule.append("font-style: italic; ");
	                }
	                if (defaultFont.isBold()) {
	                    rule.append("font-weight: bold; ");
	                }
				}
				final StringBuilder tooltipBodyBegin = new StringBuilder("<body><div style=\"");
				tooltipBodyBegin.append(rule);
				tooltipBodyBegin.append("\">");
				tooltipBodyBegin.append("<img src =\"");
				tooltipBodyBegin.append(bwNoteIconUrl.toString());
				tooltipBodyBegin.append("\">");
				final String tooltipText = noteText.replaceFirst("<body>", 
					tooltipBodyBegin.toString()).replaceFirst("</body>", "</div></body>");
				return tooltipText;
			}
		});
	}

	public boolean showNotesInMap(MapModel model) {
		final String property = MapStyleModel.getExtension(model).getProperty(NoteController.SHOW_NOTES_IN_MAP);
		return Boolean.parseBoolean(property);
	}

}
