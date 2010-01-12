package org.freeplane.plugin.script;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.freeplane.plugin.script.ExecuteScriptAction.ExecutionMode;
import org.freeplane.plugin.script.ScriptingConfiguration.ScriptMetaData;
import org.junit.Test;

public class ScriptingConfigurationTest {
	@Test
	public void testAnalyseScriptContent() {
//		ScriptingConfiguration config = new ScriptingConfiguration();
		String scriptName = "TestScript";
		// it's case insensitive
		// it's tolerant on white space
		// it doesn't mind 
		String content = "// some comment"
	        + "\n//@ExecutionModes (\t{\n  ExecutionMode.ON_selECTED_NODE,\n \tON_SelECTED_NODE_RECURSIVELY } )"
	        + "\n//  @CacheScriptContent ( true\t ) " //
	        + "\n def test() {}\n";
		ScriptMetaData metaData = new ScriptMetaData(scriptName);
		ScriptingConfiguration.analyseScriptContent(content, metaData);
		assertEquals("expected only modes set in the script", 2, metaData.getExecutionModes().size());
		assertTrue("ON_SELECTED_NODE was set", metaData.getExecutionModes().contains(ExecutionMode.ON_SELECTED_NODE));
		assertTrue("ON_SELECTED_NODE_RECURSIVELY was set", metaData.getExecutionModes().contains(ExecutionMode.ON_SELECTED_NODE_RECURSIVELY));
		assertTrue("CacheScriptContent was set to true", metaData.isCached());
		
		content = "// some comment"
	        + "\n//   @ExecutionModes (\t{\n  ExecutionMode.ON_selECTED_NODE_recursively,\n \tON_SelECTED_NODE_RECURSIVELY } )";
		metaData = new ScriptingConfiguration.ScriptMetaData(scriptName);
		ScriptingConfiguration.analyseScriptContent(content, metaData);
		assertEquals("duplicated modes should not matter", 1, metaData.getExecutionModes().size());
		assertTrue("ON_SELECTED_NODE_RECURSIVELY was set", metaData.getExecutionModes().contains(ExecutionMode.ON_SELECTED_NODE_RECURSIVELY));
		assertTrue("CacheScriptContent=false is the default", !metaData.isCached());
		
		content = "=\"blabla\""
			+ "\n//   @CacheScriptContent ( true\t ) ";
		metaData = new ScriptingConfiguration.ScriptMetaData(scriptName);
		ScriptingConfiguration.analyseScriptContent(content, metaData);
		assertEquals("single node mode should be removed for '=' scripts", 2, metaData.getExecutionModes().size());
		assertTrue("ON_SELECTED_NODE shouldn't been removed", metaData.getExecutionModes().contains(ExecutionMode.ON_SELECTED_NODE));
		assertTrue("ON_SELECTED_NODE_RECURSIVELY shouldn't been removed", metaData.getExecutionModes().contains(ExecutionMode.ON_SELECTED_NODE_RECURSIVELY));
		assertTrue("CacheScriptContent was set to true", metaData.isCached());
		
		// assert that duplicate entries do no harm
	}
}
