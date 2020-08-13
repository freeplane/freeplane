package org.freeplane.view.swing.ui.mindmapmode;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.SwingUtilities;

import org.freeplane.core.ui.LengthUnits;
import org.freeplane.core.util.Quantity;
import org.freeplane.features.map.IMapSelection;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.nodestyle.mindmapmode.MNodeStyleController;
import org.freeplane.view.swing.map.MainView;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;
import org.freeplane.view.swing.ui.DefaultNodeMouseWheelListener;

public class MNodeMouseWheelListener extends DefaultNodeMouseWheelListener {

	public MNodeMouseWheelListener(MouseWheelListener mapMouseWheelListener) {
		super(mapMouseWheelListener);
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if(! e.isAltDown()){
			super.mouseWheelMoved(e);
			return;
		}
		final MainView view = (MainView) e.getComponent();
		final MapView map = (MapView) SwingUtilities.getAncestorOfClass(MapView.class, view);
		if(map.usesLayoutSpecificMaxNodeWidth())
			return;
		final int wheelRotation = e.getWheelRotation();
		final NodeView nodeView = view.getNodeView();
		if(! nodeView.isSelected())
			map.selectAsTheOnlyOneSelected(nodeView);

		final double factor = e.isControlDown() ? 1 : 6 * LengthUnits.pt.factor();
		double newZoomedWidth =  Math.max((view.getWidth() - wheelRotation * factor) / map.getZoom(), 0);
		final IMapSelection selection = Controller.getCurrentController().getSelection();
		Quantity<LengthUnits> newZoomedWidthQuantity = LengthUnits.pixelsInPt(newZoomedWidth);
		final ModeController modeController = map.getModeController();
		final MNodeStyleController styleController = (MNodeStyleController) modeController.getExtension(NodeStyleController.class);

		selection.preserveRootNodeLocationOnScreen();

		for (final NodeModel node: selection.getSelection()) {
			styleController.setMinNodeWidth(node, newZoomedWidthQuantity);
			styleController.setMaxNodeWidth(node, newZoomedWidthQuantity);
		}
	}



}
