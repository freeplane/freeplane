package org.docear.plugin.pdfutilities.features;

import org.freeplane.features.map.NodeModel;

public class AnnotationNodeModel extends AnnotationModel {
	
	private NodeModel node;	
	
	public NodeModel getNode() {
		return node;
	}

	public void setNode(NodeModel node) {
		this.node = node;
	}

	public AnnotationNodeModel(NodeModel node, AnnotationID id) {		
		super(id);
		this.node = node;
	}
	
	public AnnotationNodeModel(NodeModel node, AnnotationID id, AnnotationType type) {
		super( id, type);
		this.node = node;		
	}

	public String getTitle() {
		return node.getText();
	}

	public void setTitle(String title) {
		this.node.setText(title);
	}
	
}
