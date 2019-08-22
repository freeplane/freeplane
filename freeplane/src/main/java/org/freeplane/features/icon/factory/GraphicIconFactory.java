package org.freeplane.features.icon.factory;

import java.net.URL;
import java.util.Locale;
import java.util.WeakHashMap;

import javax.swing.Icon;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.LengthUnits;
import org.freeplane.core.ui.svgicons.FreeplaneIconFactory;
import org.freeplane.core.util.Quantity;
import org.freeplane.features.icon.UIIcon;

class GraphicIconFactory implements IconFactory {
	private static final String DEFAULT_IMAGE_PATH = "/images/";
	static final IconFactory FACTORY = new GraphicIconFactory();
	private static final Icon ICON_NOT_FOUND = FACTORY.getIcon(ResourceController.getResourceController()
	    .getResource(DEFAULT_IMAGE_PATH + "IconNotFound.png"));
	
	
	private final WeakValueCache<String, Icon> ICON_CACHE = new WeakValueCache<String, Icon>();
	private final WeakHashMap<Icon, URL> ICON_URLS = new WeakHashMap<Icon, URL>();
	

	
	private GraphicIconFactory() {};

	public Icon getIcon(final UIIcon uiIcon) {
		return getIcon(uiIcon.getUrl(), DEFAULT_UI_ICON_HEIGHT);
	}

	public Icon getIcon(final URL url) {
		return getIcon(url, DEFAULT_UI_ICON_HEIGHT);
	}

	private String createCacheKey(final URL url, final int heightPixels) {
		return url.toString() + "#" + heightPixels;
	}

	public Icon getIcon(UIIcon uiIcon, Quantity<LengthUnits> iconHeight) {
		return getIcon(uiIcon.getUrl(), iconHeight);
	}

	public Icon getIcon(final URL url, Quantity<LengthUnits> iconHeight) {
		Icon result = ICON_NOT_FOUND;
		if (url != null) {
			final int heightPixels = iconHeight.toBaseUnitsRounded();
			final String cacheKey = createCacheKey(url, heightPixels);
			if (ICON_CACHE.containsKey(cacheKey)) {
				result = ICON_CACHE.get(cacheKey);
			}
			else {
				if (url.getPath().toLowerCase(Locale.ENGLISH).endsWith(".svg")) {
					result = FreeplaneIconFactory.createSVGIcon(url, heightPixels);
					ICON_URLS.put(FreeplaneIconFactory.toImageIcon(result), url);
				}
				else {
					result = FreeplaneIconFactory.createIconPrivileged(url);
				}
				ICON_CACHE.put(cacheKey, result);
				ICON_URLS.put(result, url);
				ICON_URLS.put(result, url);
			}
		}
		return result;
	}

	public boolean canScaleIcon(final Icon icon) {
		return ICON_URLS.containsKey(icon);
	}

	public Icon getScaledIcon(final Icon icon, Quantity<LengthUnits> iconHeight) {
		if (iconHeight.toBaseUnitsRounded() == icon.getIconHeight())
			return icon;
		final URL iconUrl = ICON_URLS.get(icon);
		if (iconUrl != null)
			return getIcon(iconUrl, iconHeight);
		else
			throw new IllegalArgumentException("unknown icon");
	}

}