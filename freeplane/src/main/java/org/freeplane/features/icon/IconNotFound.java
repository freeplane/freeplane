package org.freeplane.features.icon;

import javax.swing.Icon;

import org.freeplane.core.resources.ResourceController;

public class IconNotFound extends MindIcon {
	private static final int ORDER = Integer.MAX_VALUE;
    private static final String DEFAULT_IMAGE_PATH = "/images";

	public IconNotFound(final String originallyRequestedIconName) {
		super(originallyRequestedIconName, "IconNotFound.svg", "icon not found", ORDER);
	}

	@Override
	public String getImagePath() {
		return DEFAULT_IMAGE_PATH;
	}

    /**
     * creates an ImageIcon from <code>getImagePath()/filename</code>. If this is not possible returns an IconNotFound.
     * THIS METHOD is USED for displaying add-on images, so there is not image 'name'.
     */
    public static Icon createIconOrReturnNotFoundIcon(final String... fileNames) {
        for(String fileName : fileNames) {
            final Icon icon = ResourceController.getResourceController()
            .getIcon(DEFAULT_IMAGE_PATH + "/" + fileName);
        if (icon != null)
            return icon;
        }
        return new IconNotFound("?").getIcon();
    }
 
}
