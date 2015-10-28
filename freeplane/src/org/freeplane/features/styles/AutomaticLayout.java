package org.freeplane.features.styles;

import org.freeplane.core.extension.IExtension;

public enum AutomaticLayout implements IExtension{
	HEADINGS(true, true), ALL(true, false),COLUMN(false, false);
	public boolean applyToLeaves;
	public boolean addStyle;
	private AutomaticLayout(boolean addStyle, boolean applyToLeaves) {
		this.applyToLeaves = applyToLeaves;
		this.addStyle = addStyle;
	}
	
}
