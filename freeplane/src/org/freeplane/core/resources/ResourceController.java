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

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Vector;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.FileUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.AController.IActionOnChange;
import org.freeplane.features.mode.Controller;

/**
 * @author Dimitry Polivaev
 */
public abstract class ResourceController {
	public static final String FREEPLANE_PROPERTIES = "/freeplane.properties";
	public static final String LOCAL_PROPERTIES = "LocalProperties.";
	public static final String RESOURCE_DRAW_RECTANGLE_FOR_SELECTION = "standarddrawrectangleforselection";
	// some plugins have their own file for registration of defaults
	public static final String PLUGIN_DEFAULTS_RESOURCE = "defaults.properties";

	static public ResourceController getResourceController() {
		return Controller.getCurrentController().getResourceController();
	}

	final private List<IFreeplanePropertyListener> propertyChangeListeners = new Vector<IFreeplanePropertyListener>();
	private ResourceBundles resources;
    public static final String FREEPLANE_RESOURCE_URL_PROTOCOL = "freeplaneresource";
	public static final String OBJECT_TYPE = "ObjectType";
	public ResourceController() {
		super();
	}

	public void addLanguageResources(final String language, final URL url) {
		resources.addResources(language, url);
	}

	public void addLanguageResources(final String language, final Map<String, String> resources) {
		this.resources.addResources(language, resources);
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
		for (final Entry<Object, Object> entry : getProperties().entrySet()) {
			final String key = (String) entry.getKey();
			listener.propertyChanged(key, (String) entry.getValue(), null);
		}
	}

	protected void loadAnotherLanguage() {
		resources.loadAnotherLanguage();
	}

	public void firePropertyChanged(final String property, final String value, final String oldValue) {
		if (oldValue == null || !oldValue.equals(value)) {
			setProperty(property, value);
			for (final IFreeplanePropertyListener listener : getPropertyChangeListeners()) {
				listener.propertyChanged(property, value, oldValue);
			}
		}
	}

	public boolean getBooleanProperty(final String key) {
		return Boolean.parseBoolean(getProperty(key));
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Enum<T>> T getEnumProperty(String propertyName, Enum<T>  defaultValue) {
		try{
			final String cacheProptertyValue = getProperty(propertyName).toUpperCase(Locale.ENGLISH);
			defaultValue = Enum.valueOf(defaultValue.getClass(), cacheProptertyValue);
		}
		catch (Exception e) {
			LogUtils.severe(e);
		}
		return (T)defaultValue;
	}


	/**
	 * @param resourcesNodeTextColor
	 * @return
	 */
	public String getDefaultProperty(final String key) {
		return null;
	}

	/** register defaults in freeplane.properties respectively defaults.properties instead! */
	public double getDoubleProperty(final String key, final double defaultValue) {
		try {
			return Double.parseDouble(ResourceController.getResourceController().getProperty(key));
		}
		catch (final Exception e) {
			return defaultValue;
		}
	}

	/**
	 * @return
	 */
	abstract public String getFreeplaneUserDirectory();

	/** register defaults in freeplane.properties respectively defaults.properties instead! */
	public int getIntProperty(final String key, final int defaultValue) {
		try {
			return Integer.parseInt(getProperty(key));
		}
		catch (final NumberFormatException nfe) {
			return defaultValue;
		}
	}


	public int getIntProperty(String key) {
		return Integer.parseInt(getProperty(key));
    }

	public double getDoubleProperty(String key) {
		return Double.parseDouble(getProperty(key));
    }

	/** register defaults in freeplane.properties respectively defaults.properties instead. */
	public long getLongProperty(final String key, final int defaultValue) {
		try {
			return Long.parseLong(getProperty(key));
		}
		catch (final NumberFormatException nfe) {
			return defaultValue;
		}
	}

	abstract public Properties getProperties();

	abstract public String getProperty(final String key);

	/** register defaults in freeplane.properties respectively defaults.properties instead! */
	public String getProperty(final String key, final String value) {
		return getProperties().getProperty(key, value);
	}

	public Collection<IFreeplanePropertyListener> getPropertyChangeListeners() {
		return Collections.unmodifiableCollection(propertyChangeListeners);
	}

	public URL getResource(final String name) {
		return getClass().getResource(name);
	}

	public InputStream getResourceStream(final String resFileName) throws IOException {
		final URL resUrl = getResource(resFileName);
		if (resUrl == null) {
			LogUtils.severe("Can't find " + resFileName + " as resource.");
			throw new IllegalArgumentException("Can't find " + resFileName + " as resource.");
		}
		return new BufferedInputStream(resUrl.openStream());
	}

	public String getResourceBaseDir() {
		return "";
	}

	public String getInstallationBaseDir() {
		return "";
    }

	/** Returns the ResourceBundle with the current language */
	public ResourceBundle getResources() {
		if (resources == null) {
			resources = new ResourceBundles(this);
		}
		return resources;
	}

	public String getLanguageCode() {
	    return resources.getLanguageCode();
    }
	
	public String getDefaultLanguageCode() {
		return resources.getDefaultLanguageCode();
	}

	public String getText(final String key, final String resource) {
		return ((ResourceBundles) getResources()).getResourceString(key, resource);
	}

	protected void init() {
	}

	public void removePropertyChangeListener(final IFreeplanePropertyListener listener) {
		propertyChangeListeners.remove(listener);
	}

	abstract public void saveProperties();

	abstract public void setDefaultProperty(final String key, final String value);

	public void setProperty(final String property, final boolean value) {
		setProperty(property, Boolean.toString(value));
	}

	abstract public void setProperty(final String property, final String value);
	
	/** adds properties from url to properties. Existing properties in resultProps will be overridden.
	 * @return false if anything went wrong. */
	protected static boolean loadProperties(Properties resultProps, final URL url) {
		InputStream in = null;
		try {
			in = new BufferedInputStream(url.openStream());
			resultProps.load(in);
			LogUtils.info("Loaded properties from " + url);
			return true;
		}
		catch (final Exception ex) {
			System.err.println("Could not load properties from " + url);
		}
        finally {
        	FileUtils.silentlyClose(in);
        }
		return false;
	}

	/** will add properties from propertiesUrl if they don't exist yet. */
	public void addDefaults(URL propertiesUrl) {
		Properties props = new Properties();
		loadProperties(props, propertiesUrl);
		addDefaults(props);
    }

	/** use generic to make it useable with Properties. KT and VT must be of type String. */
	public <KT, VT> void addDefaults(Map<KT, VT> defaultProperties) {
		for (Entry<KT, VT> entry : defaultProperties.entrySet()) {
			if (getProperty((String) entry.getKey()) == null)
				setProperty((String) entry.getKey(), (String) entry.getValue());
		}
    }

	public boolean isApplet() {
		return false;
	}

	public void removePropertyChangeListener(final Class<? extends IActionOnChange> clazz, final AFreeplaneAction action) {
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
