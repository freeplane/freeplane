package org.freeplane.plugin.workspace.config.node;

import java.net.URI;

import org.freeplane.plugin.workspace.controller.IWorkspaceNodeEventListener;
import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;
import org.freeplane.plugin.workspace.io.annotation.ExportAsAttribute;

public class FilesystemLinkNode extends AWorkspaceNode implements IWorkspaceNodeEventListener{
	private URI linkPath;
	
	public FilesystemLinkNode(String id) {
		super(id);
	}
	
	@ExportAsAttribute("path")
	public URI getLinkPath() {
		return linkPath;
	}
	
	public void setLinkPath(URI linkPath) {
		this.linkPath = linkPath;
	}	

	public void handleEvent(WorkspaceNodeEvent event) {
	}


	public String getTagName() {
		return "filesystem_link";
	}
}
