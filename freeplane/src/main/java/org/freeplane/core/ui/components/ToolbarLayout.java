package org.freeplane.core.ui.components;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

import javax.swing.JToolBar.Separator;

public class ToolbarLayout implements LayoutManager {

    public static final int MAX_WIDTH_BY_PARENT_WIDTH = -1;
    private BlockEndPosition blockEndPosition;
	private int maximumWidth = MAX_WIDTH_BY_PARENT_WIDTH;

	enum BlockEndPosition{ON_SEPARATOR, ANYWHERE};
	ToolbarLayout(BlockEndPosition blockEndPosition){
		this.blockEndPosition = blockEndPosition;
		
	}
	
	public int getMaximumWidth() {
        return maximumWidth;
    }

	public void setMaximumWidth(int maximumWidth) {
        this.maximumWidth = maximumWidth;
    }

    public void addLayoutComponent(final String name, final Component comp) {
	}

	public void layoutContainer(final Container container) {
		if(! container.isVisible())
			return;
		final int maximumWidth = calculateMaxWidth(container);
		int heigth = 0;
		int blockWidth = 0;
		int blockHeight = 0;
		int lastBlockWidth = 0;
		int lastBlockHeight = 0;
		int lastBlockStart = 0;
		int lastBlockFinish = 0;
		for (int i = 0;; i++) {
			final Component component = i < container.getComponentCount() ? container.getComponent(i) : null;
			if (component == null || component instanceof Separator || blockEndPosition == BlockEndPosition.ANYWHERE) {
				if (i > container.getComponentCount() || lastBlockWidth + blockWidth > maximumWidth) {
					int x = 0;
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
        Container parent = container.getParent();
        if (parent != null)
            return parent.getWidth();
        else if (maximumWidth >= 0)
            return maximumWidth;
        else {
            return Integer.MAX_VALUE;
        }
    }
	private int getPreferredWidth(final Component c, final int maxWidth) {
		final int width = ! c.isVisible() ? 0 : 
				c instanceof Separator && blockEndPosition == BlockEndPosition.ANYWHERE ? maxWidth : 
					c.getPreferredSize().width;
		return width;
	}

	public Dimension minimumLayoutSize(final Container comp) {
		return new Dimension(0, 0);
	}

	public Dimension preferredLayoutSize(final Container container) {
		final int maxWidth = calculateMaxWidth(container);
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
			if (component == null || component instanceof Separator || blockEndPosition == BlockEndPosition.ANYWHERE) {
				if (i > container.getComponentCount() || lastBlockWidth + blockWidth > maxWidth) {
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
		return new Dimension(width, heigth);
	}

	public void removeLayoutComponent(final Component comp) {
	}

	public static ToolbarLayout horizontal() {
		return new ToolbarLayout(BlockEndPosition.ON_SEPARATOR);
	}

	public static ToolbarLayout vertical() {
		return new ToolbarLayout(BlockEndPosition.ANYWHERE);
	}
}
