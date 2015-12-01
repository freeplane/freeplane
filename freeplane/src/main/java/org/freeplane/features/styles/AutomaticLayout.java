package org.freeplane.features.styles;

import org.freeplane.core.extension.IExtension;

public enum AutomaticLayout implements IExtension{
	HEADINGS(false), ALL(true);
	public boolean applyToLeaves;
	private AutomaticLayout(boolean applyToLeaves) {
		this.applyToLeaves = applyToLeaves;
	}
	
}
