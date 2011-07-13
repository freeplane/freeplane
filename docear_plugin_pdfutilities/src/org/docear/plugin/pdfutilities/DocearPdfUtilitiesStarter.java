package org.docear.plugin.pdfutilities;

import java.awt.Container;
import java.awt.dnd.DropTarget;

import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.INodeViewLifeCycleListener;
import org.freeplane.features.mode.ModeController;
import org.freeplane.view.swing.map.NodeView;

public class DocearPdfUtilitiesStarter {
	
	
	private ModeController modecontroller;
	
	public DocearPdfUtilitiesStarter(ModeController modeController){
		
		this.modecontroller = modeController;		
		LogUtils.info("starting DocearPdfUtilitiesStarter(ModeController)");		
		
		
		
		this.modecontroller.addINodeViewLifeCycleListener(new INodeViewLifeCycleListener(){

			public void onViewCreated(Container nodeView) {
				NodeView node = (NodeView)nodeView;
				final DropTarget dropTarget = new DropTarget(node.getMainView(), new DocearNodeDropListener());
				dropTarget.setActive(true);			
			}

			public void onViewRemoved(Container nodeView) {
				// TODO Auto-generated method stub
				
			}
			
		});		
	}

	
}
