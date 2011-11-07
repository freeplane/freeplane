package org.freeplane.plugin.workspace.config.creator;

import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.config.WorkspaceConfiguration;
import org.freeplane.plugin.workspace.config.node.WorkspaceRoot;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;

public class WorkspaceRootCreator extends AWorkspaceNodeCreator {
	private final WorkspaceConfiguration configuration;
	public WorkspaceRootCreator(WorkspaceConfiguration configuration) {
		this.configuration = configuration;
	}

	public AWorkspaceTreeNode getNode(XMLElement data) {		
		WorkspaceRoot node = new WorkspaceRoot();
		String name = data.getAttribute("name", "workspace");
		String version = data.getAttribute("version", WorkspaceController.WORKSPACE_VERSION);
		String meta = data.getAttribute("meta", "");
		
		node.setName(name);
		node.setVersion(version);
		node.setMeta(meta);
		this.configuration.setConfigurationInfo(node);
		return node;
	}
	
}
