package org.freeplane.plugin.script;

class CachedResult {
	final Object returnedValue;
	final RelatedElements relatedElements;


	CachedResult(Object returnedValue, RelatedElements relatedElements) {
		this.returnedValue = returnedValue;
		this.relatedElements = relatedElements;
	}
}
