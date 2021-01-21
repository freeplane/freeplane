package org.freeplane.core.util;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

import javax.imageio.ImageIO;
import javax.swing.Icon;


public class DataUriConverter {

    public static String imageToHtml(Icon icon) throws IOException {
		BufferedImage image = new BufferedImage(
        		icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);

        // paint the image..
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        icon.paintIcon(null, g, 0, 0);
        g.dispose();

        // convert the image
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        System.out.println("baos.toByteArray() " + baos.toByteArray());
        System.out.println("baos.toByteArray().length " + baos.toByteArray().length);
        String data = Base64.getEncoder().encodeToString(baos.toByteArray());
        String imageString = "data:image/png;base64," + data;
        String html =
                "<img src='" + imageString + "'>";
		return html;
	}
}