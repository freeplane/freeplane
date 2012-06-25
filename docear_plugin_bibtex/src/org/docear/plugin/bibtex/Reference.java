package org.docear.plugin.bibtex;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import net.sf.jabref.BibtexEntry;
import net.sf.jabref.GUIGlobals;
import net.sf.jabref.gui.FileListTableModel;

import org.docear.plugin.bibtex.jabref.JabRefAttributes;
import org.freeplane.features.map.NodeModel;

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
	private List<URI> uris = new ArrayList<URI>();
	
	public Reference(BibtexEntry entry, NodeModel node) {		
		JabRefAttributes jabRefAttributes = ReferencesController.getController().getJabRefAttributes();		
		
		attributes = new ArrayList<Reference.Item>();
		
		if (entry.getCiteKey() == null) {
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
		
//		boolean isFile = true;
//		String url = entry.getField("file");
//		if (url != null) {
//			ArrayList<URI> uris = jabRefAttributes.parsePaths(entry, url.toString());
//			if (uris.size() > 0) {
//				uri = jabRefAttributes.parsePaths(entry, url.toString()).get(0);
//				if (uri != null) {
//					url = WorkspaceUtils.resolveURI(uri, node.getMap()).getPath();
//				}
//				else {
//					url = null;
//				}
//			}
//		}
//		
//		if (url == null) {
//			isFile = false;
//			url = entry.getField("url");
//			if (url != null) {
//				url = url.trim();
//			}
//			if (url != null && url.length()>0) {
//				url = url.split(" ")[0];
//			}
//		}
//		
//		if (isFile) {
//			uri = new File(url.trim()).toURI();
//		}
//		else {
//			if (url != null) {
//				try {
//					try {
//						uri = new URL(url).toURI();
//					}			
//					catch (MalformedURLException e) {				
//						uri = new URL("http://"+url).toURI();
//					}
//				}
//				catch(Exception e) {
//					e.printStackTrace();
//				}
//			}			
//		}
		
	}

	public Item getKey() {
		return key;
	}
	
	public ArrayList<Item> getAttributes() {
		return attributes;
	}

	public List<URI> getUris() {
		return uris;
	}

	public void addUri(URI uri) {
		this.uris.add(uri);
	}

	
}
