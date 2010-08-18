package org.freeplane.plugin.script;

import groovy.lang.Script;

import java.util.regex.Pattern;

import org.freeplane.plugin.script.proxy.Proxy.Node;

public abstract class FreeplaneScriptBaseClass extends Script {
	private Pattern nodeIdPattern = Pattern.compile("ID_\\d+");

	/** translate raw node ids to nodes. */
	public Object getProperty(String property) {
		if (nodeIdPattern.matcher(property).matches()) {
			return N(property);
		}
		else {
			return super.getProperty(property);
		}
	}

	/** Shortcut for node.map.node(id) - necessary for ids to other maps. */
	public Node N(String id) {
		final Node node = (Node) getBinding().getVariable("node");
		return node.getMap().node(id);
	}
	
	/** Shortcut for node.map.node(id).text. */
	public String T(String id) {
		final Node n = N(id);
		return n == null ? null : n.getText();
	}
	
	/** Shortcut for node.map.node(id).value. */
	public Object V(String id) {
		final Node n = N(id);
		return n == null ? null : n.getValue();
	}
}
