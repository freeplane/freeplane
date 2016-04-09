/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2016 dimitry
 *
 *  This file author is dimitry
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
package org.freeplane.plugin.script;

import java.net.URL;
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;

class RestrictingClassLoader extends ClassLoader {
	private ScriptingSecurityManager securityManager;

	public void setSecurityManager(ScriptingSecurityManager securityManager) {
		this.securityManager = securityManager;
	}

	public RestrictingClassLoader(ClassLoader parent) {
		super(parent);
	}

	public boolean implies(Permission permission) {
		return securityManager != null && securityManager.implies(permission);
	}

	static ClassLoader createClassLoader() {
	        final List<URL> urls = new ArrayList<URL>();
	        for (String path : ScriptResources.getClasspath()) {
	            urls.add(GenericScript.pathToUrl(path));
	        }
	        urls.addAll(GenericScript.jarsInExtDir());
	        
		ClassLoader classLoader = new PrivilegedURLClassLoader(urls.toArray(new URL[urls.size()]),
		    GenericScript.class.getClassLoader());
		return new RestrictingClassLoader(classLoader);
	}
}