package org.freeplane.addins.mindmapmode.styles;

import javax.swing.JDialog;

import org.freeplane.controller.resources.WindowConfigurationStorage;
import org.freeplane.io.xml.n3.nanoxml.IXMLElement;


class NormalWindowConfigurationStorage extends WindowConfigurationStorage {

	@Override
	protected void marschallSpecificElements(IXMLElement xml) {
		xml.setName("window_configuration_storage");
	}
	public static void decorateDialog(String marshalled, JDialog dialog) {
		NormalWindowConfigurationStorage storage = new NormalWindowConfigurationStorage();
		storage.unmarschall(marshalled, dialog);
	}
}
