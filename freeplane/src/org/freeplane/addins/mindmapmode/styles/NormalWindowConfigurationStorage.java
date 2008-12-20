package org.freeplane.addins.mindmapmode.styles;

import javax.swing.JDialog;

import org.freeplane.controller.resources.WindowConfigurationStorage;
import org.freeplane.io.xml.n3.nanoxml.IXMLElement;

class NormalWindowConfigurationStorage extends WindowConfigurationStorage {
	public static void decorateDialog(final String marshalled, final JDialog dialog) {
		final NormalWindowConfigurationStorage storage = new NormalWindowConfigurationStorage();
		storage.unmarschall(marshalled, dialog);
	}

	@Override
	protected void marschallSpecificElements(final IXMLElement xml) {
		xml.setName("window_configuration_storage");
	}
}
