package org.freeplane.features.attribute.clipboard;

import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.io.StringWriter;
import java.util.stream.Collectors;

import org.freeplane.core.util.TypeReference;
import org.freeplane.features.attribute.AttributeSelection;
import org.freeplane.features.attribute.NodeAttribute;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.n3.nanoxml.XMLWriter;

public class AttributeClipboardController {
	public Transferable copy(final AttributeSelection selection) {
		String attributesContent = toAttributesContent(selection);
		String stringContent = toStringContent(selection);
		return new AttributeTransferable(attributesContent, stringContent);
	}

	private String toStringContent(AttributeSelection selection) {
		return selection.nodeAttributeStream().map(this::toStringContent)
		.collect(Collectors.joining("\n"));
	}

	private String toStringContent(NodeAttribute attribute) {
		return attribute.name() + '\t' + String.valueOf(attribute.value());
	}

	private String toAttributesContent(AttributeSelection selection) {
		StringWriter writer = new StringWriter();
		final XMLWriter xmlWriter = new XMLWriter(writer);
		selection.nodeAttributeStream().map(this::toAttributesContent)
				.forEach(element -> {
					try {
						xmlWriter.write(element);
					}
					catch (IOException e) {
						throw new RuntimeException(e);
					}
				});
		return writer.toString();
	}

	private XMLElement toAttributesContent(NodeAttribute attribute) {
		final XMLElement element = new XMLElement("attribute");
		element.setAttribute("name", attribute.name());
		element.setAttribute("name", attribute.name());
		Object value = attribute.value();
		if(value  instanceof String)
			element.setAttribute("value", value.toString());
		else
			element.setAttribute("object", TypeReference.toSpec(value));
		return element;
	}

}
