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

/**
 * @author foltin
 */
public abstract class HookFactoryAdapter implements IHookFactory {
	/** Contains PluginType -> Object (baseClass) relations. */
	protected HashMap allRegistrationInstances;

	/**
	 *
	 */
	protected HookFactoryAdapter() {
		super();
	}

	public void deregisterAllRegistrationContainer() {
		allRegistrationInstances.clear();
	}

	/**
	 * See getRegistrations. The registration makes sense for the factory, as
	 * the factory observes every object creation. <br>
	 * Moreover, the factory can tell other hooks it creates, who is its base
	 * plugin.
	 */
	public void registerRegistrationContainer(
	                                          final IHookFactory.RegistrationContainer container,
	                                          final IHookRegistration instanciatedRegistrationObject) {
		if (container.isPluginBase) {
			allRegistrationInstances.put(container.correspondingPlugin
			    .getLabel(), instanciatedRegistrationObject);
		}
	}
}
