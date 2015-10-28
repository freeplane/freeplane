package org.freeplane.features.styles;

import org.freeplane.core.extension.IExtension;

public enum AutomaticLayout implements IExtension{
	HEADINGS(true, false), ALL(true, true),COLUMNS(false, false);
	public boolean addStyle;
	public boolean applyToLeaves;
	private AutomaticLayout(boolean addStyle, boolean applyToLeaves) {
		this.applyToLeaves = applyToLeaves;
		this.addStyle = addStyle;
	}
	
}
