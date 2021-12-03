package org.freeplane.features.styles.mindmapmode.styleeditorpanel;

import java.awt.Font;
import java.io.InputStream;

import org.freeplane.core.resources.ResourceController;

public class IconFont {
	static private Font createIconTextFont() {
		try (InputStream fontInputStream= ResourceController.getResourceController()
				.getResource("/fonts/icons.ttf").openStream()){
			return Font.createFont(Font.TRUETYPE_FONT, fontInputStream);
		}
		catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}
	public static final Font FONT = createIconTextFont();
}