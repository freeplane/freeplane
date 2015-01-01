package org.freeplane.view.swing.map;

import java.awt.Dimension;

import javax.swing.JComponent;

import org.freeplane.features.mode.ModeController;
import org.freeplane.features.nodestyle.NodeStyleController;

public class ContentSizeCalculator {
	public static final Dimension ZERO = new Dimension(0, 0);
	public static ContentSizeCalculator INSTANCE = new ContentSizeCalculator();
	public  Dimension calculateContentSize(final NodeView view) {
		if(! view.isContentVisible())
			return ZERO;
    	final JComponent content = view.getContent();
        final ModeController modeController = view.getMap().getModeController();
        final NodeStyleController nsc = NodeStyleController.getController(modeController);
        Dimension contentSize;
        if (content instanceof ZoomableLabel){
        	int maxNodeWidth = nsc.getMaxWidth(view.getModel());
        	contentSize=  ((ZoomableLabel)content).getPreferredSize(maxNodeWidth);
        }
        else{
        	contentSize=  content.getPreferredSize();
        }
        int minNodeWidth = nsc.getMinWidth(view.getModel());
        int contentWidth = Math.max(view.getZoomed(minNodeWidth),contentSize.width);
        int contentHeight = contentSize.height;
        final Dimension contentProfSize = new Dimension(contentWidth, contentHeight);
        return contentProfSize;
    }

}
