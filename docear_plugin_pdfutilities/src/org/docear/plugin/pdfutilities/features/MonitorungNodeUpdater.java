package org.docear.plugin.pdfutilities.features;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

import org.docear.plugin.core.CoreConfiguration;
import org.docear.plugin.core.mindmap.AMindmapUpdater;
import org.docear.plugin.pdfutilities.PdfUtilitiesController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.attribute.Attribute;
import org.freeplane.features.attribute.AttributeController;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.link.mindmapmode.MLinkController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.workspace.WorkspaceUtils;

public class MonitorungNodeUpdater extends AMindmapUpdater {
	
	private final static Attribute keyAttribute = new Attribute("splmm_dirmon_path", TextUtils.getText(PdfUtilitiesController.MON_INCOMING_FOLDER));
	private static HashMap<String, String> monitoringAttributes;
	private static ArrayList<Attribute> newMonitoringAttributes;
	
	public MonitorungNodeUpdater(String title) {
		super(title);	
		monitoringAttributes = new HashMap<String, String>();		
		MonitorungNodeUpdater.monitoringAttributes.put("splmm_dirmon_auto", TextUtils.getText(PdfUtilitiesController.MON_AUTO));
		MonitorungNodeUpdater.monitoringAttributes.put("splmm_dirmon_subdirs", TextUtils.getText(PdfUtilitiesController.MON_SUBDIRS));
		
		newMonitoringAttributes = new ArrayList<Attribute>();
		newMonitoringAttributes.add(new Attribute(TextUtils.getText(PdfUtilitiesController.MON_MINDMAP_FOLDER), CoreConfiguration.LIBRARY_PATH));
		newMonitoringAttributes.add(new Attribute(TextUtils.getText(PdfUtilitiesController.MON_FLATTEN_DIRS), 0));		
	}

	@Override
	public boolean updateMindmap(MapModel map) {
		return updateNodesRecursive(map.getRootNode());
	}
	
	/**
	 * @param node
	 * @return
	 */
	private boolean updateNodesRecursive(NodeModel node) {
		boolean changes = false;
		for(NodeModel child : node.getChildren()) {
			changes = changes | updateNodesRecursive(child);
		}
		changes = changes | updateNode(node);
		return changes;
	}

	private boolean updateNode(NodeModel node) {		
		boolean isOldMinitoringNode = false;
		
		NodeAttributeTableModel attributeTable = AttributeController.getController().createAttributeTableModel(node);
		if (attributeTable == null) {
			return false;
		}
		
		for (int i=0; i<attributeTable.getRowCount(); i++) {
			Attribute attribute = attributeTable.getAttribute(i);
			
			if (attribute.getName().equals(keyAttribute.getName())) {
				isOldMinitoringNode = true;
				AttributeController.getController().performSetValueAt(attributeTable, keyAttribute.getValue(), i, 0);
				try {
					String path = (String) attribute.getValue();
					URI uri = new File(path).toURI();
					if (uri.getScheme().length() == 1) {
						throw new Exception("absolut windows paths do not work in linux!");
					}
					uri = MLinkController.toLinkTypeDependantURI(node.getMap().getFile(), WorkspaceUtils.resolveURI(uri));
					AttributeController.getController().performSetValueAt(attributeTable, uri, i, 1);
				}
				catch(Exception e) {					
					LogUtils.warn(e);
				}				
			}
			
			String newAttributeName = monitoringAttributes.get(attribute.getName());
			
			if (newAttributeName != null) {
				isOldMinitoringNode = true;
				AttributeController.getController().performSetValueAt(attributeTable, newAttributeName, i, 0);
			}
		}
		
		if (isOldMinitoringNode) {
			for (Attribute attribute : newMonitoringAttributes) {
				AttributeController.getController().performInsertRow(attributeTable, attributeTable.getRowCount(), attribute.getName(), attribute.getValue());				
			}
		}
		
		return isOldMinitoringNode;
	}
	
	



}
