package org.freeplane.plugin.bugreport;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogTool;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private XmlRpcHandler handler;
	private Logger parentLogger;

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		parentLogger = Logger.getAnonymousLogger().getParent();
		handler = new XmlRpcHandler();
		parentLogger.addHandler(handler);
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher(){

			public boolean dispatchKeyEvent(KeyEvent e) {
				if(e.getID() == KeyEvent.KEY_PRESSED && e.isAltDown() && e.isAltDown() && e.isShiftDown() && e.getKeyCode() == KeyEvent.VK_B){
					final RuntimeException runtimeException = new RuntimeException(ResourceBundles.getText("user_initiated_bug_report"));
					LogTool.severe(runtimeException);
				}
				return false;
            }
			
		});

	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		parentLogger.removeHandler(handler);
	}

}
