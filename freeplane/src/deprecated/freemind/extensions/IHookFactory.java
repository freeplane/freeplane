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

import java.util.List;
import java.util.Vector;

import freemind.controller.actions.generated.instance.Plugin;

public interface IHookFactory {
	public static class RegistrationContainer {
		public Plugin correspondingPlugin;
		public Class hookRegistrationClass;
		public boolean isPluginBase;

		public RegistrationContainer() {
		}
	}

	public abstract IModeControllerHook createIModeControllerHook(
	                                                              String hookName);

	public abstract void deregisterAllRegistrationContainer();

	/**
	 * @return returns a list of menu position strings for the
	 *         StructuredMenuHolder.
	 */
	public abstract List getHookMenuPositions(String hookName);

	/**
	 */
	public abstract HookInstanciationMethod getInstanciationMethod(
	                                                               String hookName);

	/**
	 * A plugin base class is a common registration class of multiple plugins.
	 * It is useful to embrace several related plugins (example: EncryptedNote
	 * -> Registration).
	 *
	 * @return the base class if declared and successfully instanciated or NULL.
	 */
	public abstract Object getPluginBaseClass(String hookName);

	/**
	 * @return a string vector with representatives for plugins.
	 */
	public abstract Vector getPossibleIModeControllerHooks();

	/**
	 * @return a string vector with representatives for plugins.
	 */
	public abstract Vector getPossibleNodeHooks();

	/**
	 * Each Plugin can have a list of HookRegistrations that are called after
	 * the corresponding mode is enabled. (Like singletons.) One of these can
	 * operate as the pluginBase that is accessible to every normal
	 * plugin_action via the getPluginBaseClass method.
	 *
	 * @return A list of RegistrationContainer elements. The field
	 *         hookRegistrationClass of RegistrationContainer is a class that is
	 *         (probably) of HookRegistration type. You have to register every
	 *         registration via the registerRegistrationContainer method when
	 *         instanciated (this is typically done in the ModeController).
	 */
	public abstract List getRegistrations();

	/**
	 * See getRegistrations. The registration makes sense for the factory, as
	 * the factory observes every object creation. <br>
	 * Moreover, the factory can tell other hooks it creates, who is its base
	 * plugin.
	 */
	public abstract void registerRegistrationContainer(
	                                                   IHookFactory.RegistrationContainer container,
	                                                   IHookRegistration instanciatedRegistrationObject);
}
