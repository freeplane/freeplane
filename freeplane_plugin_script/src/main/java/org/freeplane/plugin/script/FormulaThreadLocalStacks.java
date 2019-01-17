package org.freeplane.plugin.script;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.apache.commons.lang.StringUtils;
import org.freeplane.core.util.LogUtils;

public class FormulaThreadLocalStacks {

	private FormulaThreadLocalStacks() {

	}
	private final ThreadLocal<UniqueStack<NodeScript>> stack =
			new ThreadLocal<UniqueStack<NodeScript>>() {
		@Override protected UniqueStack<NodeScript> initialValue() {
			return new UniqueStack<NodeScript>();
		}
	};
	public static final FormulaThreadLocalStacks INSTANCE = new FormulaThreadLocalStacks();

	private UniqueStack<NodeScript> stack() {
		return stack.get();
	}

	public boolean push(NodeScript nodeScript) {
		final boolean success = stack().push(nodeScript);
		if (!success && ! ignoresCycles()) {
			LogUtils.warn("Circular reference detected! Traceback (innermost last):\n " //
			        + stackTrace(nodeScript));
		}
		return success;
	}

	public void pop() {
		stack().pop();
	}

	private String stackTrace(NodeScript nodeScript) {
		ArrayList<String> entries = new ArrayList<String>(stack().size());
		for (NodeScript node : stack()) {
			entries.add(node.format(nodeScript));
		}
		entries.add(nodeScript.format(nodeScript));
		return StringUtils.join(entries.iterator(), "\n -> ");
	}

	public List<NodeScript> findCycle(NodeScript nodeScript) {
		return stack().findCycle(nodeScript);
	}

	public <T> T ignoreCycles(Supplier<T> closure) {
		return stack().ignoreCycles(closure);
	}

	public boolean ignoresCycles() {
		return stack().ignoreCycles();
	}
}
