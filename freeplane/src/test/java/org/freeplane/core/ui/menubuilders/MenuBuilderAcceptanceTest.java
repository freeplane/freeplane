package org.freeplane.core.ui.menubuilders;

import static org.mockito.Mockito.mock;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.freeplane.core.ui.menubuilders.action.IDefaultAcceleratorMap;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.PhaseProcessor;
import org.freeplane.core.ui.menubuilders.menu.MenuBuildProcessFactory;
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
		final PhaseProcessor buildProcessor = new MenuBuildProcessFactory().createBuildProcessor(
		    Controller.getCurrentModeController(), new FreeplaneResourceAccessor(), mock(IDefaultAcceleratorMap.class));
		final String menuResource = "/xml/mindmapmode.generic.xml";
		final InputStream resource = MenuBuilderAcceptanceTest.class.getResourceAsStream(menuResource);
		final BufferedReader reader = new BufferedReader(new InputStreamReader(resource));
		menuStructure = XmlEntryStructureBuilder.buildMenuStructure(reader);
		buildProcessor.build(menuStructure);
	}


	@Test
	public void test() {
	}

}
