package org.docear.plugin.bibtex;

import java.util.ArrayList;
import java.util.Map.Entry;

import net.sf.jabref.BibtexEntry;

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
	
	public Reference(BibtexEntry entry) { 
		JabRefAttributes jabRefAttributes = ReferencesController.getController().getJabRefAttributes();		
		
		attributes = new ArrayList<Reference.Item>();
		this.key = new Item(jabRefAttributes.getKeyAttribute(), entry.getCiteKey());		
		for (Entry<String, String> valueAttributes : jabRefAttributes.getValueAttributes().entrySet()) {
			attributes.add(new Item(valueAttributes.getKey(), entry.getField(valueAttributes.getValue())));
		}
	}

	public Item getKey() {
		return key;
	}
	
	public ArrayList<Item> getAttributes() {
		return attributes;
	}
}
