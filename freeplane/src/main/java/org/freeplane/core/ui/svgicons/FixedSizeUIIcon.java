package org.freeplane.core.ui.svgicons;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.freeplane.core.util.LogUtils;

public class FixedSizeUIIcon implements Icon {

    private final int width;
    private final int height;
    private final URL url;
    private Image image;
	private double scaleX = 1;
	private double scaleY = 1;
	private boolean failure = false;
	private FixedSizeUIIcon chainedIcon = null;


    public FixedSizeUIIcon(URL url, int width, int height) {
        super();
        this.url = url;
        this.width = width;
        this.height = height;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        if (isValid()) {
            if(image == null)
                createImage(g);
            if(image != null)
				drawImage(c, g, x, y);
        }
    }

	private void drawImage(Component c, Graphics g, int x, int y) {
		final Graphics2D gg = (Graphics2D) g;
		final AffineTransform transform = gg.getTransform();
		if(scaleX != transform.getScaleX() || scaleY != transform.getScaleY()) {
			chainedIcon().paintIcon(c, g, x, y);
		}
		else if(scaleX == 1 && scaleY == 1) {
			gg.drawImage(image, x, y, c);
		}
		else {
			AffineTransform newTransform = AffineTransform.getTranslateInstance(
					x * transform.getScaleX() + transform.getTranslateX(), 
					y * transform.getScaleY()  + transform.getTranslateY());
			gg.setTransform(newTransform);
			gg.drawImage(image, 0, 0, c);
			gg.setTransform(transform);
		}
	}

    private Icon chainedIcon() {
		if(chainedIcon == null)
			chainedIcon = new FixedSizeUIIcon(url, width, height);
		return chainedIcon;
	}

	private boolean isValid() {
        return width >= 0 && height >= 0 && failure == false;
    }

    private void createImage(Graphics g) {
        try {
        	final Graphics2D g2 = (Graphics2D) g;
    		final AffineTransform transform = g2.getTransform();
    		scaleX = transform.getScaleX();
    		scaleY = transform.getScaleY();
    		final int scaledWidth = (int) (getIconWidth() * scaleX);
    		final int scaledHeight = (int) (getIconHeight() * scaleY);

            if(url.getPath().endsWith(".svg"))
            	image = new SVGIconCreator(url).setHeight(scaledHeight).setWidth(scaledWidth).loadImage();
            else {
                Image unloadedScaledImage = ImageIO.read(url).getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
                image = new ImageIcon(unloadedScaledImage).getImage();
            }
        } catch (Exception e) {
            LogUtils.severe(e);
            failure = true;
            image = null;
        }
    }

    @Override
    public int getIconWidth() {
        return width;
    }

    @Override
    public int getIconHeight() {
        return height;
    }

    public static FixedSizeUIIcon withHeigth(URL url, int heightInPixel, boolean hasStandardSize) {
        FixedSizeUIIcon fixedSizeUIIcon = new FixedSizeUIIcon(url, heightInPixel, heightInPixel);
        return hasStandardSize ? fixedSizeUIIcon : fixedSizeUIIcon.withProportionalWidth();
    }

    public FixedSizeUIIcon withProportionalWidth() {
        if (! isValid())
            return this;
        try {
            if(url.getPath().endsWith(".svg")) {
                Dimension size = new SVGIconCreator(url).getSize();
                return new FixedSizeUIIcon(url, height * size.width / size.height , height);
            } else {
                BufferedImage image = ImageIO.read(url);
                return new FixedSizeUIIcon(url, height * image.getWidth() / image.getHeight() , height);
            }
        } catch (Exception e) {
            LogUtils.severe("Can not create icon for URL " + url, e);
            failure = true;
            return null;
        }
    }

}
