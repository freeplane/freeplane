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
package org.freeplane.controller.resources;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import org.freeplane.controller.Controller;

class FreemindResourceBundle extends ResourceBundle {
	private static final String DEFAULT_LANGUAGE = "en";
	public static final String POSTFIX_TRANSLATE_ME = "[translate me]";
	/**
	 *
	 */
	final private ResourceController controller;
	private PropertyResourceBundle defaultResources;
	private PropertyResourceBundle languageResources;

	FreemindResourceBundle(final ResourceController controller) {
		this.controller = controller;
		try {
			String lang = this.controller.getProperty(ResourceController.RESOURCE_LANGUAGE);
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
			/*
			 * fc, 26.4.2008. the following line is a bug, as the
			 * defaultResources are used, even, when a single string is missing
			 * inside a bundle and not only, when the complete bundle is
			 * missing.
			 */
			defaultResources = getLanguageResources(DEFAULT_LANGUAGE);
		}
		catch (final Exception ex) {
			org.freeplane.main.Tools.logException(ex);
			Logger.global.severe("Error loading Resources");
		}
	}

	@Override
	public Enumeration getKeys() {
		return defaultResources.getKeys();
	}

	/**
	 * @throws IOException
	 */
	private PropertyResourceBundle getLanguageResources(final String lang) throws IOException {
		final URL systemResource = Controller.getResourceController().getResource(
		    "Resources_" + lang + ".properties");
		if (systemResource == null) {
			return null;
		}
		final InputStream in = systemResource.openStream();
		if (in == null) {
			return null;
		}
		final PropertyResourceBundle bundle = new PropertyResourceBundle(in);
		in.close();
		return bundle;
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
				return languageResources.getString(key);
			}
			catch (final Exception ex) {
				return defaultResources.getString(key)
				        + FreemindResourceBundle.POSTFIX_TRANSLATE_ME;
			}
		}
		catch (final Exception e) {
			return resource;
		}
	}

	@Override
	protected Object handleGetObject(final String key) {
		try {
			return languageResources.getString(key);
		}
		catch (final Exception ex) {
			Logger.global.severe("Warning - resource string not found:" + key);
			return defaultResources.getString(key) + FreemindResourceBundle.POSTFIX_TRANSLATE_ME;
		}
	}
}
