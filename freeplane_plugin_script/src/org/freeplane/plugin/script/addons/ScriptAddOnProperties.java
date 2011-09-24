package org.freeplane.plugin.script.addons;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.freeplane.main.addons.AddOnProperties;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.script.ExecuteScriptAction.ExecutionMode;
import org.freeplane.plugin.script.ScriptingEngine;
import org.freeplane.plugin.script.ScriptingPermissions;

public class ScriptAddOnProperties extends AddOnProperties {
	public static class Script {
		public String name;
		public File file;
		public Map<String, String> menuMapping = new LinkedHashMap<String, String>();
		public List<ExecutionMode> executionModes;
		public ScriptingPermissions permissions;
		public String scriptBody;
	}

	private List<Script> scripts;

	public ScriptAddOnProperties(String name) {
		super(AddOnType.SCRIPT);
		setName(name);
	}

	public ScriptAddOnProperties(final XMLElement addOnelement) {
		super(AddOnType.SCRIPT, addOnelement);
		this.scripts = parseScripts(addOnelement.getChildrenNamed("scripts"));
		validate();
	}

	private void validate() {
		if (scripts == null || scripts.isEmpty())
			throw new RuntimeException(this + ": on parsing add-on XML file: no scripts defined");
		for (Script script : scripts) {
			if (script.name == null)
				throw new RuntimeException(this + ": on parsing add-on XML file: no name");
			if (!script.file.exists())
				throw new RuntimeException(this + ": on parsing add-on XML file: Script " + script + " does not exist");
			if (script.executionModes == null || script.executionModes.isEmpty())
				throw new RuntimeException(this + ": on parsing add-on XML file: no execution_modes");
			if (script.menuMapping == null || script.menuMapping.isEmpty())
				throw new RuntimeException(this + ": on parsing add-on XML file: no menu mappings");
			if (script.permissions == null)
				throw new RuntimeException(this + ": on parsing add-on XML file: no permissions");
		}
	}

	private List<Script> parseScripts(Vector<XMLElement> xmlElements) {
		final ArrayList<Script> scripts = new ArrayList<Script>();
		if (xmlElements == null || xmlElements.isEmpty())
			return scripts;
		for (XMLElement scriptXmlNode : xmlElements.get(0).getChildren()) {
			final Script script = new Script();
			for (Entry<Object, Object> entry : scriptXmlNode.getAttributes().entrySet()) {
				if (entry.getKey().equals("name")) {
					script.name = (String) entry.getValue();
					script.file = new File(ScriptingEngine.getUserScriptDir(), script.name);
				}
				else if (entry.getKey().toString().startsWith("execution_mode")) {
					script.executionModes = parseExecutionModes(entry.getValue().toString());
				}
				else if (!entry.getKey().toString().startsWith("execute_scripts_")) {
					script.menuMapping.put(entry.getKey().toString(), entry.getValue().toString());
				}
			}
			script.permissions = new ScriptingPermissions(scriptXmlNode.getAttributes());
			scripts.add(script);
		}
		return scripts;
	}

	public static List<ExecutionMode> parseExecutionModes(final String executionModesCSV) {
		final ArrayList<ExecutionMode> executionModes = new ArrayList<ExecutionMode>();
		for (String string : executionModesCSV.toString().split("\\s*,\\s*")) {
			try {
				executionModes.add(ExecutionMode.valueOf(string.toUpperCase()));
			}
			catch (Exception e) {
				throw new RuntimeException("invalid execution mode found in " + executionModesCSV, e);
			}
		}
		return executionModes;
	}

	public XMLElement toXml() {
		final XMLElement xmlElement = super.toXml();
		addScriptsAsChild(xmlElement);
		return xmlElement;
	}

	private void addScriptsAsChild(XMLElement parent) {
		XMLElement xmlElement = new XMLElement("scripts");
		for (Script script : scripts) {
			XMLElement scriptXmlElement = new XMLElement("script");
			scriptXmlElement.setAttribute("name", script.name);
			if (script.executionModes != null && !script.executionModes.isEmpty()) {
				scriptXmlElement.setAttribute("execution_modes",
				    StringUtils.join(script.executionModes.iterator(), ','));
			}
			for (Entry<String, String> entry : script.menuMapping.entrySet()) {
				scriptXmlElement.setAttribute(entry.getKey(), entry.getValue());
			}
			final List<String> permissionNames = ScriptingPermissions.getPermissionNames();
			for (String permission : permissionNames) {
				scriptXmlElement.setAttribute(permission, Boolean.toString(script.permissions.get(permission)));
			}
			xmlElement.addChild(scriptXmlElement);
		}
		parent.addChild(xmlElement);
	}
}
