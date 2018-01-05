package org.freeplane.features.map.mindmapmode;

import org.freeplane.features.map.NodeModel;

public class SingleNodeStructureManipulator {
	private final MMapController mapController;
	
	public SingleNodeStructureManipulator(MMapController mapController) {
		super();
		this.mapController = mapController;
	}

	public void insertNode(final NodeModel newNode, final NodeModel parent, final int index,
                             final boolean newNodeIsLeft) {
		mapController.insertSingleNewNode(newNode, parent, index, newNodeIsLeft);
	}
	
	public void deleteNode(final NodeModel parentNode, final int index) {
		mapController.deleteSingleNode(parentNode, index);
	}
	
	public void moveNode(final NodeModel child, final NodeModel newParent, final int newIndex,
                        final boolean isLeft, final boolean changeSide) {
		mapController.moveSingleNode(child, newParent, newIndex, isLeft, changeSide);
	}

	public void deleteNode(NodeModel child) {
		deleteNode(child.getParentNode(), child.getIndex());
		
	}	
	
}
