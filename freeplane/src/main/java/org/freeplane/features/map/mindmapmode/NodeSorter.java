package org.freeplane.features.map.mindmapmode;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import org.freeplane.features.map.FreeNode;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.SummaryNode;
import org.freeplane.features.mode.Controller;

public class NodeSorter {
	final Comparator<NodeModel> comparator;

	public NodeSorter(Comparator<NodeModel> comparator) {
		super();
		this.comparator = comparator;
	}

	public void sortNodes(NodeModel node) {
		sortNodes(node, 0);
	}

	private void sortNodes(final NodeModel parent, int fromIndex) {
		final int childCount = parent.getChildCount();
		if (fromIndex >= childCount)
			return;
		final Vector<NodeModel> sortVector = new Vector<NodeModel>(childCount - fromIndex);
		int nodeIndex = fromIndex;
		while (nodeIndex < childCount) {
			final NodeModel child = parent.getChildAt(nodeIndex);
			nodeIndex++;
			if (SummaryNode.isSummaryNode(child)) {
				sortNodes(child, 0);
				break;
			}
			else if (SummaryNode.isFirstGroupNode(child)) {
				break;
			}
			else
				sortVector.add(child);
		}
		Collections.sort(sortVector, comparator);
		final MMapController mapController = (MMapController) Controller.getCurrentModeController()
		    .getMapController();
		for (final NodeModel child : sortVector) {
			Controller.getCurrentModeController().getExtension(FreeNode.class).undoableDeactivateHook(child);
			mapController.moveNode(child, fromIndex++);
		}
		sortNodes(parent, nodeIndex);
	}
}