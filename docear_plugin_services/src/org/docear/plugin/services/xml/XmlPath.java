package org.docear.plugin.services.xml;

public class XmlPath {
	
	private final XmlPath parent;
	private final String name;
	
	public XmlPath(XmlPath parent, String name) {
		this.parent = parent;
		this.name = name;
	}
	
	public XmlPath(String name) {
		this(null, name);
	}
	
	public String toString() {
		if(parent == null) {
			return "/"+name;
		}
		return parent.toString()+"/"+name;
	}
	
	public int length() {
		if(parent == null) {
			return 0;
		}
		return parent.length() + 1;
	}
	
	

}
