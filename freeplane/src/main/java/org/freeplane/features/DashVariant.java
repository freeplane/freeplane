package org.freeplane.features;

import javax.swing.Icon;

import org.freeplane.core.ui.components.DashIconFactory;
import org.freeplane.core.ui.components.RenderedContent;
import org.freeplane.core.ui.components.RenderedContentSupplier;
import org.freeplane.core.ui.components.UITools;

public enum DashVariant  implements RenderedContentSupplier<DashVariant> {
	SOLID(new int[] {}), 
	CLOSE_DOTS(new int[]{3, 3}), 
	DASHES(new int[]{7, 7}), 
	DISTANT_DOTS(new int[]{2, 7}), 
	DOTS_AND_DASHES(new int[]{2, 7, 7, 7});
	
	public static DashVariant DEFAULT = DashVariant.SOLID;
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