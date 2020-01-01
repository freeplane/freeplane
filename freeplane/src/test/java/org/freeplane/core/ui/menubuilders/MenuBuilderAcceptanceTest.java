package org.freeplane.core.ui.menubuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import javax.swing.JPopupMenu;
import javax.swing.JToolBar;

import org.freeplane.core.ui.IUserInputListenerFactory;
import org.freeplane.core.ui.menubuilders.action.EntriesForAction;
import org.freeplane.core.ui.menubuilders.action.IAcceleratorMap;
import org.freeplane.core.ui.menubuilders.generic.BuildPhaseListener;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.PhaseProcessor;
import org.freeplane.core.ui.menubuilders.menu.MenuBuildProcessFactory;
import org.freeplane.core.ui.menubuilders.menu.TestMenuBarFactory;
import org.freeplane.features.mode.Controller;
import org.junit.BeforeClass;
import org.junit.Test;

public class MenuBuilderAcceptanceTest {
	private static Entry menuStructure;

	static{
		new HeadlessFreeplaneRunner();
	}
	
	@BeforeClass
	static public void setup() {
		final IUserInputListenerFactory userInputListenerFactory = mock(IUserInputListenerFactory.class);
		when(userInputListenerFactory.getToolBar("/main_toolbar")).thenReturn(new JToolBar());
		when(userInputListenerFactory.getNodePopupMenu()).thenReturn(new JPopupMenu());
		when(userInputListenerFactory.getMapPopup()).thenReturn(new JPopupMenu());
		when(userInputListenerFactory.getMenuBar()).thenReturn(TestMenuBarFactory.createFreeplaneMenuBar());
		final PhaseProcessor buildProcessor = new MenuBuildProcessFactory(
		    userInputListenerFactory, Controller.getCurrentModeController(), new FreeplaneResourceAccessor(),
		    mock(IAcceleratorMap.class), 
		    new EntriesForAction(), Collections.<BuildPhaseListener>emptyList()).getBuildProcessor();
		final String menuResource = "/xml/mindmapmodemenu.xml";
		final InputStream resource = MenuBuilderAcceptanceTest.class.getResourceAsStream(menuResource);
		final BufferedReader reader = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8));
		menuStructure = XmlEntryStructureBuilder.buildMenuStructure(reader);
		buildProcessor.build(menuStructure);
	}


	@Test
	public void test() {
	}

}
