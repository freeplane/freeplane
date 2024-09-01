/*
 * Created on 23 Jun 2024
 *
 * author dimitry
 */
package org.freeplane.core.resources.components;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;

@SuppressWarnings("serial")
public class ResponsiveFlowLayout extends FlowLayout {
    public ResponsiveFlowLayout() {
        super();
    }

    public ResponsiveFlowLayout(int align) {
        super(align);
    }

    public ResponsiveFlowLayout(int align, int hgap, int vgap) {
        super(align, hgap, vgap);
    }

    @Override
    public Dimension preferredLayoutSize(Container target) {
        int parentWidth = target.getParent().getWidth();
        if(parentWidth == 0)
            return super.preferredLayoutSize(target);

        synchronized (target.getTreeLock()) {
            Dimension dim = new Dimension(0, 0);
            int nmembers = target.getComponentCount();
            int maxWidth = parentWidth;
            int x = 0;
            int rowHeight = 0;
            boolean firstComponent = true;

            for (int i = 0; i < nmembers; i++) {
                Component m = target.getComponent(i);
                if (m.isVisible()) {
                    Dimension d = m.getPreferredSize();
                    if (firstComponent) {
                        firstComponent = false;
                    } else {
                        x += getHgap();
                    }

                    if (x + d.width > maxWidth) {
                        dim.height += rowHeight + getVgap();
                        rowHeight = d.height;
                        x = d.width;
                    } else {
                        x += d.width;
                        rowHeight = Math.max(rowHeight, d.height);
                    }
                    dim.width = Math.max(dim.width, x);
                }
            }
            dim.height += rowHeight + getVgap();

            Insets insets = target.getInsets();
            dim.width += insets.left + insets.right + getHgap();
            dim.height += insets.top + insets.bottom + getVgap();

            return dim;
        }
    }
}
