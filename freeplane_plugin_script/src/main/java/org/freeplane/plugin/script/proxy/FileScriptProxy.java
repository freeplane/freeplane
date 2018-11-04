package org.freeplane.plugin.script.proxy;

import java.io.File;

import org.freeplane.api.Script;
import org.freeplane.plugin.script.IScript;
import org.freeplane.plugin.script.ScriptContext;
import org.freeplane.plugin.script.ScriptingEngine;
import org.freeplane.plugin.script.ScriptingPermissions;

class FileScriptProxy extends ScriptProxy implements Script {
	private final File file;

	public FileScriptProxy(File file, ScriptContext scriptContext) {
		super(scriptContext);
		this.file = file;
	}
	@Override
	protected IScript createScript(ScriptingPermissions scriptingPermissions) {
		return ScriptingEngine.createScript(file, scriptingPermissions, false);
	}
}
