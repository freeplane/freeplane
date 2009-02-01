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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.util.MultipleValueMap;

class FreeplaneResourceBundle extends ResourceBundle {
	private static final String DEFAULT_LANGUAGE = "en";
	public static final String POSTFIX_TRANSLATE_ME = "[translate me]";
	/**
	 *
	 */
	final private ResourceController controller;
	private HashMap<String, String> defaultResources;
	private final MultipleValueMap<String, URL> externalResources;
	private String lang;
	private HashMap<String, String> languageResources;

	FreeplaneResourceBundle(final ResourceController controller) {
		this.controller = controller;
		externalResources = new MultipleValueMap<String, URL>();
		try {
			loadLocalLanguageResources();
			defaultResources = getLanguageResources(DEFAULT_LANGUAGE);
		}
		catch (final Exception ex) {
			org.freeplane.core.util.Tools.logException(ex);
			Logger.global.severe("Error loading Resources");
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
			e.printStackTrace();
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

	/**
	 * @throws IOException
	 */
	private HashMap<String, String> getLanguageResources(final String lang) throws IOException {
		final URL systemResource = Controller.getResourceController().getResource(
		    "/Resources" + "_" + lang + ".properties");
		if (systemResource == null) {
			return null;
		}
		final HashMap resources = getLanguageResources(systemResource);
		final Iterator<URL> iterator = externalResources.get(lang).iterator();
		while (iterator.hasNext()) {
			resources.putAll(getLanguageResources(iterator.next()));
		}
		return resources;
	}

	private HashMap<String, String> getLanguageResources(final URL systemResource) throws IOException {
		final InputStream in = systemResource.openStream();
		if (in == null) {
			return null;
		}
		final Properties bundle = new Properties();
		bundle.load(in);
		in.close();
		return new HashMap(bundle);
	}

	String getResourceString(final String key) {
		try {
			return getString(key);
		}
		catch (final Exception ex) {
			return key;
		}
	}

	String getResourceString(final String key, final String resource) {
		try {
			try {
				return languageResources.get(key);
			}
			catch (final Exception ex) {
				return defaultResources.get(key) + FreeplaneResourceBundle.POSTFIX_TRANSLATE_ME;
			}
		}
		catch (final Exception e) {
			return resource;
		}
	}

	@Override
	protected Object handleGetObject(final String key) {
		try {
			return languageResources.get(key);
		}
		catch (final Exception ex) {
			Logger.global.severe("Warning - resource string not found:" + key);
			return defaultResources.get(key) + FreeplaneResourceBundle.POSTFIX_TRANSLATE_ME;
		}
	}

	private void loadLocalLanguageResources() throws IOException {
		lang = controller.getProperty(ResourceController.RESOURCE_LANGUAGE);
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
			e.printStackTrace();
		}
	}
}
