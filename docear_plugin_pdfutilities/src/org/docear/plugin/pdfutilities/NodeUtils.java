package org.docear.plugin.pdfutilities;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.mode.Controller;

public class NodeUtils {
	
	private final MMapController currentMapController;
	
	public NodeUtils(){
		this.currentMapController = (MMapController) Controller.getCurrentModeController().getMapController();
		
	}
	
	public NodeModel insertChildNodesFrom(URI uri, List<PdfAnnotation> annotations, boolean isLeft, NodeModel target){
		File file = Tools.getFilefromUri(uri);
		if(file == null){
			return null;
		}
		else{
			return this.insertChildNodesFrom(file, annotations, isLeft, target);
		}
	}
	
	public NodeModel insertChildNodesFrom(File file, List<PdfAnnotation> annotations, boolean isLeft, NodeModel target){
		NodeModel node = this.insertChildNodeFrom(file, isLeft, target);
		this.insertChildNodesFrom(annotations, isLeft, node);
		return node;
	}
	
	public List<NodeModel> insertChildNodesFrom(List<PdfAnnotation> annotations, boolean isLeft, NodeModel target){
		List<NodeModel> nodes = new ArrayList<NodeModel>();
		
		for(PdfAnnotation annotation : annotations){
			NodeModel node = this.insertChildNodeFrom(annotation.getFile(), annotation.getTitle(), isLeft, target);
			this.insertChildNodesFrom(annotation.getChildren(), isLeft, node);
			nodes.add(node);
		}
		
		return nodes;
	}
	
	public NodeModel insertChildNodeFrom(File file, boolean isLeft, NodeModel target){
		return this.insertChildNodeFrom(file, file.getName(), isLeft, target);
	}
	
	public NodeModel insertChildNodeFrom(File file, String title, boolean isLeft, NodeModel target){		
		final NodeModel node = this.currentMapController.newNode(title, target.getMap());
		
		final URI uri;
		if (ResourceController.getResourceController().getProperty("links").equals("relative")) {
			uri = LinkController.toRelativeURI(node.getMap().getFile(), file);
		}
		else {
			uri = file.getAbsoluteFile().toURI();
		}
		
		((MLinkController) LinkController.getController()).setLink(node, uri, false);
		this.currentMapController.insertNode(node, target, false, isLeft, isLeft);
		
		return node;
	}

}
