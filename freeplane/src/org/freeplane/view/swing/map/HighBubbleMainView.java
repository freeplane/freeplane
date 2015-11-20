package org.freeplane.view.swing.map;

import java.awt.Insets;

@SuppressWarnings("serial")
public class HighBubbleMainView extends BubbleMainView {
    final private static Insets insets = new Insets(HORIZONTAL_MARGIN,  HORIZONTAL_MARGIN,  HORIZONTAL_MARGIN, HORIZONTAL_MARGIN);

    @Override
    public Insets getInsets() {
        return HighBubbleMainView.insets;
    }

}
