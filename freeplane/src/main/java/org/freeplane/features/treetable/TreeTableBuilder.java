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
	static final String XML_TREE_TABLE = "tree_table";
	static final String XML_WRAP_TEXT = "WRAP_TEXT";

	@Override
	public Object createElement(final Object parent, final String tag, final XMLElement attributes) {
		if (XML_TREE_TABLE.equals(tag))
			return parent;
		return null;
	}

	@Override
	public void endElement(final Object parent, final String tag, final Object element, final XMLElement dom) {
		if (XML_TREE_TABLE.equals(tag) && element instanceof NodeModel)
			TreeTableModel.createFor((NodeModel) element)
				.setTextWrapped(Boolean.parseBoolean(dom.getAttribute(XML_WRAP_TEXT, "false")));
	}

	void registerBy(final ReadManager readManager, final WriteManager writeManager) {
		readManager.addElementHandler(XML_TREE_TABLE, this);
		writeManager.addExtensionElementWriter(TreeTableModel.class, new IExtensionElementWriter() {
			@Override
			public void writeContent(final ITreeWriter writer, final Object node, final IExtension extension)
					throws IOException {
				final XMLElement element = new XMLElement(XML_TREE_TABLE);
				if (((TreeTableModel) extension).isTextWrapped())
					element.setAttribute(XML_WRAP_TEXT, "true");
				writer.addElement(extension, element);
			}
		});
	}
}
