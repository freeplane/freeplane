package org.freeplane.plugin.script;

import org.freeplane.plugin.script.dependencies.RelatedElements;

class CachedResult {
	final Object returnedValue;
	final RelatedElements relatedElements;


	CachedResult(Object returnedValue, RelatedElements relatedElements) {
		this.returnedValue = returnedValue;
		this.relatedElements = relatedElements;
	}
}
