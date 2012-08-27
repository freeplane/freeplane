package org.docear.plugin.pdfutilities.features;

import org.docear.plugin.core.util.CompareVersion;

public class PDFReaderHandle {

	public enum RegistryBranch {
		DEFAULT, WOW6432NODE
	};
	
	private String name;
	private String execFile;
	private String version;
	private boolean isDefault;	
	private final RegistryBranch branch;
	
	/**
	 * @param Name
	 * @param file
	 */
	public PDFReaderHandle(String name, String execFile, RegistryBranch branch) {
		this.name = name;
		this.execFile = execFile;
		this.branch = branch;
	}	

	public PDFReaderHandle(RegistryBranch branch) {
		this.branch = branch;
	}

	public String getName() {
		return this.name;
	}
	
	public String toString() {
		return this.getName();
	}

	public String getExecFile() {
		return execFile;
	}
	
	public int compare(PDFReaderHandle handle) {		
		return CompareVersion.compareVersions(this.version, (handle == null ? null : handle.version));
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}
	
	public void setExecFile(String file) {
		this.execFile = file;
	}

	public boolean isComplete() {
		return (name != null && version != null && execFile != null);
	}

	public RegistryBranch getRegistryBranch() {
		return branch;
	}
	
	public boolean isDefault() {
		return isDefault;
	}

	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}

}
