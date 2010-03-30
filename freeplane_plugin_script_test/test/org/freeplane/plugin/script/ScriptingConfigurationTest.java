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

import org.freeplane.plugin.script.ExecuteScriptAction.ExecutionMode;
import org.freeplane.plugin.script.ScriptingConfiguration.ScriptMetaData;
import org.junit.Assert;
import org.junit.Test;

public class ScriptingConfigurationTest {
	@Test
	public void testAnalyseScriptContent() {
		//		ScriptingConfiguration config = new ScriptingConfiguration();
		final String scriptName = "TestScript";
		// it's case insensitive
		// it's tolerant on white space
		// it doesn't mind 
		String content = "// some comment"
		        + "\n//@ExecutionModes (\t{\n  ExecutionMode.ON_selECTED_NODE,\n \tON_SelECTED_NODE_RECURSIVELY } )"
		        + "\n//  @CacheScriptContent ( true\t ) " //
		        + "\n def test() {}\n";
		ScriptMetaData metaData = new ScriptMetaData(scriptName);
		ScriptingConfiguration.analyseScriptContent(content, metaData);
		Assert.assertEquals("expected only modes set in the script", 2, metaData.getExecutionModes().size());
		Assert.assertTrue("ON_SELECTED_NODE was set", metaData.getExecutionModes().contains(
		    ExecutionMode.ON_SELECTED_NODE));
		Assert.assertTrue("ON_SELECTED_NODE_RECURSIVELY was set", metaData.getExecutionModes().contains(
		    ExecutionMode.ON_SELECTED_NODE_RECURSIVELY));
		Assert.assertTrue("CacheScriptContent was set to true", metaData.cacheContent());
		content = "// some comment"
		        + "\n//   @ExecutionModes (\t{\n  ExecutionMode.ON_selECTED_NODE_recursively,\n \tON_SelECTED_NODE_RECURSIVELY } )";
		metaData = new ScriptingConfiguration.ScriptMetaData(scriptName);
		ScriptingConfiguration.analyseScriptContent(content, metaData);
		Assert.assertEquals("duplicated modes should not matter", 1, metaData.getExecutionModes().size());
		Assert.assertTrue("ON_SELECTED_NODE_RECURSIVELY was set", metaData.getExecutionModes().contains(
		    ExecutionMode.ON_SELECTED_NODE_RECURSIVELY));
		Assert.assertTrue("CacheScriptContent=false is the default", !metaData.cacheContent());
		content = "=\"blabla\"" + "\n//   @CacheScriptContent ( true\t ) ";
		metaData = new ScriptingConfiguration.ScriptMetaData(scriptName);
		ScriptingConfiguration.analyseScriptContent(content, metaData);
		Assert.assertEquals("single node mode should be removed for '=' scripts", 2, metaData.getExecutionModes()
		    .size());
		Assert.assertTrue("ON_SELECTED_NODE shouldn't been removed", metaData.getExecutionModes().contains(
		    ExecutionMode.ON_SELECTED_NODE));
		Assert.assertTrue("ON_SELECTED_NODE_RECURSIVELY shouldn't been removed", metaData.getExecutionModes().contains(
		    ExecutionMode.ON_SELECTED_NODE_RECURSIVELY));
		Assert.assertTrue("CacheScriptContent was set to true", metaData.cacheContent());
		// assert that duplicate entries do no harm
	}
}
