/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2009 Tamas Eppel
 *
 *  This file author is Tamas Eppel
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.features.icon;

import java.net.URL;
import java.util.regex.Pattern;

import javax.swing.Icon;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.LengthUnits;
import org.freeplane.core.util.Quantity;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.icon.factory.IconFactory;
import org.freeplane.features.map.NodeModel;

/**
 * Base class for all icons used in FreePlane.
 *
 * @author Tamas Eppel
 *
 */
public class UIIcon implements IconDescription, NamedIcon {
	private static final String DEFAULT_IMAGE_PATH = "/images";
	protected static final String SEPARATOR = "/";
	protected static final String THEME_FOLDER_KEY = "icon.theme.folder";
	protected static final ResourceController RESOURCE_CONTROLLER = ResourceController.getResourceController();
	private static final Pattern parentDirPattern = Pattern.compile(SEPARATOR + "[^" + SEPARATOR + ".]+" + SEPARATOR
	        + "\\.\\." + SEPARATOR);
	private final String name;
	private final String fileName;
	private final String descriptionTranslationKey;
	private final String shortcutKey;
	private URL resourceURL;

	public UIIcon(final String name, final String fileName) {
		this(name, fileName, "", "?");
	}

	public UIIcon(final String name, final String fileName, final String descriptionTranslationKey) {
		this(name, fileName, descriptionTranslationKey, "?");
	}

	public UIIcon(final String name, final String fileName, final String descriptionTranslationKey,
	              final String shortcutKey) {
		this.name = name;
		this.fileName = fileName;
		this.descriptionTranslationKey = descriptionTranslationKey;
		this.shortcutKey = shortcutKey;
	}

	public String getFileName() {
		return fileName;
	}

	/**
	 * @return key for the shortcut in the property file
	 */
	@Override
	public String getShortcutKey() {
		return shortcutKey;
	}

	@Override
	public String getDescriptionTranslationKey() {
		return descriptionTranslationKey;
	}

	@Override
	public String getTranslatedDescription() {
		return TextUtils.getText(descriptionTranslationKey, "");
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Icon getIcon() {
		return IconFactory.getInstance().getIcon(this);
	}

	@Override
	public Icon getIcon(Quantity<LengthUnits> iconHeight) {
		return IconFactory.getInstance().getIcon(this, iconHeight);
	}

	public String getImagePath() {
		return DEFAULT_IMAGE_PATH;
	}

	public URL getUrl() {
		if (resourceURL != null) {
			return resourceURL;
		}
		final String path = getPath();
		resourceURL = RESOURCE_CONTROLLER.getIconResource(path);
		return resourceURL;
	}

	public String getPath() {
		StringBuilder builder = new StringBuilder();
		builder = new StringBuilder();
		builder.append(this.getImagePath());
		builder.append(SEPARATOR);
		builder.append(fileName);
		final String path = parentDirPattern.matcher(builder.toString()).replaceFirst(SEPARATOR);
		return path;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((descriptionTranslationKey == null) ? 0 : descriptionTranslationKey.hashCode());
		result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((shortcutKey == null) ? 0 : shortcutKey.hashCode());
		result = prime * result + this.getClass().hashCode();
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		final UIIcon other = (UIIcon) obj;
		if (descriptionTranslationKey == null) {
			if (other.descriptionTranslationKey != null) {
				return false;
			}
		}
		else if (!descriptionTranslationKey.equals(other.descriptionTranslationKey)) {
			return false;
		}
		if (fileName == null) {
			if (other.fileName != null) {
				return false;
			}
		}
		else if (!fileName.equals(other.fileName)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		}
		else if (!name.equals(other.name)) {
			return false;
		}
		if (shortcutKey == null) {
			if (other.shortcutKey != null) {
				return false;
			}
		}
		else if (!shortcutKey.equals(other.shortcutKey)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public NamedIcon zoom(float zoom) {
		 return new ZoomedIcon(this, zoom);
	}
}
