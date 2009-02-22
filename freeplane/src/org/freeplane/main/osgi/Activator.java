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

import java.util.LinkedList;
import java.util.Set;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.main.application.FreeplaneStarter;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * @author Dimitry Polivaev
 * 05.01.2009
 */
public class Activator implements BundleActivator {
	private FreeplaneStarter starter;

	public void start(final BundleContext context) throws Exception {
		startFramework(context);
	}

	private void startFramework(final BundleContext context) {
	    starter = new FreeplaneStarter();
		Controller controller = starter.createController();
		try {
	        final ServiceReference[] controllerProviders = context.getServiceReferences(ControllerExtensionProvider.class.getName(), null);
	        if(controllerProviders != null){
	        	for(int i = 0; i < controllerProviders.length; i++){
	        		final ControllerExtensionProvider service = (ControllerExtensionProvider) context.getService(controllerProviders[i]);
	        		service.installExtension(controller);
	        	}
	        }
        }
        catch (InvalidSyntaxException e) {
	        e.printStackTrace();
        }
		try {
	        final Set<String> modes = controller.getModes();
	        for(String modeName : modes){
	        	final ServiceReference[] modeControllerProviders = context.getServiceReferences(ModeControllerExtensionProvider.class.getName(), "(mode="+modeName+")");
	        	if(modeControllerProviders != null){
	        		final ModeController modeController = controller.getModeController(modeName);
	        		for(int i = 0; i < modeControllerProviders.length; i++){
	        			final ModeControllerExtensionProvider service = (ModeControllerExtensionProvider) context.getService(modeControllerProviders[i]);
	        			service.installExtension(modeController);
	        		}
	        	}
	        }
        }
        catch (InvalidSyntaxException e) {
	        e.printStackTrace();
        }
		starter.createFrame(getCallParameters());
    }

	private String[] getCallParameters() {
		int i = 1;
		String param;
		LinkedList<String> parameters = new LinkedList<String>();
		for(;;) {
			param = System.getProperty("org.freeplane.param" + i++, null);
			if(param == null){
				break;
			}
			if(param.equals("")){
				continue;
			}
			parameters.add(param);
		}
		
	    final String[] array = parameters.toArray(new String[parameters.size()]);
		return array;
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
