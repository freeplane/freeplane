package org.freeplane.plugin.formula;

public class NullObject {

public static NullObject INSTANCE = new NullObject();

	private NullObject() {
	}

	private static final String MESSAGE = "NULL returned";

	@Override
	public String toString() {
		return MESSAGE;
	}
}
