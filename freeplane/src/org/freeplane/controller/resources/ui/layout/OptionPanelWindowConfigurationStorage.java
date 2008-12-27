package org.freeplane.controller.resources.ui.layout;

import javax.swing.JDialog;

import org.freeplane.controller.resources.WindowConfigurationStorage;
import org.freeplane.io.xml.n3.nanoxml.IXMLElement;

class OptionPanelWindowConfigurationStorage extends WindowConfigurationStorage {
	public static OptionPanelWindowConfigurationStorage decorateDialog(final String marshalled,
	                                                                   final JDialog dialog) {
		final OptionPanelWindowConfigurationStorage storage = new OptionPanelWindowConfigurationStorage();
		final IXMLElement xml = storage.unmarschall(marshalled, dialog);
		if (xml != null) {
			storage.panel = xml.getAttribute("panel", null);
			return storage;
		}
		return null;
	}

	protected String panel;

	public String getPanel() {
		return panel;
	}

	@Override
	protected void marschallSpecificElements(final IXMLElement xml) {
		xml.setName("option_panel_window_configuration_storage");
		xml.setAttribute("panel", panel);
	}

	public void setPanel(final String panel) {
		this.panel = panel;
	}
}
