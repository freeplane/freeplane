package org.freeplane.features.attribute.mindmapmode.clipboard;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.StringReader;

import org.freeplane.core.io.xml.XMLLocalParserFactory;
import org.freeplane.core.util.TypeReference;
import org.freeplane.features.attribute.Attribute;
import org.freeplane.features.attribute.clipboard.AttributeClipboardController;
import org.freeplane.features.attribute.clipboard.AttributeTransferable;
import org.freeplane.features.attribute.mindmapmode.MAttributeController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.n3.nanoxml.IXMLParser;
import org.freeplane.n3.nanoxml.IXMLReader;
import org.freeplane.n3.nanoxml.StdXMLReader;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.n3.nanoxml.XMLException;

public class MAttributeClipboardController extends AttributeClipboardController{
	private final MAttributeController attributeController;

	public MAttributeClipboardController(MAttributeController attributeController) {
		this.attributeController = attributeController;
	}
	public boolean canPaste(Transferable t) {
		return t.isDataFlavorSupported(AttributeTransferable.attributesFlavor);
	}

	public void paste(Transferable t, NodeModel target) {
		try {
			final String transferData = (String) t.getTransferData(AttributeTransferable.attributesFlavor);
			final IXMLParser parser = XMLLocalParserFactory.createLocalXMLParser();
			final IXMLReader xmlReader = new StdXMLReader(new StringReader(transferData));
			parser.setReader(xmlReader);
			while(! xmlReader.atEOF()) {
				final XMLElement storage = (XMLElement) parser.parse();
				String  name = storage.getAttribute("name", null);
				Object value = TypeReference.create(storage.getAttribute("object", null));
				attributeController.addAttribute(target, new Attribute(name, value));
			}

		}
		catch (UnsupportedFlavorException | IOException | XMLException e) {
			throw new IllegalArgumentException(e);
		}
	}
}
