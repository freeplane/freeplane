package org.docear.plugin.services.xml;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.docear.plugin.services.xml.Traverser.TraversalMethod;

public class DocearXmlElement {

	private URI href;
	private final String name;
	private String content;
	
	private final Map<String, String> attributes = new HashMap<String, String>();
	private final List<DocearXmlElement> children = new ArrayList<DocearXmlElement>();
	private DocearXmlElement parent;
	
	public DocearXmlElement(String name) {
		this.name = name;
	}
	
	public void addChild(DocearXmlElement child) {
		if(child != null) {
			children.add(child);
			child.setParent(this);
		}
	}
	
	private void setParent(DocearXmlElement element) {
		this.parent = element;
	}
	
	public DocearXmlElement getParent() {
		return parent;
	}

	public void removeChild(DocearXmlElement child) {
		if(child != null) {
			children.remove(child);
		}
	}
	
	public List<DocearXmlElement> getChildren() {
		return children;
	}
	
	public boolean hasChildren() {
		return childCount() > 0;
	}
	
	public int childCount() {
		return children.size();
	}
	
	public void setAttribute(String name, String value) {
		attributes.put(name, value);
	}
	
	public String getAttributeValue(String name) {
		return attributes.get(name);
	}
	
	public boolean hasAttributes() {
		return attributes.size() > 0;
	}
	
	public Map<String, String> getAttributes() {
		return attributes;
	}
	
	public URI getHref() {
		return href;
	}

	public void setHref(URI href) {
		this.href = href;
	}

	public String getName() {
		return name;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	public String toString() {
		return "["+name+":children="+childCount()+";attr=]";
	}
	
	public Collection<DocearXmlElement> findAll(final String elementName) {
		return new  Traverser(TraversalMethod.DEPTH_FIRST) {
			public boolean acceptElement(DocearXmlElement element, XmlPath path) {
				if(element.getName().equals(elementName)) {
					return true;
				}
				return false;
			}
		}.traverse(this);
		
	}

	public DocearXmlElement find(final String elementName) {
		Collection<DocearXmlElement> results = new  Traverser(TraversalMethod.DEPTH_FIRST) {
			public boolean acceptElement(DocearXmlElement element, XmlPath path) {
				if(element.getName().equals(elementName)) {
					return true;
				}
				return false;
			}
		}.traverse(this);
		
		if(results.size() == 0) {
			return null;
		}
		return results.iterator().next();
	}
}
