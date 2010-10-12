package org.freeplane.plugin.script.filter;

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.common.filter.condition.ISelectableCondition;
import org.freeplane.features.common.filter.condition.NodeCondition;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.script.ScriptingEngine;

public class ScriptCondition extends NodeCondition {
	private static final String NAME = "script_condition";
	private static final String SCRIPT = "SCRIPT";
	final private String script;

	static ISelectableCondition load(final XMLElement element) {
		return new ScriptCondition(element.getAttribute(SCRIPT, null));
	}

	public ScriptCondition(final String script) {
		super();
		this.script = script;
	}

	public boolean checkNode(final NodeModel node) {
		final Object result = ScriptingEngine.executeScript(node, script);
		// FIXME: temporarily!
		System.out.println(this + ": got '" + result + "' for " + node);
		return result != null && Boolean.valueOf(String.valueOf(result));
	}

	@Override
    protected String createDesctiption() {
	    return TextUtils.format("plugins/script_filter", script);
    }

	public void toXml(final XMLElement element) {
		final XMLElement child = new XMLElement();
		child.setName(ScriptCondition.NAME);
		super.attributesToXml(child);
		child.setAttribute(SCRIPT, script);
		element.addChild(child);
	}
}
