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
package org.freeplane.features.common.note;

import java.awt.Font;

import javax.swing.ImageIcon;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.filter.FilterController;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.MindIcon;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.mindmapmode.note.MNoteController;

/**
 * @author Dimitry Polivaev
 */
public class NoteController implements IExtension {
	private static boolean firstRun = true;
	private static ImageIcon noteIcon = null;
	public static final String RESOURCES_DON_T_SHOW_NOTE_ICONS = "don_t_show_note_icons";

	public static NoteController getController(final ModeController modeController) {
		return (NoteController) modeController.getExtension(NoteController.class);
	}

	public static void install(final ModeController modeController, final NoteController noteController) {
		modeController.addExtension(NoteController.class, noteController);
		if (firstRun) {
			noteIcon = new ImageIcon(ResourceController.getResourceController().getResource("/images/knotes.png"));
			MindIcon.factory(NoteController.NODE_NOTE_ICON, noteIcon);
			firstRun = false;
		}
	}

	final private ModeController modeController;
	/**
     *
     */
    public static final String NODE_NOTE_ICON = "accessories.plugins.NodeNoteIcon";

	public NoteController(final ModeController modeController) {
		super();
		this.modeController = modeController;
		modeController.getMapController().getReadManager().addElementHandler("richcontent", new NoteBuilder(this));
		final NoteWriter noteWriter = new NoteWriter(this);
		modeController.getMapController().getWriteManager().addExtensionElementWriter(NoteModel.class, noteWriter);
	}

	public ModeController getModeController() {
		return modeController;
	}

	public final String getNoteText(final NodeModel node) {
		final NoteModel extension = (NoteModel) node.getExtension(NoteModel.class);
		return extension != null ? extension.getNoteText() : null;
	}

	public final String getXmlNoteText(final NodeModel node) {
		final NoteModel extension = (NoteModel) node.getExtension(NoteModel.class);
		return extension != null ? extension.getXmlNoteText() : null;
	}

	/**
	 * @param node
	 */
	protected void onWrite(final NodeModel node) {
	}

	protected void setStateIcon(final NodeModel node, final boolean enabled) {
		boolean showIcon = enabled;
		if (ResourceController.getResourceController().getBooleanProperty(
		    NoteController.RESOURCES_DON_T_SHOW_NOTE_ICONS)) {
			showIcon = false;
		}
		node.setStateIcon(NoteController.NODE_NOTE_ICON, (showIcon) ? noteIcon : null);
		if(enabled){
			final Font defaultFont = ResourceController.getResourceController().getDefaultFont();
            StringBuilder rule = new StringBuilder();
            rule.append("font-family: " + defaultFont.getFamily() + ";");
            rule.append("font-size: " + defaultFont.getSize() + "pt;");
            rule.append("margin-top:0;");
			final String noteText = NoteModel.getNoteText(node)
			.replaceFirst("<body>", "<body><div style=\"" + rule + "\">")
			.replaceFirst("</body>", "</div></body>");
			(getModeController().getMapController()).setToolTip(node, "nodeNoteText", noteText );
		}
		else{
			(getModeController().getMapController()).setToolTip(node, "nodeNoteText",  null);
		}
	}

	public static void install(Controller controller) {
		FilterController.getController(controller).getConditionFactory().addConditionController(6,
		    new NoteConditionController());
    }
}
