package org.freeplane.uispec4j.framework;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import org.freeplane.core.util.TextUtils;
import org.uispec4j.MenuItem;
import org.uispec4j.UISpec4J;
import org.uispec4j.UISpecTestCase;
import org.uispec4j.utils.ExceptionContainer;

public abstract class FreeplaneTestCase extends UISpecTestCase {
	 static private FreeplaneAdapter freeplaneFrameAdapter;
	 static protected String openMap;

	  /**
	   * Returns the Window created by the adapter.
	   *
	   * @throws AdapterNotFoundException if the <code>uispec4j.adapter</code> property does not refer
	   *                                  to a valid adapter
	   */
	  public FreeplaneWindow getFreeMindWindow(){
	    return (FreeplaneWindow) getMainWindow();
	  }
	  
	private void initializeMainWindow() {
		if(freeplaneFrameAdapter == null){
			openMap = System.getProperty("openMap");
			final String[] args;
			if(openMap != null){
				args = new String[1];
				args[0] = openMap;
			}
			else{
				args = new String[0];
			}
			freeplaneFrameAdapter = new FreeplaneAdapter();
			freeplaneFrameAdapter.getMainWindow();
		}
	}

	protected void tearDown() throws Exception {
		assertNoErrorsLogged();
		super.tearDown();
	}

	protected void assertNoErrorsLogged() {
		assertFalse(freeplaneFrameAdapter.checkLogErrors());
	}
	
	protected MenuItem getMenu(String key){
		return getMainWindow().getMenuBar().getMenu(getMenuItemName(key));
	}

	protected String getMenuItemName(String key) {
		return freeplaneFrameAdapter.getMenuItemName(key);
	}
	
	protected String getResourceString(String item) {
		return TextUtils.getText(item);
	}

	protected void superSetUp() throws Exception {
		super.setUp();
		UISpec4J.setWindowInterceptionTimeLimit(100000);
		initializeMainWindow();
		setAdapter(freeplaneFrameAdapter);
		freeplaneFrameAdapter.checkLogErrors();
	}

	protected void setUp() throws Exception {
	}
	
	// initialize main frame in the main thread, 
	// but run the tests in the swing thread.
	public void runBare() throws Throwable {
		superSetUp();
		runBareInSwingThread();		
	}

	private void runBareInSwingThread() throws Throwable {
		if (SwingUtilities.isEventDispatchThread()) {
			super.runBare();
			return;
		}
		final ExceptionContainer container = new ExceptionContainer();
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					try {
						FreeplaneTestCase.super.runBare();
					}
					catch (Throwable e) {
						container.set(e);
					}
				}
			});
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e.getCause());
		}
		catch (InvocationTargetException e) {
			throw new RuntimeException(e.getCause());
		}
		container.rethrowIfNeeded();
	}
}
