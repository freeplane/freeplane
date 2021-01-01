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

import javax.swing.Icon;

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.icon.factory.IconFactory;

/**
 * Base class for all icons used in FreePlane.
 *
 * @author Tamas Eppel
 *
 */
public class UIIcon implements IconDescription, NamedIcon {
	private static final String DEFAULT_IMAGE_PATH = "/images";
	protected static final ResourceController RESOURCE_CONTROLLER = ResourceController.getResourceController();
	private final String name;
	private final String file;
	private final String descriptionTranslationKey;
	private final String shortcutKey;
	private URL resourceURL;
    private final int order;

	public UIIcon(final String name, final String file, int order) {
		this(name, file, "", "?", order);
	}

	public UIIcon(final String name, final String file, final String descriptionTranslationKey, int order) {
		this(name, file, descriptionTranslationKey, "?", order);
	}

	public UIIcon(final String name, final String file, final String descriptionTranslationKey,
	              final String shortcutKey, int order) {
		this.name = name;
		this.file = file;
		this.descriptionTranslationKey = descriptionTranslationKey;
		this.shortcutKey = shortcutKey;
        this.order = order;
	}

	@Override
    public String getFile() {
		return file;
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
	public Icon getIcon(Quantity<LengthUnit> iconHeight) {
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
		resourceURL = RESOURCE_CONTROLLER.getResource(path);
		return resourceURL;
	}

	public String getPath() {
		return this.getImagePath() + '/' + file;
	}

	@Override
    public int getOrder() {
        return order;
    }

	@Override
    public int hashCode() {
	    return super.hashCode();
     }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
	public String toString() {
		return name;
	}

	@Override
	public NamedIcon zoom(float zoom) {
		 return new ZoomedIcon(this, zoom);
	}

    @Override
    public boolean hasStandardSize() {
        return true;
    }
}
