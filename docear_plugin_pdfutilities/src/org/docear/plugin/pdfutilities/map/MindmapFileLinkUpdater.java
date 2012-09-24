package org.docear.plugin.pdfutilities.map;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.docear.plugin.core.mindmap.AMindmapUpdater;
import org.docear.plugin.core.util.Tools;
import org.docear.plugin.pdfutilities.features.IAnnotation;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.workspace.model.WorkspaceTreeModelEvent;
import org.freeplane.plugin.workspace.model.WorkspaceTreeModelEvent.WorkspaceTreeModelEventType;

public class MindmapFileLinkUpdater extends AMindmapUpdater {
	
	WorkspaceTreeModelEvent event;
	Map<File, File> fileMap = new HashMap<File, File>();

	public MindmapFileLinkUpdater(String title, WorkspaceTreeModelEvent event,	Map<File, File> fileMap) {
		super(title);		
		this.event = event;
		this.fileMap = fileMap;
	}

	@Override
	public boolean updateMindmap(MapModel map) {
		if(map == null) return false;		
		return updateLinks(map.getRootNode());
	}

	private boolean updateLinks(NodeModel node) {
		if(node == null) return false;
		File link = Tools.getFilefromUri(Tools.getAbsoluteUri(node));
		if(link != null){
			if(fileMap.containsKey(link)){
				((MLinkController) LinkController.getController()).setLinkTypeDependantLink(node, fileMap.get(link));
				if(event != null && event.getType() == WorkspaceTreeModelEventType.rename && node.getText().equals(link.getName())){
					node.setText(fileMap.get(link).getName());
				}
				IAnnotation annotation = AnnotationController.getModel(node, false);
				if(annotation != null && annotation.getAnnotationID() != null && fileMap.containsKey(Tools.getFilefromUri(annotation.getAnnotationID().getUri()))){
					annotation.getAnnotationID().setId(fileMap.get(Tools.getFilefromUri(annotation.getAnnotationID().getUri())).toURI(), annotation.getAnnotationID().getObjectNumber());
				}
			}
		}
		for(NodeModel child : node.getChildren()){
			updateLinks(child);
		}
		return true;
	}
	
}
