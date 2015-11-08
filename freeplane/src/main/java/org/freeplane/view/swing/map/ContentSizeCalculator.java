package org.freeplane.view.swing.map;

import java.awt.Dimension;

import javax.swing.JComponent;

import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.nodestyle.NodeStyleController;

public class ContentSizeCalculator {
	public static final Dimension ZERO = new Dimension(0, 0);
	public static ContentSizeCalculator INSTANCE = new ContentSizeCalculator();
	public  Dimension calculateContentSize(final NodeView view) {
		if(! view.isContentVisible())
			return ZERO;
    	final JComponent content = view.getContent();
        final MapView map = view.getMap();
		final ModeController modeController = map.getModeController();
        final NodeStyleController nsc = NodeStyleController.getController(modeController);
        final NodeModel node = view.getModel();
		final int minNodeWidth = map.getZoomed(nsc.getMinWidth(node).toBaseUnits());
		final int maxNodeWidth = map.getZoomed(nsc.getMaxWidth(node).toBaseUnits());
        Dimension contentSize;
        if (content instanceof ZoomableLabel){
        	contentSize=  ((ZoomableLabel)content).getPreferredSize(minNodeWidth, maxNodeWidth);
        }
        else{
        	contentSize=  content.getPreferredSize();
        }
        return contentSize;
    }

}
