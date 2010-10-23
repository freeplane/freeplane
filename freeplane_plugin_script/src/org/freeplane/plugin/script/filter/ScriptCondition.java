package org.freeplane.plugin.script.filter;

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.common.filter.condition.ASelectableCondition;
import org.freeplane.features.common.filter.condition.ASelectableCondition;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.script.ScriptingEngine;

public class ScriptCondition extends ASelectableCondition {
	private static final String SCRIPT_FILTER_DESCRIPTION_RESOURCE = "plugins/script_filter";
	private static final String SCRIPT_FILTER_ERROR_RESOURCE = "plugins/script_filter_error";
	private static final String NAME = "script_condition";
	private static final String SCRIPT = "SCRIPT";
	final private String script;

	static ASelectableCondition load(final XMLElement element) {
		return new ScriptCondition(element.getAttribute(SCRIPT, null));
	}

	public ScriptCondition(final String script) {
		super();
		this.script = script;
	}

	public boolean checkNode(final NodeModel node) {
		final Object result = ScriptingEngine.executeScript(node, script);
		System.out.println(this + ": got '" + result + "' for " + node);
		if (result == null || !(result instanceof Boolean)) {
			throw new RuntimeException(TextUtils.format(SCRIPT_FILTER_ERROR_RESOURCE, createDesctiption(),
			    node.toString(), String.valueOf(result)));
		}
		return (Boolean) result;
	}

	@Override
	protected String createDesctiption() {
		return TextUtils.format(SCRIPT_FILTER_DESCRIPTION_RESOURCE, script);
	}

	public void fillXML(final XMLElement child) {
		super.fillXML(child);
		child.setAttribute(SCRIPT, script);
	}

	@Override
    protected String getName() {
	    return NAME;
    }
}
