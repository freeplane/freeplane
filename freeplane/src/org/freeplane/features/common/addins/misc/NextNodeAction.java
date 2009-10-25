package org.freeplane.features.common.addins.misc;

import java.awt.event.ActionEvent;
import java.lang.ref.WeakReference;
import java.util.Enumeration;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.IMapSelection;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ActionLocationDescriptor;

public class NextNodeAction extends AFreeplaneAction {
	public enum Direction{BACK, BACK_N_FOLD, FORWARD, FORWARD_N_FOLD};
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Direction direction;
	public NextNodeAction(Controller controller, Direction direction) {
		super("NextNodeAction." + direction.toString(), controller);
		this.direction = direction;
	}
	/**
	 * 
	 */
	public void actionPerformed(ActionEvent e) {
		IMapSelection selection = getController().getSelection();
		NodeModel selected = selection.getSelected();
		NodeModel next = selected;
		do{
			switch(direction){
			case FORWARD:
			case FORWARD_N_FOLD:
				next = getNext(next);
				break;
			case BACK:
			case BACK_N_FOLD:
				next = getPrevious(next);
				break;
			}
		} while (! next.isVisible());
		getModeController().getMapController().select(next);

	}
	private NodeModel getNext(NodeModel selected) {
			if(selected.getChildCount() != 0){
				return (NodeModel) selected.getChildAt(0);
			}
			for(;;){
				NodeModel parentNode = selected.getParentNode();
				if(parentNode == null){
					return selected;
				}
				int index = parentNode.getIndex(selected)+1;
				int childCount = parentNode.getChildCount();
				if(index < childCount){
					if(direction == Direction.FORWARD_N_FOLD){
						getModeController().getMapController().setFolded(selected, true);
					}
					return (NodeModel) parentNode.getChildAt(index);
				}
				selected = parentNode;
			}
	}
	private NodeModel getPrevious(NodeModel selected) {
		for(;;){
			NodeModel parentNode = selected.getParentNode();
			if(parentNode == null){
				break;
			}
			if(direction == Direction.BACK_N_FOLD){
				getModeController().getMapController().setFolded(selected, true);
			}
			int index = parentNode.getIndex(selected)-1;
			if(index < 0){
				if(direction == Direction.BACK_N_FOLD){
					getModeController().getMapController().setFolded(parentNode, true);
				}
				return parentNode;
			}
			selected = (NodeModel) parentNode.getChildAt(index);
			break;
		}
		for(;;){
			if(selected.getChildCount() == 0){
				return selected;
			}
			selected = (NodeModel) selected.getChildAt(selected.getChildCount() - 1);
		}
	}
}
