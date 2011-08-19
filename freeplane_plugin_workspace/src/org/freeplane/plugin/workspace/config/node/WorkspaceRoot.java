package org.freeplane.plugin.workspace.config.node;

import java.awt.Component;

import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.config.IConfigurationInfo;
import org.freeplane.plugin.workspace.controller.IWorkspaceNodeEventListener;
import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;
import org.freeplane.plugin.workspace.io.annotation.ExportAsAttribute;

public class WorkspaceRoot extends AWorkspaceNode implements IConfigurationInfo, IWorkspaceNodeEventListener {
	
	private String version=WorkspaceController.WORKSPACE_VERSION;
	private Object meta=null;
	

	public WorkspaceRoot() {
		super(null);
	}

	public String getTagName() {
		return "workspace";
	}

	public void handleEvent(WorkspaceNodeEvent event) {
		if (event.getType() == WorkspaceNodeEvent.MOUSE_RIGHT_CLICK) {
			WorkspaceController.getController().getPopups()
					.showWorkspacePopup((Component) event.getSource(), event.getX(), event.getY());

		}
	}

	public void setVersion(String version) {
		this.version = version;
	}
	
	@ExportAsAttribute("version")
	public String getVersion() {
		return this.version;
	}
	
	public void setMeta(String meta) {
		this.meta=meta;		
	}
	
	@ExportAsAttribute("meta")
	public Object getMeta() {
		return this.meta;
	}
}
