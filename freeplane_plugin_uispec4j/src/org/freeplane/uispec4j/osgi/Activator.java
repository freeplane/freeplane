package org.freeplane.uispec4j.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.uispec4j.UISpec4J;

public class Activator implements BundleActivator {

	static private BundleContext bundleContext;

	public static BundleContext getBundleContext() {
    	return bundleContext;
    }

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
	      UISpec4J.init();
	      Activator.bundleContext = context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
	}

}
