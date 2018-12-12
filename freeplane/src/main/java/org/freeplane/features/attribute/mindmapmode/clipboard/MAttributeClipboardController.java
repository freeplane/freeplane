package org.freeplane.features.attribute.mindmapmode.clipboard;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.StringReader;

import org.freeplane.core.io.xml.XMLLocalParserFactory;
import org.freeplane.core.util.TypeReference;
import org.freeplane.features.attribute.Attribute;
import org.freeplane.features.attribute.AttributeController;
import org.freeplane.features.attribute.AttributeSelection;
import org.freeplane.features.attribute.NodeAttribute;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.attribute.clipboard.AttributeClipboardController;
import org.freeplane.features.attribute.clipboard.AttributeTransferable;
import org.freeplane.features.attribute.mindmapmode.MAttributeController;
import org.freeplane.features.clipboard.mindmapmode.MClipboardController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.n3.nanoxml.IXMLParser;
import org.freeplane.n3.nanoxml.IXMLReader;
import org.freeplane.n3.nanoxml.StdXMLReader;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.n3.nanoxml.XMLException;

public class MAttributeClipboardController
extends AttributeClipboardController implements MClipboardController{
	private final MAttributeController attributeController;

	public MAttributeClipboardController(MAttributeController attributeController) {
		this.attributeController = attributeController;
	}
	@Override
	public boolean canPaste(Transferable t) {
		return t.isDataFlavorSupported(AttributeTransferable.attributesFlavor);
	}

	@Override
	public void paste(Transferable t) {
		try {
			final NodeModel target = Controller.getCurrentController().getSelection().getSelected();
			final String transferData = (String) t.getTransferData(AttributeTransferable.attributesFlavor);
			final IXMLParser parser = XMLLocalParserFactory.createLocalXMLParser();
			final IXMLReader xmlReader = new StdXMLReader(new StringReader(transferData));
			parser.setReader(xmlReader);
			while(! xmlReader.atEOF()) {
				final XMLElement storage = (XMLElement) parser.parse();
				String  name = storage.getAttribute("name", null);
				final String object = storage.getAttribute("object", null);
				Object value = TypeReference.create(object);
				attributeController.addAttribute(target, new Attribute(name, value));
			}

		}
		catch (UnsupportedFlavorException | IOException | XMLException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public boolean canCut() {
		return canCopy();
	}

	@Override
	public void cut() {
		copy();
		final AttributeSelection attributeSelection = AttributeController.getAttributeSelection();
		attributeSelection.nodeAttributeStream().forEach(this::delete);

	}

	private void delete(NodeAttribute nodeAttribute) {
		final NodeModel node = nodeAttribute.node;
		final NodeAttributeTableModel model = node.getExtension(NodeAttributeTableModel.class);
		final int attributeIndex = model.getAttributeIndex(nodeAttribute.attribute);
		attributeController.performRemoveRow(node, model, attributeIndex);

	}
}
