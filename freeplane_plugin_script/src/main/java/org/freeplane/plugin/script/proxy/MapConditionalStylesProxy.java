package org.freeplane.plugin.script.proxy;

import org.freeplane.api.ConditionalStyle;
import org.freeplane.api.ConditionalStyleRO;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.styles.ConditionalStyleModel;
import org.freeplane.features.styles.LogicalStyleController;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.features.styles.mindmapmode.MLogicalStyleController;
import org.freeplane.plugin.script.ScriptContext;

import static java.util.Objects.requireNonNull;

public class MapConditionalStylesProxy extends AConditionalStylesProxy<MapModel> {
	MapConditionalStylesProxy(MapModel delegate, ScriptContext scriptContext) {
		super(delegate, scriptContext);
	}

	@Override
	MapConditionalStyleProxy createProxy(ConditionalStyleModel.Item item) {
		return new MapConditionalStyleProxy(getDelegate(), item);
	}

	@Override
	MapConditionalStyleProxy createProxy(boolean isActive, String script, String styleName, boolean isLast) {
		return new MapConditionalStyleProxy(getDelegate(), isActive, script, styleName, isLast);
	}

	@Override
	ConditionalStyleModel getConditionalStyleModel() {
		return MapStyleModel.getExtension(getDelegate()).getConditionalStyleModel();
	}

	@Override
	public void add(ConditionalStyleRO conditionalStyle) {
		MapConditionalStyleProxy cs = (MapConditionalStyleProxy) requireNonNull(conditionalStyle, CONDITIONAL_STYLE_MUST_NOT_BE_NULL);
		MLogicalStyleController controller = (MLogicalStyleController) LogicalStyleController.getController();
		controller.addConditionalStyle(getDelegate(), getConditionalStyleModel(), cs.isActive(), cs.getCondition(), cs.getStyle(), cs.isLast());
		callDelayedRefresh(getDelegate());
	}

	@Override
	public void insert(int index, ConditionalStyleRO conditionalStyle) {
		MapConditionalStyleProxy cs = (MapConditionalStyleProxy) requireNonNull(conditionalStyle, CONDITIONAL_STYLE_MUST_NOT_BE_NULL);
		MLogicalStyleController controller = (MLogicalStyleController) LogicalStyleController.getController();
		controller.insertConditionalStyle(getDelegate(), getConditionalStyleModel(), index, cs.isActive(), cs.getCondition(), cs.getStyle(), cs.isLast());
		callDelayedRefresh(getDelegate());
	}

	@Override
	public void move(int index, int toIndex) {
		MLogicalStyleController controller = (MLogicalStyleController) LogicalStyleController.getController();
		controller.moveConditionalStyle(getDelegate(), getConditionalStyleModel(), index, toIndex);
		callDelayedRefresh(getDelegate());
	}

	@Override
	public ConditionalStyle remove(int index) {
		MLogicalStyleController controller = (MLogicalStyleController) LogicalStyleController.getController();
		ConditionalStyleModel.Item item = controller.removeConditionalStyle(getConditionalStyleModel(), index);
		callDelayedRefresh(getDelegate());
		return new MapConditionalStyleProxy(getDelegate(), item);
	}

    private void callDelayedRefresh(MapModel map) {
        LogicalStyleController.getController().refreshMapLaterUndoable(map);
    }
}
