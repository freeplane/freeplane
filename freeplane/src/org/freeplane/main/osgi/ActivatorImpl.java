/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
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
package org.freeplane.main.osgi;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.jar.Manifest;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.FileUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.filter.FilterController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.main.application.FreeplaneGUIStarter;
import org.freeplane.main.application.FreeplaneStarter;
import org.freeplane.main.application.SingleInstanceManager;
import org.freeplane.main.headlessmode.FreeplaneHeadlessStarter;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;

/**
 * @author Dimitry Polivaev
 * 05.01.2009
 */
class ActivatorImpl implements BundleActivator {
	private static final String HEADLESS_RUN_PROPERTY_NAME = FreeplaneStarter.class.getName() + ".headless";
	private FreeplaneStarter starter;

	private String[] getCallParameters() {
		String param;
		final LinkedList<String> parameters = new LinkedList<String>();
		for (int i = 1;; i++) {
			param = System.getProperty("org.freeplane.param" + i, null);
			if (param == null) {
				break;
			}
			if (param.equals("")) {
				continue;
			}
			parameters.add(param);
		}
		final String[] array = parameters.toArray(new String[parameters.size()]);
		return array;
	}

	public void start(final BundleContext context) throws Exception {
		try {
			startFramework(context);
		}
		catch (final Exception e) {
			e.printStackTrace();
			throw e;
		}
		catch (final Error e) {
			e.printStackTrace();
			throw e;
		}
	}

	private void loadPlugins(final BundleContext context) {
		final String resourceBaseDir = FreeplaneGUIStarter.getResourceBaseDir();
		final File baseDir = new File(resourceBaseDir).getAbsoluteFile().getParentFile();
		List<Bundle> loadedPlugins = new LinkedList<Bundle>();
		loadPlugins(context, new File(baseDir, "plugins"), loadedPlugins);
		final String freeplaneUserDirectory = Compat.getFreeplaneUserDirectory();
		loadPlugins(context, new File(freeplaneUserDirectory), loadedPlugins);
		for(Bundle plugin:loadedPlugins){
			try{
				plugin.start();
				System.out.println("Started: " + plugin.getLocation() + " (id#" + plugin.getBundleId() + ")");
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	private void loadPlugins(final BundleContext context, final File file, List<Bundle> loadedPlugins) {
		if (!file.exists() || !file.isDirectory()) {
			return;
		}
		final File manifest = new File(file, "META-INF/MANIFEST.MF");
		if (manifest.exists()) {
			InputStream manifestContent = null;
			try {
				manifestContent = new FileInputStream(manifest);
				final Manifest bundleManifest = new Manifest(manifestContent);
				final String name = bundleManifest.getMainAttributes().getValue("Bundle-SymbolicName");
				if (name == null) {
					return;
				}
				final Bundle[] bundles = context.getBundles();
				for (int i = 0; i < bundles.length; i++) {
					final Bundle installedBundle = bundles[i];
					if (installedBundle.getSymbolicName().equals(name)) {
						System.out.println("Bundle " + name + " already installed");
						return;
					}
				}
				final String location = "reference:file:" + file.getAbsolutePath();
				final Bundle bundle = context.installBundle(location);
				System.out.println("Installed: " + location + " (id#" + bundle.getBundleId() + ")");
				loadedPlugins.add(bundle);
			}
			catch (final Exception e) {
				e.printStackTrace();
			}
			finally {
				FileUtils.silentlyClose(manifestContent);
			}
			return;
		}
		final File[] childFiles = file.listFiles();
		for (int i = 0; i < childFiles.length; i++) {
			final File child = childFiles[i];
			loadPlugins(context, child, loadedPlugins);
		}
	}

	private void startFramework(final BundleContext context) {
        registerClasspathUrlHandler(context);
		if (null == System.getProperty("org.freeplane.core.dir.lib", null)) {
			final File root = new File(FreeplaneGUIStarter.getResourceBaseDir()).getAbsoluteFile().getParentFile();
			try {
				String rootUrl = root.toURI().toURL().toString();
				if (!rootUrl.endsWith("/")) {
					rootUrl = rootUrl + "/";
				}
				final String libUrl = rootUrl + "core/org.freeplane.core/lib/";
				System.setProperty("org.freeplane.core.dir.lib", libUrl);
			}
			catch (final MalformedURLException e) {
			}
		}
		// initialize ApplicationController - SingleInstanceManager needs the configuration
		starter =  createStarter();
		final SingleInstanceManager singleInstanceManager = new SingleInstanceManager(starter);
		singleInstanceManager.start(getCallParameters());
		if (singleInstanceManager.isSlave()) {
			LogUtils.info("opened files in master - exiting now");
			System.exit(0);
		}
		else if (singleInstanceManager.isMasterPresent()) {
			starter.setDontLoadLastMaps();
		}
		loadPlugins(context);
		final Controller controller = starter.createController();
		starter.createModeControllers(controller);
		try {
			final ServiceReference[] controllerProviders = context.getServiceReferences(
			    IControllerExtensionProvider.class.getName(), null);
			if (controllerProviders != null) {
				for (int i = 0; i < controllerProviders.length; i++) {
					final ServiceReference controllerProvider = controllerProviders[i];
					final IControllerExtensionProvider service = (IControllerExtensionProvider) context
					    .getService(controllerProvider);
					service.installExtension(controller);
					context.ungetService(controllerProvider);
				}
			}
		}
		catch (final InvalidSyntaxException e) {
			e.printStackTrace();
		}
		try {
			final Set<String> modes = controller.getModes();
			for (final String modeName : modes) {
				final ServiceReference[] modeControllerProviders = context.getServiceReferences(
				    IModeControllerExtensionProvider.class.getName(), "(mode=" + modeName + ")");
				if (modeControllerProviders != null) {
					final ModeController modeController = controller.getModeController(modeName);
					Controller.getCurrentController().selectModeForBuild(modeController);
					for (int i = 0; i < modeControllerProviders.length; i++) {
						final ServiceReference modeControllerProvider = modeControllerProviders[i];
						final IModeControllerExtensionProvider service = (IModeControllerExtensionProvider) context
						    .getService(modeControllerProvider);
						service.installExtension(modeController);
						context.ungetService(modeControllerProvider);
					}
				}
			}
		}
		catch (final InvalidSyntaxException e) {
			e.printStackTrace();
		}
		if ("true".equals(System.getProperty("org.freeplane.exit_on_start", null))) {
			controller.getViewController().invokeLater(new Runnable() {
				public void run() {
					try {
						Thread.sleep(1000);
					}
					catch (final InterruptedException e) {
					}
					System.exit(0);
				}
			});
			return;
		}
		controller.getViewController().invokeLater(new Runnable() {
			public void run() {
				final Bundle[] bundles = context.getBundles();
				final HashSet<String> plugins = new HashSet<String>();
				for(Bundle bundle:bundles){
					plugins.add(bundle.getSymbolicName());
				}
				FilterController.getController(controller).loadDefaultConditions();
				starter.buildMenus(controller, plugins);
				starter.createFrame(getCallParameters());
			}
		});
	}

	public FreeplaneStarter createStarter() {
		if(Boolean.getBoolean(HEADLESS_RUN_PROPERTY_NAME))
			return new FreeplaneHeadlessStarter();
		else
			return new FreeplaneGUIStarter();
    }

    private void registerClasspathUrlHandler(final BundleContext context) {
        Hashtable<String, String[]> properties = new Hashtable<String, String[]>();
        properties.put(URLConstants.URL_HANDLER_PROTOCOL, new String[] { ResourceController.FREEPLANE_RESOURCE_URL_PROTOCOL });
        context.registerService(URLStreamHandlerService.class.getName(), new ResourcesUrlHandler(), properties);
    }

	public void stop(final BundleContext context) throws Exception {
		starter.stop();
		final Bundle[] bundles = context.getBundles();
		for (int i = 0; i < bundles.length; i++) {
			final Bundle bundle = bundles[i];
			if (bundle.getState() >= Bundle.ACTIVE && bundle.getSymbolicName().startsWith("org.freeplane.plugin.")) {
				try {
					bundle.stop();
				}
				catch (final Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
