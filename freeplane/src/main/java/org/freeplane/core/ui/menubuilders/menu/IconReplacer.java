package org.freeplane.core.ui.menubuilders.menu;

import java.awt.Font;

import javax.swing.AbstractButton;
import javax.swing.Icon;

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.core.ui.svgicons.FreeplaneIconFactory;
import org.freeplane.features.icon.factory.IconFactory;

class IconReplacer {
	
	static void replaceByScaledImageIcon(final AbstractButton actionComponent) {
		final Icon icon = actionComponent.getIcon();
		final IconFactory imageIconFactory = IconFactory.getInstance();
		if (icon != null && imageIconFactory.canScaleIcon(icon)) {
			final Font font = actionComponent.getFont();
			final int fontHeight = actionComponent.getFontMetrics(font).getHeight();
			final Quantity<LengthUnit> iconHeight = new Quantity<LengthUnit>(fontHeight, LengthUnit.px);
			actionComponent.setIcon(FreeplaneIconFactory.toImageIcon(imageIconFactory.getScaledIcon(icon, iconHeight)));
		}
	}

    static void replaceByImageIcon(AbstractButton actionComponent) {
        final Icon icon = actionComponent.getIcon();
        if(icon != null) {
            actionComponent.setIcon(FreeplaneIconFactory.toImageIcon(icon));
        }
    }
}