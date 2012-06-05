package org.docear.plugin.services.xml;

import java.io.Reader;

import org.freeplane.n3.nanoxml.IXMLBuilder;

public class DocearXmlBuilder implements IXMLBuilder {
	
	private DocearXmlElement currentElement = null;

	public void startBuilding(String systemID, int lineNr) throws Exception { 
		currentElement = new DocearXmlRootElement();
	}
	
	public void newProcessingInstruction(String target, Reader reader) throws Exception {		
	}	

	public void startElement(String name, String nsPrefix, String nsSystemID, String systemID, int lineNr) throws Exception {
		DocearXmlElement element = new DocearXmlElement(name);
		if(currentElement != null) {
			currentElement.addChild(element);
		}
		
		currentElement = element;
	}
	
	public void addAttribute(String key, String nsPrefix, String nsSystemID, String value, String type) throws Exception {
		currentElement.setAttribute(key, value);		
	}

	public void addPCData(Reader reader, String systemID, int lineNr) throws Exception {
		if (currentElement != null) {
			int ch = -1;
			StringBuffer buffer = new StringBuffer();
			while ((ch = reader.read()) != -1) {
				buffer.append((char)ch);				
			}
			currentElement.setContent(buffer.toString());
		}		
	}

	public void elementAttributesProcessed(String arg0, String arg1, String arg2) throws Exception {		
	}

	public void endElement(String name, String nsPrefix, String nsSystemID) throws Exception {
		currentElement = currentElement.getParent();		
	}

	public Object getResult() throws Exception {
		return null;
	}
	
	public DocearXmlElement getRoot() {
		return currentElement;
	}
}
