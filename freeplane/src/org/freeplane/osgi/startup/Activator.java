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
package org.freeplane.osgi.startup;

import org.freeplane.startup.FreeplaneStarter;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * @author Dimitry Polivaev
 * 05.01.2009
 */
public class Activator implements BundleActivator {

	public void start(BundleContext context) throws Exception {
		FreeplaneStarter starter = new FreeplaneStarter();
		starter.createController();
		final Bundle[] bundles = context.getBundles();
		for(int i = 0; i < bundles.length; i++){
			Bundle bundle = bundles[i];
			if(bundle.getState() < Bundle.STARTING && bundle.getSymbolicName().startsWith("org.freeplane.plugin.")){
				try {
	                bundle.start();
                }
                catch (Exception e) {
	                e.printStackTrace();
                }
			}
		}
		starter.createFrame(new String[] {});
    }

	public void stop(BundleContext context) throws Exception {
	    // TODO Auto-generated method stub
	    
    }
}
