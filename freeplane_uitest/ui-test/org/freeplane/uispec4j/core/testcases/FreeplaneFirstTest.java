package org.freeplane.uispec4j.core.testcases;

import org.freeplane.uispec4j.framework.FreeplaneTestCase;
import org.freeplane.uispec4j.framework.FreeplaneWindow;
import org.freeplane.uispec4j.framework.Node;
import org.junit.Test;
import org.uispec4j.Key;
import org.uispec4j.MenuItem;
import org.uispec4j.interception.FileChooserHandler;
import org.uispec4j.interception.WindowInterceptor;



public class FreeplaneFirstTest extends FreeplaneTestCase {
	@Test
	public void testFileOpen() throws Exception {
		final FreeplaneWindow mainWindow = getFreeMindWindow();
		MenuItem openMenu = mainWindow.getMenuBar().getMenu("File").getSubMenu("Open...");
		WindowInterceptor
		   .init(openMenu.triggerClick())
		   .process(FileChooserHandler.init()
		            .titleEquals(null)
		            .assertAcceptsFilesOnly()
		            .select(openMap))
		   .run();
		assertFalse(mainWindow.getToolbar("/main_toolbar").getButton("Undo").isEnabled());
	}
	@Test
	public void testUp() throws Exception {
		final FreeplaneWindow mainWindow = getFreeMindWindow();
		mainWindow.getAwtComponent();
		final Node node = mainWindow.getNode("22");
		node.selectAsTheOnlyOneSelected();
		node.pressKey(Key.UP);
		assertEquals("11", node.getSelected().getText());
	  }
	@Test
	public void testCtrlUp() throws Exception {
		final FreeplaneWindow mainWindow = getFreeMindWindow();
		mainWindow.getAwtComponent();
		final Node node = mainWindow.getNode("22");
		node.selectAsTheOnlyOneSelected();
		node.pressKey(Key.control(Key.UP));
		node.pressKey(Key.DOWN);
		assertEquals("11", node.getSelected().getText());
		mainWindow.getToolbar("/main_toolbar").getButton("Undo").click();
	  }
}
