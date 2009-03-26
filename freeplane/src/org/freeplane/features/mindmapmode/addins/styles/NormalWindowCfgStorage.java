package org.freeplane.features.mindmapmode.addins.styles;

import javax.swing.JDialog;

import org.freeplane.core.resources.WindowConfigurationStorage;
import org.freeplane.n3.nanoxml.XMLElement;

class NormalWindowCfgStorage extends WindowConfigurationStorage {
	public static void decorateDialog(final String marshalled, final JDialog dialog) {
		final NormalWindowCfgStorage storage = new NormalWindowCfgStorage();
		storage.unmarschall(marshalled, dialog);
	}

	@Override
	protected void marshallSpecificElements(final XMLElement xml) {
		xml.setName("window_configuration_storage");
	}
}
