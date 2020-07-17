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
package org.freeplane.core.resources;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import org.freeplane.core.util.FileUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.collection.MultipleValueMap;

/**
 * Class for managing localized resources. See translation property files.
 */
public class ResourceBundles extends ResourceBundle {
	public static final String LANGUAGE_AUTOMATIC = "automatic";
	private static final String DEFAULT_LANGUAGE = "en";
	public static final String POSTFIX_TRANSLATE_ME = "[translate me]";
	public static final String RESOURCE_LANGUAGE = "language";
	/**
	 *
	 */
//	final private ResourceController controller;
	private Map<String, String> defaultResources;
	private final MultipleValueMap<String, URL> externalResources;
	private String lang;
	private Map<String, String> languageResources;
	final private Properties userLanguageResources;
	final private boolean isUserDefined;
	private boolean userResourcesHaveChanged;

	ResourceBundles(String language) {
		final URL systemResource = getSystemResourceUrl(DEFAULT_LANGUAGE);
		isUserDefined = systemResource.getProtocol().equalsIgnoreCase("file");
		externalResources = new MultipleValueMap<String, URL>();
		userLanguageResources = new Properties();
		userResourcesHaveChanged = false;
		try {
			loadLocalLanguageResources(language);
			if(lang.equals(DEFAULT_LANGUAGE))
				defaultResources = languageResources;
			else
				defaultResources = getLanguageResources(DEFAULT_LANGUAGE);
		}
		catch (final Exception ex) {
			LogUtils.severe(ex);
			LogUtils.severe("Error loading Resources");
		}
	}

	public void addResources(final String language, final Map<String, String> resources) {
		if (language.equalsIgnoreCase(DEFAULT_LANGUAGE)) {
			defaultResources.putAll(resources);
		}
		else if (language.equalsIgnoreCase(lang)) {
			languageResources.putAll(resources);
		}
    }

	public void addResources(final String language, final URL url) {
		try {
			addResources(language, getLanguageResources(url));
			externalResources.put(language, url);
		}
		catch (final IOException e) {
			LogUtils.severe(e);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
	public Enumeration getKeys() {
		final Iterator<String> iterator = defaultResources.keySet().iterator();
		return new Enumeration() {
			public boolean hasMoreElements() {
				return iterator.hasNext();
			}

			public Object nextElement() {
				return iterator.next();
			}
		};
	}

	public String getLanguageCode() {
		return lang;
	}
	
	public String getDefaultLanguageCode() {
		return DEFAULT_LANGUAGE;
	}

	/**
	 * @throws IOException
	 */
	private Map<String, String> getLanguageResources(final String lang) throws IOException {
		final URL systemResource = getSystemResourceUrl(lang);
		if (systemResource == null) {
			// no double logging: System.out.println("core resource " + resourceName + " not found");
			return null;
		}
		final Map<String, String> resources = getLanguageResources(systemResource);
		final Iterator<URL> iterator = externalResources.get(lang).iterator();
		while (iterator.hasNext()) {
			resources.putAll(getLanguageResources(iterator.next()));
		}
		return resources;
	}

	private URL getSystemResourceUrl(final String lang) {
	    String resourceName = "/translations/Resources" + "_" + lang + ".properties";
		final URL systemResource = ResourceController.getResourceController().getResource(resourceName);
	    return systemResource;
    }

	@SuppressWarnings({ "unchecked", "rawtypes" })
    private Map<String, String> getLanguageResources(final URL systemResource) throws IOException {
		try (InputStream in= new BufferedInputStream(systemResource.openStream())){
			final Properties bundle = new Properties();
			bundle.load(in);
			return new HashMap(bundle);
        }
	}

	public String getResourceString(final String key) {
		final String resourceString = getResourceString(key, key);
		if (resourceString == key) {
			if(isUserDefined)
				System.out.println("missing key " + key);
			else
				System.err.println("missing key " + key);
			return '[' + key + ']';
		}
		return resourceString;
	}

	public String getResourceString(final String key, final String resource) {
		String value = getLanguageString(key);
		if (value != null) {
			return value;
		}
		value = getOriginalString(key);
		if (value != null) {
			return value + ResourceBundles.POSTFIX_TRANSLATE_ME;
		}
		return resource;
	}

	public String getOriginalString(final String key) {
		return defaultResources.get(key);
	}

	private String getLanguageString(final String key) {
		final String userString = userLanguageResources.getProperty(key);
		return userString != null ? userString : languageResources.get(key);
	}

	public String putResourceString(final String key, final String resource) {
		return languageResources.put(key, resource);
	}
	
	public void putUserResourceString(final String key, final String value) {
		if(value != null && ! value.isEmpty())
			addUserDefinedString(key, value);
		else {
			removeUserDefinedString(key);
		}
	}

	private void addUserDefinedString(final String key, final String value) {
		if(! value.equals(userLanguageResources.getProperty(key))) {
			userResourcesHaveChanged = true;
			userLanguageResources.setProperty(key, value);
		}
	}

	private void removeUserDefinedString(final String key) {
		if (userLanguageResources.contains(key)) {
			userLanguageResources.remove(key);
			userResourcesHaveChanged = true;
		}
	}
	
	@Override
	protected Object handleGetObject(final String key) {
		try {
			return getLanguageString(key);
		}
		catch (final Exception ex) {
			LogUtils.severe("Warning - resource string not found:" + key);
			return getOriginalString(key) + ResourceBundles.POSTFIX_TRANSLATE_ME;
		}
	}

	private void loadLocalLanguageResources(String newLanguage) throws IOException {
		loadInternalResources(newLanguage);
		loadUserResources();
	}

	private void loadUserResources() throws IOException {
		userLanguageResources.clear();
		final File userResourceFile = userResourceFile();
		if(userResourceFile.exists()) {
			try (final BufferedInputStream in = new BufferedInputStream(new FileInputStream(userResourceFile))) {
				userLanguageResources.load(in);
			}
		}
		userResourcesHaveChanged = false;
	}
	
	public void saveUserResources() throws IOException {
		final File userResourceFile = userResourceFile();
		if(userResourcesHaveChanged) {
			userLanguageResources.store(new BufferedOutputStream(new FileOutputStream(userResourceFile)), "");
			userResourcesHaveChanged = false;
		}
	}


	private File userResourceFile() {
		return new File(ResourceController.getResourceController().getFreeplaneUserDirectory(), "UserResources_" + lang + ".properties");
	}

	private void loadInternalResources(String newLanguage) throws IOException {
		lang = newLanguage;
		if (lang == null || lang.equals(LANGUAGE_AUTOMATIC)) {
			final String country = Locale.getDefault().getCountry();
			if(! country.equals("")){
				lang = Locale.getDefault().getLanguage() + "_" + country;
				languageResources = getLanguageResources(lang);
				if (languageResources != null) {
					LogUtils.info("language resources for " + lang + " found");
					return;
				}
			}
			lang = Locale.getDefault().getLanguage();
			languageResources = getLanguageResources(lang);
			if (languageResources != null) {
				LogUtils.info("language resources for " + lang + " found");
				return;
			}
			LogUtils.info("language resources for " + lang + " not found");
		}
		if ("no".equals(lang)) {
			lang = "nb";
		}
		languageResources = getLanguageResources(lang);
		if (languageResources != null) {
			return;
		}
		LogUtils.info("language resources for " + lang + " not found");
		lang = DEFAULT_LANGUAGE;
		languageResources = getLanguageResources(lang);
		if (languageResources != null) {
			return;
		}
		LogUtils.severe("language resources for " + lang + " not found, aborting");
		System.exit(1);
	}

	public void loadAnotherLanguage(String newLanguage) {
		try {
			if(! lang.equals(newLanguage))
				loadLocalLanguageResources(newLanguage);
		}
		catch (final IOException e) {
			LogUtils.severe(e);
		}
	}
}
