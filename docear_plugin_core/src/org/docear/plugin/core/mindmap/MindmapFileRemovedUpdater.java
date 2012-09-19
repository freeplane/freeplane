package org.docear.plugin.core.mindmap;

import java.io.File;
import java.util.Set;

import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.workspace.WorkspaceUtils;

public class MindmapFileRemovedUpdater extends AMindmapUpdater {
	
	private Set<File> deletedFiles = null;

	public MindmapFileRemovedUpdater(String title, Set<File> deletedFiles) {
		super(title);		
		this.deletedFiles = deletedFiles;
	}

	@Override
	public boolean updateMindmap(MapModel map) {
		if(map == null) return false;		
		return updateLinks(map.getRootNode());		
	}

	private boolean updateLinks(NodeModel parent) {
		//TODO: wenn titel des knotens der datei entspricht --> löschen
		//nur die jeweilige(n) Datei(en) löschen, nicht alle
		//referenzen entfernen
		
		NodeLinks links = NodeLinks.getLinkExtension(parent);		
		if (links != null) {
			File link;
			try {
				link = WorkspaceUtils.resolveURI(links.getHyperLink(), parent.getMap());			
			}
			catch(Exception e) {
				link = null;
			}
			
			if (link != null && deletedFiles.contains(link)) {
				links.setHyperLink(null);
			}
			
			
			
		}
		
		for(NodeModel child : parent.getChildren()){
			updateLinks(child);
		}
		
		return true;
	}

}
