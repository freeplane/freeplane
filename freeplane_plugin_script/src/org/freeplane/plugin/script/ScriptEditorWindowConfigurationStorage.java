package org.freeplane.plugin.script;

import javax.swing.JDialog;

import org.freeplane.core.resources.WindowConfigurationStorage;
import org.freeplane.n3.nanoxml.XMLElement;

class ScriptEditorWindowConfigurationStorage extends WindowConfigurationStorage {
	public ScriptEditorWindowConfigurationStorage() {
	    super("manage_style_editor_window_configuration_storage");
    }

	public static ScriptEditorWindowConfigurationStorage decorateDialog(final String marshalled, final JDialog dialog) {
		final ScriptEditorWindowConfigurationStorage storage = new ScriptEditorWindowConfigurationStorage();
		final XMLElement xml = storage.unmarschall(marshalled, dialog);
		if (xml != null) {
			storage.leftRatio = Integer.parseInt(xml.getAttribute("left_ratio", null));
			storage.topRatio = Integer.parseInt(xml.getAttribute("top_ratio", null));
			return storage;
		}
		return null;
	}

	protected int leftRatio;
	protected int topRatio;

	public int getLeftRatio() {
		return leftRatio;
	}

	public int getTopRatio() {
		return topRatio;
	}

	@Override
	protected void marshallSpecificElements(final XMLElement xml) {
		xml.setAttribute("left_ratio", Integer.toString(leftRatio));
		xml.setAttribute("top_ratio", Integer.toString(topRatio));
	}

	public void setLeftRatio(final int leftRatio) {
		this.leftRatio = leftRatio;
	}

	public void setTopRatio(final int topRatio) {
		this.topRatio = topRatio;
	}
}
