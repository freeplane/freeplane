package org.docear.plugin.bibtex;

import java.util.HashMap;

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.attribute.AttributeController;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.map.NodeModel;

public class SplmmAttributes extends JabRefAttributes {
	private final HashMap<String, String> splmmValueAttributes = new HashMap<String, String>();
	
	private final static String SPLMM_KEY_ATTRIBUTE = "splmm_refkey";
	
	
	public SplmmAttributes() {
		super();
		splmmValueAttributes.put("splmm_refauthors", TextUtils.getText("jabref_author"));
		splmmValueAttributes.put("splmm_reftitle", TextUtils.getText("jabref_title"));
		splmmValueAttributes.put("splmm_refyear", TextUtils.getText("jabref_year"));
		splmmValueAttributes.put("splmm_publishedin", TextUtils.getText("jabref_journal"));
	}
	
	public boolean translate(NodeModel node) {		
		String splmmKey = getSplmmKey(node);
		if (splmmKey == null) {
			return false;
		}
		
		NodeAttributeTableModel attributes = AttributeController.getController().createAttributeTableModel(node);
		for (String splmmAttributeName : attributes.getAttributeKeyList()) {
			String attributeName = splmmValueAttributes.get(splmmAttributeName);
			if (attributeName != null) {
				AttributeController.getController().performSetValueAt(attributes, attributeName, attributes.getAttributePosition(splmmKey), 0);
			}
		}
		
		AttributeController.getController().performSetValueAt(attributes, this.getKeyAttribute(), attributes.getAttributePosition(SPLMM_KEY_ATTRIBUTE), 0);
		return true;
	}
	
	public String getSplmmKey(NodeModel node) {
		return getAttributeValue(node, SPLMM_KEY_ATTRIBUTE);
	}

}
