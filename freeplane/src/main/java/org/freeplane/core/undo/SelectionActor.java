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
	static private SelectionActor lastSelectionActorOnAct = null;
	private final boolean onlyOnAct;

	public static SelectionActor createForActOnly(IMapSelection selection) {
		final SelectionActor selectionActor = new SelectionActor(selection, true);
		if(!selectionActor.containsSameSelection(lastSelectionActorOnAct))
			lastSelectionActorOnAct = selectionActor;
		return lastSelectionActorOnAct;
	}

	public static SelectionActor create(IMapSelection selection) {
		final SelectionActor selectionActor = new SelectionActor(selection, false);
		if(!selectionActor.containsSameSelection(lastSelectionActor))
			lastSelectionActor = selectionActor;
		return lastSelectionActor;
	}

	private SelectionActor(IMapSelection selection, boolean onlyOnAct) {
		super();
		this.onlyOnAct = onlyOnAct;
		map = selection.getSelected().getMap();
		final List<NodeModel> nodes = selection.getOrderedSelection();
		this.nodeIDs = new String[nodes.size()];
		int index = 0;
		for(NodeModel node : nodes)
			nodeIDs[index++] = node.createID();
	}

	private boolean containsSameSelection(SelectionActor other) {
		return other != null
				&& map.equals(other.map)
				&& Arrays.equals(nodeIDs, other.nodeIDs);
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
		if(this.containsSameSelection(new SelectionActor(selection, onlyOnAct)))
			return;
		NodeModel[] nodes = new NodeModel[nodeIDs.length];
		int index = 0;
		for(String id : nodeIDs)
			nodes[index++] = map.getNodeForID(id);
		selection.replaceSelection(nodes);
	}

	@Override
	public String getDescription() {
		return "Restore selection";
	}

	@Override
	public void undo() {
		if(! onlyOnAct)
			restoreSelection();
	}
}
