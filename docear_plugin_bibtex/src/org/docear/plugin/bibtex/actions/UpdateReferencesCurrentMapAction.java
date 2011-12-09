package org.docear.plugin.bibtex.actions;

import java.awt.event.ActionEvent;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import net.sf.jabref.BibtexDatabase;
import net.sf.jabref.BibtexEntry;

import org.docear.plugin.bibtex.JabRefAttributes;
import org.docear.plugin.bibtex.Reference;
import org.docear.plugin.bibtex.ReferencesController;
import org.docear.plugin.core.mindmap.MindmapUpdateController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.MapController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.url.mindmapmode.SaveAll;
import org.freeplane.plugin.workspace.WorkspaceUtils;
import org.freeplane.view.swing.map.NodeView;
import org.jdesktop.swingworker.SwingWorker;

public class UpdateReferencesCurrentMapAction extends AFreeplaneAction {

	private final HashMap<String, LinkedList<NodeModel>> referenceNodes;
	private final HashMap<String, LinkedList<NodeModel>> pdfNodes;
	private final HashMap<String, String> pdfReferences;

	private JabRefAttributes jabRefAttributes;
	private BibtexDatabase database;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UpdateReferencesCurrentMapAction(String key) {
		super(key);
		referenceNodes = new HashMap<String, LinkedList<NodeModel>>();
		pdfNodes = new HashMap<String, LinkedList<NodeModel>>();
		pdfReferences = new HashMap<String, String>();
	}

	@SuppressWarnings("unchecked")
	public void actionPerformed(ActionEvent e) {
		jabRefAttributes = ReferencesController.getController().getJabRefAttributes();
		database = ReferencesController.getController().getJabrefWrapper().getDatabase();

		new SaveAll().actionPerformed(null);

		MindmapUpdateController mindmapUpdateController = new MindmapUpdateController();
		// mindmapUpdateController.addMindmapUpdater(new
		// ReferenceUpdater(TextUtils.getText("update_references_current_mindmaps")));
		// mindmapUpdateController.updateCurrentMindmap();

		final long start = System.currentTimeMillis();
		SwingWorker swingWorker = new SwingWorker<Void, Void>() {			
			@Override
			protected Void doInBackground() throws Exception {
				buildPdfIndex();
				
				buildIndex(Controller.getCurrentController().getMap().getRootNode());
				System.out.println("map entries: " + referenceNodes.size());

				int size = 0;
				for (Entry<?, ?> entry : referenceNodes.entrySet()) {
					size += ((LinkedList<NodeModel>) entry.getValue()).size();
				}
				System.out.println("total entries: " + size);

				updateReferenceNodes();
				return null;
			}
			
			protected void done() {
				unfoldAll(Controller.getCurrentController().getMap().getRootNode());
				System.out.println("execution time: " + (System.currentTimeMillis() - start));
			}

		};
		
		foldAll(Controller.getCurrentController().getMap().getRootNode());
		swingWorker.execute();
		
		
		
		

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
	}

	private void updateReferenceNodes() {
		int i = 0;
		for (Entry<String, LinkedList<NodeModel>> entry : referenceNodes.entrySet()) {
			Reference reference = new Reference(database.getEntryByKey(entry.getKey()));
			for (NodeModel node : entry.getValue()) {
				i++;
				if (i % 100 == 0) {
					System.out.println("node: " + i);
				}
				ReferencesController.getController().getJabRefAttributes().setReferenceToNode(reference, node);

			}
		}
	}

	protected void foldAll(final NodeModel node) {
		final MapController modeController = Controller.getCurrentModeController().getMapController();
		for (NodeModel child : modeController.childrenUnfolded(node)) {
			foldAll(child);
		}
		setFolded(node, true);
	}

	public void unfoldAll(final NodeModel node) {
		setFolded(node, false);
		final MapController mapController = Controller.getCurrentModeController().getMapController();
		for (final NodeModel child : mapController.childrenUnfolded(node)) {
			unfoldAll(child);
		}
	}

	private void setFolded(final NodeModel node, final boolean state) {
		final MapController mapController = Controller.getCurrentModeController().getMapController();
		if (!node.isRoot() && mapController.hasChildren(node) && (mapController.isFolded(node) != state)) {
			mapController.setFolded(node, state);
		}
	}

	private void buildIndex(NodeModel parent) {
		getReference(parent);

		for (NodeModel child : parent.getChildren()) {
			buildIndex(child);
		}
	}

	private void getReference(NodeModel node) {
		String key = jabRefAttributes.getBibtexKey(node);
		if (key == null) {
			URI uri = NodeLinks.getLink(node);
			if (uri == null) {
				return;
			}
			// TODO:
			String path = WorkspaceUtils.resolveURI(uri).getName();
			key = this.pdfReferences.get(path);
		}
		if (key != null) {
			LinkedList<NodeModel> nodes = referenceNodes.get(key);
			if (nodes == null) {
				nodes = new LinkedList<NodeModel>();
				referenceNodes.put(key, nodes);
			}
			else {
				nodes.add(node);
			}
		}
	}
}
