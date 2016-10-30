package org.freeplane.features.link;

import javax.swing.Icon;

import org.freeplane.core.ui.components.DashIconFactory;
import org.freeplane.core.ui.components.RenderedContent;
import org.freeplane.core.ui.components.RenderedContentSupplier;
import org.freeplane.core.ui.components.UITools;

public enum DashVariant  implements RenderedContentSupplier<DashVariant> {
	SOLID(null), 
	DOT(new int[]{3, 3}), 
	DASH(new int[]{7, 7}), 
	DOT_DASH(new int[]{2, 7}), 
	DOT_THREE_DASHES(new int[]{2, 7, 7, 7});
	
	
	public final int[] variant;
	public final Icon icon;

	private DashVariant(int[] variant) {
		this.variant = variant;
		final int LINE_WIDTH = 2;
		final int ICON_HEIGHT = Math.round(12 * UITools.FONT_SCALE_FACTOR);
		final int ICON_WIDTH = ICON_HEIGHT * 5;
		icon = DashIconFactory.createIcon(ICON_WIDTH, ICON_HEIGHT, LINE_WIDTH, variant);
	}

	@Override
	public RenderedContent<DashVariant> createRenderedContent() {
		return new RenderedContent<DashVariant>(this, null, icon);
	}

}