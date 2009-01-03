package org.freeplane.features.mindmapmode.addins.styles;

import javax.swing.JDialog;

import org.freeplane.core.io.IXMLElement;
import org.freeplane.core.resources.WindowConfigurationStorage;

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
