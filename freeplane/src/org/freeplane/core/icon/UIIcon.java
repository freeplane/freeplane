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
package org.freeplane.core.icon;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.KeyStroke;

import org.freeplane.core.icon.factory.ImageIconFactory;
import org.freeplane.core.resources.ResourceController;

/**
 * Base class for all icons used in FreePlane.
 *
 * @author Tamas Eppel
 *
 */
public class UIIcon implements IIconInformation, Comparable<UIIcon> {

	private static final String DEFAULT_IMAGE_PATH = "/images";

	protected static final String SEPARATOR = "/";

	protected static final String THEME_FOLDER_KEY = "icon.theme.folder";

	protected static final ResourceController RESOURCE_CONTROLLER = ResourceController.getResourceController();
	
	private final String name;
	private final String fileName;
	private final String description;
	private final String shortcutKey;

	private URL resourceURL;

	public UIIcon(final String name, final String fileName) {
		this(name, fileName, "", "?");
	}

	public UIIcon(final String name, final String fileName, final String description) {
		this(name, fileName, description, "?");
	}

	public UIIcon(final String name, final String fileName,
			final String description, final String shortcutKey) {
		this.name = name;
		this.fileName = fileName;
		this.description = description;
		this.shortcutKey = shortcutKey;
	}

	public String getFileName() {
		return this.fileName;
	}

	/**
	 * @return key for the shortcut in the property file
	 */
	public String getShortcutKey() {
		return this.shortcutKey;
	}

	/**
	 * @return description of icon
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * @return name of icon
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @return associated ImageIcon for this icon
	 */
	public Icon getIcon() {
		return ImageIconFactory.getInstance().getImageIcon(this);
	}

	public KeyStroke getKeyStroke() {
		return null; // as in MindIcon TODO ask?
	}

	public String getDefaultImagePath() {
		return DEFAULT_IMAGE_PATH;
	}

	public URL getUrl() {
		if (resourceURL != null){
			return resourceURL;
		}
		final String path = getPath();
		resourceURL = RESOURCE_CONTROLLER.getResource(path);
		
		return resourceURL;
	}

	public String getPath() {
		StringBuilder builder = new StringBuilder();

		builder = new StringBuilder();
		builder.append(this.getDefaultImagePath());
		builder.append(SEPARATOR);
		builder.append(this.fileName);

		final String path = builder.toString();
		return path;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.description == null) ? 0 : this.description.hashCode());
		result = prime * result
				+ ((this.fileName == null) ? 0 : this.fileName.hashCode());
		result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
		result = prime * result
				+ ((this.shortcutKey == null) ? 0 : this.shortcutKey.hashCode());
		result = prime * result + this.getClass().hashCode();
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		final UIIcon other = (UIIcon) obj;
		if (this.description == null) {
			if (other.description != null)
				return false;
		} else if (!this.description.equals(other.description))
			return false;
		if (this.fileName == null) {
			if (other.fileName != null)
				return false;
		} else if (!this.fileName.equals(other.fileName))
			return false;
		if (this.name == null) {
			if (other.name != null)
				return false;
		} else if (!this.name.equals(other.name))
			return false;
		if (this.shortcutKey == null) {
			if (other.shortcutKey != null)
				return false;
		} else if (!this.shortcutKey.equals(other.shortcutKey))
			return false;
		return true;
	}

	public int compareTo(final UIIcon uiIcon) {
		return this.getPath().compareTo(uiIcon.getPath());
	}
	
	@Override
	public String toString() {
		return name;
	}
}
