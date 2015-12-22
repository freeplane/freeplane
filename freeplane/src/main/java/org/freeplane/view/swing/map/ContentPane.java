package org.freeplane.view.swing.map;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.LayoutManager;

import javax.swing.JComponent;


class ContentPane extends JComponent {
	static private LayoutManager layoutManager = new ContentPaneLayout();
	private static final long serialVersionUID = 1L;

	ContentPane() {
		setLayout(ContentPane.layoutManager);
	}

	@Override
	final public void paint(final Graphics g) {
		final NodeView parent = (NodeView) getParent();
		final PaintingMode paintingMode = parent.getMap().getPaintingMode();
		if(paintingMode.equals(PaintingMode.CLOUDS))
				return;
		final boolean selected = parent.isSelected();
		if(paintingMode.equals(PaintingMode.SELECTED_NODES) == selected)
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