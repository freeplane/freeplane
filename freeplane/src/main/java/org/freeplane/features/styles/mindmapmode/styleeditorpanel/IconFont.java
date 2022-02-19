package org.freeplane.features.styles.mindmapmode.styleeditorpanel;

import java.awt.Font;
import java.io.InputStream;

import javax.swing.JButton;

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
	public static final String REVERT_CHARACTER = "\ue901";
	public static final String REFRESH_CHARACTER = "\ue900";
	public static final String TRANSPARENT_CHARACTER = "\ue902";
	public static final String COPY_CHARACTER = "\ue92c";
	public static final String PASTE_CHARACTER = "\ue92d";
	public static JButton createIconButton() {
		JButton button = new JButton();
		button.setFont(FONT.deriveFont(button.getFont().getSize2D()));
		return button;
	}
}