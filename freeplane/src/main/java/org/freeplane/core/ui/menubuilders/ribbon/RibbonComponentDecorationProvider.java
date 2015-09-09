package org.freeplane.core.ui.menubuilders.ribbon;

import java.awt.Dimension;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.menubuilders.generic.ResourceAccessor;
import org.freeplane.features.icon.factory.ImageIconFactory;
import org.pushingpixels.flamingo.api.common.RichTooltip;
import org.pushingpixels.flamingo.api.common.icon.ImageWrapperResizableIcon;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;

public class RibbonComponentDecorationProvider {
	private ResourceAccessor resourceAccessor;
	
	public RibbonComponentDecorationProvider(ResourceAccessor resourceAccessor) {
		this.resourceAccessor = resourceAccessor;
	}

	public ResizableIcon createIcon(ImageIcon ico) {
		if(ico != null)
			return ImageWrapperResizableIcon.getIcon(ico.getImage(), new Dimension(ico.getIconWidth(), ico.getIconHeight()));
		return null;
	}
	
	public ResizableIcon createIcon(URL location) {
		if(location != null) {
			ImageIcon icon = ImageIconFactory.getInstance().getImageIcon(location);
			return createIcon(icon);
		}
		return null;
	}
	
	public ResizableIcon createIcon(Path location) {
		if(location != null) {
			try {
				return createIcon(location.toUri().toURL());
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}		
		}
		return null;
	}
	
	public ResizableIcon getActionIcon(final AFreeplaneAction action) {
		ImageIcon ico = (ImageIcon) action.getValue(Action.SMALL_ICON);
		if(ico != null) {
			return createIcon(ico);
		}
		else {
			String resource = resourceAccessor.getProperty(action.getIconKey());
			if (resource != null) {
				URL location = resourceAccessor.getResource(resource);
				return createIcon(location);
			}
			else {
				return createIcon(Paths.get(""));
			}
		}
	}
	
	public RichTooltip createRichTooltip(final String title, final String tooltip, final KeyStroke ks) {
		RichTooltip tip = null;
		if (tooltip != null && !"".equals(tooltip)) {
			tip = new RichTooltip(title, tooltip); //TextUtils.removeTranslateComment(tooltip)
		}
		else {
			tip = new RichTooltip(title, "  ");
		}
		if(ks != null) {
			tip.addFooterSection(formatShortcut(ks));
		}
		return tip;
	}
	
	private String formatShortcut(KeyStroke ks) {
		StringBuilder sb = new StringBuilder();
		if(ks != null) {
			String[] st = ks.toString().split("[\\s]+");
			for (String s : st) {
				if("pressed".equals(s.trim())) {
					continue;
				}
				if(sb.length() > 0) {
					sb.append(" + ");
				}
				sb.append(s.substring(0, 1).toUpperCase(Locale.ENGLISH));
				sb.append(s.substring(1));
			}
		}
		return sb.toString();
	}
	
}
