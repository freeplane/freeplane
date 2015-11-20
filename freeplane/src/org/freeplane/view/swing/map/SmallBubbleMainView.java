package org.freeplane.view.swing.map;

import java.awt.Insets;

@SuppressWarnings("serial")
public class SmallBubbleMainView extends BubbleMainView {
    final private static Insets insets = new Insets(0,  HORIZONTAL_MARGIN,  0, HORIZONTAL_MARGIN);

    @Override
    public Insets getInsets() {
        return SmallBubbleMainView.insets;
    }
}
