package org.docear.plugin.bibtex;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import net.sf.jabref.BibtexDatabase;
import net.sf.jabref.BibtexEntry;
import net.sf.jabref.Globals;
import net.sf.jabref.labelPattern.LabelPatternUtil;

import org.docear.plugin.core.mindmap.AMindmapUpdater;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.workspace.WorkspaceUtils;

public class ReferenceUpdater extends AMindmapUpdater {
	private final HashMap<String, LinkedList<NodeModel>> referenceNodes;
	private final HashMap<String, String> pdfReferences;

	private JabRefAttributes jabRefAttributes;
	private BibtexDatabase database;

	public ReferenceUpdater(String title) {
		super(title);
		referenceNodes = new HashMap<String, LinkedList<NodeModel>>();
		pdfReferences = new HashMap<String, String>();

	}

	public boolean updateMindmap(MapModel map) {
		jabRefAttributes = ReferencesController.getController().getJabRefAttributes();
		database = ReferencesController.getController().getJabrefWrapper().getDatabase();
		if (database == null) {
			return false;
		}
		if (this.pdfReferences.size() == 0) {
			buildPdfIndex();
		}
		return updateMap(map);
	}
	
	private boolean updateMap(MapModel map) {
		referenceNodes.clear();
		buildIndex(map.getRootNode());

		return updateReferenceNodes();
	}

	private void buildPdfIndex() {
		for (BibtexEntry entry : database.getEntries()) {
			String paths = entry.getField("file");
			if (paths == null || paths.length() == 0) {
				continue;
			}

			// TODO
			for (String name : jabRefAttributes.parsePathNames(entry, paths)) {
				if (entry.getCiteKey() == null) {
					LabelPatternUtil.makeLabel(Globals.prefs.getKeyPattern(), database, entry);
				}
				this.pdfReferences.put(name, entry.getCiteKey());
			}
		}
	}

	private boolean updateReferenceNodes() {
//		int i = 0;
		boolean changes = false;
		for (Entry<String, LinkedList<NodeModel>> entry : referenceNodes.entrySet()) {

			BibtexEntry bibtexEntry = database.getEntryByKey(entry.getKey());
			if (bibtexEntry != null) {
				for (NodeModel node : entry.getValue()) {
					Reference reference = new Reference(bibtexEntry, node);
	//				i++;
	//				if (i % 100 == 0) {
	//					LogUtils.info("node: " + i);
	//				}
					String key = jabRefAttributes.getBibtexKey(node);
					if (key == null) {
						changes = true;
						ReferencesController.getController().getJabRefAttributes().setReferenceToNode(reference, node);
					}
					else {
						changes = changes
								| ReferencesController.getController().getJabRefAttributes().updateReferenceToNode(reference, node);
					}
	
				}
			}
		}
		return changes;
	}

	private void buildIndex(NodeModel parent) {
		getReference(parent);

		for (NodeModel child : parent.getChildren()) {
			buildIndex(child);
		}
	}

	private void getReference(NodeModel node) {
		try {
			String key = jabRefAttributes.getBibtexKey(node);
			if (key == null) {
				URI uri = NodeLinks.getLink(node);
				if (uri == null) {
					return;
				}
				// TODO:
				File file;

				file = WorkspaceUtils.resolveURI(uri, node.getMap());
				if (file == null) {
					return;
				}
				String path = file.getName();
				key = this.pdfReferences.get(path);
			}
			if (key != null) {
				LinkedList<NodeModel> nodes = referenceNodes.get(key);
				if (nodes == null) {
					nodes = new LinkedList<NodeModel>();
					referenceNodes.put(key, nodes);
				}
				nodes.add(node);
			}
		}
		catch (Exception e) {
			LogUtils.warn("referenceupdater uri: " + NodeLinks.getLink(node));
			LogUtils.warn(e);
		}
	}

}
