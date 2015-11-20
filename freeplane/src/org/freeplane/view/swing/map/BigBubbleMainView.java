package org.freeplane.view.swing.map;

import java.awt.Insets;

import org.freeplane.features.nodestyle.NodeStyleModel;
import org.freeplane.features.nodestyle.NodeStyleModel.Shape;

@SuppressWarnings("serial")
public class BigBubbleMainView extends BubbleMainView {
    final private static Insets insets = new Insets(HORIZONTAL_MARGIN,  HORIZONTAL_MARGIN,  HORIZONTAL_MARGIN, HORIZONTAL_MARGIN);

	@Override
    public
    Shape getShape() {
		return NodeStyleModel.Shape.bubble;
	}
	
   @Override
    public Insets getInsets() {
        return BigBubbleMainView.insets;
    }

}
