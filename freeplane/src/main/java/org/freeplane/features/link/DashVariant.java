package org.freeplane.features.link;

import javax.swing.Icon;

import org.freeplane.core.ui.components.DashIconFactory;

public enum DashVariant {
	SOLID(null), 
	DOT(new int[]{3, 3}), 
	DASH(new int[]{7, 7}), 
	DOT_DASH(new int[]{2, 7}), 
	DOT_THREE_DASHES(new int[]{2, 7, 7, 7});
	
	public final int[] variant;

	private DashVariant(int[] variant) {
		this.variant = variant;
	}

	public Icon createIcon(int iconWidth, int iconHeight, int lineWidth) {
		return DashIconFactory.createIcon(iconWidth, iconHeight, lineWidth, variant);
	}
}