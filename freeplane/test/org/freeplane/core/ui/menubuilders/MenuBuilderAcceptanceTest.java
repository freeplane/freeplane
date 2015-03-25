package org.freeplane.core.ui.menubuilders;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

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
		final PhaseProcessor buildProcessor = new BuildProcessFactory().createBuildProcessor(Controller.getCurrentModeController(), new ResourceDependentMenuEntryBuilder());
		final String menuResource = "/xml/mindmapmoderibbon.out.xml";
		final InputStream resource = MenuBuilderAcceptanceTest.class.getResourceAsStream(menuResource);
		final BufferedReader reader = new BufferedReader(new InputStreamReader(resource));
		menuStructure = XmlEntryStructureBuilder.buildMenuStructure(reader);
		buildProcessor.build(menuStructure);
	}


	@Test
	public void test() {
	}

}
