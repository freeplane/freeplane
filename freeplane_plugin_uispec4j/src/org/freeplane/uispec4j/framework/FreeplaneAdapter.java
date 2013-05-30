package org.freeplane.uispec4j.framework;

import java.awt.Component;
import java.util.logging.Logger;

import javax.swing.JFrame;

import org.freeplane.core.util.TextUtils;
import org.freeplane.uispec4j.osgi.Activator;
import org.osgi.framework.Bundle;
import org.uispec4j.Trigger;
import org.uispec4j.UISpecAdapter;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

/**
 * Adapter that intercepts the window displayed by the main() of a given class.<p/>
 * This adapter keeps the reference of the intercepted window, so that main() is not called on
 * subsequent calls. If you need to run main() again, you can either call {@link #reset()} or create a new
 * adapter.
 */
public class FreeplaneAdapter implements UISpecAdapter {
	static private FreeplaneWindow window;
	static private ErrorCheckHandler errorCheck = new ErrorCheckHandler();

	public FreeplaneAdapter() {
	}

	synchronized public Window getMainWindow() {
		if (window == null) {
			intercept();
			Logger parentLogger = Logger.getAnonymousLogger().getParent();
			parentLogger.addHandler(errorCheck);
		}
		return window;
	}

	/**
	 * Remove mnemonics
	 * @param string
	 * @return
	 */
	String getMenuItemName(String string) {
		final String resourceString = TextUtils.getText(string);
		return resourceString.replaceFirst("&", "");
	}

	private Window intercept() {
		final WindowInterceptor interceptor = WindowInterceptor
				.init(new Trigger() {
					public void run() throws Exception {
						Bundle[] bundles = Activator.getBundleContext().getBundles();
						for(Bundle b:bundles){
							if(b.getSymbolicName().equals("org.freeplane.core")){
								b.start();
								return;
							}
						}
					}
				});
		interceptor.process(new WindowHandler() {
			public Trigger process(Window window) throws Exception {
				setWindow(window);
				return Trigger.DO_NOTHING;
			}
		}).process(new WindowHandler() {
			public Trigger process(Window window) throws Exception {
				setWindow(window);
				return Trigger.DO_NOTHING;
			}
		}).run();
		return window;
	}

	public void reset() {
		window = null;
	}

	static private void setWindow(Window window) {
		final Component f = window.getAwtComponent();
		if (f instanceof JFrame)
			FreeplaneAdapter.window = new FreeplaneWindow((JFrame)f);
	}

	public boolean checkLogErrors() {
		return errorCheck.checkErrors();
	}

}
