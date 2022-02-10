package org.freeplane.core.ui.svgicons;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.Icon;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;

import com.kitfox.svg.SVGCache;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGUniverse;
import com.kitfox.svg.app.beans.SVGIcon;

class SVGIconCreator {

	private static final String ACCENT_COLOR_REPLACEMENTS_PROPERTY = "accentColorReplacements";
	private static final String FOR_DARK_LOOK_AND_FEELS = "ForDarkLookAndFeels";
	private static final String FOR_LIGHT_LOOK_AND_FEELS = "ForLightLookAndFeels";
	private static String accentColorReplacements = null;
	private static void initializeAccentRolorReplacements() {
		accentColorReplacements = ResourceController.getResourceController().getProperty(ACCENT_COLOR_REPLACEMENTS_PROPERTY +
				(UITools.isLightLookAndFeelInstalled() ? FOR_LIGHT_LOOK_AND_FEELS : FOR_DARK_LOOK_AND_FEELS));
	}
	private final URL url;
    private int heightPixels = -1;
    private int widthPixels = -1;
    private URI svgUri;
    private boolean diagramWasAlreadyLoaded;

    SVGIconCreator(URL url) {
        this.url = url;
    }

    Icon createIcon() {
        return new CachingIcon(this::createSvgIcon);
    }

    Dimension getSize() {
        SVGIcon icon = createSvgIcon();
        int iconWidth = icon.getIconWidth();
        int iconHeight = icon.getIconHeight();
        if(! diagramWasAlreadyLoaded) {
            SVGUniverse svgUniverse = SVGCache.getSVGUniverse();
            svgUniverse.removeDocument(svgUri);
        }
        return new Dimension(iconWidth, iconHeight);
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
    	if(url.getQuery() == null) {
    		try {
    			svgUri = new URI(url.toString());
    			diagramWasAlreadyLoaded = svgUniverse.getDiagram(svgUri, false) != null;
    			if(! diagramWasAlreadyLoaded)
    				svgUniverse.loadSVG(url);
    			return;
    		}
    		catch (URISyntaxException ex) {
    		}
    	}
    	String internalUri = getInternalUri();
		svgUri = svgUniverse.getStreamBuiltURI(internalUri);
        diagramWasAlreadyLoaded = svgUniverse.getDiagram(svgUri, false) != null;
        if(! diagramWasAlreadyLoaded)
            svgUniverse.loadSVG(openStream(), internalUri);
    }

	private String getInternalUri() {
		String query = url.getQuery();
		return query == null ? url.getPath() :  url.getPath() + "?" + url.getQuery();
	}

	private InputStream openStream() throws IOException {
		initializeAccentRolorReplacements();
		boolean urlContainsAccentColorReplacementQuery = ResourceController.USE_ACCENT_COLOR.equals(url.getQuery());
		InputStream stream = (urlContainsAccentColorReplacementQuery ? urlWithoutQuery() : url).openStream();
		if(!accentColorReplacements.isEmpty() && urlContainsAccentColorReplacementQuery)
			return ReplacingInputStream.replace(stream, accentColorReplacements);
		else
			return stream;
	}

	private URL urlWithoutQuery() throws MalformedURLException {
		String urlString = url.toString();
		int queryIndex = urlString.lastIndexOf('?');
		return new URL(urlString.substring(0, queryIndex));
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