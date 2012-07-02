package org.docear.plugin.bibtex.jabref;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.ws.rs.core.UriBuilder;

import net.sf.jabref.BibtexDatabase;
import net.sf.jabref.BibtexEntry;
import net.sf.jabref.GUIGlobals;
import net.sf.jabref.Globals;
import net.sf.jabref.export.DocearReferenceUpdateController;
import net.sf.jabref.gui.FileListEntry;
import net.sf.jabref.gui.FileListTableModel;
import net.sf.jabref.labelPattern.LabelPatternUtil;

import org.apache.commons.io.FilenameUtils;
import org.docear.plugin.bibtex.Reference;
import org.docear.plugin.bibtex.Reference.Item;
import org.docear.plugin.bibtex.ReferencesController;
import org.docear.plugin.bibtex.dialogs.DuplicatePdfDialogPanel;
import org.docear.plugin.core.CoreConfiguration;
import org.docear.plugin.core.features.DocearMapModelExtension;
import org.docear.plugin.core.features.MapModificationSession;
import org.docear.plugin.pdfutilities.util.NodeUtils;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.attribute.Attribute;
import org.freeplane.features.attribute.AttributeController;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.link.LinkModel;
import org.freeplane.features.link.NodeLinkModel;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.plugin.workspace.WorkspaceUtils;

public class JabRefAttributes {
	private boolean nodeDirty = false;

	private HashMap<String, String> valueAttributes = new HashMap<String, String>();
	private String keyAttribute;

	public JabRefAttributes() {
		registerAttributes();
	}

	public void registerAttributes() {
		this.keyAttribute = TextUtils.getText("bibtex_key");

		this.valueAttributes.put("authors", "author");
		this.valueAttributes.put("title", "title");
		this.valueAttributes.put("year", "year");
		this.valueAttributes.put("journal", "journal");
	}

	public String getKeyAttribute() {
		return keyAttribute;
	}

	public HashMap<String, String> getValueAttributes() {
		return valueAttributes;
	}

	public String getBibtexKey(NodeModel node) {
		return getAttributeValue(node, this.keyAttribute);
	}

	public String getAttributeValue(NodeModel node, String attributeName) {
		NodeAttributeTableModel attributeTable = (NodeAttributeTableModel) node.getExtension(NodeAttributeTableModel.class);
		if (attributeTable == null) {
			return null;
		}
		for (Attribute attribute : attributeTable.getAttributes()) {
			if (attribute.getName().equals(attributeName)) {
				return attribute.getValue().toString();
			}
		}

		return null;
	}

	public boolean isReferencing(BibtexEntry entry, NodeModel node) {
		String nodeKey = getBibtexKey(node);
		String entryKey = entry.getCiteKey();
		if (nodeKey != null && entryKey != null && nodeKey.equals(entryKey)) {
			return true;
		}
		return false;
	}

	public void setReferenceToNode(BibtexEntry entry) throws ResolveDuplicateEntryAbortedException {
		NodeModel node = Controller.getCurrentModeController().getMapController().getSelectedNode();
		setReferenceToNode(new Reference(entry, node), node);
	}

	public void removeReferenceFromNode(NodeModel node) {
		NodeAttributeTableModel attributeTable = AttributeController.getController(MModeController.getMModeController()).createAttributeTableModel(node);

		if (attributeTable == null) {
			return;
		}

		for (String attributeKey : attributeTable.getAttributeKeyList()) {
			if (this.valueAttributes.containsKey(attributeKey) || this.keyAttribute.equals(attributeKey)) {
				AttributeController.getController(MModeController.getMModeController()).performRemoveRow(attributeTable,
						attributeTable.getAttributePosition(attributeKey));
			}
		}
	}

	// public void updateReferenceOnPdf(URI uri, NodeModel node) {
	// BibtexEntry entry = findBibtexEntryForPDF(uri, node);
	// if (entry != null) {
	// setReferenceToNode(new Reference(entry, node), node);
	// }
	// }

	@SuppressWarnings("unchecked")
	public boolean updateReferenceToNode(Reference reference, NodeModel node) throws ResolveDuplicateEntryAbortedException {
		if (DocearReferenceUpdateController.isLocked()) {
			return false;
		}

		DocearReferenceUpdateController.lock();
		boolean changes = false;
		try {
			MapModificationSession session = node.getMap().getExtension(DocearMapModelExtension.class).getMapModificationSession();
			Set<String> ignores = null;
			if (session != null) {
				ignores = (Set<String>) session.getSessionObject(MapModificationSession.FILE_IGNORE_LIST);
				if (ignores == null) {
					ignores = new HashSet<String>();
					session.putSessionObject(MapModificationSession.FILE_IGNORE_LIST, ignores);
				}
			}
			for (URI uri : reference.getUris()) {
				File file = WorkspaceUtils.resolveURI(uri, node.getMap());
				try {
					if (ignores != null) {
						if (file != null) {
							if (ignores.contains(file.getName())) {
								throw new ResolveDuplicateEntryAbortedException(file);
							}
						}
					}
					resolveDuplicateLinks(WorkspaceUtils.resolveURI(uri));
					reference = new Reference(findBibtexEntryForPDF(uri, node.getMap()), node);
				}
				catch (NullPointerException e) {
					LogUtils.warn(e.getMessage());
				}
				catch (ResolveDuplicateEntryAbortedException ex) {
					if (ignores != null) {
						if (file != null) {
							ignores.add(file.getName());
						}
					}
					throw ex;
				}
			}

			NodeUtils.setAttributeValue(node, reference.getKey().getName(), reference.getKey().getValue());

			NodeAttributeTableModel attributeTable = (NodeAttributeTableModel) node.getExtension(NodeAttributeTableModel.class);
			if (attributeTable == null) {
				return false;
			}

			AttributeController attributeController = AttributeController.getController(MModeController.getMModeController());
			Vector<Attribute> attributes = attributeTable.getAttributes();
			ArrayList<Item> inserts = new ArrayList<Item>();
			for (Item item : reference.getAttributes()) {
				boolean found = false;
				for (int i = 0; i < attributes.size() && !found; i++) {
					Attribute attribute = attributes.get(i);
					if (attribute.getName().equals(item.getName())) {
						found = true;
						if (item.getValue() == null) {
							attributeController.performRemoveRow(attributeTable, i);
							changes = true;
						}
						else if (!attribute.getValue().equals(item.getValue())) {
							attributeController.performSetValueAt(attributeTable, item.getValue(), i, 1);
							attribute.setValue(item.getValue());
							changes = true;
						}
					}
				}
				if (!found && item.getValue() != null) {
					inserts.add(item);
				}
			}

			for (Item item : inserts) {
				changes = true;
				AttributeController.getController(MModeController.getMModeController()).performInsertRow(attributeTable, 0, item.getName(), item.getValue());
			}
		}
		finally {
			// do not overwrite existing links
			NodeLinks nodeLinks = NodeLinks.getLinkExtension(node);
			if (nodeLinks == null || nodeLinks.getHyperLink() == null) {
				// add link to node
				if (reference.getUris().size() > 0) {
					((MLinkController) MLinkController.getController()).setLinkTypeDependantLink(node, reference.getUris().iterator().next());
					changes = true;
				}
			}

			DocearReferenceUpdateController.unlock();
		}

		return changes;
	}

	public boolean updateReferenceToNode(BibtexEntry entry, NodeModel node) throws ResolveDuplicateEntryAbortedException {
		return updateReferenceToNode(new Reference(entry, node), node);
	}

	public boolean setReferenceToNode(BibtexEntry entry, NodeModel node) throws ResolveDuplicateEntryAbortedException {
		return setReferenceToNode(new Reference(entry, node), node);
	}

	public boolean setReferenceToNode(Reference reference, NodeModel node) throws ResolveDuplicateEntryAbortedException {
		return updateReferenceToNode(reference, node);
	}

	public void removePdfFromBibtexEntry(File file, BibtexEntry entry) {
		String filename = file.getName();
		FileListTableModel model = new FileListTableModel();
		String oldVal = entry.getField(GUIGlobals.FILE_FIELD);
		if (oldVal == null) {
			return;
		}
		model.setContent(oldVal);

		for (int i = 0; i < model.getRowCount(); i++) {
			FileListEntry fle = model.getEntry(i);
			File f = new File(fle.getLink());
			if (filename.equals(f.getName())) {
				model.removeEntry(i);
				System.out.println(oldVal + " <--> " + model.getStringRepresentation());
			}
		}
		entry.setField(GUIGlobals.FILE_FIELD, model.getStringRepresentation());

	}

	public List<String> retrieveFileLinksFromEntry(BibtexEntry entry) {
		String jabrefFiles = entry.getField("file");
		if (jabrefFiles != null) {
			// path linked in jabref
			return parsePathNames(entry, jabrefFiles);
		}
		return Collections.emptyList();
	}

	private void removeDuplicateLinks(File file, BibtexEntry entry) {
		BibtexDatabase database = ReferencesController.getController().getJabrefWrapper().getDatabase();

		Iterator<BibtexEntry> iter = database.getEntries().iterator();
		while (iter.hasNext()) {
			BibtexEntry item = iter.next();
			if (item != entry) {
				ReferencesController.getController().getJabRefAttributes().removePdfFromBibtexEntry(file, item);
			}
		}
	}

	public void resolveDuplicateLinks(BibtexEntry entry) throws InterruptedException {
		for (String s : retrieveFileLinksFromEntry(entry)) {
			try {
				resolveDuplicateLinks(new File(s));
			}
			catch (Exception ex) {
				LogUtils.warn(ex.getMessage());
			}
		}
	}

	public void removeLinkFromNode(NodeModel node) {
		for (LinkModel linkModel : NodeLinks.getLinkExtension(node).getLinks()) {
			if (linkModel instanceof NodeLinkModel) {
				((MLinkController) LinkController.getController()).removeArrowLink((NodeLinkModel) linkModel);
			}
		}
	}

	public void resolveDuplicateLinks(File file) throws NullPointerException, ResolveDuplicateEntryAbortedException {
		List<BibtexEntry> entries = new ArrayList<BibtexEntry>();

		BibtexDatabase database = ReferencesController.getController().getJabrefWrapper().getDatabase();

		for (BibtexEntry entry : database.getEntries()) {
			for (String jabrefFile : retrieveFileLinksFromEntry(entry)) {
				if (jabrefFile.endsWith(file.getName())) {
					entries.add(entry);
					break;
				}
			}
		}

		if (entries.size() <= 1) {
			return;
		}
		DuplicatePdfDialogPanel panel = new DuplicatePdfDialogPanel(entries, file);
		int answer = JOptionPane.showConfirmDialog(UITools.getFrame(), panel, TextUtils.getText("docear.reference.duplicate_file.title"),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

		if (answer != JOptionPane.OK_OPTION) {
			throw new ResolveDuplicateEntryAbortedException(file);
		}
		else {
			removeDuplicateLinks(file, panel.getSelectedEntry());
			ReferencesController.getController().getJabrefWrapper().getBasePanel().runCommand("save");
			setNodeDirty(true);
		}
	}

	public BibtexEntry findBibtexEntryForPDF(URI uri, MapModel map) throws ResolveDuplicateEntryAbortedException {
		return findBibtexEntryForPDF(uri, map, false);
	}

	public BibtexEntry findBibtexEntryForPDF(URI uri, MapModel map, boolean ignoreDuplicates) throws ResolveDuplicateEntryAbortedException {
		BibtexDatabase database = ReferencesController.getController().getJabrefWrapper().getDatabase();
		if (database == null) {
			return null;
		}
		// file name linked in a node
		File nodeFile = WorkspaceUtils.resolveURI(uri, map);
		if (nodeFile == null) {
			return null;
		}
		String nodeFileName = nodeFile.getName();
		String baseName = FilenameUtils.removeExtension(nodeFileName);

		MapModificationSession session = map.getExtension(DocearMapModelExtension.class).getMapModificationSession();

		Set<String> ignores = null;
		if (session != null) {
			ignores = (Set<String>) session.getSessionObject(MapModificationSession.FILE_IGNORE_LIST);
			if (ignores == null) {
				ignores = new HashSet<String>();
				session.putSessionObject(MapModificationSession.FILE_IGNORE_LIST, ignores);
			}
		}

		try {
			if (ignores != null) {
				if (nodeFileName != null) {
					if (ignores.contains(nodeFileName)) {
						throw new ResolveDuplicateEntryAbortedException(nodeFile);
					}
				}
			}
			if (!ignoreDuplicates) {
				resolveDuplicateLinks(nodeFile);
			}

			for (BibtexEntry entry : database.getEntries()) {
				String jabrefFiles = entry.getField(GUIGlobals.FILE_FIELD);
				if (jabrefFiles != null) {
					// path linked in jabref
					for (String jabrefFile : parsePathNames(entry, jabrefFiles)) {
						if (jabrefFile.endsWith(nodeFileName)) {
							return entry;
						}
					}
				}
			}
		}
		catch (ResolveDuplicateEntryAbortedException e) {
			if (ignores != null) {
				if (nodeFileName != null) {
					ignores.add(nodeFileName);
				}
			}
			throw e;
		}

		BibtexEntry entry = database.getEntryByKey(baseName);
		return entry;
	}

	public ArrayList<String> parsePathNames(BibtexEntry entry, String path) {
		ArrayList<String> fileNames = new ArrayList<String>();

		ArrayList<String> paths = extractPaths(path);
		if (path == null) {
			LogUtils.warn("Could not extract path from: " + entry.getCiteKey());
			return fileNames;
		}
		if (paths == null || paths.size() == 0) {
			return fileNames;
		}
		for (String s : paths) {
			try {
				if (Compat.isWindowsOS()) {
					fileNames.add(new File(s).getPath());
				}
				else {
					// DOCEAR - maybe no escape removal -> could cause problems
					// like in win os
					fileNames.add(new File(removeEscapingCharacter(s)).getPath());
				}
			}
			catch (Exception e) {
				continue;
			}
		}
		return fileNames;
	}

	public ArrayList<URI> parsePaths(BibtexEntry entry, String pathInBibtexFile) {
		ArrayList<URI> uris = new ArrayList<URI>();
		ArrayList<String> paths = extractPaths(pathInBibtexFile);

		for (String path : paths) {
			if (path == null) {
				LogUtils.warn("Could not extract path from: " + entry.getCiteKey());
				continue;
			}
			path = removeEscapingCharacter(path);
			if (isAbsolutePath(path) && (new File(path)).exists()) {
				uris.add(new File(path).toURI());
			}
			else {
				URI uri = CoreConfiguration.referencePathObserver.getUri();
				URI absUri = WorkspaceUtils.absoluteURI(uri);

				final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
				Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
				URI pdfUri = absUri.resolve(UriBuilder.fromPath(path).build());
				Thread.currentThread().setContextClassLoader(contextClassLoader);
				File file = null;
				try {
					file = new File(pdfUri);
				}
				catch (IllegalArgumentException e) {
					LogUtils.warn(e.getMessage() + " for: " + path);
				}
				if (file != null && file.exists()) {
					uris.add(pdfUri);
				}
			}
		}
		return uris;
	}

	private static boolean isAbsolutePath(String path) {
		return path.matches("^/.*") || path.matches("^[a-zA-Z]:.*");
	}

	private static String removeEscapingCharacter(String string) {
		return string.replaceAll("([^\\\\]{1,1})[\\\\]{1}", "$1");
	}

	public static ArrayList<String> extractPaths(String fileField) {
		ArrayList<String> paths = new ArrayList<String>();

		if (fileField != null) {
			FileListTableModel model = new FileListTableModel();
			model.setContent(fileField);

			for (int i = 0; i < model.getRowCount(); i++) {
				paths.add(model.getEntry(i).getLink());
			}
		}

		return paths;
	}

	public void generateBibtexEntry(BibtexEntry entry) {
		BibtexDatabase database = ReferencesController.getController().getJabrefWrapper().getDatabase();
		LabelPatternUtil.makeLabel(Globals.prefs.getKeyPattern(), database, entry);
	}

	public boolean isNodeDirty() {
		return nodeDirty;
	}

	public void setNodeDirty(boolean nodeDirty) {
		this.nodeDirty = nodeDirty;
	}

}
