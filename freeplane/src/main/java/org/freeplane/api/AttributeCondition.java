package org.freeplane.api;

public interface AttributeCondition {
	boolean check(String attributeName, Object attributeValue);
}
