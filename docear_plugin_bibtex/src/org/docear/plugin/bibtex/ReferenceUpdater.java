package org.docear.plugin.bibtex;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.jabref.BibtexDatabase;
import net.sf.jabref.BibtexEntry;
import net.sf.jabref.GUIGlobals;
import net.sf.jabref.Globals;
import net.sf.jabref.export.DocearReferenceUpdateController;
import net.sf.jabref.labelPattern.LabelPatternUtil;

import org.docear.plugin.bibtex.jabref.JabRefAttributes;
import org.docear.plugin.bibtex.jabref.ResolveDuplicateEntryAbortedException;
import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.features.MapModificationSession;
import org.docear.plugin.core.mindmap.AMindmapUpdater;
import org.docear.plugin.core.util.Tools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.workspace.WorkspaceUtils;

public class ReferenceUpdater extends AMindmapUpdater {

	private final HashMap<BibtexEntry, Set<NodeModel>> referenceNodes;
	private final HashMap<String, BibtexEntry> pdfReferences;
	private final HashMap<String, BibtexEntry> urlReferences;

	private JabRefAttributes jabRefAttributes;
	private BibtexDatabase database;
	
	private long time = 0;

	public ReferenceUpdater(String title) {
		super(title);		
		time = System.currentTimeMillis();
		referenceNodes = new HashMap<BibtexEntry, Set<NodeModel>>();
		pdfReferences = new HashMap<String, BibtexEntry>();
		urlReferences = new HashMap<String, BibtexEntry>();
	}

	public boolean updateMindmap(MapModel map) {
		System.out.println("REferenceupdater");
		if (DocearController.getController().getSemaphoreController().isLocked("MindmapUpdate")) {
			return false;
		}
		if(DocearReferenceUpdateController.isLocked()) {
			return false;
		}
		try {
			System.out.println("start REferenceupdater");
    		DocearReferenceUpdateController.lock();    		
    		DocearController.getController().getSemaphoreController().lock("MindmapUpdate");
    		
    		jabRefAttributes = ReferencesController.getController().getJabRefAttributes();
    		database = ReferencesController.getController().getJabrefWrapper().getDatabase();
    		if (database == null) {
    			return false;
    		}
    		if (this.pdfReferences.size() == 0) {
    			buildPdfIndex();
    		}
    		if (this.urlReferences.size() == 0) {
    			buildUrlIndex();
    		}    		
    		return updateMap(map);
		}
		finally {
			DocearController.getController().getSemaphoreController().unlock("MindmapUpdate");
			DocearReferenceUpdateController.unlock();
			System.out.println("Referenceupdater: done: "+(System.currentTimeMillis()-time));
		}
		
	}

	private boolean updateMap(MapModel map) {
		referenceNodes.clear();
		buildIndex(map.getRootNode());
		System.out.println("ReferenceUpdater: buildIndex for nodes: "+(System.currentTimeMillis()-time));

		return updateReferenceNodes();
	}

	private void buildPdfIndex() {
		if (getSessionObject(MapModificationSession.FILE_IGNORE_LIST) == null) {
			putSessionObject(MapModificationSession.FILE_IGNORE_LIST, new HashSet<String>());
		}
		for (BibtexEntry entry : database.getEntries()) {
			String paths = entry.getField(GUIGlobals.FILE_FIELD);
			if (paths == null || paths.trim().length() == 0) {
				continue;
			}

			for (String path : jabRefAttributes.parsePathNames(entry, paths)) {
				String name = new File(path).getName();
				if(((Set<String>) getSessionObject(MapModificationSession.FILE_IGNORE_LIST)).contains(name)) {
					continue;
				}

				if (entry.getCiteKey() == null) {
					LabelPatternUtil.makeLabel(Globals.prefs.getKeyPattern(), database, entry);
				}

				if (this.pdfReferences.get(name) == null) {
					this.pdfReferences.put(name, entry);
				}
				else {
					try {
						BibtexEntry singleEntry = jabRefAttributes.resolveDuplicateLinks(new File(path));
						this.pdfReferences.put(name, singleEntry);
					}
					catch (ResolveDuplicateEntryAbortedException e) {
						this.pdfReferences.remove(name);
						((Set<String>) getSessionObject(MapModificationSession.FILE_IGNORE_LIST)).add(e.getFile().getName());
						LogUtils.info("ignore pdf on mindmap update: " + e.getFile());
					}
				}
			}
		}
		System.out.println("ReferenceUpdater: buildPdfIndex: "+(System.currentTimeMillis()-time));
	}

	private void buildUrlIndex() {
		if (getSessionObject(MapModificationSession.URL_IGNORE_LIST) == null) {
			putSessionObject(MapModificationSession.URL_IGNORE_LIST, new HashSet<String>());
		}
		for (BibtexEntry entry : database.getEntries()) {
			String url = entry.getField("url");
			if (url == null || url.trim().length() == 0 || ((Set<String>) getSessionObject(MapModificationSession.URL_IGNORE_LIST)).contains(url)) {
				continue;
			}

			if (entry.getCiteKey() == null) {
				LabelPatternUtil.makeLabel(Globals.prefs.getKeyPattern(), database, entry);
			}

			
			if (this.urlReferences.get(url) == null) {			
				this.urlReferences.put(url, entry);
			}
			else {
				try {
					BibtexEntry singleEntry = jabRefAttributes.resolveDuplicateLinks(new URL(url));
					this.urlReferences.put(url, singleEntry);
				}
				catch (MalformedURLException e) {
					LogUtils.warn(e);
				}
				catch (ResolveDuplicateEntryAbortedException e) {
					this.urlReferences.remove(url);
					((Set<String>) getSessionObject(MapModificationSession.URL_IGNORE_LIST)).add(e.getUrl().toExternalForm());
					LogUtils.info("ignore url on mindmap update: " + e.getUrl());
				}

			}
		}
		System.out.println("ReferenceUpdater: buildUrlIndex: "+(System.currentTimeMillis()-time));
	}
	
	private boolean isIgnored(Reference reference, NodeModel node) {
		if (reference.getUris().size() > 0) {
			File file = WorkspaceUtils.resolveURI(reference.getUris().iterator().next(), node.getMap());
			if (file != null) {
				if (((Set<String>) getSessionObject(MapModificationSession.FILE_IGNORE_LIST)).contains(file.getName())) {
					return true;
				}
			}
		}
		
		URL u = reference.getUrl();
		if (u != null) {
			if (((Set<String>) getSessionObject(MapModificationSession.URL_IGNORE_LIST)).contains(u.toExternalForm())) {
				return true;
			}
		}
		
		URI uri = Tools.getAbsoluteUri(node);
		if (uri != null) {
    		File file = WorkspaceUtils.resolveURI(uri, node.getMap());				
    		if (file != null) {
    			if (!reference.containsLink(uri)) {
    				return true;
    			}
    			
    			if (file != null) {
    				if (((Set<String>) getSessionObject(MapModificationSession.FILE_IGNORE_LIST)).contains(file.getName())) {
    					return true;
    				}
    			}
    		}
    		else {
    			u = null;
    			try {
    				u = uri.toURL();
    			}
    			catch (MalformedURLException e) {
    				LogUtils.warn(e.getMessage());
    			}
    			if (u != null) {
    				if (((Set<String>) getSessionObject(MapModificationSession.URL_IGNORE_LIST)).contains(u.toExternalForm())) {
    					return true;
    				}
    			}
    		}
		}
		
		return false;
	}

	private boolean updateReferenceNodes() {		
		boolean changes = false;
		for (Entry<BibtexEntry, Set<NodeModel>> entry : referenceNodes.entrySet()) {
			// BibtexEntry bibtexEntry = database.getEntryByKey(entry.getKey());
			// if (bibtexEntry != null) {
			BibtexEntry bibtexEntry = entry.getKey();
			Reference reference = new Reference(bibtexEntry);
			for (NodeModel node : entry.getValue()) {
				if (isIgnored(reference, node)) {
					continue;
				}

				String key = jabRefAttributes.getBibtexKey(node);
				try {
					if (key == null) {
						changes = true;
						ReferencesController.getController().getJabRefAttributes().setReferenceToNode(reference, node);
					}
					else {
						changes = changes | ReferencesController.getController().getJabRefAttributes().setReferenceToNode(bibtexEntry, node);
					}
				}
				catch (ResolveDuplicateEntryAbortedException e) {
					if (e.getFile() != null) {
						((Set<String>) getSessionObject(MapModificationSession.FILE_IGNORE_LIST)).add(e.getFile().getName());
					}
					else {
						((Set<String>) getSessionObject(MapModificationSession.URL_IGNORE_LIST)).add(e.getUrl().toExternalForm());
					}
				}

			}
		}
		return changes;
	}

	private void buildIndex(NodeModel parent) {
		addNodeToReferenceIndex(parent);

		for (NodeModel child : parent.getChildren()) {
			buildIndex(child);
		}
	}

	private void addNodeToReferenceIndex(NodeModel node) {
		try {
			String key = jabRefAttributes.getBibtexKey(node);

			URI uri = NodeLinks.getLink(node);
			if (uri != null) {
				File file;

				file = WorkspaceUtils.resolveURI(uri, node.getMap());
				if (file != null) {
					String fileName = file.getName();
					BibtexEntry entry = this.pdfReferences.get(fileName);
					if (entry != null) {
						addReferenceToIndex(node, entry);
					}
					return;
				}

				BibtexEntry entry = this.urlReferences.get(uri.toURL().toExternalForm());
				if (entry != null) {
					addReferenceToIndex(node, entry);
					return;
				}
			}

			if (key != null) {
				BibtexEntry bibtexEntry = database.getEntryByKey(key);
				addReferenceToIndex(node, bibtexEntry);
				return;
			}
		}
		catch (Exception e) {
			LogUtils.warn("referenceupdater uri: " + NodeLinks.getLink(node));
			LogUtils.warn(e);
		}
	}

	private void addReferenceToIndex(NodeModel node, BibtexEntry entry) {
		Set<NodeModel> nodes = referenceNodes.get(entry);
		if (nodes == null) {
			nodes = new HashSet<NodeModel>();
			referenceNodes.put(entry, nodes);
		}
		nodes.add(node);
	}

}
