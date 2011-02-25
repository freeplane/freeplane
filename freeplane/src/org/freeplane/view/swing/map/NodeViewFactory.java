/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.view.swing.map;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.freeplane.features.common.edge.EdgeController;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.features.common.nodestyle.NodeStyleController;
import org.freeplane.features.common.nodestyle.NodeStyleModel;
import org.freeplane.features.common.note.NoteController;

class NodeViewFactory {
	private static class ContentPane extends JComponent {
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
	}

	private static class ContentPaneLayout implements LayoutManager {
		public void addLayoutComponent(final String name, final Component comp) {
		}

		public void layoutContainer(final Container parent) {
			final int componentCount = parent.getComponentCount();
			final int width = parent.getWidth();
			int y = 0;
			for (int i = 0; i < componentCount; i++) {
				final Component component = parent.getComponent(i);
				if (component.isVisible()) {
					component.validate();
					final Dimension preferredCompSize = component.getPreferredSize();
					if (component instanceof MainView) {
						component.setBounds(0, y, width, preferredCompSize.height);
					}
					else {
						final int x = (int) (component.getAlignmentX() * (width - preferredCompSize.width));
						component.setBounds(x, y, preferredCompSize.width, preferredCompSize.height);
					}
					y += preferredCompSize.height;
				}
			}
		}

		public Dimension minimumLayoutSize(final Container parent) {
			return preferredLayoutSize(parent);
		}

		public Dimension preferredLayoutSize(final Container parent) {
			final Dimension prefSize = new Dimension(0, 0);
			final int componentCount = parent.getComponentCount();
			for (int i = 0; i < componentCount; i++) {
				final Component component = parent.getComponent(i);
				if (component.isVisible()) {
					component.validate();
					final Dimension preferredCompSize = component.getPreferredSize();
					prefSize.height += preferredCompSize.height;
					prefSize.width = Math.max(prefSize.width, preferredCompSize.width);
				}
			}
			return prefSize;
		}

		public void removeLayoutComponent(final Component comp) {
		}
	}

	private static NodeViewFactory factory;

	static NodeViewFactory getInstance() {
		if (NodeViewFactory.factory == null) {
			NodeViewFactory.factory = new NodeViewFactory();
		}
		return NodeViewFactory.factory;
	}

	private NodeViewFactory() {
	}

	private void fireNodeViewCreated(final NodeView newView) {
		newView.getMap().getModeController().onViewCreated(newView);
	}

	JComponent newContentPane(final NodeView view) {
		return new ContentPane();
	}

	MainView newMainView(final NodeView node) {
		final NodeModel model = node.getModel();
		if (model.isRoot()) {
			return new RootMainView();
		}
		final String shape = NodeStyleController.getController(node.getMap().getModeController()).getShape(model);
		if (shape.equals(NodeStyleModel.STYLE_FORK)) {
			return new ForkMainView();
		}
		else if (shape.equals(NodeStyleModel.STYLE_BUBBLE)) {
			return new BubbleMainView();
		}
		else {
			System.err.println("Tried to create a NodeView of unknown Style " + shape);
			return new ForkMainView();
		}
	}

	/**
	 * Factory method which creates the right NodeView for the model.
	 */
	NodeView newNodeView(final NodeModel model, final int position, final MapView map, final Container parent) {
		final NodeView newView = new NodeView(model, position, map, parent);
		model.addViewer(newView);
		newView.setLayout(SelectableLayout.getInstance());
		newView.setMainView(newMainView(newView));
        newView.updateNoteViewer();
        newView.update();
        fireNodeViewCreated(newView); 
		return newView;
	}

	private static Map<Color, Icon> coloredNoteIcons  = new HashMap<Color, Icon>();
	private Icon coloredIcon = createColoredIcon();
	
	public ZoomableLabel createNoteViewer() {
		final ZoomableLabel label = new ZoomableLabel();
		label.setIcon(coloredIcon);
		label.setVerticalTextPosition(JLabel.TOP);
		return label;
	}

	private Icon createColoredIcon() {
		return new Icon() {
			public void paintIcon(Component c, Graphics g, int x, int y) {
				NodeView nodeView = (NodeView) SwingUtilities.getAncestorOfClass(NodeView.class, c);
				if(nodeView == null)
					return;
				final Color iconColor = EdgeController.getController(nodeView.getMap().getModeController()).getColor(nodeView.getModel());
				createColoredIcon(iconColor).paintIcon(c, g, x, y);
			}
			
			public int getIconWidth() {
				return createColoredIcon(Color.BLACK).getIconWidth();
			}
			
			public int getIconHeight() {
				return createColoredIcon(Color.BLACK).getIconHeight();
			}
		};
    }

	private Icon createColoredIcon(Color iconColor) {
	    Icon icon = coloredNoteIcons.get(iconColor);
		if(icon == null){
			final BufferedImage img;
			try {
				img = ImageIO.read(NoteController.bwNoteIconUrl);
				final int oldRGB = 0xffffff & Color.BLACK.getRGB();
				final int newRGB = 0xffffff & iconColor.getRGB();
				if(oldRGB != newRGB){
					for (int x = 0; x < img.getWidth(); x++) {
						for (int y = 0; y < img.getHeight(); y++) {
							final int rgb =  img.getRGB(x, y);
							if ((0xffffff &rgb) == oldRGB)
								img.setRGB(x, y, 0xff000000 & rgb| newRGB);
						}
					}
				}
				icon = new ImageIcon(img);
				coloredNoteIcons.put(iconColor, icon);
			}
			catch (IOException e) {
			}
		}
	    return icon;
    }
}
