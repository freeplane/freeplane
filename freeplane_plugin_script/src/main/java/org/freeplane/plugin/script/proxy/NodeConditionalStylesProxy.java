package org.freeplane.plugin.script.proxy;

import org.freeplane.api.ConditionalStyle;
import org.freeplane.api.ConditionalStyleRO;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.styles.ConditionalStyleModel;
import org.freeplane.features.styles.LogicalStyleController;
import org.freeplane.features.styles.mindmapmode.MLogicalStyleController;
import org.freeplane.plugin.script.ScriptContext;

import static java.util.Objects.requireNonNull;

public class NodeConditionalStylesProxy extends AConditionalStylesProxy<NodeModel> {
	NodeConditionalStylesProxy(NodeModel delegate, ScriptContext scriptContext) {
		super(delegate, scriptContext);
	}

	@Override
	NodeConditionalStyleProxy createProxy(ConditionalStyleModel.Item item) {
		return new NodeConditionalStyleProxy(getDelegate(), item);
	}

	@Override
	NodeConditionalStyleProxy createProxy(boolean isActive, String script, String styleName, boolean isLast) {
		return new NodeConditionalStyleProxy(getDelegate(), isActive, script, styleName, isLast);
	}

	@Override
	ConditionalStyleModel getConditionalStyleModel() {
		return ConditionalStyleModel.createConditionalStyleModel(getDelegate());
	}

	@Override
	public void add(ConditionalStyleRO conditionalStyle) {
		NodeConditionalStyleProxy cs = (NodeConditionalStyleProxy) requireNonNull(conditionalStyle, CONDITIONAL_STYLE_MUST_NOT_BE_NULL);
		MLogicalStyleController controller = (MLogicalStyleController) LogicalStyleController.getController();
		controller.addConditionalStyle(getDelegate().getMap(), getConditionalStyleModel(), cs.isActive(), cs.getCondition(), cs.getStyle(), cs.isLast());
		callDelayedRefresh(getDelegate());
	}

	@Override
	public void insert(int index, ConditionalStyleRO conditionalStyle) {
		NodeConditionalStyleProxy cs = (NodeConditionalStyleProxy) requireNonNull(conditionalStyle, CONDITIONAL_STYLE_MUST_NOT_BE_NULL);
		MLogicalStyleController controller = (MLogicalStyleController) LogicalStyleController.getController();
		controller.insertConditionalStyle(getDelegate().getMap(), getConditionalStyleModel(), index, cs.isActive(), cs.getCondition(), cs.getStyle(), cs.isLast());
		callDelayedRefresh(getDelegate());
	}

	@Override
	public void move(int index, int toIndex) {
		MLogicalStyleController controller = (MLogicalStyleController) LogicalStyleController.getController();
		controller.moveConditionalStyle(getDelegate().getMap(), getConditionalStyleModel(), index, toIndex);
		callDelayedRefresh(getDelegate());
	}

	@Override
	public ConditionalStyle remove(int index) {
		MLogicalStyleController controller = (MLogicalStyleController) LogicalStyleController.getController();
		ConditionalStyleModel.Item item = controller.removeConditionalStyle(getDelegate().getMap(), getConditionalStyleModel(), index);
		callDelayedRefresh(getDelegate());
		return new NodeConditionalStyleProxy(getDelegate(), item);
	}

    private void callDelayedRefresh(NodeModel nodeModel) {
        Controller.getCurrentModeController().getMapController()
            .refreshNodeLaterUndoable(nodeModel, NodeModel.UNKNOWN_PROPERTY, null, null);
    }
}
