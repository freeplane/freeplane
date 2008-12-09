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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.freeplane.controller.Controller;
import org.freeplane.main.Tools;

import freemind.controller.actions.generated.instance.Plugin;
import freemind.controller.actions.generated.instance.PluginClasspath;

/**
 * @author foltin
 */
public class HookDescriptorBase {
	private static HashMap classLoaderCache = new HashMap();
	public static final String FREEMIND_BASE_DIR_STRING = "${freemind.base.dir}";
	protected final String mXmlPluginFile;
	protected final Plugin pluginBase;

	/**
	 * @param pluginBase
	 * @param frame
	 * @param xmlPluginFile
	 */
	public HookDescriptorBase(final Plugin pluginBase,
	                          final String xmlPluginFile) {
		super();
		this.pluginBase = pluginBase;
		mXmlPluginFile = xmlPluginFile;
	}

	/**
	 * This string is used to identify known classloaders as they are cached.
	 */
	private String createPluginClasspathString(final List pluginClasspathList) {
		String result = "";
		for (final Iterator i = pluginClasspathList.iterator(); i.hasNext();) {
			final PluginClasspath type = (PluginClasspath) i.next();
			result += type.getJar() + ",";
		}
		return result;
	}

	/**
	 * @throws MalformedURLException
	 */
	private ClassLoader getClassLoader(final List pluginClasspathList) {
		final String key = createPluginClasspathString(pluginClasspathList);
		if (HookDescriptorBase.classLoaderCache.containsKey(key)) {
			return (ClassLoader) HookDescriptorBase.classLoaderCache.get(key);
		}
		try {
			final URL[] urls = new URL[pluginClasspathList.size()];
			int j = 0;
			for (final Iterator i = pluginClasspathList.iterator(); i.hasNext();) {
				final PluginClasspath classPath = (PluginClasspath) i.next();
				final String jarString = classPath.getJar();
				File file = new File(jarString);
				if (!file.isAbsolute()) {
					file = new File(getPluginDirectory(), jarString);
				}
				urls[j++] = Tools.fileToUrl(file);
			}
			final ClassLoader loader = new URLClassLoader(urls, Controller
			    .getResourceController().getFreeMindClassLoader());
			HookDescriptorBase.classLoaderCache.put(key, loader);
			return loader;
		}
		catch (final MalformedURLException e) {
			org.freeplane.main.Tools.logException(e);
			return this.getClass().getClassLoader();
		}
	}

	protected String getFromPropertiesIfNecessary(final String string) {
		if (string == null) {
			return string;
		}
		if (string.startsWith("%")) {
			return Controller.getResourceController().getProperty(
			    string.substring(1));
		}
		return string;
	}

	/**
	 */
	protected String getFromResourceIfNecessary(final String string) {
		if (string == null) {
			return string;
		}
		if (string.startsWith("%")) {
			return Controller.getText(string.substring(1));
		}
		return string;
	}

	public Plugin getPluginBase() {
		return pluginBase;
	}

	public ClassLoader getPluginClassLoader() {
		final List pluginClasspathList = getPluginClasspath();
		final ClassLoader loader = getClassLoader(pluginClasspathList);
		return loader;
	}

	public List getPluginClasspath() {
		final Vector returnValue = new Vector();
		for (final Iterator i = pluginBase.getListChoiceList().iterator(); i
		    .hasNext();) {
			final Object obj = i.next();
			if (obj instanceof PluginClasspath) {
				final PluginClasspath pluginClasspath = (PluginClasspath) obj;
				returnValue.add(pluginClasspath);
			}
		}
		return returnValue;
	}

	/**
	 * @return the relative/absolute(?) position of the plugin xml file.
	 */
	private String getPluginDirectory() {
		return Controller.getResourceController().getFreemindBaseDir() + "/"
		        + new File(mXmlPluginFile).getParent();
	}
}
