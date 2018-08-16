package org.freeplane.core.undo;

import java.util.Arrays;
import java.util.List;

import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

public class SelectionActor implements IActor {
	private final String[] nodeIDs;
	final private MapModel map;
	static private SelectionActor lastSelectionActor = null;

	static SelectionActor create(IMapSelection selection){
		final SelectionActor selectionActor = new SelectionActor(selection);
		if(!selectionActor.equals(lastSelectionActor))
			lastSelectionActor = selectionActor;
		return lastSelectionActor;
	}

	private SelectionActor(IMapSelection selection) {
		super();
		map = selection.getSelected().getMap();
		final List<NodeModel> nodes = selection.getOrderedSelection();
		this.nodeIDs = new String[nodes.size()];
		int index = 0;
		for(NodeModel node : nodes)
			nodeIDs[index++] = node.createID();
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + map.hashCode();
		result = prime * result + Arrays.hashCode(nodeIDs);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SelectionActor other = (SelectionActor) obj;
		if (!map.equals(other.map))
			return false;
		if (!Arrays.equals(nodeIDs, other.nodeIDs))
			return false;
		return true;
	}

	@Override
	public void act() {
		restoreSelection();
	}

	private void restoreSelection() {
		final Controller controller = Controller.getCurrentController();
		if(! map.equals(controller.getMap()))
			return;

		final IMapSelection selection = controller.getSelection();
		if(this.equals(new SelectionActor(selection)))
			return;
		NodeModel[] nodes = new NodeModel[nodeIDs.length];
		int index = 0;
		for(String id : nodeIDs)
			nodes[index++] = map.getNodeForID_(id);
		selection.replaceSelection(nodes);
	}

	@Override
	public String getDescription() {
		return "Restore selection";
	}

	@Override
	public void undo() {
		restoreSelection();
	}
}
