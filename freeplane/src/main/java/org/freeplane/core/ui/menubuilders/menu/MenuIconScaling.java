package org.freeplane.core.ui.menubuilders.menu;

import java.awt.Font;

import javax.swing.AbstractButton;
import javax.swing.Icon;

import org.freeplane.core.ui.LengthUnits;
import org.freeplane.core.util.Quantity;
import org.freeplane.features.icon.factory.ImageIconFactory;

class MenuIconScaling {
	
	static void scaleIcon(final AbstractButton actionComponent) {
		final Icon icon = actionComponent.getIcon();
		final ImageIconFactory imageIconFactory = ImageIconFactory.getInstance();
		if (icon != null && imageIconFactory.canScaleIcon(icon)) {
			final Font font = actionComponent.getFont();
			final int fontHeight = actionComponent.getFontMetrics(font).getHeight();
			final Quantity<LengthUnits> iconHeight = new Quantity<LengthUnits>(fontHeight, LengthUnits.px);
			actionComponent.setIcon(imageIconFactory.getScaledIcon(icon, iconHeight));
		}
	}
}