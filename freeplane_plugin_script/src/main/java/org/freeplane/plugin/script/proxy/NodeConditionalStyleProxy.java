package org.freeplane.plugin.script.proxy;

import org.freeplane.api.ConditionalStyleNotFoundException;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.map.NodeModel;
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

	@Override
	ConditionalStyleModel getConditionalStyleModel() {
		return ConditionalStyleModel.getExtension(getDelegate());
	}

	public NodeConditionalStyleProxy(NodeModel nodeModel, boolean isActive, String script, String styleName, boolean isLast) {
		super(nodeModel, isActive, script, styleName, isLast);
	}

	public NodeConditionalStyleProxy(NodeModel nodeModel, boolean isActive, ASelectableCondition condition, IStyle style, boolean isLast) {
		super(nodeModel, isActive, condition, style, isLast);
	}

	@Override
	public void setStyleName(String styleName) {
		IStyle iStyle = styleByNameOrThrowException(getDelegate().getMap(), styleName);
		MLogicalStyleController controller = (MLogicalStyleController) LogicalStyleController.getController();
		ConditionalStyleModel.Item item = getItem();
		controller.modifyConditionalStyleAndCallNodeChanged(getDelegate(), item, iStyle, item.getCondition(), item.isActive(), item.isLast());
	}

	@Override
	public void setScript(String script) {
		ASelectableCondition condition = script != null ? new ScriptCondition(script) : null;
		MLogicalStyleController controller = (MLogicalStyleController) LogicalStyleController.getController();
		ConditionalStyleModel.Item item = getItem();
		controller.modifyConditionalStyleAndCallNodeChanged(getDelegate(), item, item.getStyle(), condition, item.isActive(), item.isLast());
	}

	@Override
	public void setActive(boolean isActive) {
		MLogicalStyleController controller = (MLogicalStyleController) LogicalStyleController.getController();
		ConditionalStyleModel.Item item = getItem();
		controller.modifyConditionalStyleAndCallNodeChanged(getDelegate(), item, item.getStyle(), item.getCondition(), isActive, item.isLast());
	}

	@Override
	public void setLast(boolean isLast) {
		MLogicalStyleController controller = (MLogicalStyleController) LogicalStyleController.getController();
		ConditionalStyleModel.Item item = getItem();
		controller.modifyConditionalStyleAndCallNodeChanged(getDelegate(), item, item.getStyle(), item.getCondition(), item.isActive(), isLast);
	}

	@Override
	public void moveTo(int toIndex) {
		int index = getIndex();
		if (index == -1)
			throw new ConditionalStyleNotFoundException(this.toString());
		if (toIndex == index)
			return;
		MLogicalStyleController.getController().moveConditionalStyleAndCallNodeChanged(getDelegate(), getConditionalStyleModel(), index, toIndex);
	}

	@Override
	public NodeConditionalStyleProxy remove() {
		int index = getIndex();
		if (index == -1)
			throw new ConditionalStyleNotFoundException(this.toString());
		ConditionalStyleModel.Item item = MLogicalStyleController.getController().removeConditionalStyleAndCallNodeChanged(getDelegate(), getConditionalStyleModel(), index);
		return new NodeConditionalStyleProxy(getDelegate(), item);
	}
}
