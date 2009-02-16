/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
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
package org.freeplane.core.model;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.KeyStroke;

import org.freeplane.core.Compat;
import org.freeplane.core.resources.ResourceController;

/**
 * This class represents a MindIcon than can be applied to a node or a whole
 * branch.
 */
public class MindIcon implements Comparable, IIconInformation {
	/**
	 * Set of all created icons. Name -> MindIcon
	 */
	private static HashMap createdIcons = new HashMap();
	private static ImageIcon iconNotFound;
	public static final int LAST = MindIcon.UNKNOWN;
	private static Vector mAllIconNames;
	static int nextNumber = MindIcon.UNKNOWN - 1;
	public static final String PROPERTY_STRING_ICONS_LIST = "icons.list";
	private static final int UNKNOWN = -1;

	public static MindIcon factory(final String iconName) {
		if (MindIcon.createdIcons.containsKey(iconName)) {
			return (MindIcon) MindIcon.createdIcons.get(iconName);
		}
		final MindIcon icon = new MindIcon(iconName);
		MindIcon.createdIcons.put(iconName, icon);
		return icon;
	}

	/**
	 */
	public static MindIcon factory(final String iconName, final ImageIcon icon) {
		if (MindIcon.createdIcons.containsKey(iconName)) {
			return (MindIcon) MindIcon.createdIcons.get(iconName);
		}
		final MindIcon mindIcon = new MindIcon(iconName, icon);
		MindIcon.getAllIconNames().add(iconName);
		MindIcon.createdIcons.put(iconName, mindIcon);
		return mindIcon;
	}

	public static Vector getAllIconNames() {
		if (MindIcon.mAllIconNames != null) {
			return MindIcon.mAllIconNames;
		}
		final Vector mAllIconNames = new Vector();
		final String icons = ResourceController.getResourceController().getProperty(MindIcon.PROPERTY_STRING_ICONS_LIST);
		final StringTokenizer tokenizer = new StringTokenizer(icons, ";");
		while (tokenizer.hasMoreTokens()) {
			mAllIconNames.add(tokenizer.nextToken());
		}
		return mAllIconNames;
	}

	public static String getIconsPath() {
		return "/images/icons/";
	}

	/**
	 * Stores the once created ImageIcon.
	 */
	private ImageIcon associatedIcon;
	private JComponent component = null;
	private String name;
	private int number = MindIcon.UNKNOWN;

	private MindIcon(final String name) {
		setName(name);
		associatedIcon = null;
	}

	/**
	 */
	private MindIcon(final String name, final ImageIcon icon) {
		setName(name);
		associatedIcon = icon;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(final Object o) {
		if (o instanceof MindIcon) {
			final MindIcon icon = (MindIcon) o;
			final int i1 = getNumber();
			final int i2 = icon.getNumber();
			return i1 < i2 ? -1 : i1 == i2 ? 0 : +1;
		}
		throw new ClassCastException();
	}

	public String getDescription() {
		final String resource = new String("icon_" + getName());
		return ResourceController.getResourceController().getText(resource, resource);
	}

	public ImageIcon getIcon() {
		if (MindIcon.iconNotFound == null) {
			MindIcon.iconNotFound = new ImageIcon(ResourceController.getResourceController().getResource(
			    "/images/IconNotFound.png"));
		}
		if (associatedIcon != null) {
			return associatedIcon;
		}
		if (name != null) {
			URL imageURL = ResourceController.getResourceController().getResource(getIconFileName());
			if (imageURL == null) {
				try {
					final File file = new File(ResourceController.getResourceController().getFreeplaneUserDirectory(), "icons/"
					        + getName() + ".png");
					if (file.canRead()) {
						imageURL = Compat.fileToUrl(file);
					}
				}
				catch (final Exception e) {
				}
			}
			final ImageIcon icon = imageURL == null ? MindIcon.iconNotFound : new ImageIcon(imageURL);
			setIcon(icon);
			return icon;
		}
		else {
			setIcon(MindIcon.iconNotFound);
			return MindIcon.iconNotFound;
		}
	}

	public String getIconBaseFileName() {
		return getName() + ".png";
	}

	public String getIconFileName() {
		return MindIcon.getIconsPath() + getIconBaseFileName();
	}

	public KeyStroke getKeyStroke() {
		return null;
	}

	public String getKeystrokeResourceName() {
		return "keystroke_icon_" + name;
	}

	/**
	 * Get the value of name.
	 *
	 * @return Value of name.
	 */
	public String getName() {
		return name == null ? "notfound" : name;
	}

	private int getNumber() {
		if (number == MindIcon.UNKNOWN) {
			number = MindIcon.getAllIconNames().indexOf(name);
		}
		if (number == MindIcon.UNKNOWN) {
			number = MindIcon.nextNumber--;
		}
		return number;
	}

	/**
	 */
	public JComponent getRendererComponent() {
		if (component == null) {
			component = new JLabel(getIcon());
		}
		return component;
	}

	/**
	 * Set the value of icon.
	 *
	 * @param _associatedIcon
	 *            Value to assign to icon.
	 */
	protected void setIcon(final ImageIcon _associatedIcon) {
		associatedIcon = _associatedIcon;
	}

	/**
	 * Set the value of name.
	 *
	 * @param name
	 *            Value to assign to name.
	 */
	public void setName(final String name) {
		this.name = name;
		return;
		/* here, we must check, whether the name is allowed. */
		//
	}

	@Override
	public String toString() {
		return "Icon_name: " + name;
	}
}
