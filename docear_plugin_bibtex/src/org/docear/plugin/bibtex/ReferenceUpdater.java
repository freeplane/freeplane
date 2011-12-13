package org.docear.plugin.bibtex;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import net.sf.jabref.BibtexDatabase;
import net.sf.jabref.BibtexEntry;

import org.docear.plugin.core.mindmap.AMindmapUpdater;
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
		if (this.pdfReferences.size() == 0) {
			buildPdfIndex();
		}
		return updateMap(map);
	}

	@SuppressWarnings("unchecked")
	private boolean updateMap(MapModel map) {
		referenceNodes.clear();
		buildIndex(map.getRootNode());
		System.out.println("map entries: " + referenceNodes.size());

		int size = 0;
		for (Entry<?, ?> entry : referenceNodes.entrySet()) {
			size += ((LinkedList<NodeModel>) entry.getValue()).size();
		}
		System.out.println("total entries: " + size);

		return updateReferenceNodes();
	}

	private void buildPdfIndex() {
		for (BibtexEntry entry : database.getEntries()) {
			String path = entry.getField("file");
			if (path == null || path.length() == 0) {
				continue;
			}

			// TODO
			path = jabRefAttributes.parsePathName(entry, path);
			this.pdfReferences.put(path, entry.getCiteKey());
		}
		System.out.println("pdf size: " + pdfReferences.size());
	}

	private boolean updateReferenceNodes() {
		// int i = 0;
		boolean changes = false;
		for (Entry<String, LinkedList<NodeModel>> entry : referenceNodes.entrySet()) {
			Reference reference = new Reference(database.getEntryByKey(entry.getKey()));
			for (NodeModel node : entry.getValue()) {
				// i++;
				// if (i % 100 == 0) {
				// System.out.println("node: " + i);
				// }
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
				// if(uri.getScheme() == null) {
				// file = new File(uri);
				// }
				// else {
				file = WorkspaceUtils.resolveURI(uri);
				// }
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
			System.out.println(node.getText());
			System.out.println("referenceupdater uri: " + NodeLinks.getLink(node));
		}
	}

}
