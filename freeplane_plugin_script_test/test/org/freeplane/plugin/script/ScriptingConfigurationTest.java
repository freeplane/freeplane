/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2010 Volker Boerchers
 *
 *  This file author is Volker Boerchers
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
 *  along with this program.  If not, see &lt;http://www.gnu.org/licenses/&gt;.
 */
package org.freeplane.plugin.script;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.freeplane.main.headlessmode.FreeplaneHeadlessStarter;
import org.freeplane.plugin.script.ExecuteScriptAction.ExecutionMode;
import org.freeplane.plugin.script.ScriptingConfiguration.ScriptMetaData;
import org.junit.BeforeClass;
import org.junit.Test;

public class ScriptingConfigurationTest {
	private String scriptName = "TestScript";

    @BeforeClass
    public static void initStatics() {
        new FreeplaneHeadlessStarter().createController();
    }

	@Test
	public void testAnalyseScriptContent1() {
		// it's case insensitive
		// it's tolerant on white space
		String content = "// some comment" //
		        + "//@ExecutionModes (\t{  ExecutionMode.ON_selECTED_NODE" //
		        + ", \tON_SelECTED_NODE_RECURSIVELY = \"/menu_bar/help\" } )" //
		        + "//  @CacheScriptContent ( true\t ) " //
		        + " def test() {}";
		ScriptMetaData metaData = new ScriptingConfiguration().analyseScriptContent(content, scriptName);
		assertEquals("expected only modes set in the script", 2, metaData.getExecutionModes().size());
		assertTrue("ON_SELECTED_NODE was set", metaData.getExecutionModes().contains(ExecutionMode.ON_SELECTED_NODE));
		assertTrue("ON_SELECTED_NODE_RECURSIVELY was set",
		    metaData.getExecutionModes().contains(ExecutionMode.ON_SELECTED_NODE_RECURSIVELY));
		assertEquals("menu location for ON_SELECTED_NODE_RECURSIVELY was set explicitely", "/menu_bar/help",
		    metaData.getMenuLocation(ExecutionMode.ON_SELECTED_NODE_RECURSIVELY));
		assertTrue("CacheScriptContent was set to true", metaData.cacheContent());
	}

	@Test
	public void testAnalyseScriptContentWithTitleKey() {
		// it's case insensitive
		// it's tolerant on white space
		String content = "// some comment" //
		        + "\n//@ExecutionModes (\t{\n  ExecutionMode.ON_selECTED_NODE=\"/menu_bar/help[icon_button_ok]\"" //
		        + ",\n \tON_SelECTED_NODE_RECURSIVELY = \"/menu_bar/help[Test_Script]\" } )" //
		        + "\n def test() {}\n";
		ScriptMetaData metaData = new ScriptingConfiguration().analyseScriptContent(content, scriptName);
		assertEquals("expected only modes set in the script", 2, metaData.getExecutionModes().size());
		assertTrue("ON_SELECTED_NODE was set", metaData.getExecutionModes().contains(ExecutionMode.ON_SELECTED_NODE));
		assertEquals("wrong menu location", "/menu_bar/help",
		    metaData.getMenuLocation(ExecutionMode.ON_SELECTED_NODE));
		assertEquals("wrong title key", "icon_button_ok", metaData.getTitleKey(ExecutionMode.ON_SELECTED_NODE));
		assertTrue("ON_SELECTED_NODE_RECURSIVELY was set",
		    metaData.getExecutionModes().contains(ExecutionMode.ON_SELECTED_NODE_RECURSIVELY));
		assertEquals("wrong menu location", "/menu_bar/help",
		    metaData.getMenuLocation(ExecutionMode.ON_SELECTED_NODE_RECURSIVELY));
		assertEquals("wrong title key", "Test_Script", metaData.getTitleKey(ExecutionMode.ON_SELECTED_NODE_RECURSIVELY));
	}

	@Test
	public void testAnalyseScriptContentRemoveDuplicates() {
		String content = "// some comment"
		        + "\n//   @ExecutionModes (\t{\n  ExecutionMode.ON_selECTED_NODE_recursively,\n \tON_SelECTED_NODE_RECURSIVELY } )";
		ScriptMetaData metaData = new ScriptingConfiguration().analyseScriptContent(content, scriptName);
		assertEquals("duplicated modes should not matter", 1, metaData.getExecutionModes().size());
		assertTrue("ON_SELECTED_NODE_RECURSIVELY was set",
		    metaData.getExecutionModes().contains(ExecutionMode.ON_SELECTED_NODE_RECURSIVELY));
		assertTrue("CacheScriptContent=false is the default", !metaData.cacheContent());
	}

	@Test
	public void testAnalyseScriptContentForFormula() {
		String content = "=\"blabla\"" //
		        + "\n//   @CacheScriptContent ( true\t ) ";
		ScriptMetaData metaData = new ScriptingConfiguration().analyseScriptContent(content, scriptName);
		assertEquals("single node mode should be removed for '=' scripts", 2, metaData.getExecutionModes().size());
		assertTrue("ON_SELECTED_NODE shouldn't been removed",
		    metaData.getExecutionModes().contains(ExecutionMode.ON_SELECTED_NODE));
		assertTrue("ON_SELECTED_NODE_RECURSIVELY shouldn't been removed",
		    metaData.getExecutionModes().contains(ExecutionMode.ON_SELECTED_NODE_RECURSIVELY));
		assertTrue("CacheScriptContent was set to true", metaData.cacheContent());
		// assert that duplicate entries do no harm
	}

	@Test
	public void testParseExecutionModes() throws Exception {
		ScriptMetaData metaData = new ScriptMetaData("test");
		ScriptingConfiguration.setExecutionModes(
		    "@ExecutionModes({on_selected_node=\"/menu/bla/blupp\",on_single_node=\"/menu/hi/ho\"})", metaData);
		ScriptingConfiguration.setExecutionModes(
		    "@ExecutionModes({on_selected_node=\"/menu/bla/blupp\"on_single_node=\"/menu/hi/ho\"})", metaData);
		ScriptingConfiguration.setExecutionModes("@ExecutionModes({on_single_node=\"/menu/hi/ho\"})", metaData);
		ScriptingConfiguration.setExecutionModes("@ExecutionModes({on_single_node})", metaData);
		ScriptingConfiguration.setExecutionModes("@ExecutionModes({\"/menu/hi/ho\"})", metaData);
		ScriptingConfiguration.setExecutionModes("@ExecutionModes({})", metaData);
	}
}
