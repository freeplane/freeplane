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
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Properties;

import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.FreeplaneVersion;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.icon.IconController;
import org.freeplane.features.icon.mindmapmode.MIconController;
import org.freeplane.features.mode.mindmapmode.MModeController;

/**
 * @author Dimitry Polivaev
 */
public class ApplicationResourceController extends ResourceController {
	final private File autoPropertiesFile;
	final private Properties defProps;
	private LastOpenedList lastOpened;
	final private Properties props;
	public static final String FREEPLANE_BASEDIRECTORY_PROPERTY = "org.freeplane.basedirectory";
	public static final String FREEPLANE_GLOBALRESOURCEDIR_PROPERTY = "org.freeplane.globalresourcedir";
	public static final String DEFAULT_FREEPLANE_GLOBALRESOURCEDIR = "resources";
	private ArrayList<File> resourceDirectories;

	public static void showSysInfo() {
		final StringBuilder info = new StringBuilder();
		info.append("freeplane_version = ");
		final FreeplaneVersion freeplaneVersion = FreeplaneVersion.getVersion();
		info.append(freeplaneVersion);
		String revision = freeplaneVersion.getRevision();

		info.append("; freeplane_xml_version = ");
		info.append(FreeplaneVersion.XML_VERSION);
		if(! revision.equals("")){
			info.append("\ngit revision = ");
			info.append(revision);
		}
		info.append("\njava_version = ");
		info.append(System.getProperty("java.version"));
		info.append("; os_name = ");
		info.append(System.getProperty("os.name"));
		info.append("; os_version = ");
		info.append(System.getProperty("os.version"));
		LogUtils.info(info.toString());
	}


	public static String RESOURCE_BASE_DIRECTORY;
	public static String INSTALLATION_BASE_DIRECTORY;
	static {
		try {
			RESOURCE_BASE_DIRECTORY = new File(System.getProperty(ApplicationResourceController.FREEPLANE_GLOBALRESOURCEDIR_PROPERTY,
			ApplicationResourceController.DEFAULT_FREEPLANE_GLOBALRESOURCEDIR)).getCanonicalPath();
			INSTALLATION_BASE_DIRECTORY = new File(System.getProperty(ApplicationResourceController.FREEPLANE_BASEDIRECTORY_PROPERTY, RESOURCE_BASE_DIRECTORY + "/..")).getCanonicalPath();
		} catch (IOException e) {
		}
	}

	/**
	 * @param controller
	 */
	public ApplicationResourceController() {
		super();
		resourceDirectories = new ArrayList<File>(2);
		defProps = readDefaultPreferences();
		props = readUsersPreferences(defProps);
		final File userDir = createUserDirectory();
		final String resourceBaseDir = getResourceBaseDir();
		if (resourceBaseDir != null) {
			try {
				final File userResourceDir = new File(userDir, "resources");
				userResourceDir.mkdirs();
				resourceDirectories.add(userResourceDir);
				final File resourceDir = new File(resourceBaseDir);
				resourceDirectories.add(resourceDir);
			}
			catch (final Exception e) {
				e.printStackTrace();
			}
		}
		setDefaultLocale(props);
		autoPropertiesFile = getUserPreferencesFile();
		addPropertyChangeListener(new IFreeplanePropertyListener() {
			@Override
			public void propertyChanged(final String propertyName, final String newValue, final String oldValue) {
				if (propertyName.equals(ResourceBundles.RESOURCE_LANGUAGE)) {
					loadAnotherLanguage();
				}
			}
		});
	}

	private File createUserDirectory() {
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
	public URL getResource(final String resourcePath) {
		return AccessController.doPrivileged(new PrivilegedAction<URL>() {

			@Override
			public URL run() {
				final String relName = removeSlashAtStart(resourcePath);
				for(File directory : resourceDirectories) {
					File fileResource = new File(directory, relName);
					if (fileResource.exists()) {
						try {
							return Compat.fileToUrl(fileResource);
						} catch (MalformedURLException e) {
							throw new RuntimeException(e);
						}
					}
				}
				URL resource = ApplicationResourceController.super.getResource(resourcePath);
				if (resource != null) {
					return resource;
				}
				if ("/lib/freeplaneviewer.jar".equals(resourcePath)) {
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
		});
	}

	private String removeSlashAtStart(final String name) {
		final String relName;
		if (name.startsWith("/")) {
			relName = name.substring(1);
		}
		else {
			relName = name;
		}
		return relName;
	}

	@Override
	public String getResourceBaseDir() {
		return RESOURCE_BASE_DIRECTORY;
	}

	@Override
	public String getInstallationBaseDir() {
		return INSTALLATION_BASE_DIRECTORY;
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
		try (InputStream in = new FileInputStream(getUserPreferencesFile())){
			auto.load(in);
		}
		catch (final Exception ex) {
			System.err.println("User properties not found, new file created");
		}
		return auto;
	}

	@Override
	public void saveProperties() {
        MIconController iconController = (MIconController)MModeController.getMModeController().getExtension(IconController.class);
        if(iconController == null)
        	return;
        iconController.saveRecentlyUsedActions();
		try (OutputStream out = new FileOutputStream(autoPropertiesFile)){
			final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(out, "8859_1");
			outputStreamWriter.write("#Freeplane ");
			outputStreamWriter.write(FreeplaneVersion.getVersion().toString());
			outputStreamWriter.write('\n');
			outputStreamWriter.flush();
			props.store(out, null);
			((ResourceBundles)getResources()).saveUserResources();
		}
		catch (final Exception ex) {
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
				localeDef = new Locale(lang.substring(0, 2), lang.substring(3, 5));
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
