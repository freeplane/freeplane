package org.freeplane.core.enumeration;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.IAttributeHandler;
import org.freeplane.core.io.IExtensionAttributeWriter;
import org.freeplane.core.io.ITreeWriter;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.features.map.NodeBuilder;
import org.freeplane.features.map.NodeModel;

public class NodeEnumerationAttributeHandler <T extends Enum<T> & DefaultValueSupplier<T> & IExtension> implements IAttributeHandler, IExtensionAttributeWriter{

	private final Class<T> enumClass;
	private final boolean onceForMap;
	public NodeEnumerationAttributeHandler(Class<T>  enumClass){
		this.enumClass = enumClass;
		this.onceForMap = enumClass.getAnnotation(OnceForMap.class) != null;

	}

	public void registerBy(ReadManager readManager, WriteManager writeManager) {
		final String attributeName = attributeName();
		readManager.addAttributeHandler(NodeBuilder.XML_NODE, attributeName, this);
		readManager.addAttributeHandler(NodeBuilder.XML_STYLENODE, attributeName, this);
		writeManager.addExtensionAttributeWriter(enumClass, this);
	}

	private String attributeName() {
		return enumClass.getSimpleName();
	}

	@Override
	public void setAttribute(Object object, String value) {
		NodeModel node = (NodeModel)object;
		if(! onceForMap || node.isRoot())
			node.addExtension(Enum.valueOf(enumClass, value));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void writeAttributes(ITreeWriter writer, Object userObject, IExtension extension) {
		T value = (T)extension;
		if(! value.equals(value.getDefaultValue()))
			writer.addAttribute(attributeName(), value.name());
	}
}
