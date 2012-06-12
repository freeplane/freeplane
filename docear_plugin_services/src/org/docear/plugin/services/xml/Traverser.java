package org.docear.plugin.services.xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class Traverser {
	public enum TraversalMethod {
		BREADTH_FIRST, DEPTH_FIRST
	}

	private final TraversalMethod method;
	
	public Traverser(TraversalMethod method) {
		this.method = method;
	}
	
	abstract public boolean acceptElement(DocearXmlElement element, XmlPath path);
	
	public Collection<DocearXmlElement> traverse(DocearXmlElement element) {
		List<DocearXmlElement> acceptedElements = new ArrayList<DocearXmlElement>();		
		acceptedElements.addAll(traverse(element, null));
		return acceptedElements;
	}
	
	private Collection<DocearXmlElement> traverse(DocearXmlElement element, XmlPath path) {
		List<DocearXmlElement> acceptedElements = new ArrayList<DocearXmlElement>();
		int count = 0;
		if(this.method.equals(TraversalMethod.BREADTH_FIRST)) {
			for(DocearXmlElement child : element.getChildren()) {
				if(acceptElement(child, new XmlPath(path, child.getName()+"["+count+"]"))) {
					acceptedElements.add(child);
				}
				count++;
			}
			count = 0;
			for(DocearXmlElement child : element.getChildren()) {
				acceptedElements.addAll(traverse(child, new XmlPath(path, child.getName()+"["+count+"]")));
				count++;
			}			
		}
		else {
			for(DocearXmlElement child : element.getChildren()) {
				XmlPath nPath = new XmlPath(path, child.getName()+"["+count+"]");
				if(acceptElement(child, nPath)) {
					acceptedElements.add(child);
				}
				acceptedElements.addAll(traverse(child, nPath));
				count++;
			}
		}

		return acceptedElements;
	}

}
