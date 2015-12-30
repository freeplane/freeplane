package org.freeplane.view.swing.ui.mindmapmode;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.SwingUtilities;

import org.freeplane.core.ui.LengthUnits;
import org.freeplane.core.util.Quantity;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.nodestyle.NodeSizeModel;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.nodestyle.mindmapmode.MNodeStyleController;
import org.freeplane.view.swing.map.MainView;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.ui.DefaultNodeMouseWheelListener;

public class MNodeMouseWheelListener extends DefaultNodeMouseWheelListener {

	public MNodeMouseWheelListener(MouseWheelListener mapMouseWheelListener) {
		super(mapMouseWheelListener);
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		final MainView view = (MainView) e.getComponent();
		final MapView map = (MapView) SwingUtilities.getAncestorOfClass(MapView.class, view);
		final int wheelRotation = e.getWheelRotation();
		final int factor = e.isShiftDown() ? 1 : 6;
		final NodeModel node = view.getNodeView().getModel();
		float newZoomedWidth =  (view.getWidth() - wheelRotation * factor) / map.getZoom();
		Quantity<LengthUnits> newZoomedWidthQuantity = new Quantity<>(newZoomedWidth, LengthUnits.px).in(LengthUnits.pt);
		final ModeController modeController = map.getModeController();
		final MNodeStyleController styleController = (MNodeStyleController) modeController.getExtension(NodeStyleController.class);
		Controller.getCurrentController().getSelection().keepNodePosition(node, 0.5f, 0.5f);
		styleController.setMinNodeWidth(node, newZoomedWidthQuantity);
		styleController.setMaxNodeWidth(node, newZoomedWidthQuantity);
	}
	
	

}
