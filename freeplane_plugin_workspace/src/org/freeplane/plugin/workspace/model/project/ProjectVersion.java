package org.freeplane.plugin.workspace.model.project;

public class ProjectVersion {
	
	private final String versionString;

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public ProjectVersion(String versionString) {
		this.versionString = versionString;
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public String getVersionString() {
		return versionString;
	}
	
	public String toString() {
		return versionString;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ProjectVersion) {
			return this.getVersionString().equals(((ProjectVersion) obj).getVersionString());
		}
		return super.equals(obj);
	}
	
	
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
