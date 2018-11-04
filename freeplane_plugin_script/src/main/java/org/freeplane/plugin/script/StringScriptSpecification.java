package org.freeplane.plugin.script;

class StringScriptSpecification implements ScriptSpecification{
	final String source;
	final String type;
	final ScriptingPermissions permissions;

	public StringScriptSpecification(String source, String type) {
		this(source, type, null);
	}

	public StringScriptSpecification(String source, String type, ScriptingPermissions permissions) {
		super();
		this.source = source;
		this.type = type;
		this.permissions = permissions;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((permissions == null) ? 0 : permissions.hashCode());
		result = prime * result + source.hashCode();
		result = prime * result + type.hashCode();
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
		StringScriptSpecification other = (StringScriptSpecification) obj;
		if (!source.equals(other.source))
			return false;
		if (!type.equals(other.type))
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