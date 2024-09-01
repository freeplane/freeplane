package org.freeplane.core.ui.menubuilders.menu;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.AbstractButton;
import javax.swing.Icon;

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.svgicons.FreeplaneIconFactory;
import org.freeplane.features.icon.factory.IconFactory;

class IconReplacer {
	static void replaceByScaledImageIcon(final AbstractButton actionComponent) {
		final Icon icon = actionComponent.getIcon();
		final IconFactory imageIconFactory = IconFactory.getInstance();
		if (icon != null && imageIconFactory.canScaleIcon(icon)) {
			if(actionComponent.getText() != null) {
				final Font font = actionComponent.getFont();
				final int fontHeight = actionComponent.getFontMetrics(font).getHeight();
				final Quantity<LengthUnit> iconHeight = new Quantity<LengthUnit>(fontHeight, LengthUnit.px);
				actionComponent.setIcon(FreeplaneIconFactory.toImageIcon(imageIconFactory.getScaledIcon(icon, iconHeight)));
			}
			else {
				Dimension preferredSize = actionComponent.getPreferredSize();
				int iconHeight = icon.getIconHeight();
				int iconWidth = icon.getIconWidth();
				if(iconWidth > 0 && iconHeight > 0) {
					int preferredHeight = preferredSize.height;
					int height = actionComponent.getHeight();
					int preferredWidth = preferredSize.width;
					int width = actionComponent.getWidth();
					if (height > preferredHeight) {
						int newIconHeight = Math.min(
								iconHeight + height - preferredHeight,
								(iconWidth + width - preferredWidth) * iconHeight / iconWidth);
						final Quantity<LengthUnit> iconHeightQuantity = new Quantity<LengthUnit>(
								newIconHeight, LengthUnit.px);
						actionComponent.setIcon(FreeplaneIconFactory.toImageIcon(imageIconFactory.getScaledIcon(icon, iconHeightQuantity)));
					}
				}
			}
		}
	}

    static void replaceByImageIcon(Entry entry, AbstractButton actionComponent, EntryAccessor entryAccessor) {
	    Icon icon = entryAccessor.getIcon(entry);
	    if (icon == null) {
	    	icon = actionComponent.getIcon();
	    }
        if(icon != null) {
            actionComponent.setIcon(FreeplaneIconFactory.toImageIcon(icon));
        }
    }

}