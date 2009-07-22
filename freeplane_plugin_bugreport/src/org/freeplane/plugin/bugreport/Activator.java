package org.freeplane.plugin.bugreport;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.freeplane.core.resources.ResourceBundles;
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
	public void start(final BundleContext context) throws Exception {
		parentLogger = Logger.getAnonymousLogger().getParent();
		handler = new XmlRpcHandler();
		parentLogger.addHandler(handler);
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
			public boolean dispatchKeyEvent(final KeyEvent e) {
				if (e.getID() == KeyEvent.KEY_PRESSED && e.isAltDown() && e.isAltDown() && e.isShiftDown()
				        && e.getKeyCode() == KeyEvent.VK_B) {
					final RuntimeException runtimeException = new RuntimeException(ResourceBundles
					    .getText("user_initiated_bug_report"));
					LogTool.severe(runtimeException);
				}
				return false;
			}
		});
		new Thread(new Runnable(){

			public void run() {
				final BufferedReader in
		          = new BufferedReader(new InputStreamReader(System.in));
				try {
	                for(String line = in.readLine();!line.equals("stop"); line = in.readLine()){
	                	System.out.println(line);
	                	if(line.equals("stack")){
	                		StringBuilder writer = new StringBuilder();
	                		final Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
	                		for(Entry<Thread, StackTraceElement[]> stackTraceEntry : allStackTraces.entrySet()){
	                			writer.append(stackTraceEntry.getKey().getName());
	                			writer.append('\n');
	                			final StackTraceElement[] stackTraceElements = stackTraceEntry.getValue();
	                			for(StackTraceElement stackTraceElement:stackTraceElements){
		                			writer.append("\tat ");
		                			writer.append(stackTraceElement.toString());
		                			writer.append('\n');
	                			}
	                		}
		                	System.out.println(writer.toString());
	                	}
	                }
	                System.exit(-1);                }
                catch (IOException e) {
                }
            }}, "ConsoleReader").start();
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(final BundleContext context) throws Exception {
		parentLogger.removeHandler(handler);
	}
}
