package org.docear.plugin.bibtex;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.jabref.BibtexDatabase;
import net.sf.jabref.BibtexEntry;
import net.sf.jabref.Globals;
import net.sf.jabref.labelPattern.LabelPatternUtil;

import org.docear.plugin.bibtex.jabref.JabRefAttributes;
import org.docear.plugin.bibtex.jabref.ResolveDuplicateEntryAbortedException;
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
	private final HashMap<String, Set<BibtexEntry>> pdfReferences;

	private JabRefAttributes jabRefAttributes;
	private BibtexDatabase database;

	public ReferenceUpdater(String title) {
		super(title);
		referenceNodes = new HashMap<BibtexEntry, Set<NodeModel>>();
		pdfReferences = new HashMap<String, Set<BibtexEntry>>();

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
			for (String path : jabRefAttributes.parsePathNames(entry, paths)) {
				String name = new File(path).getName();

				if (entry.getCiteKey() == null) {
					LabelPatternUtil.makeLabel(Globals.prefs.getKeyPattern(), database, entry);
				}
				Set<BibtexEntry> entries = this.pdfReferences.get(name);
				if (entries == null) {
					entries = new HashSet<BibtexEntry>();
					this.pdfReferences.put(name, entries);
				}
				entries.add(entry);
			}
		}
	}

	private boolean updateReferenceNodes() {
		if (getSessionObject(MapModificationSession.FILE_IGNORE_LIST) == null) {
			putSessionObject(MapModificationSession.FILE_IGNORE_LIST, new HashSet<String>());
		}

		boolean changes = false;
		for (Entry<BibtexEntry, Set<NodeModel>> entry : referenceNodes.entrySet()) {
			// BibtexEntry bibtexEntry = database.getEntryByKey(entry.getKey());
			// if (bibtexEntry != null) {
			BibtexEntry bibtexEntry = entry.getKey();
			for (NodeModel node : entry.getValue()) {
				Reference reference = new Reference(bibtexEntry, node);
				File f = WorkspaceUtils.resolveURI(Tools.getAbsoluteUri(node), node.getMap());
				if (f != null) {
					if (!reference.containsFileName(f.getName())) {
						break;
					}
				}
				// getNodeLink
				// if(nodeLink isIn reference)

				if (reference.getUris().size() > 0) {
					File file = WorkspaceUtils.resolveURI(reference.getUris().iterator().next(), node.getMap());
					if (file != null) {
						if (((Set<String>) getSessionObject(MapModificationSession.FILE_IGNORE_LIST)).contains(file.getName())) {
							continue;
						}
					}
					file = WorkspaceUtils.resolveURI(Tools.getAbsoluteUri(node), node.getMap());
					if (file != null) {
						if (((Set<String>) getSessionObject(MapModificationSession.FILE_IGNORE_LIST)).contains(file.getName())) {
							continue;
						}
					}
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
					((Set<String>) getSessionObject(MapModificationSession.FILE_IGNORE_LIST)).add(e.getFile().getName());
				}

			}
			// }
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

			URI uri = NodeLinks.getLink(node);
			if (uri != null) {
				File file;

				file = WorkspaceUtils.resolveURI(uri, node.getMap());
				if (file != null) {
					String fileName = file.getName();
					Set<BibtexEntry> entries = this.pdfReferences.get(fileName);
					if (entries != null) {
						for (BibtexEntry entry : entries) {
							Set<NodeModel> nodes = referenceNodes.get(entry);
							if (nodes == null) {
								nodes = new HashSet<NodeModel>();
								referenceNodes.put(entry, nodes);
							}
							nodes.add(node);
						}
					}
				}
			}
			else if (key != null) {
				BibtexEntry bibtexEntry = database.getEntryByKey(key);
				Set<NodeModel> nodes = referenceNodes.get(bibtexEntry);
				if (nodes == null) {
					nodes = new HashSet<NodeModel>();
					referenceNodes.put(bibtexEntry, nodes);
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
