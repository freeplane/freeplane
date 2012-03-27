package org.freeplane.view.swing.map;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.LayoutManager;

import javax.swing.JComponent;


class ContentPane extends JComponent {
	static private LayoutManager layoutManager = new ContentPaneLayout();
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	ContentPane() {
		setLayout(ContentPane.layoutManager);
	}

	@Override
	public void paint(final Graphics g) {
		switch (((NodeView) getParent()).getMap().getPaintingMode()) {
			case CLOUDS:
				return;
		}
		super.paint(g);
	}
	
	@Override
	public boolean contains(final int x, final int y) {
		if (super.contains(x, y))
			return true;
		for(int i = 0; i < getComponentCount(); i++){
		final Component comp = getComponent(i);
		if(comp.isVisible() && comp.contains(x-comp.getX(), y-comp.getY()))
			return true;
		}
		return false;
	}

}