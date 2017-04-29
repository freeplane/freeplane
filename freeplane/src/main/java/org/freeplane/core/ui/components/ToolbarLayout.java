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
				if (i > container.getComponentCount() || lastBlockWidth + blockWidth > maxWidth || component instanceof Separator && blockEndPosition == BlockEndPosition.ANYWHERE) {
					if(lastBlockFinish < i && i < container.getComponentCount() && lastBlockWidth + blockWidth <= maxWidth){
						lastBlockFinish = i;
						lastBlockHeight = Math.max(lastBlockHeight, blockHeight);
					}
					int x = 0;
					for (int j = lastBlockStart; j < lastBlockFinish; j++) {
						final Component c = container.getComponent(j);
						final Dimension cPreferredSize = c.getPreferredSize();
						c.setBounds(x, heigth, cPreferredSize.width, lastBlockHeight);
						x += cPreferredSize.width;
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
				if(component instanceof Separator && blockEndPosition == BlockEndPosition.ANYWHERE){
					if(lastBlockFinish == i) {
						final Dimension separatorPreferredSize = component.getPreferredSize();
						component.setBounds(0, heigth, maxWidth, separatorPreferredSize.height);
						heigth += separatorPreferredSize.height;
						lastBlockStart = i + 1;
						blockWidth = blockHeight = lastBlockHeight = lastBlockWidth = 0;
						continue;
					}
					else{
						lastBlockFinish = i;
						i--;
						continue;
					}
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
			final Dimension compPreferredSize = component.getPreferredSize();
			blockWidth += compPreferredSize.width;
			blockHeight = Math.max(compPreferredSize.height, blockHeight);
		}
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
				if (i > container.getComponentCount() || lastBlockWidth + blockWidth > maxWidth || component instanceof Separator && blockEndPosition == BlockEndPosition.ANYWHERE) {
					if(lastBlockFinish < i && i < container.getComponentCount() && lastBlockWidth + blockWidth <= maxWidth){
						lastBlockFinish = i;
						lastBlockHeight = Math.max(lastBlockHeight, blockHeight);
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
				width = Math.max(width, lastBlockWidth);
				if(component instanceof Separator && blockEndPosition == BlockEndPosition.ANYWHERE){
					if(lastBlockFinish == i) {
						final Dimension separatorPreferredSize = component.getPreferredSize();
						heigth += separatorPreferredSize.height;
						lastBlockStart = i + 1;
						blockWidth = blockHeight = lastBlockHeight = lastBlockWidth = 0;
						continue;
					}
					else{
						lastBlockFinish = i;
						i--;
						continue;
					}
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
			final Dimension compPreferredSize = component.getPreferredSize();
			blockWidth += compPreferredSize.width;
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
