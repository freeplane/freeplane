package org.freeplane.core.enumeration;

import java.awt.event.ActionEvent;
import java.util.List;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.ui.AMultipleNodeAction;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.mode.Controller;

@SelectableAction(checkOnPopup=true)
public class NodeEnumerationAction <T extends Enum<T> & IExtension> extends AMultipleNodeAction{

	private static final long serialVersionUID = 1L;
	private final T value;

	public NodeEnumerationAction(T value) {
		super("NodeEnumerationAction." + value.getClass().getSimpleName() + '.' + value.name());
		this.value = value;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		setSelected();
		if(isEnabled())
			super.actionPerformed(e);
	}

	@Override
	protected void actionPerformed(ActionEvent e, NodeModel node) {
		MMapController controller =  (MMapController) Controller.getCurrentModeController().getMapController();
		if(isSelected())
			controller.removeProperty(node, getValueClass());
		else
			controller.setProperty(node, value);
	}

	@Override
	public void setEnabled() {
		final List<NodeModel> nodes = getNodes();
		setEnabled(!nodes.isEmpty());
	}

	@Override
	public void setSelected() {
		final List<NodeModel> nodes = getNodes();
		final boolean selected = ! nodes.isEmpty() && nodes.get(0).getExtension(getValueClass()) == value;
		setSelected(selected);
	}

	protected Class<T> getValueClass() {
		return (Class<T>) value.getClass();
	}


}
