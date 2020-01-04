package org.freeplane.core.ui.svgicons;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.Icon;

import com.kitfox.svg.SVGCache;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGUniverse;
import com.kitfox.svg.app.beans.SVGIcon;

class SVGIconCreator {

	private final URL url;
    private int heightPixels = -1;
    private int widthPixels = -1;
    private URI svgUri;
    private boolean diagramWasAlreadyLoaded;

    SVGIconCreator(URL url) {
        this.url = url;
    }

    Icon createIcon() {
        SVGIcon icon = createSvgIcon();
        return new CachingIcon(icon);
    }

    Image loadImage() {
        SVGIcon icon = createSvgIcon();
        int iconWidth = icon.getIconWidth();
        int iconHeight = icon.getIconHeight();
        BufferedImage image = new BufferedImage(iconWidth, iconHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        icon.paintIcon(null, graphics, 0, 0);
        graphics.dispose();
        if(! diagramWasAlreadyLoaded) {
            SVGUniverse svgUniverse = SVGCache.getSVGUniverse();
            svgUniverse.removeDocument(svgUri);
        }
        return image;
    }

    private SVGIcon createSvgIcon() {
        SVGUniverse svgUniverse = SVGCache.getSVGUniverse();
        SVGIcon icon = new SVGIcon();
        try {
            load(svgUniverse);
            final SVGDiagram diagram = svgUniverse.getDiagram(svgUri);
            if(heightPixels >= 0 && widthPixels >= 0) {
                icon.setPreferredSize(new Dimension(widthPixels, heightPixels));
            }
            else if(heightPixels >= 0 || widthPixels >= 0) {
                float aspectRatio = diagram.getHeight() / diagram.getWidth();
                if(heightPixels >= 0)
                    icon.setPreferredSize(new Dimension((int) (heightPixels / aspectRatio), heightPixels));
                else
                    icon.setPreferredSize(new Dimension(widthPixels, (int) (widthPixels * aspectRatio)));
            }
            icon.setAutosize(SVGIcon.AUTOSIZE_STRETCH);
            icon.setAntiAlias(FreeplaneIconFactory.isSvgAntialiasEnabled());
            icon.setSvgURI(svgUri);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        return icon;
    }

    private void load(SVGUniverse svgUniverse) throws IOException {
        try {
            svgUri = new URI(url.toString());
            diagramWasAlreadyLoaded = svgUniverse.getDiagram(svgUri, false) != null;
            if(! diagramWasAlreadyLoaded)
                svgUniverse.loadSVG(url);
        }
        catch (URISyntaxException ex) {
            svgUri = svgUniverse.getStreamBuiltURI(url.getPath()); 
            diagramWasAlreadyLoaded = svgUniverse.getDiagram(svgUri, false) != null;
            if(! diagramWasAlreadyLoaded)
                svgUniverse.loadSVG(url.openStream(), url.getPath());
        }
    }

	SVGIconCreator setHeight(final int heightPixels) {
		this.heightPixels = heightPixels;
        return this;
	}

	SVGIconCreator setWidth(final int widthPixels) {
		this.widthPixels = widthPixels;
        return this;
	}
}