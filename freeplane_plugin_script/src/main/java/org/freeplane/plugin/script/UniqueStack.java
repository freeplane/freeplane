package org.freeplane.plugin.script;

import java.util.*;
import java.util.function.Supplier;

/** A minimal implementation of a stack that may contain an element only once - not threadsafe.
 * The stack may contains null but note that null is used by {@link #pop()} to signal an empty stack. */
public class UniqueStack implements Iterable<NodeScript> {
	private final ArrayList<NodeScript> nodeScriptStack = new ArrayList<>(8);
	private final ArrayList<ScriptContext> scriptContextStack = new ArrayList<>(8);
	private final HashSet<NodeScript> set = new HashSet<>(8);
	private boolean ignoreCycles;

	/** creates an empty stack. */
	UniqueStack() {
	}

	/** returns true only if the element was actually added. */
	boolean push(final ScriptContext scriptContext) {
		final NodeScript nodeScript = scriptContext.getNodeScript();
		if (set.add(nodeScript)) {
			nodeScriptStack.add(nodeScript);
			scriptContextStack.add(scriptContext);
			return true;
		}
		return false;
	}

	/** returns the last element in the stack or null if it is empty. */
	void pop() {
		if (!scriptContextStack.isEmpty()) {
			scriptContextStack.remove(scriptContextStack.size() - 1);
			final NodeScript last = nodeScriptStack.remove(nodeScriptStack.size() - 1);
			set.remove(last);
		}
	}

	ScriptContext getCurrentContext() {
		return scriptContextStack.isEmpty() ? null : scriptContextStack.get(scriptContextStack.size() - 1);
	}

	@Override
	public Iterator<NodeScript> iterator() {
		return nodeScriptStack.iterator();
	}

	int size() {
		return nodeScriptStack.size();
	}

	@Override
	public String toString() {
		return nodeScriptStack.toString();
	}

	List<NodeScript> findCycle(final NodeScript element) {
		if (set.contains(element)) {
			final int cycleBegin = nodeScriptStack.lastIndexOf(element);
			final ArrayList<NodeScript> cycle = new ArrayList<>(nodeScriptStack.size() - cycleBegin + 1);
			cycle.addAll(nodeScriptStack.subList(cycleBegin, nodeScriptStack.size()));
			cycle.add(element);
			return cycle;
		}
		else
			return Collections.emptyList();
	}

	<V> V ignoreCycles(final Supplier<V> closure) {
		final boolean oldSuppressWarningsOnCyclicDependencies = ignoreCycles;
		ignoreCycles = true;
		try {
			return closure.get();
		}
		finally {
			ignoreCycles = oldSuppressWarningsOnCyclicDependencies;
		}
	}

	boolean ignoreCycles() {
		return ignoreCycles;
	}
}
