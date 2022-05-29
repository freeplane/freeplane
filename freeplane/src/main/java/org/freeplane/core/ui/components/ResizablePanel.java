package org.freeplane.core.ui.components;

import java.awt.LayoutManager;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JPanel;

public class ResizablePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private ComponentListener parentListener = new ComponentAdapter() {
		@Override
		public void componentResized(ComponentEvent e) {
			revalidate();
			repaint();
		}
	};

	public ResizablePanel(LayoutManager layout) {
		super(layout);
	}

	@Override
	public void addNotify() {
		getParent().addComponentListener(parentListener);
		super.addNotify();
	}

	@Override
	public void removeNotify() {
		super.removeNotify();
		getParent().removeComponentListener(parentListener);
	}
}