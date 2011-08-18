package org.docear.plugin.pdfutilities.ui.conflict;

import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeModel;

public class SolveConflictForNode implements ISolveConflictCommand {
	
	private NodeModel target;
	private String newTitle;

	public SolveConflictForNode(NodeModel target, String newTitle) {
		super();
		this.setTarget(target);
		this.setNewTitle(newTitle);		
	}

	public void solveConflict() {
		NodeChangeEvent event = new NodeChangeEvent(this.getTarget(), NodeModel.NODE_TEXT, this.getTarget().getText(), this.getNewTitle());
		this.getTarget().setText(getNewTitle());
		this.getTarget().fireNodeChanged(event);
	}

	public String getNewTitle() {
		return newTitle;
	}

	public void setNewTitle(String newTitle) {
		this.newTitle = newTitle;
	}

	public NodeModel getTarget() {
		return target;
	}

	public void setTarget(NodeModel target) {
		this.target = target;
	}

}
