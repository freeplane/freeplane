package org.freeplane.core.icon;

public class IconNotFound extends MindIcon {

	private static final String DEFAULT_IMAGE_PATH = "/images";
	
	private static final IconNotFound ICON_NOT_FOUND = new IconNotFound();
	
	public IconNotFound() {
		super("icon_not_found", "IconNotFound.png", "icon not found");
	}
	
	@Override
	public String getDefaultImagePath() {
		return DEFAULT_IMAGE_PATH;
	}
	
	public static IconNotFound instance() {
		return ICON_NOT_FOUND;
	}
}
