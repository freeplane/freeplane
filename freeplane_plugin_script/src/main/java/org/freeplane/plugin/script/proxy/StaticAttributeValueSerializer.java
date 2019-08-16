package org.freeplane.plugin.script.proxy;

import org.freeplane.api.AttributeValueSerializer;
import org.freeplane.core.util.TypeReference;


class StaticAttributeValueSerializer implements AttributeValueSerializer{
	final static AttributeValueSerializer INSTANCE = new StaticAttributeValueSerializer();
	
	private StaticAttributeValueSerializer() {};
	
	@Override
	public String serialize(Object value) {
		return TypeReference.toSpec(value);
	}

}
