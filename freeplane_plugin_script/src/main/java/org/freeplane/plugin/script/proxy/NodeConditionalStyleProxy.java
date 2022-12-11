package org.freeplane.plugin.script.proxy;

import org.freeplane.api.ConditionalStyleNotFoundException;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.styles.ConditionalStyleModel;
import org.freeplane.features.styles.IStyle;
import org.freeplane.features.styles.LogicalStyleController;
import org.freeplane.features.styles.mindmapmode.MLogicalStyleController;
import org.freeplane.plugin.script.filter.ScriptCondition;

import static org.freeplane.plugin.script.proxy.NodeStyleProxy.styleByNameOrThrowException;

public class NodeConditionalStyleProxy extends AConditionalStyleProxy<NodeModel> {
	public NodeConditionalStyleProxy(NodeModel nodeModel, ConditionalStyleModel.Item item) {
		super(nodeModel, item);
	}

	public NodeConditionalStyleProxy(NodeModel nodeModel, boolean isActive, String script, String styleName, boolean isLast) {
		super(nodeModel, isActive, script, styleName, isLast);
	}

	public NodeConditionalStyleProxy(NodeModel nodeModel, boolean isActive, ASelectableCondition condition, IStyle style, boolean isLast) {
		super(nodeModel, isActive, condition, style, isLast);
	}

	@Override
	ConditionalStyleModel getConditionalStyleModel() {
		return ConditionalStyleModel.createConditionalStyleModel(getDelegate());
	}

	@Override
	public void setActive(boolean isActive) {
		MLogicalStyleController controller = (MLogicalStyleController) LogicalStyleController.getController();
		controller.setActiveForConditionalStyle(getDelegate().getMap(), getConditionalStyleModel(), getIndex(), isActive);
		callDelayedRefresh(getDelegate());
	}

	@Override
	public void setScript(String script) {
		MLogicalStyleController controller = (MLogicalStyleController) LogicalStyleController.getController();
		ScriptCondition condition = script == null ? null : new ScriptCondition(script);
		controller.setConditionForConditionalStyle(getDelegate().getMap(), getConditionalStyleModel(), getIndex(), condition);
		callDelayedRefresh(getDelegate());
	}

	@Override
	public void setStyleName(String styleName) {
		MLogicalStyleController controller = (MLogicalStyleController) LogicalStyleController.getController();
		IStyle style = styleByNameOrThrowException(getDelegate().getMap(), styleName);
		controller.setStyleForConditionalStyle(getDelegate().getMap(), getConditionalStyleModel(), getIndex(), style);
		callDelayedRefresh(getDelegate());
	}

	@Override
	public void setLast(boolean isLast) {
		MLogicalStyleController controller = (MLogicalStyleController) LogicalStyleController.getController();
		controller.setLastForConditionalStyle(getDelegate().getMap(), getConditionalStyleModel(), getIndex(), isLast);
		callDelayedRefresh(getDelegate());
	}

	@Override
	public void moveTo(int toIndex) {
		int index = getIndex();
		if (index == -1)
			throw new ConditionalStyleNotFoundException(this.toString());
		if (toIndex == index)
			return;
		MLogicalStyleController.getController().moveConditionalStyle(getDelegate().getMap(), getConditionalStyleModel(), index, toIndex);
		callDelayedRefresh(getDelegate());
	}

	@Override
	public NodeConditionalStyleProxy remove() {
		int index = getIndex();
		if (index == -1)
			throw new ConditionalStyleNotFoundException(this.toString());
		ConditionalStyleModel.Item item = MLogicalStyleController.getController().removeConditionalStyle(getDelegate().getMap(), getConditionalStyleModel(), index);
		callDelayedRefresh(getDelegate());
		return new NodeConditionalStyleProxy(getDelegate(), item);
	}

	private void callDelayedRefresh(NodeModel nodeModel) {
	    Controller.getCurrentModeController().getMapController()
	        .refreshNodeLaterUndoable(nodeModel, NodeModel.UNKNOWN_PROPERTY, null, null);
	}
}
