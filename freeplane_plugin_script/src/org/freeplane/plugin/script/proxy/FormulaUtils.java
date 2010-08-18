package org.freeplane.plugin.script.proxy;

import org.freeplane.features.common.map.NodeModel;
import org.freeplane.plugin.script.ScriptingEngine;

public class FormulaUtils {

	public static Object evalNodeText(NodeModel nodeModel) {
        final String text = nodeModel.getText();
		// will fail if nodeModel is not initialized
    	if (text .length() > 1 && text.charAt(0) == '=')
	        return evalNodeTextImpl(text, nodeModel);
        else
    		return text;
    }

	private static Object evalNodeTextImpl(String text, NodeModel nodeModel) {
	    return ScriptingEngine.executeScript(nodeModel, text.substring(1));
    }
}
