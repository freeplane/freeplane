package org.freeplane.plugin.script;

import java.io.File;

class FileScriptSpecification implements ScriptSpecification{
	final File source;
	final ScriptingPermissions permissions;

	public FileScriptSpecification(File source) {
		this(source, null);
	}

	public FileScriptSpecification(File source, ScriptingPermissions permissions) {
		super();
		this.source = source;
		this.permissions = permissions;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((permissions == null) ? 0 : permissions.hashCode());
		result = prime * result + source.hashCode();
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FileScriptSpecification other = (FileScriptSpecification) obj;
		if (!source.equals(other.source))
			return false;
		if (permissions == null) {
			if (other.permissions != null)
				return false;
		}
		else if (!permissions.equals(other.permissions))
			return false;
		return true;
	}

}