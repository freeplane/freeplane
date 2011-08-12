package org.freeplane.plugin.workspace.config.node;

import java.io.File;
import java.net.URI;
import java.net.URL;

import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;
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
		if(event.getType() == WorkspaceNodeEvent.WSNODE_OPEN_DOCUMENT) {
			try {
				URL url = getLinkPath().toURL().openConnection().getURL();
				Controller.getCurrentController().getViewController().openDocument(Compat.fileToUrl(new File(url.toURI())));
			}
			catch (Exception e) {
				LogUtils.warn("could not open document ("+getLinkPath()+")", e);
			}
		}
	}


	public String getTagName() {
		return "filesystem_link";
	}
}
