package org.freeplane.plugin.script.proxy;

import org.freeplane.api.ConditionalStyle;
import org.freeplane.features.styles.ConditionalStyleModel;
import org.freeplane.plugin.script.ScriptContext;

import java.util.Iterator;

public abstract class AConditionalStylesProxy<T> extends AbstractProxy<T> implements Proxy.ConditionalStyles, Iterable<ConditionalStyle> {
	static final String CONDITIONAL_STYLE_MUST_NOT_BE_NULL = "ConditionalStyle mustn't be null";

	AConditionalStylesProxy(T delegate, ScriptContext scriptContext) {
		super(delegate, scriptContext);
	}

	abstract AConditionalStyleProxy<T> createProxy(ConditionalStyleModel.Item item);

	abstract AConditionalStyleProxy<T> createProxy(boolean isActive, String script, String styleName, boolean isLast);

	abstract ConditionalStyleModel getConditionalStyleModel();

	public void add(boolean isActive, String script, String styleName, boolean isLast) {
		add(createProxy(isActive, script, styleName, isLast));
	}

	public void insert(int index, boolean isActive, String script, String styleName, boolean isLast) {
		insert(index, createProxy(isActive, script, styleName, isLast));
	}

	@Override
	public Iterator<ConditionalStyle> iterator() {
		Iterator<ConditionalStyleModel.Item> stylesIterator = getConditionalStyleModel().iterator();
		return new Iterator<ConditionalStyle>() {
			@Override
			public boolean hasNext() {
				return stylesIterator.hasNext();
			}

			@Override
			public ConditionalStyle next() {
				return createProxy(stylesIterator.next());
			}

			@Override
			public void remove() {
				stylesIterator.remove();
			}
		};
	}
}
