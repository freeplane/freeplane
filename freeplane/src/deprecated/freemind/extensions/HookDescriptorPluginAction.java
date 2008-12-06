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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import freemind.controller.actions.generated.instance.Plugin;
import freemind.controller.actions.generated.instance.PluginAction;
import freemind.controller.actions.generated.instance.PluginMenu;
import freemind.controller.actions.generated.instance.PluginMode;
import freemind.controller.actions.generated.instance.PluginProperty;

/**
 * This is an information class that holds all outer properties of a hook, i.e.
 * all contents of the XML description file. Don't use this class for anything
 * except for the implementation of a HookFactory.
 *
 * @author foltin
 */
public class HookDescriptorPluginAction extends HookDescriptorBase {
	public Vector menuPositions;
	final private Vector modes;
	final private PluginAction pluginAction;
	final private Properties properties;

	public HookDescriptorPluginAction(final String xmlPluginFile,
	                                  final Plugin pluginBase,
	                                  final PluginAction pluginAction) {
		super(pluginBase, xmlPluginFile);
		this.pluginAction = pluginAction;
		if (pluginAction.getName() == null) {
			pluginAction.setName(pluginAction.getLabel());
		}
		menuPositions = new Vector();
		properties = new Properties();
		modes = new Vector();
		for (final Iterator i = pluginAction.getListChoiceList().iterator(); i
		    .hasNext();) {
			final Object obj = i.next();
			if (obj instanceof PluginMenu) {
				final PluginMenu menu = (PluginMenu) obj;
				menuPositions.add(menu.getLocation());
			}
			if (obj instanceof PluginProperty) {
				final PluginProperty property = (PluginProperty) obj;
				properties.put(property.getName(), property.getValue());
			}
			if (obj instanceof PluginMode) {
				final PluginMode mode = (PluginMode) obj;
				modes.add(mode.getClassName());
			}
		}
	}

	public String getBaseClass() {
		return pluginAction.getBase();
	}

	public String getClassName() {
		return pluginAction.getClassName();
	}

	public String getDocumentation() {
		return getFromResourceIfNecessary(pluginAction.getDocumentation());
	}

	public String getIconPath() {
		return pluginAction.getIconPath();
	}

	public HookInstanciationMethod getInstanciationMethod() {
		if (pluginAction.getInstanciation() != null) {
			final HashMap allInstMethods = HookInstanciationMethod
			    .getAllInstanciationMethods();
			for (final Iterator i = allInstMethods.keySet().iterator(); i
			    .hasNext();) {
				final String name = (String) i.next();
				if (pluginAction.getInstanciation().equalsIgnoreCase(name)) {
					return (HookInstanciationMethod) allInstMethods.get(name);
				}
			}
		}
		return HookInstanciationMethod.Other;
	}

	public String getKeyStroke() {
		return getFromPropertiesIfNecessary(pluginAction.getKeyStroke());
	}

	public Vector getModes() {
		return modes;
	}

	public String getName() {
		return getFromResourceIfNecessary(pluginAction.getName());
	}

	/**
	 */
	public Properties getProperties() {
		return properties;
	}

	/**
	 * @return whether or not the plugin can be on/off and this should be
	 *         displayed in the menus.
	 */
	public boolean isSelectable() {
		return pluginAction.getIsSelectable();
	}

	@Override
	public String toString() {
		return "[HookDescriptor props=" + properties + ", menu positions="
		        + menuPositions + "]";
	}
}
