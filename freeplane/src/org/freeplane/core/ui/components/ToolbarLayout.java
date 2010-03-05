package org.freeplane.core.ui.components;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

import javax.swing.JToolBar.Separator;

class ToolbarLayout implements LayoutManager{

	private static final ToolbarLayout instance = new ToolbarLayout();

	public void addLayoutComponent(String name, Component comp) {
	}

	public void layoutContainer(Container container) {
		final int maxWidth = container.getParent().getWidth();
		int heigth = 0;
		int blockWidth = 0;
		int blockHeight = 0;
		int lastBlockWidth = 0;
		int lastBlockHeight = 0;
		int lastBlockStart = 0;
		for(int i = 0;; i++){
			Component component = i < container.getComponentCount() ? container.getComponent(i) : null;
			if(component == null || component instanceof Separator){
				if(component == null){
					lastBlockWidth += blockWidth;
					lastBlockHeight = Math.max(blockHeight, lastBlockHeight);
				}
				if(component == null || lastBlockWidth + blockWidth > maxWidth){
					int x = 0;
					for(int j = lastBlockStart; j < i; j++){
						final Component c = container.getComponent(j);
						Dimension cPreferredSize = c.getPreferredSize();
						c.setBounds(x, heigth, cPreferredSize.width, lastBlockHeight);
						x += cPreferredSize.width;
					}
					heigth += lastBlockHeight;
					lastBlockWidth = 0;
					lastBlockHeight = 0;
					lastBlockStart = i;
				}
				else{
					lastBlockWidth += blockWidth;
					lastBlockHeight = Math.max(blockHeight, lastBlockHeight);
				}
				blockWidth = 0;
				blockHeight = 0;
			}
			if(component == null){
				break;
			}
			Dimension compPreferredSize = component.getPreferredSize();
			blockWidth += compPreferredSize.width;
			blockHeight = Math.max(compPreferredSize.height, blockHeight);
		}
	}

	public Dimension minimumLayoutSize(Container comp) {
		return new Dimension(0, 0);
	}

	public Dimension preferredLayoutSize(Container container) {
		final int maxWidth = container.getParent().getWidth();
		int width = 0;
		int heigth = 0;
		int blockWidth = 0;
		int blockHeight = 0;
		int lastBlockWidth = 0;
		int lastBlockHeight = 0;
		for(int i = 0;; i++){
			Component component = i < container.getComponentCount() ? container.getComponent(i) : null;
			if(component == null || component instanceof Separator){
				if(lastBlockWidth + blockWidth > maxWidth){
					width = Math.max(width, lastBlockWidth);
					heigth += lastBlockHeight;
					lastBlockWidth = 0;
					lastBlockHeight = 0;
				}
				else{
					lastBlockWidth += blockWidth;
					lastBlockHeight = Math.max(blockHeight, lastBlockHeight);
				}
				blockWidth = 0;
				blockHeight = 0;
			}
			if(component == null){
				width = Math.max(width, lastBlockWidth);
				heigth += lastBlockHeight;
				break;
			}
			Dimension compPreferredSize = component.getPreferredSize();
			blockWidth += compPreferredSize.width;
			blockHeight = Math.max(compPreferredSize.height, blockHeight);
		}
		return new Dimension(width, heigth);
		
	}

	public void removeLayoutComponent(Component comp) {
	}

	public static LayoutManager getInstance() {
		return instance;
	}

}
