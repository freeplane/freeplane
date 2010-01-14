/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is created by Dimitry Polivaev in 2008.
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
package org.freeplane.core.resources;

import java.awt.Font;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Vector;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.controller.AController.IActionOnChange;
import org.freeplane.core.modecontroller.INodeSelectionListener;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.LogTool;

/**
 * @author Dimitry Polivaev
 */
public abstract class ResourceController {
	public static final String FREEPLANE_PROPERTIES = "/freeplane.properties";
	public static final String LOCAL_PROPERTIES = "LocalProperties.";
	public static final String RESOURCE_DRAW_RECTANGLE_FOR_SELECTION = "standarddrawrectangleforselection";
	// TODO rladstaetter 15.02.2009 remove static
	private static ResourceController resourceController;

	static public ResourceController getResourceController() {
		return ResourceController.resourceController;
	}

	static public void setResourceController(final ResourceController resourceController) {
		ResourceController.resourceController = resourceController;
		LogTool.info("called ResourceController.setResourceController(...)");
	}

	final private List<IFreeplanePropertyListener> propertyChangeListeners = new Vector<IFreeplanePropertyListener>();
	private ResourceBundles resources;

	public ResourceController() {
		super();
	}

	public void addLanguageResources(final String language, final URL url) {
		resources.addResources(language, url);
	}

	public void addPropertyChangeListener(final IFreeplanePropertyListener listener) {
		propertyChangeListeners.add(listener);
	}

	/**
	 * @param listener
	 *            The new listener. All currently available properties are sent
	 *            to the listener after registration. Here, the oldValue
	 *            parameter is set to null.
	 */
	public void addPropertyChangeListenerAndPropagate(final IFreeplanePropertyListener listener) {
		addPropertyChangeListener(listener);
		final Properties properties = getProperties();
		for (final Iterator it = properties.keySet().iterator(); it.hasNext();) {
			final String key = (String) it.next();
			listener.propertyChanged(key, properties.getProperty(key), null);
		}
	}

	public void clearLanguageResources() {
		resources.reloadLanguage();
	}

	protected void firePropertyChanged(final String property, final String value, final String oldValue) {
		if (oldValue == null || !oldValue.equals(value)) {
			setProperty(property, value);
			for (IFreeplanePropertyListener listener : getPropertyChangeListeners()) {
				listener.propertyChanged(property, value, oldValue);
			}
		}
	}

	public String getAdjustableProperty(final String label) {
		String value = getProperty(label);
		if (value == null) {
			return value;
		}
		if (value.startsWith("?") && !value.equals("?")) {
			final String localValue = ((ResourceBundles) getResources()).getResourceString(
			    ResourceController.LOCAL_PROPERTIES + label, null);
			value = localValue == null ? value.substring(1).trim() : localValue;
			setDefaultProperty(label, value);
		}
		return value;
	}

	public boolean getBooleanProperty(final String key) {
		return Boolean.parseBoolean(getProperty(key));
	}

	public Font getDefaultFont() {
		final int fontSize = getDefaultFontSize();
		final int fontStyle = getDefaultFontStyle();
		final String fontFamily = getDefaultFontFamilyName();
		return new Font(fontFamily, fontStyle, fontSize);
	}

	/**
	 */
	public String getDefaultFontFamilyName() {
		return getProperty("defaultfont");
	}

	/**
	 */
	public int getDefaultFontSize() {
		return Integer.parseInt(getProperty("defaultfontsize"));
	}

	/**
	 */
	public int getDefaultFontStyle() {
		return Integer.parseInt(getProperty("defaultfontstyle"));
	}

	/**
	 * @param resourcesNodeTextColor
	 * @return
	 */
	public String getDefaultProperty(final String key) {
		return null;
	}

	public double getDoubleProperty(final String key, final double defaultValue) {
		try {
			return Double.parseDouble(ResourceController.getResourceController().getProperty("user_zoom"));
		}
		catch (final Exception e) {
			return defaultValue;
		}
	}

	/**
	 * @return
	 */
	abstract public String getFreeplaneUserDirectory();

	public int getIntProperty(final String key, final int defaultValue) {
		try {
			return Integer.parseInt(getProperty(key));
		}
		catch (final NumberFormatException nfe) {
			return defaultValue;
		}
	}

	public long getLongProperty(final String key, final int defaultValue) {
		try {
			return Long.parseLong(getProperty(key));
		}
		catch (final NumberFormatException nfe) {
			return defaultValue;
		}
	}

	/**
	 * @return
	 */
	abstract public Properties getProperties();

	abstract public String getProperty(final String key);

	public String getProperty(final String key, final String value) {
		return getProperties().getProperty(key, value);
	}

	public Collection<IFreeplanePropertyListener> getPropertyChangeListeners() {
		return Collections.unmodifiableCollection(propertyChangeListeners);
	}

	public URL getResource(final String name) {
		return getClass().getResource(name);
	}

	public String getResourceBaseDir() {
		return "";
	}

	/** Returns the ResourceBundle with the current language */
	public ResourceBundle getResources() {
		if (resources == null) {
			resources = new ResourceBundles(this);
		}
		return resources;
	}

	public String getText(final String key, final String resource) {
		return ((ResourceBundles) getResources()).getResourceString(key, resource);
	}

	protected void init(final Controller controller) {
		controller.addAction(new ShowSelectionAsRectangleAction(controller));
	}

	boolean isSelectionAsRectangle() {
		return Boolean.parseBoolean(getProperty(ResourceController.RESOURCE_DRAW_RECTANGLE_FOR_SELECTION));
	}

	abstract public void loadProperties(InputStream inStream) throws IOException;

	abstract public void loadPropertiesFromXML(InputStream inStream) throws IOException;

	public void removePropertyChangeListener(final IFreeplanePropertyListener listener) {
		propertyChangeListeners.remove(listener);
	}

	abstract public void saveProperties(Controller controller);

	abstract public void setDefaultProperty(final String key, final String value);

	public void setProperty(final String property, final boolean value) {
		setProperty(property, Boolean.toString(value));
	}

	abstract public void setProperty(final String property, final String value);


	public void toggleSelectionAsRectangle() {
		setProperty(ResourceController.RESOURCE_DRAW_RECTANGLE_FOR_SELECTION, Boolean.toString(!isSelectionAsRectangle()) );
	}

	public boolean isApplet() {
	    return false;
    }

	public void removePropertyChangeListener(final Class<? extends IActionOnChange> clazz, AFreeplaneAction action) {
		final Iterator<IFreeplanePropertyListener> iterator = propertyChangeListeners.iterator();
		while (iterator.hasNext()) {
			final IFreeplanePropertyListener next = iterator.next();
			if (next instanceof IActionOnChange && ((IActionOnChange) next).getAction() == action) {
				iterator.remove();
				return;
			}
		}
    }
}
