package org.freeplane.core.ui.textchanger;

import javax.swing.JComponent;

public enum TranslatedElement {
	BORDER, TEXT, TOOLTIP;
	public String getKey(JComponent component) {
		return (String) (component).getClientProperty(this);
	}

	public void setKey(JComponent component, String key) {
		component.putClientProperty(this, key);
	}

	public String getTitleKey() {
		return "TranslatedElement." + name();
	}
}