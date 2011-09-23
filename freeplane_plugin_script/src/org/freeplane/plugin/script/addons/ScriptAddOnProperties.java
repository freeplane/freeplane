package org.freeplane.plugin.script.addons;

import java.io.File;
import java.util.List;
import java.util.Vector;

import org.freeplane.main.addons.AddOnProperties;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.script.ScriptingEngine;
import org.freeplane.plugin.script.ScriptingPermissions;

public class ScriptAddOnProperties extends AddOnProperties {
	private ScriptingPermissions permissions;
	private File script;

	public ScriptAddOnProperties(String name) {
		super(AddOnType.SCRIPT);
		setName(name);
	}

	public ScriptAddOnProperties(final XMLElement addOnelement) {
		super(AddOnType.SCRIPT, addOnelement);
		this.setPermissions(parseScriptingPermissions(addOnelement.getChildrenNamed("permissions")));
		this.setScript(new File(ScriptingEngine.getUserScriptDir(), getName() + ".groovy"));
		validate();
	}

	private void validate() {
		if (!script.exists())
			throw new RuntimeException("while parsing add-on XML file: Script " + script + " does not exist");
	}

	private ScriptingPermissions parseScriptingPermissions(Vector<XMLElement> xmlElements) {
		if (xmlElements == null || xmlElements.isEmpty())
			return null;
		return new ScriptingPermissions(xmlElements.get(0).getAttributes());
	}

	public ScriptingPermissions getPermissions() {
		return permissions;
	}

	public void setPermissions(ScriptingPermissions permissions) {
		this.permissions = permissions;
	}

	public File getScript() {
		return script;
	}

	public void setScript(File script) {
		this.script = script;
	}

	public XMLElement toXml() {
		final XMLElement xmlElement = super.toXml();
		addPermissionsAsChild(xmlElement);
		// no need to add script - it's generated from the add-on name in ctor
		return xmlElement;
	}

	private void addPermissionsAsChild(XMLElement parent) {
		XMLElement xmlElement = new XMLElement("permissions");
		final List<String> permissionNames = ScriptingPermissions.getPermissionNames();
		for (String permission : permissionNames) {
	        xmlElement.setAttribute(permission, Boolean.toString(permissions.get(permission)));
        }
		parent.addChild(xmlElement);
    }
}
