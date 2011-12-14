package org.docear.plugin.bibtex;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map.Entry;

import net.sf.jabref.BibtexEntry;

import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.workspace.WorkspaceUtils;

public class Reference {
	public class Item {
		private String name;
		private String value;
		
		public Item(String name, String value) {
			this.name = name;
			this.value = value;
		}

		public String getName() {
			return name;
		}

		public String getValue() {
			return value;
		}
	}
	
	private final Item key;
	private final ArrayList<Item> attributes;
	private URI uri = null;
	
	public Reference(BibtexEntry entry, NodeModel node) { 
		JabRefAttributes jabRefAttributes = ReferencesController.getController().getJabRefAttributes();		
		
		attributes = new ArrayList<Reference.Item>();
		this.key = new Item(jabRefAttributes.getKeyAttribute(), entry.getCiteKey());		
		for (Entry<String, String> valueAttributes : jabRefAttributes.getValueAttributes().entrySet()) {
			attributes.add(new Item(valueAttributes.getKey(), entry.getField(valueAttributes.getValue())));
		}
		
		boolean isFile = true;
		String url = entry.getField("file");
		if (url != null) {
			uri = jabRefAttributes.parsePath(entry, url.toString());
			if (uri != null) {
				url = WorkspaceUtils.resolveURI(uri, node.getMap()).getPath();
			}
			else {
				url = null;
			}
		}
		
		if (url == null) {
			isFile = false;
			url = entry.getField("url");
			if (url != null) {
				url = url.trim();
			}
			if (url != null && url.length()>0) {
				url = url.split(" ")[0];
			}
		}
		
		if (isFile) {
			uri = new File(url.trim()).toURI();
		}
		else {
			if (url != null) {
				try {
					try {
						uri = new URL(url).toURI();
					}			
					catch (MalformedURLException e) {				
						uri = new URL("http://"+url).toURI();
					}
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}			
		}
		
	}

	public Item getKey() {
		return key;
	}
	
	public ArrayList<Item> getAttributes() {
		return attributes;
	}

	public URI getUri() {
		return uri;
	}

	public void setUri(URI uri) {
		this.uri = uri;
	}

	
}
