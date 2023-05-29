package org.freeplane.core.ui.components;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.JSeparator;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

public class ToolbarLayout implements LayoutManager {

    public static final int MAX_WIDTH_BY_PARENT_WIDTH = -1;

    public static ToolbarLayout fix() {
        return new ToolbarLayout(BlockEndPosition.ON_EVERY_SEPARATOR);
    }

    public static ToolbarLayout horizontal() {
        return new ToolbarLayout(BlockEndPosition.ON_SEPARATOR);
    }

    public static ToolbarLayout vertical() {
        return new ToolbarLayout(BlockEndPosition.ANYWHERE);
    }

    private BlockEndPosition blockEndPosition;
	private int maximumWidth = MAX_WIDTH_BY_PARENT_WIDTH;

	enum BlockEndPosition{ON_SEPARATOR, ON_EVERY_SEPARATOR, ANYWHERE}
	ToolbarLayout(BlockEndPosition blockEndPosition){
		this.blockEndPosition = blockEndPosition;

	}

	public int getMaximumWidth() {
        return maximumWidth;
    }

	public void setMaximumWidth(int maximumWidth) {
        this.maximumWidth = maximumWidth;
    }

    @Override
    public void addLayoutComponent(final String name, final Component comp) {
	}

	@Override
    public void layoutContainer(final Container container) {
		if(! container.isVisible())
			return;
		int blockWidth = 0;
		int blockHeight = 0;
		int lastBlockWidth = 0;
		int lastBlockHeight = 0;
		int lastBlockStart = 0;
		int lastBlockFinish = 0;
		Insets insets = container.getInsets();
		int leftMargin = insets.left;
		int heigth =  insets.top;
		final int maximumWidth = calculateMaxWidth(container) - insets.left - insets.right;
		for (int i = 0;; i++) {
			final Component component = i < container.getComponentCount() ? container.getComponent(i) : null;
			if (component == null || component instanceof JSeparator || blockEndPosition == BlockEndPosition.ANYWHERE) {
				if (i > container.getComponentCount() || blockEndPosition == BlockEndPosition.ON_EVERY_SEPARATOR || lastBlockWidth + blockWidth > maximumWidth) {
					int x = leftMargin;
					for (int j = lastBlockStart; j < lastBlockFinish; j++) {
						final Component c = container.getComponent(j);
						final int width = getPreferredWidth(c, maximumWidth);
						c.setBounds(x, heigth, width, lastBlockHeight);
						x += width;
					}
					heigth += lastBlockHeight;
					lastBlockWidth = blockWidth;
					lastBlockHeight = blockHeight;
					lastBlockStart = lastBlockFinish;
				}
				else {
					lastBlockWidth += blockWidth;
					lastBlockHeight = Math.max(blockHeight, lastBlockHeight);
				}
				lastBlockFinish = i;
				blockWidth = blockHeight = 0;
			}
			if (component == null) {
				if (lastBlockStart == container.getComponentCount()) {
					break;
				}
				lastBlockFinish = container.getComponentCount();
				continue;
			}
			blockWidth += getPreferredWidth(component, maximumWidth);
			final Dimension compPreferredSize = component.getPreferredSize();
			blockHeight = Math.max(compPreferredSize.height, blockHeight);
		}
	}

    private int calculateMaxWidth(final Container container) {
        if(container.isMaximumSizeSet())
            return container.getMaximumSize().width;
        Container viewport = SwingUtilities.getAncestorOfClass(JViewport.class, container);
        if (viewport != null)
            return viewport.getWidth();
        Container parent = container.getParent();
        if (parent != null)
            return parent.getWidth();
        if (maximumWidth >= 0)
            return maximumWidth;

        return Integer.MAX_VALUE;
    }
	private int getPreferredWidth(final Component c, final int maxWidth) {
		final int width = ! c.isVisible() ? 0 :
				c instanceof JSeparator && blockEndPosition == BlockEndPosition.ANYWHERE ? maxWidth :
					c.getPreferredSize().width;
		return width;
	}

	@Override
    public Dimension minimumLayoutSize(final Container comp) {
		return new Dimension(0, 0);
	}

	@Override
    public Dimension preferredLayoutSize(final Container container) {
	    Insets insets = container.getInsets();
		int maxWidth = calculateMaxWidth(container) - insets.left - insets.right;
		for(;;) {
	        int width = 0;
	        int heigth = 0;
	        int blockWidth = 0;
	        int blockHeight = 0;
	        int lastBlockWidth = 0;
	        int lastBlockHeight = 0;
	        int lastBlockStart = 0;
	        int lastBlockFinish = 0;
	        for (int i = 0;; i++) {
	            final Component component = i < container.getComponentCount() ? container.getComponent(i) : null;
	            if (component == null || component instanceof JSeparator || blockEndPosition == BlockEndPosition.ANYWHERE) {
	                if (i > container.getComponentCount() || blockEndPosition == BlockEndPosition.ON_EVERY_SEPARATOR || lastBlockWidth + blockWidth > maxWidth) {
	                    heigth += lastBlockHeight;
	                    lastBlockWidth = blockWidth;
	                    lastBlockHeight = blockHeight;
	                    lastBlockStart = lastBlockFinish;
	                }
	                else {
	                    lastBlockWidth += blockWidth;
	                    lastBlockHeight = Math.max(blockHeight, lastBlockHeight);
	                }
	                width = Math.max(width, lastBlockWidth);
	                lastBlockFinish = i;
	                blockWidth = blockHeight = 0;
	            }
	            if (component == null) {
	                if (lastBlockStart == container.getComponentCount()) {
	                    break;
	                }
	                lastBlockFinish = container.getComponentCount();
	                continue;
	            }
	            blockWidth += getPreferredWidth(component, maxWidth);
	            final Dimension compPreferredSize = component.getPreferredSize();
	            blockHeight = Math.max(compPreferredSize.height, blockHeight);
	        }
	        if(maxWidth >= width) {
	            Dimension preferredSize = new Dimension(width + insets.left + insets.right, heigth + insets.top + insets.bottom);
	            return preferredSize;
	        }
	        else
	            maxWidth = width;
		}
	}

	@Override
    public void removeLayoutComponent(final Component comp) {
	}
}
