package org.freeplane.core.ui.components;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

import javax.swing.JToolBar.Separator;

class ToolbarLayout implements LayoutManager {

	private BlockEndPosition blockEndPosition;

	enum BlockEndPosition{ON_SEPARATOR, ANYWHERE};
	ToolbarLayout(BlockEndPosition blockEndPosition){
		this.blockEndPosition = blockEndPosition;
		
	}
	public void addLayoutComponent(final String name, final Component comp) {
	}

	public void layoutContainer(final Container container) {
		if(! container.isVisible())
			return;
		final int maxWidth = container.getParent().getWidth();
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
					int x = 0;
					for (int j = lastBlockStart; j < lastBlockFinish; j++) {
						final Component c = container.getComponent(j);
						final int width = getPreferredWidth(c, maxWidth);
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
			blockWidth += getPreferredWidth(component, maxWidth);
			final Dimension compPreferredSize = component.getPreferredSize();
			blockHeight = Math.max(compPreferredSize.height, blockHeight);
		}
	}
	private int getPreferredWidth(final Component c, final int maxWidth) {
		final int width = c instanceof Separator && blockEndPosition == BlockEndPosition.ANYWHERE ? maxWidth : c.getPreferredSize().width;
		return width;
	}

	public Dimension minimumLayoutSize(final Container comp) {
		return new Dimension(0, 0);
	}

	public Dimension preferredLayoutSize(final Container container) {
		final int maxWidth = container.getParent().getWidth();
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

	public static LayoutManager horizontal() {
		return new ToolbarLayout(BlockEndPosition.ON_SEPARATOR);
	}

	public static LayoutManager vertical() {
		return new ToolbarLayout(BlockEndPosition.ANYWHERE);
	}
}
