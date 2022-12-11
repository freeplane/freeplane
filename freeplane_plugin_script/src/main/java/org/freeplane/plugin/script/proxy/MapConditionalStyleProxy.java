package org.freeplane.plugin.script.proxy;

import org.freeplane.api.ConditionalStyleNotFoundException;
import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.styles.ConditionalStyleModel;
import org.freeplane.features.styles.IStyle;
import org.freeplane.features.styles.LogicalStyleController;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.features.styles.mindmapmode.MLogicalStyleController;
import org.freeplane.plugin.script.filter.ScriptCondition;

import static org.freeplane.plugin.script.proxy.NodeStyleProxy.styleByNameOrThrowException;

public class MapConditionalStyleProxy extends AConditionalStyleProxy<MapModel> {
	public MapConditionalStyleProxy(MapModel mapModel, ConditionalStyleModel.Item item) {
		super(mapModel, item);
	}

	public MapConditionalStyleProxy(MapModel mapModel, boolean isActive, String script, String styleName, boolean isLast) {
		super(mapModel, isActive, script, styleName, isLast);
	}

	public MapConditionalStyleProxy(MapModel mapModel, boolean isActive, ASelectableCondition condition, IStyle style, boolean isLast) {
		super(mapModel, isActive, condition, style, isLast);
	}

	@Override
	ConditionalStyleModel getConditionalStyleModel() {
		return MapStyleModel.getExtension(getDelegate()).getConditionalStyleModel();
	}

	@Override
	public void setActive(boolean isActive) {
		MLogicalStyleController controller = (MLogicalStyleController) LogicalStyleController.getController();
		controller.setActiveForConditionalStyle(getDelegate(), getConditionalStyleModel(), getIndex(), isActive);
		callDelayedRefresh(getDelegate());
	}

	@Override
	public void setScript(String script) {
		MLogicalStyleController controller = (MLogicalStyleController) LogicalStyleController.getController();
		ScriptCondition condition = script == null ? null : new ScriptCondition(script);
		controller.setConditionForConditionalStyle(getDelegate(), getConditionalStyleModel(), getIndex(), condition);
		callDelayedRefresh(getDelegate());
	}

	@Override
	public void setStyleName(String styleName) {
		MLogicalStyleController controller = (MLogicalStyleController) LogicalStyleController.getController();
		IStyle style = styleByNameOrThrowException(getDelegate(), styleName);
		controller.setStyleForConditionalStyle(getDelegate(), getConditionalStyleModel(), getIndex(), style);
		callDelayedRefresh(getDelegate());
	}

	@Override
	public void setLast(boolean isLast) {
		MLogicalStyleController controller = (MLogicalStyleController) LogicalStyleController.getController();
		controller.setLastForConditionalStyle(getDelegate(), getConditionalStyleModel(), getIndex(), isLast);
		callDelayedRefresh(getDelegate());
	}

	@Override
	public void moveTo(int toIndex) {
		int index = getIndex();
		if (index == -1)
			throw new ConditionalStyleNotFoundException(this.toString());
		if (toIndex == index)
			return;
		MLogicalStyleController.getController().moveConditionalStyle(getDelegate(), getConditionalStyleModel(), index, toIndex);
		callDelayedRefresh(getDelegate());
	}

	@Override
	public MapConditionalStyleProxy remove() {
		int index = getIndex();
		if (index == -1)
			throw new ConditionalStyleNotFoundException(this.toString());
		ConditionalStyleModel.Item item = MLogicalStyleController.getController().removeConditionalStyle(getDelegate(), getConditionalStyleModel(), index);
		callDelayedRefresh(getDelegate());
		return new MapConditionalStyleProxy(getDelegate(), item);
	}

	private void callDelayedRefresh(MapModel map) {
        LogicalStyleController.getController().refreshMapLaterUndoable(map);
	}
}
