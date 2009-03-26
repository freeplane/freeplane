package org.freeplane.features.mindmapmode.addins.styles;

import javax.swing.JDialog;

import org.freeplane.core.resources.WindowConfigurationStorage;
import org.freeplane.n3.nanoxml.XMLElement;

class StyleEditorWindowCfgStorage extends WindowConfigurationStorage {
	public static StyleEditorWindowCfgStorage decorateDialog(final String marshalled, final JDialog dialog) {
		final StyleEditorWindowCfgStorage storage = new StyleEditorWindowCfgStorage();
		final XMLElement xml = storage.unmarschall(marshalled, dialog);
		if (xml != null) {
			storage.dividerPosition = Integer.parseInt(xml.getAttribute("divider_position", "100"));
			return storage;
		}
		return null;
	}

	protected int dividerPosition;

	public int getDividerPosition() {
		return dividerPosition;
	}

	@Override
	protected void marshallSpecificElements(final XMLElement xml) {
		xml.setName("manage_style_editor_window_configuration_storage");
		xml.setAttribute("divider_position", Integer.toString(dividerPosition));
	}

	public void setDividerPosition(final int dividerPosition) {
		this.dividerPosition = dividerPosition;
	}
}
