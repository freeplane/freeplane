package org.freeplane.core.ui.svgicons;

import java.awt.Color;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.ColorUtils;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.icon.factory.IconFactory;

import com.formdev.flatlaf.FlatLaf;
import com.kitfox.svg.SVGCache;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGUniverse;
import com.kitfox.svg.app.beans.SVGIcon;
import com.kitfox.svg.util.FontSystem;

class SVGIconCreator {

    private static final Logger svgFontSystemLogger = Logger.getLogger(FontSystem.class.getName());
    static {
        svgFontSystemLogger.setLevel(Level.SEVERE);
    }

	private static final Pattern PROPERTY_REFERENCE = Pattern.compile("\\$\\{[\\w.]+\\}");
	private static final String UI_COLOR_REPLACEMENTS_PROPERTY_FOR = "uiColorReplacementsFor";
	private static final String DARK_LOOK_AND_FEELS = "DarkLookAndFeels";
	private static final String LIGHT_LOOK_AND_FEELS = "LightLookAndFeels";
	private static final String FLAT_LOOK_AND_FEELS = "FlatLookAndFeels";
	private static String uiColorReplacements = null;
	private static void initializeAccentColorReplacements() {
		if(uiColorReplacements == null) {
			LookAndFeel lookAndFeel = UIManager.getLookAndFeel();
			String lookAndFeelId = lookAndFeel.getName().replaceAll("\\W+", "");
			String lafSpecificReplacementPropertyName = UI_COLOR_REPLACEMENTS_PROPERTY_FOR + lookAndFeelId;
			String lafSpecificReplacementPropertyValue = ResourceController.getResourceController().getProperty(lafSpecificReplacementPropertyName, null);
			String uiColorReplacementsWithPlaceholder = null;
			if(lafSpecificReplacementPropertyValue != null && ! lafSpecificReplacementPropertyValue.isEmpty())
			{
				uiColorReplacementsWithPlaceholder = lafSpecificReplacementPropertyValue;
			}
			else if(! Compat.isApplet() && lookAndFeel instanceof FlatLaf) {
				String flatReplacementPropertyName = UI_COLOR_REPLACEMENTS_PROPERTY_FOR + FLAT_LOOK_AND_FEELS;
				String flatReplacementPropertyValue = ResourceController.getResourceController().getProperty(flatReplacementPropertyName, null);
				if(flatReplacementPropertyValue != null && ! flatReplacementPropertyValue.isEmpty())
				{
					uiColorReplacementsWithPlaceholder = flatReplacementPropertyValue;
				}
			}
			if(uiColorReplacementsWithPlaceholder == null) {
				String defaultReplacementPropertyName = UI_COLOR_REPLACEMENTS_PROPERTY_FOR +
						(UITools.isLightLookAndFeelInstalled() ? LIGHT_LOOK_AND_FEELS : DARK_LOOK_AND_FEELS);
				String defaultReplacementPropertyValue = ResourceController.getResourceController().getProperty(defaultReplacementPropertyName);
				uiColorReplacementsWithPlaceholder = defaultReplacementPropertyValue;
			}
			uiColorReplacements = replacePropertyReferences(uiColorReplacementsWithPlaceholder);
		}
	}

	private static String replacePropertyReferences(String uiColorReplacementsWithPlaceholder) {
		Matcher matcher = PROPERTY_REFERENCE.matcher(uiColorReplacementsWithPlaceholder);
		matcher.reset();
		boolean result = matcher.find();
		if (result) {
		    StringBuffer sb = new StringBuffer();
		    do {
				String match = matcher.group();
				String propertyName = match.substring(2, match.length() - 1);
				Color accentColor = UIManager.getColor(propertyName);
				if(accentColor == null) {
					accentColor = Color.BLUE;
					LogUtils.severe("Color property " + propertyName
							+ " required by replacement " + uiColorReplacementsWithPlaceholder
							+ " is not defined, " + accentColor + " is used");
				}
		        matcher.appendReplacement(sb, ColorUtils.colorToString(accentColor));
		        result = matcher.find();
		    } while (result);
		    matcher.appendTail(sb);
		    return sb.toString();
		}
		return uiColorReplacementsWithPlaceholder;
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
        IconFactory.getInstance().registerIcon(icon, url);
        return icon;
    }

    private void load(SVGUniverse svgUniverse) throws IOException {
    	boolean urlContainsAccentColorReplacementQuery = url.toString().endsWith(ResourceController.USE_ACCENT_COLOR_QUERY);
		if(! urlContainsAccentColorReplacementQuery) {
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
            svgUniverse.loadSVG(openStream(urlContainsAccentColorReplacementQuery), internalUri);
    }

	private String getInternalUri() {
		String query = url.getQuery();
		return query == null ? url.getPath() :  url.getPath() + "?" + url.getQuery();
	}

	private InputStream openStream(boolean urlContainsAccentColorReplacementQuery) throws IOException {
		initializeAccentColorReplacements();
		InputStream stream = (urlContainsAccentColorReplacementQuery ? urlWithoutQuery() : url).openStream();
		if(!uiColorReplacements.isEmpty() && urlContainsAccentColorReplacementQuery)
			return ReplacingInputStream.replace(stream, uiColorReplacements);
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