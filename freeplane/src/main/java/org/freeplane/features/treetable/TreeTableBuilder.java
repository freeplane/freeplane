package org.freeplane.features.treetable;

import java.io.IOException;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.IElementDOMHandler;
import org.freeplane.core.io.IExtensionElementWriter;
import org.freeplane.core.io.ITreeWriter;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.features.map.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;

class TreeTableBuilder implements IElementDOMHandler {

	@Override
	public Object createElement(final Object parent, final String tag, final XMLElement attributes) {
		if (TreeTableColumnsXml.XML_TREE_TABLE.equals(tag))
			return parent;
		return null;
	}

	@Override
	public void endElement(final Object parent, final String tag, final Object element, final XMLElement dom) {
		if (!TreeTableColumnsXml.XML_TREE_TABLE.equals(tag) || !(element instanceof NodeModel))
			return;
		final NodeModel node = (NodeModel) element;
		TreeTableModel.createFor(node).setColumns(TreeTableColumnsXml.fromElement(dom));
	}

	void registerBy(final ReadManager readManager, final WriteManager writeManager) {
		readManager.addElementHandler(TreeTableColumnsXml.XML_TREE_TABLE, this);
		writeManager.addExtensionElementWriter(TreeTableModel.class, new IExtensionElementWriter() {
			@Override
			public void writeContent(final ITreeWriter writer, final Object node, final IExtension extension)
					throws IOException {
				final TreeTableModel model = (TreeTableModel) extension;
				writer.addElement(model, TreeTableColumnsXml.toElement(model.getColumns()));
			}
		});
	}
}
