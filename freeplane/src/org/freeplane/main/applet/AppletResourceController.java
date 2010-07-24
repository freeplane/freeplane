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
package org.freeplane.main.applet;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import javax.swing.JApplet;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.resources.ResourceController;

/**
 * @author Dimitry Polivaev
 */
class AppletResourceController extends ResourceController {
	private Properties userProps;

	public AppletResourceController(final FreeplaneApplet freeplaneApplet) {
		super();
		final URL defaultPropsURL = getResource(ResourceController.FREEPLANE_PROPERTIES);
		try {
			userProps = new Properties();
			final InputStream in = new BufferedInputStream(defaultPropsURL.openStream());
			userProps.load(in);
			in.close();
		}
		catch (final Exception ex) {
			System.err.println("Could not load properties.");
		}
		final Enumeration allKeys = userProps.propertyNames();
		while (allKeys.hasMoreElements()) {
			final String key = (String) allKeys.nextElement();
			setPropertyByParameter(freeplaneApplet, key);
		}
		ResourceController.setResourceController(this);
	}

	@Override
	public String getFreeplaneUserDirectory() {
		return null;
	}

	@Override
	public int getIntProperty(final String key, final int defaultValue) {
		try {
			return Integer.parseInt(getProperty(key));
		}
		catch (final NumberFormatException nfe) {
			return defaultValue;
		}
	}

	@Override
	public Properties getProperties() {
		return userProps;
	}

	@Override
	public String getProperty(final String key) {
		return userProps.getProperty(key);
	};

	@Override
	public URL getResource(final String name) {
		final URL resourceURL = super.getResource(name);
		if (resourceURL == null || !resourceURL.getProtocol().equals("jar")
		        && System.getProperty("freeplane.debug", null) == null) {
			return null;
		}
		return resourceURL;
	}

	@Override
	public void init(final Controller controller) {
		super.init(controller);
	}

	@Override
	public void loadProperties(final InputStream inStream) throws IOException {
		userProps.load(inStream);
	}

	@Override
	public void loadPropertiesFromXML(final InputStream in) throws IOException, InvalidPropertiesFormatException {
		userProps.loadFromXML(in);
	}

	@Override
	public void saveProperties(final Controller controller) {
	}

	@Override
	public void setDefaultProperty(final String key, final String value) {
		userProps.setProperty(key, value);
	}

	@Override
	public void setProperty(final String key, final String value) {
		userProps.setProperty(key, value);
	}

	void setPropertyByParameter(JApplet applet, final String key) {
		final String val = applet.getParameter(key);
		if (val != null && val != "") {
			userProps.setProperty(key, val);
		}
	}

	@Override
	public boolean isApplet() {
		return true;
	}
}
