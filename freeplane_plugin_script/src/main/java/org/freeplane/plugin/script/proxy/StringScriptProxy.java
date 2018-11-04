package org.freeplane.plugin.script.proxy;

import org.freeplane.api.Script;
import org.freeplane.plugin.script.IScript;
import org.freeplane.plugin.script.ScriptContext;
import org.freeplane.plugin.script.ScriptingEngine;
import org.freeplane.plugin.script.ScriptingPermissions;

public class StringScriptProxy extends ScriptProxy implements Script {
	private final String script;
	private final String type;

	public StringScriptProxy(String script, String type, ScriptContext scriptContext) {
		super(scriptContext);
		this.script = script;
		this.type = type;
	}
	@Override
	protected IScript createScript(ScriptingPermissions scriptingPermissions) {
		return ScriptingEngine.createScript(script, type, scriptingPermissions);
	}
}
