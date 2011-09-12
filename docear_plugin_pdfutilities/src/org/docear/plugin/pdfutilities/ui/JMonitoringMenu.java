package org.docear.plugin.pdfutilities.ui;

import javax.swing.JMenu;

import org.docear.plugin.pdfutilities.util.NodeUtils;
import org.freeplane.features.map.INodeSelectionListener;
import org.freeplane.features.map.NodeModel;

public class JMonitoringMenu extends JMenu implements INodeSelectionListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	

	public JMonitoringMenu(String s) {
		super(s);		
	}

	public void onDeselect(NodeModel node) {
	
	}

	public void onSelect(NodeModel node) {
		this.setEnabled(NodeUtils.isMonitoringNode(node));		
	}

}
