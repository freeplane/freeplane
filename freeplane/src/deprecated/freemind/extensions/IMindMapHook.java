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

import java.util.Properties;

import org.freeplane.modes.ModeController;

/**
 * @author foltin
 */
public interface IMindMapHook {
	/**
	 * This indirection is necessary, as all stored PermanentNodeHooks are
	 * created during the map's creation time but the registrations are
	 * underdone on ModeController's startup method later.
	 */
	public interface IPluginBaseClassSearcher {
		Object getPluginBaseObject();
	}

	/**
	 * @return The name of the hook. In the xml description, this is the unique
	 *         label.
	 */
	String getName();

	/**
	 * If a base class is specified in the plugin declaration via a
	 * plugin_registration entry with isPluginBase==true, this object is
	 * returned here. You can use it to realize something like the state of a
	 * plugin (eg. is the plugin switched on or off?).<br>
	 * An example is the menu status of the encrypted nodes. If the node is not
	 * encrypted, the encryption state cannot be toggled (see EncryptNode.java).<br>
	 * Another example arises from the collaboration mode. The state (connected,
	 * wait for second party, map sharing etc.) can be stored in the plugin
	 * base.<br>
	 * Remember, that it is most likely that you havn't specified the base class
	 * and that you get NULL here.
	 *
	 * @return The object returned is of HookRegistration type but has to be
	 *         casted anyway.
	 */
	Object getPluginBaseClass();

	/**
	 * looks for a property in the plugin properties file.
	 */
	String getResourceString(String property);

	void setController(ModeController controller);

	void setName(String name);

	void setPluginBaseClass(IPluginBaseClassSearcher baseClass);

	/**
	 * @param properties
	 *            the properties of the property file belonging to the hook are
	 *            passed.
	 */
	void setProperties(Properties properties);

	/* Hooks */
	/**
	 * This method is also called, if the node, this hook belongs to, is removed
	 * from the map.
	 */
	void shutdown();

	/**
	 * This method is also called, if the hook is created in the map.
	 */
	void startup();
}
