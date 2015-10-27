package org.freeplane.features.styles;

import org.freeplane.core.extension.IExtension;

public enum AutomaticLayout implements IExtension{
	HEADINGS(false, false), ALL(true, false),HEADINGS_CYCLIC(false, true), ALL_CYCLIC(true, true);
	public boolean applyToLeaves;
	public boolean cyclic;
	private AutomaticLayout(boolean applyToLeaves, boolean cyclic) {
		this.applyToLeaves = applyToLeaves;
		this.cyclic = cyclic;
	}
	
}
