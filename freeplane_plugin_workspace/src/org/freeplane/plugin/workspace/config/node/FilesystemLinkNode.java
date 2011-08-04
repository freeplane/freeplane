package org.freeplane.plugin.workspace.config.node;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;
import org.freeplane.plugin.workspace.controller.IWorkspaceNodeEventListener;
import org.freeplane.plugin.workspace.io.annotation.ExportAsAttribute;

public class FilesystemLinkNode extends AWorkspaceNode implements IWorkspaceNodeEventListener{
	private URL linkPath;
	
	public FilesystemLinkNode(String id) {
		super(id);
	}
	
	@ExportAsAttribute("path")
	public String getLinkPathString() {		
		if (linkPath == null || linkPath.getPath() == null) {
			return "";
		}
		try {
			URI path = new URI(linkPath.getPath());
			URI workspaceLocation = new URI(WorkspaceController.getCurrentWorkspaceController().getWorkspaceLocation());
			
			System.out.println("PATH: "+path);
			System.out.println("WORKSPACE: "+workspaceLocation);
			System.out.println("RELATIVE PATH: "+workspaceLocation.relativize(path).getPath());
			
			return workspaceLocation.relativize(path).getPath();
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		return linkPath.getPath();
	}
	
	public void setLinkPath(URL linkPath) {
		this.linkPath = linkPath;
	}	

	public void handleEvent(WorkspaceNodeEvent event) {
	}


	public String getTagName() {
		return "filesystem_link";
	}
}
