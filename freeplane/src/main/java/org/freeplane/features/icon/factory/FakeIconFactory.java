package org.freeplane.features.icon.factory;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.freeplane.core.ui.LengthUnits;
import org.freeplane.core.util.Quantity;
import org.freeplane.features.icon.UIIcon;

class FakeIconFactory implements IconFactory {
    static final IconFactory FACTORY = new FakeIconFactory();
	private FakeIconFactory() {}

	@Override
	public boolean canScaleIcon(Icon icon) {
		return true;
	}

	@Override
	public Icon getScaledIcon(Icon icon, Quantity<LengthUnits> quantity) {
		return icon(quantity.toBaseUnitsRounded());
	}
	static long paintCounter = 0;

    private Icon icon(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.GREEN);
        graphics.fillOval(2, 2, size-2, size-2);
        graphics.dispose();
        return new ImageIcon(image) {
            @Override
            public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
                paintCounter++;
                super.paintIcon(c, g, x, y);
            }
            
        };
    }

	@Override
	public Icon getIcon(URL imageURL) {
		return icon(32);
	}

	@Override
	public Icon getIcon(UIIcon icon) {
		return icon(32);
	}

	@Override
	public Icon getIcon(UIIcon uiIcon, Quantity<LengthUnits> iconHeight) {
		return icon(iconHeight.toBaseUnitsRounded());
	}

	@Override
	public Icon getIcon(URL url, Quantity<LengthUnits> iconHeight) {
		return icon(iconHeight.toBaseUnitsRounded());
	}
	
}