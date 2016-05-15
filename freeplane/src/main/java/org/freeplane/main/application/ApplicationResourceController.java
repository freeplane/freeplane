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
package org.freeplane.main.application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Properties;

import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.FileUtils;
import org.freeplane.core.util.FreeplaneVersion;
import org.freeplane.features.filter.FilterController;

/**
 * @author Dimitry Polivaev
 */
public class ApplicationResourceController extends ResourceController {
	final private File autoPropertiesFile;
	final private Properties defProps;
	private LastOpenedList lastOpened;
	final private Properties props;
	final private ClassLoader urlResourceLoader;
	private static final String FREEPLANE_MAC_PROPERTIES = "/freeplane_mac.properties";
	public static final String FREEPLANE_BASEDIRECTORY_PROPERTY = "org.freeplane.basedirectory";
	public static final String FREEPLANE_GLOBALRESOURCEDIR_PROPERTY = "org.freeplane.globalresourcedir";
	public static final String DEFAULT_FREEPLANE_GLOBALRESOURCEDIR = "resources";

	/**
	 * @param controller
	 */
	public ApplicationResourceController() {
		super();
		defProps = readDefaultPreferences();
		props = readUsersPreferences(defProps);
		final File userDir = createUserDirectory(defProps);
		final ArrayList<URL> urls = new ArrayList<URL>(2);
		final String resourceBaseDir = getResourceBaseDir();
		if (resourceBaseDir != null) {
			try {
				final File userResourceDir = new File(userDir, "resources");
				userResourceDir.mkdirs();
				if (userResourceDir.exists()) {
					final URL userResourceUrl = Compat.fileToUrl(userResourceDir);
					urls.add(userResourceUrl);
				}
				final File resourceDir = new File(resourceBaseDir);
				if (resourceDir.exists()) {
					final URL globalResourceUrl = Compat.fileToUrl(resourceDir);
					urls.add(globalResourceUrl);
				}
			}
			catch (final Exception e) {
				e.printStackTrace();
			}
		}
		if(urls.size() > 0)
			urlResourceLoader = new URLClassLoader(urls.toArray(new URL[]{}), null);
		else
			urlResourceLoader = null;
		setDefaultLocale(props);
		autoPropertiesFile = getUserPreferencesFile();
		addPropertyChangeListener(new IFreeplanePropertyListener() {
			public void propertyChanged(final String propertyName, final String newValue, final String oldValue) {
				if (propertyName.equals(ResourceBundles.RESOURCE_LANGUAGE)) {
					loadAnotherLanguage();
				}
			}
		});
	}

	private File createUserDirectory(final Properties pDefaultProperties) {
		final File userPropertiesFolder = new File(getFreeplaneUserDirectory());
		try {
			if (!userPropertiesFolder.exists()) {
				userPropertiesFolder.mkdirs();
			}
			return userPropertiesFolder;
		}
		catch (final Exception e) {
			e.printStackTrace();
			System.err.println("Cannot create folder for user properties and logging: '"
			        + userPropertiesFolder.getAbsolutePath() + "'");
			return null;
		}
	}

	@Override
	public String getDefaultProperty(final String key) {
		return defProps.getProperty(key);
	}

	@Override
	public String getFreeplaneUserDirectory() {
		return Compat.getApplicationUserDirectory();
	}

	public LastOpenedList getLastOpenedList() {
		return lastOpened;
	}

	@Override
	public Properties getProperties() {
		return props;
	}

	@Override
	public String getProperty(final String key) {
		return props.getProperty(key);
	}

	@Override
	public URL getResource(final String name) {
		if (urlResourceLoader == null) {
			return super.getResource(name);
		}
		final String relName;
		if (name.startsWith("/")) {
			relName = name.substring(1);
		}
		else {
			relName = name;
		}
		URL resource = urlResourceLoader.getResource(relName);
		if (resource != null) {
			return resource;
		}
		resource = super.getResource(name);
		if (resource != null) {
			return resource;
		}
		if ("/lib/freeplaneviewer.jar".equals(name)) {
			final String rootDir = new File(getResourceBaseDir()).getAbsoluteFile().getParent();
			try {
				final File try1 = new File(rootDir + "/plugins/org.freeplane.core/lib/freeplaneviewer.jar");
				if (try1.exists()) {
					return try1.toURL();
				}
				final File try2 = new File(rootDir + "/lib/freeplaneviewer.jar");
				if (try2.exists()) {
					return try2.toURL();
				}
			}
			catch (final MalformedURLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public String getResourceBaseDir() {
		return FreeplaneGUIStarter.getResourceBaseDir();
	}

	@Override
	public String getInstallationBaseDir() {
		return FreeplaneGUIStarter.getInstallationBaseDir();
    }

	public static File getUserPreferencesFile() {
		final String freeplaneDirectory = Compat.getApplicationUserDirectory();
		final File userPropertiesFolder = new File(freeplaneDirectory);
		final File autoPropertiesFile = new File(userPropertiesFolder, "auto.properties");
		return autoPropertiesFile;
	}

	@Override
	public void init() {
		lastOpened = new LastOpenedList();
		super.init();
	}

	private Properties readDefaultPreferences() {
		final Properties props = new Properties();
		readDefaultPreferences(props, ResourceController.FREEPLANE_PROPERTIES);
		if (Compat.isMacOsX()) {
			readDefaultPreferences(props, FREEPLANE_MAC_PROPERTIES);
		}
		final String propsLocs = props.getProperty("load_next_properties", "");
		readDefaultPreferences(props, propsLocs.split(";"));
		return props;
	}

	private void readDefaultPreferences(final Properties props, final String[] locArray) {
		for (final String loc : locArray) {
			readDefaultPreferences(props, loc);
		}
	}

	private void readDefaultPreferences(final Properties props, final String propsLoc) {
		final URL defaultPropsURL = getResource(propsLoc);
		loadProperties(props, defaultPropsURL);
	}

	private Properties readUsersPreferences(final Properties defaultPreferences) {
		final Properties auto = new Properties(defaultPreferences);
		InputStream in = null;
		try {
			final File autoPropertiesFile = getUserPreferencesFile();
			in = new FileInputStream(autoPropertiesFile);
			auto.load(in);
		}
		catch (final Exception ex) {
			System.err.println("User properties not found, new file created");
		}
		finally {
			FileUtils.silentlyClose(in);
		}
		return auto;
	}

	@Override
	public void saveProperties() {
		lastOpened.saveProperties();
		OutputStream out = null;
		try {
			out = new FileOutputStream(autoPropertiesFile);
			final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(out, "8859_1");
			outputStreamWriter.write("#Freeplane ");
			outputStreamWriter.write(FreeplaneVersion.getVersion().toString());
			outputStreamWriter.write('\n');
			outputStreamWriter.flush();
			props.store(out, null);
		}
		catch (final Exception ex) {
		}
		finally {
			if (out != null) {
				try {
					out.close();
				}
				catch (final IOException e) {
				}
			}
		}
		FilterController.getCurrentFilterController().saveConditions();
	}

	/**
	 * @param pProperties
	 */
	private void setDefaultLocale(final Properties pProperties) {
		final String lang = pProperties.getProperty(ResourceBundles.RESOURCE_LANGUAGE);
		if (lang == null) {
			return;
		}
		Locale localeDef = null;
		switch (lang.length()) {
			case 2:
				localeDef = new Locale(lang);
				break;
			case 5:
				localeDef = new Locale(lang.substring(0, 1), lang.substring(3, 4));
				break;
			default:
				return;
		}
		Locale.setDefault(localeDef);
	}

	@Override
	public void setDefaultProperty(final String key, final String value) {
		defProps.setProperty(key, value);
	}

	@Override
	public void setProperty(final String key, final String value) {
		final String oldValue = getProperty(key);
		if (oldValue == value) {
			return;
		}
		if (oldValue != null && oldValue.equals(value)) {
			return;
		}
		props.setProperty(key, value);
		firePropertyChanged(key, value, oldValue);
	}
}
