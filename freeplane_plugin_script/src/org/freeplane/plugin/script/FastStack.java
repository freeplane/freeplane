package org.freeplane.plugin.script;

import java.util.ArrayList;

/** A minimal implementation of a stack - not threadsafe.
 * The stack may contains null but note that null is used by {@link #pop()} to signal an empty stack.
 * Note that Deque might be a better choice but Deque is only available since Java 1.6. */
public class FastStack<T> {
	private ArrayList<T> stack = new ArrayList<T>();

	/** creates an empty stack. */
	public FastStack() {
	}

	/** initializes the stack with a single element. */
	public FastStack(T t) {
		push(t);
	}

	public void push(T t) {
		stack.add(t);
	}

	public T last() {
		return stack.isEmpty() ? null : stack.get(stack.size() - 1);
	}

	/** returns the last element in the stack or null if it is empty. */
	public T pop() {
		return stack.isEmpty() ? null : stack.get(stack.size() - 1);
	}

	public String toString() {
		return stack.toString();
	}
}
