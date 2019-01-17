package org.freeplane.plugin.script;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.apache.commons.lang.StringUtils;
import org.freeplane.core.util.LogUtils;

public class FormulaThreadLocalStacks {

	private FormulaThreadLocalStacks() {

	}

	private final ThreadLocal<UniqueStack> stack = new ThreadLocal<UniqueStack>() {
		@Override
		protected UniqueStack initialValue() {
			return new UniqueStack();
		}
	};
	public static final FormulaThreadLocalStacks INSTANCE = new FormulaThreadLocalStacks();

	private UniqueStack stack() {
		return stack.get();
	}

	public ScriptContext getCurrentContext() {
		return stack().getCurrentContext();
	}

	public boolean push(final ScriptContext scriptContext) {
		final boolean success = stack().push(scriptContext);
		if (!success && ! ignoresCycles()) {
			LogUtils.warn("Circular reference detected! Traceback (innermost last):\n " //
					+ stackTrace(scriptContext.getNodeScript()));
		}
		return success;
	}

	public void pop() {
		stack().pop();
	}

	private String stackTrace(final NodeScript nodeScript) {
		final ArrayList<String> entries = new ArrayList<String>(stack().size());
		for (final NodeScript node : stack()) {
			entries.add(node.format(nodeScript));
		}
		entries.add(nodeScript.format(nodeScript));
		return StringUtils.join(entries.iterator(), "\n -> ");
	}

	public List<NodeScript> findCycle(final NodeScript nodeScript) {
		return stack().findCycle(nodeScript);
	}

	public <T> T ignoreCycles(final Supplier<T> closure) {
		return stack().ignoreCycles(closure);
	}

	public boolean ignoresCycles() {
		return stack().ignoreCycles();
	}
}
