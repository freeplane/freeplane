package org.docear.plugin.core.mindmap;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.docear.plugin.core.features.AnnotationModel;
import org.docear.plugin.core.util.Tools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.workspace.WorkspaceUtils;

public class AnnotationModelUpdater extends AMindmapUpdater {
	
	Map<URI, List<AnnotationModel>> importedPdfs = new HashMap<URI, List<AnnotationModel>>();

	public AnnotationModelUpdater(String title) {
		super(title);		
	}

	private boolean updateNode(NodeModel node) {
		boolean changed = false;
		File file = WorkspaceUtils.resolveURI(NodeLinks.getValidLink(node), node.getMap());
		if(file != null && file.getName().toLowerCase().endsWith(".pdf") && AnnotationController.getModel(node, false) == null){
			try {
				if(!importedPdfs.containsKey(Tools.getAbsoluteUri(node))) {
					for(IAnnotationImporter importer : AnnotationController.getAnnotationImporters()) {
						AnnotationModel pdf = importer.importPdf(Tools.getAbsoluteUri(node));
						importedPdfs.put(Tools.getAbsoluteUri(node), this.getPlainAnnotationList(pdf));
					}
				}			
				for(AnnotationModel annotation : importedPdfs.get(Tools.getAbsoluteUri(node))){
					if(annotation.getTitle().equals(node.getText())){
						AnnotationController.setModel(node, annotation);	
						changed = true;
						break;
					}
				}
			} catch (Exception e) {
				LogUtils.warn(e);
			}
		}
		return changed;
	}
	
	private List<AnnotationModel> getPlainAnnotationList(AnnotationModel root){
		List<AnnotationModel> result = new ArrayList<AnnotationModel>();
		result.add(root);
		for(AnnotationModel child : root.getChildren()){
			result.addAll(this.getPlainAnnotationList(child));						
		}
		return result;
	}

	@Override
	public boolean updateMindmap(MapModel map) {
		return updateNodesRecursive(map.getRootNode());
	}
	
	/**
	 * @param node
	 * @return
	 */
	private boolean updateNodesRecursive(NodeModel node) {
		boolean changes = false;
		for(NodeModel child : node.getChildren()) {
			changes = changes | updateNodesRecursive(child);
		}
		changes = changes | updateNode(node);
		return changes;
	}
}
