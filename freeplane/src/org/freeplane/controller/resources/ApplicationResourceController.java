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
package org.freeplane.controller.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.URL;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.freeplane.controller.Controller;
import org.freeplane.controller.Freeplane;

import deprecated.freemind.preferences.IFreemindPropertyListener;

/**
 * @author Dimitry Polivaev
 */
public class ApplicationResourceController extends ResourceController {
	final private File autoPropertiesFile;
	final private Properties defProps;
	final private Properties props;

	/**
	 * @param controller
	 */
	public ApplicationResourceController() {
		super();
		defProps = readDefaultPreferences();
		props = readUsersPreferences(defProps);
		createUserDirectory(defProps);
		createLogger();
		setDefaultLocale(props);
		autoPropertiesFile = getUserPreferencesFile(defProps);
		addPropertyChangeListener(new IFreemindPropertyListener() {
			public void propertyChanged(final String propertyName,
			                            final String newValue,
			                            final String oldValue) {
				if (propertyName.equals(ResourceController.RESOURCE_LANGUAGE)) {
					clearLanguageResources();
					getResources();
				}
			}
		});
	}

	private void createLogger() {
		FileHandler mFileHandler = null;
		final Logger parentLogger = Logger.getAnonymousLogger().getParent();
		final Handler[] handlers = parentLogger.getHandlers();
		for (int i = 0; i < handlers.length; i++) {
			final Handler handler = handlers[i];
			if (handler instanceof ConsoleHandler) {
				parentLogger.removeHandler(handler);
			}
		}
		try {
			mFileHandler = new FileHandler(getFreemindUserDirectory()
			        + File.separator + "log", 1400000, 5, false);
			mFileHandler.setFormatter(new StdFormatter());
			mFileHandler.setLevel(Level.INFO);
			parentLogger.addHandler(mFileHandler);
			final ConsoleHandler stdConsoleHandler = new ConsoleHandler();
			stdConsoleHandler.setFormatter(new StdFormatter());
			stdConsoleHandler.setLevel(Level.WARNING);
			parentLogger.addHandler(stdConsoleHandler);
			LoggingOutputStream los;
			Logger logger = Logger.getLogger(StdFormatter.STDOUT.getName());
			los = new LoggingOutputStream(logger, StdFormatter.STDOUT);
			System.setOut(new PrintStream(los, true));
			logger = Logger.getLogger(StdFormatter.STDERR.getName());
			los = new LoggingOutputStream(logger, StdFormatter.STDERR);
			System.setErr(new PrintStream(los, true));
		}
		catch (final Exception e) {
			System.err.println("Error creating logging File Handler");
			e.printStackTrace();
		}
	}

	private void createUserDirectory(final Properties pDefaultProperties) {
		final File userPropertiesFolder = new File(
		    getFreeMindUserDirectory(pDefaultProperties));
		try {
			if (!userPropertiesFolder.exists()) {
				userPropertiesFolder.mkdir();
			}
		}
		catch (final Exception e) {
			e.printStackTrace();
			System.err
			    .println("Cannot create folder for user properties and logging: '"
			            + userPropertiesFolder.getAbsolutePath() + "'");
		}
	}

	@Override
	public String getDefaultProperty(final String key) {
		return defProps.getProperty(key);
	}

	@Override
	public String getFreemindUserDirectory() {
		return System.getProperty("user.home") + File.separator
		        + getProperty("properties_folder");
	}

	private String getFreeMindUserDirectory(final Properties defaultPreferences) {
		return System.getProperty("user.home") + File.separator
		        + defaultPreferences.getProperty("properties_folder");
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
		return ClassLoader.getSystemResource(name);
	}

	private File getUserPreferencesFile(final Properties defaultPreferences) {
		if (defaultPreferences == null) {
			System.err
			    .println("Panic! Error while loading default properties.");
			System.exit(1);
		}
		final String freemindDirectory = getFreeMindUserDirectory(defaultPreferences);
		final File userPropertiesFolder = new File(freemindDirectory);
		final File autoPropertiesFile = new File(userPropertiesFolder,
		    defaultPreferences.getProperty("autoproperties"));
		return autoPropertiesFile;
	}

	private Properties readDefaultPreferences() {
		final String propsLoc = "freemind.properties";
		final URL defaultPropsURL = ClassLoader.getSystemResource(propsLoc);
		final Properties props = new Properties();
		try {
			InputStream in = null;
			in = defaultPropsURL.openStream();
			props.load(in);
			in.close();
		}
		catch (final Exception ex) {
			ex.printStackTrace();
			System.err
			    .println("Panic! Error while loading default properties.");
		}
		return props;
	}

	private Properties readUsersPreferences(final Properties defaultPreferences) {
		Properties auto = null;
		auto = new Properties(defaultPreferences);
		try {
			InputStream in = null;
			final File autoPropertiesFile = getUserPreferencesFile(defaultPreferences);
			in = new FileInputStream(autoPropertiesFile);
			auto.load(in);
			in.close();
		}
		catch (final Exception ex) {
			System.err.println("User properties not found, new file created");
		}
		return auto;
	}

	@Override
	public void saveProperties() {
		try {
			final OutputStream out = new FileOutputStream(autoPropertiesFile);
			final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
			    out, "8859_1");
			outputStreamWriter.write("#FreeMind ");
			outputStreamWriter.write(Controller.VERSION.toString());
			outputStreamWriter.write('\n');
			outputStreamWriter.flush();
			props.store(out, null);
			out.close();
		}
		catch (final Exception ex) {
		}
		Freeplane.getController().getFilterController().saveConditions();
	}

	/**
	 * @param pProperties
	 */
	private void setDefaultLocale(final Properties pProperties) {
		final String lang = pProperties
		    .getProperty(ResourceController.RESOURCE_LANGUAGE);
		if (lang == null) {
			return;
		}
		Locale localeDef = null;
		switch (lang.length()) {
			case 2:
				localeDef = new Locale(lang);
				break;
			case 5:
				localeDef = new Locale(lang.substring(0, 1), lang.substring(3,
				    4));
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
		props.setProperty(key, value);
		firePropertyChanged(key, value, oldValue);
	}
}
