package org.freeplane.plugin.script;

class CachedResult {
	final Object returnedValue;
	final AccessedValues accessedValues;


	CachedResult(Object returnedValue, AccessedValues accessedValues) {
		this.returnedValue = returnedValue;
		this.accessedValues = accessedValues;
	}
}
