/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is to be deleted.
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
package deprecated.freemind.extensions;

import java.net.URL;
import java.util.Properties;
import java.util.logging.Logger;

import org.freeplane.modes.ModeController;

/**
 * Implments MindMapHook as an Adapter class. Implementation is straight
 * forward.
 *
 * @author foltin
 */
public class HookAdapter implements IMindMapHook {
	/**
	 * Stores the plugin base class as declared by the
	 * plugin_registration/isBaseClass attribute.
	 */
	private IPluginBaseClassSearcher baseClass;
	private ModeController controller;
	private String name;
	private Properties properties;

	/**
	 */
	public HookAdapter() {
		baseClass = null;
	}

	/**
	 */
	protected ModeController getController() {
		return controller;
	}

	/*
	 * (non-Javadoc)
	 * @see freemind.modes.NodeHook#getName()
	 */
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * @see freemind.extensions.MindMapHook#getPluginBaseClass()
	 */
	public Object getPluginBaseClass() {
		return baseClass.getPluginBaseObject();
	}

	/**
	 */
	protected Properties getProperties() {
		return properties;
	}

	public URL getResource(final String resourceName) {
		return this.getClass().getClassLoader().getResource(resourceName);
	}

	/*
	 * (non-Javadoc)
	 * @see freemind.extensions.MindMapHook#getResourceString(java.lang.String)
	 */
	public String getResourceString(final String property) {
		String result = properties.getProperty(property);
		if (result == null) {
			result = getController().getText(property);
		}
		if (result == null) {
			Logger.global.warning("The following property was not found:"
			        + property);
		}
		return result;
	}

	/**
	 */
	public void setController(final ModeController controller) {
		this.controller = controller;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setPluginBaseClass(final IPluginBaseClassSearcher baseClass) {
		this.baseClass = baseClass;
	}

	/**
	 */
	public void setProperties(final Properties properties) {
		this.properties = properties;
	}

	/*
	 * (non-Javadoc)
	 * @see freemind.modes.NodeHook#shutdownMapHook()
	 */
	public void shutdown() {
	}

	/*
	 * (non-Javadoc)
	 * @see freemind.modes.NodeHook#startupMapHook(java.lang.String)
	 */
	public void startup() {
	}
}
