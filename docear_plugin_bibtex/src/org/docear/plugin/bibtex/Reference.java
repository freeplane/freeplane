package org.docear.plugin.bibtex;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.jabref.BibtexEntry;
import net.sf.jabref.GUIGlobals;
import net.sf.jabref.gui.FileListTableModel;

import org.docear.plugin.bibtex.jabref.JabRefAttributes;

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
	private Set<URI> uris = new HashSet<URI>();
	
	public Reference(BibtexEntry entry) {		
		JabRefAttributes jabRefAttributes = ReferencesController.getController().getJabRefAttributes();		
		
		attributes = new ArrayList<Reference.Item>();
		
		if (entry.getCiteKey() == null || entry.getCiteKey().trim().length() == 0) {
			jabRefAttributes.generateBibtexEntry(entry);			
		}
		
		this.key = new Item(jabRefAttributes.getKeyAttribute(), entry.getCiteKey());		
		for (Entry<String, String> valueAttributes : jabRefAttributes.getValueAttributes().entrySet()) {
			attributes.add(new Item(valueAttributes.getKey(), entry.getField(valueAttributes.getValue())));
		}
		
		String fileField = entry.getField(GUIGlobals.FILE_FIELD);
		if (fileField != null) {
			FileListTableModel model = new FileListTableModel();
			model.setContent(fileField);
			
			for (int i=0; i<model.getRowCount(); i++) {
				uris.add(new File(model.getEntry(i).getLink()).toURI());
			}
		}
	}

	public Item getKey() {
		return key;
	}
	
	public ArrayList<Item> getAttributes() {
		return attributes;
	}

	public Set<URI> getUris() {
		return uris;
	}

	public void addUri(URI uri) {
		this.uris.add(uri);
	}
	
	public boolean containsFileName(String name) {
		for (URI uri : getUris()) {
			if (name.equals(new File(uri).getName())) {
				return true;
			}			
		}
		return false;
	}

	
}
