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
package org.freeplane.modes.browsemode;

import java.util.List;
import java.util.Vector;

import deprecated.freemind.extensions.HookFactoryAdapter;
import deprecated.freemind.extensions.HookInstanciationMethod;
import deprecated.freemind.extensions.IModeControllerHook;

/**
 * @author foltin
 */
public class BHookFactory extends HookFactoryAdapter {
	/**
	 *
	 */
	public BHookFactory() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freemind.extensions.HookFactory#createIModeControllerHook(java.lang.String
	 * )
	 */
	public IModeControllerHook createIModeControllerHook(final String hookName) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freemind.extensions.HookFactory#getHookMenuPositions(java.lang.String)
	 */
	public List getHookMenuPositions(final String hookName) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freemind.extensions.HookFactory#getInstanciationMethod(java.lang.String)
	 */
	public HookInstanciationMethod getInstanciationMethod(final String hookName) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see freemind.extensions.HookFactory#getPluginBaseClass(java.lang.String)
	 */
	public Object getPluginBaseClass(final String hookName) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see freemind.extensions.HookFactory#getPossibleIModeControllerHooks()
	 */
	public Vector getPossibleIModeControllerHooks() {
		return new Vector();
	}

	/*
	 * (non-Javadoc)
	 * @see freemind.extensions.HookFactory#getPossibleNodeHooks()
	 */
	public Vector getPossibleNodeHooks() {
		return new Vector();
	}

	/*
	 * (non-Javadoc)
	 * @see freemind.extensions.HookFactory#getRegistrations()
	 */
	public List getRegistrations() {
		return null;
	}
}
