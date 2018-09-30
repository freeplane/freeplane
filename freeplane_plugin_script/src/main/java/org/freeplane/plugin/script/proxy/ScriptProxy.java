package org.freeplane.plugin.script.proxy;

import static org.freeplane.plugin.script.ScriptingPermissions.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_EXEC_RESTRICTION;
import static org.freeplane.plugin.script.ScriptingPermissions.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_NETWORK_RESTRICTION;
import static org.freeplane.plugin.script.ScriptingPermissions.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_READ_RESTRICTION;
import static org.freeplane.plugin.script.ScriptingPermissions.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_WRITE_RESTRICTION;

import java.io.File;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.freeplane.api.NodeRO;
import org.freeplane.api.Script;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.script.*;
import org.freeplane.plugin.script.ScriptExecution;

public class ScriptProxy implements Script {
	private final File file;
	private final Map<String, Boolean> permissions;
	private final ScriptExecution scriptExecution;
	private PrintStream outStream;

	public ScriptProxy(File file, ScriptExecution scriptExecution) {
		this.file = file;
		this.scriptExecution = scriptExecution;
		permissions = new HashMap<String, Boolean>();
	}

	@Override
	public ScriptProxy startingApplications() {
		permissions.put(RESOURCES_EXECUTE_SCRIPTS_WITHOUT_EXEC_RESTRICTION, true);
		return this;
	}

	@Override
	public ScriptProxy accessingNetwork() {
		permissions.put(RESOURCES_EXECUTE_SCRIPTS_WITHOUT_NETWORK_RESTRICTION, true);
		return this;
	}

	@Override
	public ScriptProxy readingFiles() {
		permissions.put(RESOURCES_EXECUTE_SCRIPTS_WITHOUT_READ_RESTRICTION, true);
		return this;
	}

	@Override
	public ScriptProxy writingFiles() {
		permissions.put(RESOURCES_EXECUTE_SCRIPTS_WITHOUT_WRITE_RESTRICTION, true);
		return this;
	}

	@Override
	public ScriptProxy withAllPermissions() {
		permissions.put(RESOURCES_EXECUTE_SCRIPTS_WITHOUT_READ_RESTRICTION, true);
		permissions.put(RESOURCES_EXECUTE_SCRIPTS_WITHOUT_WRITE_RESTRICTION, true);
		permissions.put(RESOURCES_EXECUTE_SCRIPTS_WITHOUT_NETWORK_RESTRICTION, true);
		permissions.put(RESOURCES_EXECUTE_SCRIPTS_WITHOUT_EXEC_RESTRICTION, true);
		return this;
	}



	@Override
	public ScriptProxy withOutput(PrintStream outStream) {
		this.outStream = outStream;
		return this;
	}
	@Override
	public Object executeOn(NodeRO node) {
		final IScript script = ScriptingEngine.createScriptForFile(file, new ScriptingPermissions(permissions));
		final ScriptRunner scriptRunner = new ScriptRunner(script);
		scriptRunner.setScriptExecution(scriptExecution);
		if(outStream != null)
			scriptRunner.setOutStream(outStream);
		final NodeModel nodeModel = ((NodeProxy) node).getDelegate();
		final Object result = scriptRunner.execute(nodeModel);
		return result;
	}
}
