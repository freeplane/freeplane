package org.docear.plugin.bibtex;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import javax.ws.rs.core.UriBuilder;

import net.sf.jabref.BibtexDatabase;
import net.sf.jabref.BibtexEntry;
import net.sf.jabref.Globals;
import net.sf.jabref.labelPattern.LabelPatternUtil;

import org.docear.plugin.bibtex.Reference.Item;
import org.docear.plugin.core.CoreConfiguration;
import org.docear.plugin.pdfutilities.util.NodeUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.attribute.Attribute;
import org.freeplane.features.attribute.AttributeController;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.WorkspaceUtils;

public class JabRefAttributes {

	private HashMap<String, String> valueAttributes = new HashMap<String, String>();
	private String keyAttribute;

	public JabRefAttributes() {
		registerAttributes();
	}

	public void registerAttributes() {
		this.keyAttribute = TextUtils.getText("bibtex_key");

		this.valueAttributes.put(TextUtils.getText("jabref_author"), "author");
		this.valueAttributes.put(TextUtils.getText("jabref_title"), "title");
		this.valueAttributes.put(TextUtils.getText("jabref_year"), "year");
		this.valueAttributes
				.put(TextUtils.getText("jabref_journal"), "journal");
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
		NodeAttributeTableModel attributeTable = (NodeAttributeTableModel) node
				.getExtension(NodeAttributeTableModel.class);
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

	public void setReferenceToNode(BibtexEntry entry) {
		NodeModel node = Controller.getCurrentModeController()
				.getMapController().getSelectedNode();
		setReferenceToNode(new Reference(entry, node), node);
	}

	public void removeReferenceFromNode(NodeModel node) {
		NodeAttributeTableModel attributeTable = AttributeController
				.getController().createAttributeTableModel(node);

		if (attributeTable == null) {
			return;
		}

		for (String attributeKey : attributeTable.getAttributeKeyList()) {
			if (this.valueAttributes.containsKey(attributeKey)
					|| this.keyAttribute.equals(attributeKey)) {
				AttributeController.getController().performRemoveRow(
						attributeTable,
						attributeTable.getAttributePosition(attributeKey));
			}
		}

		NodeLinks nodeLinks = NodeLinks.getLinkExtension(node);
		if (nodeLinks != null) {
			nodeLinks.setHyperLink(null);
		}
	}

	public void updateReferenceOnPdf(URI uri, NodeModel node) {
		BibtexEntry entry = findBibtexEntryForPDF(uri, node);
		if (entry != null) {
			setReferenceToNode(new Reference(entry, node), node);
		}
	}

	public boolean updateReferenceToNode(Reference reference, NodeModel node) {
		boolean changes = false;
		NodeAttributeTableModel attributeTable = (NodeAttributeTableModel) node
				.getExtension(NodeAttributeTableModel.class);
		if (attributeTable == null) {
			return false;
		}

		AttributeController attributeController = AttributeController
				.getController();
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
					} else if (!attribute.getValue().equals(item.getValue())) {
						attributeController.performSetValueAt(attributeTable,
								item.getValue(), i, 1);
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
			AttributeController.getController().performInsertRow(
					attributeTable, 0, item.getName(), item.getValue());
		}

		// do not overwrite existing links
		NodeLinks nodeLinks = NodeLinks.getLinkExtension(node);
		if (nodeLinks != null && nodeLinks.getHyperLink() != null) {
			return changes;
		}

		// do nothing if the reference does not have a link
		if (reference.getUri() == null) {
			return changes;
		}

		// add link to node
		((MLinkController) MLinkController.getController())
				.setLinkTypeDependantLink(node, reference.getUri());
		return true;
	}

	public boolean updateReferenceToNode(BibtexEntry entry, NodeModel node) {
		return updateReferenceToNode(new Reference(entry, node), node);
	}

	public boolean setReferenceToNode(BibtexEntry entry, NodeModel node) {
		return setReferenceToNode(new Reference(entry, node), node);
	}

	public boolean setReferenceToNode(Reference reference, NodeModel node) {
		NodeUtils.setAttributeValue(node, reference.getKey().getName(),
				reference.getKey().getValue());
		return updateReferenceToNode(reference, node);
	}

	public BibtexEntry findBibtexEntryForPDF(URI uri, NodeModel node) {
		BibtexDatabase database = ReferencesController.getController()
				.getJabrefWrapper().getDatabase();
		if (database == null) {
			return null;
		}
		// file name linked in a node
		File nodeFile = WorkspaceUtils.resolveURI(uri, node.getMap());
		if (nodeFile == null) {
			return null;
		}
		String nodeFileName = nodeFile.getName();

		for (BibtexEntry entry : database.getEntries()) {
			String jabrefFiles = entry.getField("file");
			if (jabrefFiles != null) {
				// path linked in jabref
				for (String jabrefFile : parsePathNames(entry, jabrefFiles)) {
					if (jabrefFile.endsWith(nodeFileName)) {
						return entry;
					}
				}
			}
		}

		return null;

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
				fileNames.add(new File(removeEscapingCharacter(s)).getName());
			} catch (Exception e) {
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
				LogUtils.warn("Could not extract path from: "
						+ entry.getCiteKey());
				continue;
			}
			path = removeEscapingCharacter(path);
			if (isAbsolutePath(path) && (new File(path)).exists()) {
				uris.add(new File(path).toURI());
			} else {
				URI uri = CoreConfiguration.referencePathObserver.getUri();
				URI absUri = WorkspaceUtils.absoluteURI(uri);

				final ClassLoader contextClassLoader = Thread.currentThread()
						.getContextClassLoader();
				Thread.currentThread().setContextClassLoader(
						this.getClass().getClassLoader());
				URI pdfUri = absUri.resolve(UriBuilder.fromPath(path).build());
				Thread.currentThread()
						.setContextClassLoader(contextClassLoader);
				File file = null;
				try {
					file = new File(pdfUri);
				} catch (IllegalArgumentException e) {
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

	public static ArrayList<String> extractPaths(String path) {
		// String[] array = path.split("(^:|(?<=[^\\\\]):)"); // splits the
		// string at non escaped double points
		// if(array.length >= 3){
		// return array[1];
		// }
		// return null;

		String[] array = path.split("(^:|(?<=[^\\\\]):)"); // splits the string
															// at non escaped
															// double points
		ArrayList<String> paths = new ArrayList<String>();

		for (int i = 0; i < (array.length) / 2; i++) {
			paths.add(array[i * 2 + 1]);
		}
		return paths;
	}

	public void generateBibtexEntry(BibtexEntry entry) {
		BibtexDatabase database = ReferencesController.getController().getJabrefWrapper().getDatabase();
		LabelPatternUtil.makeLabel(Globals.prefs.getKeyPattern(), database, entry);
	}

}
