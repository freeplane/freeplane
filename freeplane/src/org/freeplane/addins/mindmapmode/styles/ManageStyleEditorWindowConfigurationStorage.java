package org.freeplane.addins.mindmapmode.styles;

import javax.swing.JDialog;

import org.freeplane.controller.resources.WindowConfigurationStorage;
import org.freeplane.core.io.xml.n3.nanoxml.IXMLElement;

class ManageStyleEditorWindowConfigurationStorage extends WindowConfigurationStorage {
	public static ManageStyleEditorWindowConfigurationStorage decorateDialog(
	                                                                         final String marshalled,
	                                                                         final JDialog dialog) {
		final ManageStyleEditorWindowConfigurationStorage storage = new ManageStyleEditorWindowConfigurationStorage();
		final IXMLElement xml = storage.unmarschall(marshalled, dialog);
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
	protected void marschallSpecificElements(final IXMLElement xml) {
		xml.setName("manage_style_editor_window_configuration_storage");
		xml.setAttribute("divider_position", Integer.toString(dividerPosition));
	}

	public void setDividerPosition(final int dividerPosition) {
		this.dividerPosition = dividerPosition;
	}
}
