package org.docear.plugin.bibtex;

import java.util.HashMap;

import org.docear.plugin.bibtex.jabref.JabRefAttributes;
import org.freeplane.features.attribute.AttributeController;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.mindmapmode.MModeController;

public class SplmmAttributes extends JabRefAttributes {
	private final HashMap<String, String> splmmValueAttributes = new HashMap<String, String>();
	
	private final static String SPLMM_KEY_ATTRIBUTE = "splmm_refkey";
	
	
	public SplmmAttributes() {
		super();
		splmmValueAttributes.put("splmm_refauthors", "authors");
		splmmValueAttributes.put("splmm_reftitle", "title");
		splmmValueAttributes.put("splmm_refyear", "year");
		splmmValueAttributes.put("splmm_publishedin", "journal");
	}
	
	public boolean translate(NodeModel node) {		
		String splmmKey = getSplmmKey(node);
		if (splmmKey == null) {
			return false;
		}
		
		NodeAttributeTableModel attributes = AttributeController.getController(MModeController.getMModeController()).createAttributeTableModel(node);
		for (String splmmAttributeName : attributes.getAttributeKeyList()) {
			String attributeName = splmmValueAttributes.get(splmmAttributeName.toLowerCase());
			if (attributeName != null) {
				AttributeController.getController(MModeController.getMModeController()).performSetValueAt(attributes, attributeName, attributes.getAttributePosition(splmmAttributeName), 0);
			}
		}
		
		AttributeController.getController(MModeController.getMModeController()).performSetValueAt(attributes, this.getKeyAttribute(), attributes.getAttributePosition(SPLMM_KEY_ATTRIBUTE), 0);		
		return true;
	}
	
	public String getSplmmKey(NodeModel node) {
		return getAttributeValue(node, SPLMM_KEY_ATTRIBUTE);
	}

}
