package org.docear.plugin.pdfutilities.ui;

import javax.swing.JMenu;

import org.docear.plugin.pdfutilities.util.NodeUtils;
import org.freeplane.features.map.INodeChangeListener;
import org.freeplane.features.map.INodeSelectionListener;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.ModeController;

public class JMonitoringMenu extends JMenu implements INodeSelectionListener, INodeChangeListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	

	public JMonitoringMenu(String s, ModeController modeController) {
		super(s);
		modeController.getMapController().addNodeSelectionListener(this);
		modeController.getMapController().addNodeChangeListener(this);
	}

	public void onDeselect(NodeModel node) {
	
	}

	public void onSelect(NodeModel node) {
		this.setEnabled(NodeUtils.isMonitoringNode(node));		
	}

	public void nodeChanged(NodeChangeEvent event) {
		this.setEnabled(NodeUtils.isMonitoringNode(event.getNode()));		
	}

}
