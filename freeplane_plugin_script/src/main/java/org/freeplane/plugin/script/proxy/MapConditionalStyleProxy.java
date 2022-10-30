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

import static org.freeplane.plugin.script.proxy.NodeStyleProxy.styleByNameOrException;

public class MapConditionalStyleProxy extends AConditionalStyleProxy<MapModel> {
	public MapConditionalStyleProxy(MapModel delegate, ConditionalStyleModel.Item item) {
		super(delegate, item);
	}

	@Override
	ConditionalStyleModel getConditionalStyleModel() {
		return MapStyleModel.getExtension(getDelegate()).getConditionalStyleModel();
	}

	public MapConditionalStyleProxy(MapModel delegate, boolean isActive, String script, String styleName, boolean isLast) {
		super(delegate, isActive, script, styleName, isLast);
	}

	public MapConditionalStyleProxy(MapModel delegate, boolean isActive, ASelectableCondition condition, IStyle style, boolean isLast) {
		super(delegate, isActive, condition, style, isLast);
	}

	@Override
	public void setStyleName(String styleName) {
		IStyle iStyle = styleByNameOrException(getDelegate(), styleName);
		MLogicalStyleController controller = (MLogicalStyleController) LogicalStyleController.getController();
		ConditionalStyleModel.Item item = getItem();
		controller.modifyConditionalStyleItemAndRefreshMap(getDelegate(), item, iStyle, item.getCondition(), item.isActive(), item.isLast());
	}

	@Override
	public void setScript(String script) {
		ASelectableCondition condition = script != null ? new ScriptCondition(script) : null;
		MLogicalStyleController controller = (MLogicalStyleController) LogicalStyleController.getController();
		ConditionalStyleModel.Item item = getItem();
		controller.modifyConditionalStyleItemAndRefreshMap(getDelegate(), item, item.getStyle(), condition, item.isActive(), item.isLast());
	}

	@Override
	public void setActive(boolean isActive) {
		MLogicalStyleController controller = (MLogicalStyleController) LogicalStyleController.getController();
		ConditionalStyleModel.Item item = getItem();
		controller.modifyConditionalStyleItemAndRefreshMap(getDelegate(), item, item.getStyle(), item.getCondition(), isActive, item.isLast());
	}

	@Override
	public void setLast(boolean isLast) {
		MLogicalStyleController controller = (MLogicalStyleController) LogicalStyleController.getController();
		ConditionalStyleModel.Item item = getItem();
		controller.modifyConditionalStyleItemAndRefreshMap(getDelegate(), item, item.getStyle(), item.getCondition(), item.isActive(), isLast);
	}

	@Override
	public void moveTo(int toIndex) {
		int index = getIndex();
		if (index == -1)
			throw new ConditionalStyleNotFoundException(this.toString());
		if (toIndex == index)
			return;
		MLogicalStyleController.getController().moveConditionalStyleAndRefreshMap(getDelegate(), getConditionalStyleModel(), index, toIndex);
	}

	@Override
	public MapConditionalStyleProxy remove() {
		int index = getIndex();
		if (index == -1)
			throw new ConditionalStyleNotFoundException(this.toString());
		ConditionalStyleModel.Item item = MLogicalStyleController.getController().removeConditionalStyleAndRefreshMap(getDelegate(), getConditionalStyleModel(), index);
		return new MapConditionalStyleProxy(getDelegate(), item);
	}
}
