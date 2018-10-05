package org.freeplane.plugin.script;

import java.util.*;

/** A minimal implementation of a stack that may contain an element only once - not threadsafe.
 * The stack may contains null but note that null is used by {@link #pop()} to signal an empty stack. */
public class UniqueStack<T> implements Iterable<T> {
	private final ArrayList<T> stack = new ArrayList<T>(8);
	private final HashSet<T> set = new HashSet<T>(8);

	/** creates an empty stack. */
	public UniqueStack() {
	}

	/** initializes the stack with a single element. */
	public UniqueStack(final T t) {
		push(t);
	}

	/** returns true only if the element was actually added. */
	public boolean push(final T t) {
		if (set.add(t)) {
			stack.add(t);
			return true;
		}
		return false;
	}

	/** returns the last element in the stack or null if it is empty. */
	public T pop() {
		if (stack.isEmpty()) {
			return null;
		}
		else {
			final T last = stack.remove(stack.size() - 1);
			set.remove(last);
			return last;
		}
	}

	public T first() {
		return stack.isEmpty() ? null : stack.get(0);
	}

	public T last() {
		return stack.isEmpty() ? null : stack.get(stack.size() - 1);
	}

	@Override
	public Iterator<T> iterator() {
		return stack.iterator();
	}

	public int size() {
		return stack.size();
	}

	@Override
	public String toString() {
		return stack.toString();
	}

	public List<T> findCycle(final T element) {
		if (set.contains(element)) {
			final int cycleBegin = stack.lastIndexOf(element);
			final ArrayList<T> cycle = new ArrayList<>(stack.size() - cycleBegin + 1);
			cycle.addAll(stack.subList(cycleBegin, stack.size()));
			cycle.add(element);
			return cycle;
		}
		else
			return Collections.emptyList();
	}
}
