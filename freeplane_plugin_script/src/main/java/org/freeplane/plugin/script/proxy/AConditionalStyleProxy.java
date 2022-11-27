package org.freeplane.plugin.script.proxy;


import org.freeplane.features.filter.condition.ASelectableCondition;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.styles.ConditionalStyleModel;
import org.freeplane.features.styles.IStyle;
import org.freeplane.features.styles.StyleTranslatedObject;
import org.freeplane.plugin.script.filter.ScriptCondition;

import static java.util.Objects.requireNonNull;
import static org.freeplane.plugin.script.proxy.NodeStyleProxy.styleByNameOrThrowException;

public abstract class AConditionalStyleProxy<T> implements Proxy.ConditionalStyle {
	private final T delegate;
	private final ConditionalStyleModel.Item item;
	private static final String STYLE_MUST_NOT_BE_NULL = "style mustn't be null";

	AConditionalStyleProxy(T delegate, ConditionalStyleModel.Item item) {
		this.delegate = delegate;
		this.item = item;
	}

	AConditionalStyleProxy(T delegate, boolean isActive, String script, String styleName, boolean isLast) {
		this(delegate,
				isActive,
				script == null ? null : new ScriptCondition(script),
				styleByNameOrThrowException(delegate instanceof NodeModel ? ((NodeModel) delegate).getMap() : (MapModel) delegate, styleName),
				isLast);
	}

	AConditionalStyleProxy(T delegate, boolean isActive, ASelectableCondition condition, IStyle style, boolean isLast) {
		this(delegate, new ConditionalStyleModel.Item(isActive, condition, requireNonNull(style, STYLE_MUST_NOT_BE_NULL), isLast));
	}

	T getDelegate() {
		return delegate;
	}

	abstract ConditionalStyleModel getConditionalStyleModel();

	ConditionalStyleModel.Item getItem() {
		return item;
	}

	int getIndex() {
		return getConditionalStyleModel().getStyles().indexOf(item);
	}

	@Override
	public String getConditionClassSimpleName() {
		ASelectableCondition condition = item.getCondition();
		return condition == null ? null : condition.getClass().getSimpleName();
	}

	public IStyle getStyle() {
		return item.getStyle();
	}

	public ASelectableCondition getCondition() {
		return item.getCondition();
	}

	@Override
	public String getStyleName() {
		IStyle style = item.getStyle();
		return style == null ? null : StyleTranslatedObject.toKeyString(style);
	}

	@Override
	public String getScript() {
		if (hasScriptCondition())
			return ((ScriptCondition) item.getCondition()).getScript();
		else
			return null;
	}

	@Override
	public boolean isActive() {
		return item.isActive();
	}

	@Override
	public boolean isLast() {
		return item.isLast();
	}

	@Override
	public boolean isAlways() {
		return item.getCondition() == null;
	}

	@Override
	public boolean hasScriptCondition() {
		return item.getCondition() instanceof ScriptCondition;
	}

	@Override
	public String toString() {
		return "ConditionalStyle{" +
				(isActive() ? "active" : "inactive") +
				", " + (isAlways() ? "always" : getConditionClassSimpleName()) +
				", " + getStyleName() +
				", " + (isLast() ? "stop" : "continue") +
				'}';
	}
}
