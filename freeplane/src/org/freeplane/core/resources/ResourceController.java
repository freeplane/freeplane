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
import org.freeplane.core.enums.ResourceControllerProperties;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.resources.ui.BooleanProperty;
import org.freeplane.core.util.LogTool;

/**
 * @author Dimitry Polivaev
 */
public abstract class ResourceController {
	// TODO rladstaetter 15.02.2009 remove static
	private static ResourceController resourceController;

	final private List<IFreeplanePropertyListener> propertyChangeListeners = new Vector<IFreeplanePropertyListener>();
	private FreeplaneResourceBundle resources;

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
			for (final Iterator i = getPropertyChangeListeners().iterator(); i.hasNext();) {
				final IFreeplanePropertyListener listener = (IFreeplanePropertyListener) i.next();
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
			final String localValue = ((FreeplaneResourceBundle) getResources()).getResourceString(
			    ResourceControllerProperties.LOCAL_PROPERTIES + label, null);
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

	/** Returns the ResourceBundle with the current language */
	public ResourceBundle getResources() {
		if (resources == null) {
			resources = new FreeplaneResourceBundle(this);
		}
		return resources;
	}

	public String getText(final String key, final String resource) {
		return ((FreeplaneResourceBundle) getResources()).getResourceString(key, resource);
	}

	
	protected void init(final Controller controller) {
		controller.putAction(new OptionHTMLExportFoldingAction());
		controller.putAction(new OptionSelectionMechanismAction(controller));
		controller.putAction(new ShowSelectionAsRectangleAction(controller));
	}

	boolean isSelectionAsRectangle() {
		return Boolean.parseBoolean(getProperty(ResourceControllerProperties.RESOURCE_DRAW_RECTANGLE_FOR_SELECTION));
	}

	abstract public void loadProperties(InputStream inStream) throws IOException;

	abstract public void loadPropertiesFromXML(InputStream inStream) throws IOException;

	public void removePropertyChangeListener(final IFreeplanePropertyListener listener) {
		propertyChangeListeners.remove(listener);
	}

	abstract public void saveProperties(Controller controller);

	abstract protected void setDefaultProperty(final String key, final String value);

	abstract public void setProperty(final String property, final String value);

	public void toggleSelectionAsRectangle() {
		setProperty(ResourceControllerProperties.RESOURCE_DRAW_RECTANGLE_FOR_SELECTION, new Boolean(
		    !isSelectionAsRectangle()).toString());
	}

	public void updateMenus(final ModeController modeController) {
		LogTool.warn("ResourceController.updateMenus(...) called, but not implemented.");
	}

	static public void setResourceController(final ResourceController resourceController) {
		ResourceController.resourceController = resourceController;
		LogTool.info("called ResourceController.setResourceController(...)");
	}

	static public ResourceController getResourceController() {
		return ResourceController.resourceController;
	}

	public String getResourceBaseDir() {
		return "";
	}
}
