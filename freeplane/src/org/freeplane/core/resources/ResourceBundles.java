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

import org.freeplane.core.util.LogTool;
import org.freeplane.core.util.MultipleValueMap;

/**
 * Class for managing localized resources. See translation property files.
 */
public class ResourceBundles extends ResourceBundle {
	private static final String DEFAULT_LANGUAGE = "en";
	public static final String POSTFIX_TRANSLATE_ME = "[translate me]";
	public static final String RESOURCE_LANGUAGE = "language";

	public static NamedObject createTranslatedString(final String key) {
		final String fs = ResourceBundles.getText(key);
		return new NamedObject(key, fs);
	}

	public static String getText(final String key) {
		if (key == null) {
			return null;
		}
		return ((ResourceBundles) ResourceController.getResourceController().getResources()).getResourceString(key);
	}

	public static String getText(final String key, final String defaultString) {
		if (key == null) {
			return defaultString;
		}
		return ((ResourceBundles) ResourceController.getResourceController().getResources()).getResourceString(key,
		    defaultString);
	}

	/**
	 *
	 */
	final private ResourceController controller;
	private Map<String, String> defaultResources;
	private final MultipleValueMap<String, URL> externalResources;
	private String lang;
	private Map<String, String> languageResources;

	ResourceBundles(final ResourceController controller) {
		this.controller = controller;
		externalResources = new MultipleValueMap<String, URL>();
		try {
			loadLocalLanguageResources();
			defaultResources = getLanguageResources(DEFAULT_LANGUAGE);
		}
		catch (final Exception ex) {
			LogTool.severe(ex);
			LogTool.severe("Error loading Resources");
		}
	}

	public void addResources(final String language, final URL url) {
		try {
			if (language.equalsIgnoreCase(DEFAULT_LANGUAGE)) {
				defaultResources.putAll(getLanguageResources(url));
			}
			else if (language.equalsIgnoreCase(lang)) {
				languageResources.putAll(getLanguageResources(url));
			}
			externalResources.put(language, url);
		}
		catch (final IOException e) {
			LogTool.severe(e);
		}
	}

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

	/**
	 * @throws IOException
	 */
	private Map<String, String> getLanguageResources(final String lang) throws IOException {
		final URL systemResource = ResourceController.getResourceController().getResource(
		    "/translations/Resources" + "_" + lang + ".properties");
		if (systemResource == null) {
			return null;
		}
		final Map<String, String> resources = getLanguageResources(systemResource);
		final Iterator<URL> iterator = externalResources.get(lang).iterator();
		while (iterator.hasNext()) {
			resources.putAll(getLanguageResources(iterator.next()));
		}
		return resources;
	}

	private Map<String, String> getLanguageResources(final URL systemResource) throws IOException {
		final InputStream in = new BufferedInputStream(systemResource.openStream());
		if (in == null) {
			return null;
		}
		final Properties bundle = new Properties();
		bundle.load(in);
		in.close();
		return new HashMap(bundle);
	}

	String getResourceString(final String key) {
		final String resourceString = getResourceString(key, key);
		if (resourceString == key) {
			System.err.println("missing key " + key);
			return '[' + key + ']';
		}
		return resourceString;
	}

	String getResourceString(final String key, final String resource) {
		String value = languageResources.get(key);
		if (value != null) {
			return value;
		}
		value = defaultResources.get(key);
		if (value != null) {
			return value + ResourceBundles.POSTFIX_TRANSLATE_ME;
		}
		return resource;
	}

	@Override
	protected Object handleGetObject(final String key) {
		try {
			return languageResources.get(key);
		}
		catch (final Exception ex) {
			LogTool.severe("Warning - resource string not found:" + key);
			return defaultResources.get(key) + ResourceBundles.POSTFIX_TRANSLATE_ME;
		}
	}

	private void loadLocalLanguageResources() throws IOException {
		lang = controller.getProperty(ResourceBundles.RESOURCE_LANGUAGE);
		if (lang == null || lang.equals("automatic")) {
			lang = Locale.getDefault().getLanguage() + "_" + Locale.getDefault().getCountry();
			if (getLanguageResources(lang) == null) {
				lang = Locale.getDefault().getLanguage();
				if (getLanguageResources(lang) == null) {
					lang = DEFAULT_LANGUAGE;
				}
			}
		}
		if ("no".equals(lang)) {
			lang = "nb";
		}
		languageResources = getLanguageResources(lang);
	}

	public void reloadLanguage() {
		try {
			loadLocalLanguageResources();
		}
		catch (final IOException e) {
			LogTool.severe(e);
		}
	}
}
