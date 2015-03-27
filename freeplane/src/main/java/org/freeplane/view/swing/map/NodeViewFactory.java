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
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseListener;
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

import org.freeplane.core.ui.IMouseListener;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.nodestyle.NodeStyleModel;
import org.freeplane.features.note.NoteController;
import org.freeplane.features.note.NoteModel;
import org.freeplane.features.text.DetailTextModel;
import org.freeplane.view.swing.ui.DefaultMapMouseListener;
import org.freeplane.view.swing.ui.DetailsViewMouseListener;
import org.freeplane.view.swing.ui.LinkNavigatorMouseListener;

class NodeViewFactory {
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
		String shape = shape(node);
		final MainView oldView = node.getMainView();
		if(oldView != null && oldView.getShape().equals(shape))
			return oldView;
		final ModeController modeController = node.getMap().getModeController();
		final NodeModel model = node.getModel();
		final MainView view;
		if (shape.equals(NodeStyleModel.STYLE_BUBBLE)) {
			if (model.isRoot())
				view = new RootMainView(NodeStyleModel.STYLE_BUBBLE);
			else
				view =  new BubbleMainView();
		}
		else {
			if (shape != null && ! shape.equals(NodeStyleModel.STYLE_FORK))
				System.err.println("Tried to create a NodeView of unknown Style " + String.valueOf(shape));
			if (model.isRoot())
				view = new RootMainView(NodeStyleModel.STYLE_FORK);
			else
				view = new ForkMainView();
		}
		NodeTooltipManager toolTipManager = NodeTooltipManager.getSharedInstance(modeController);
		toolTipManager.registerComponent(view);
		return view;
	}

	private String shape(NodeView node) {
		final ModeController modeController = node.getMap().getModeController();
		final NodeModel model = node.getModel();
		String shape = NodeStyleController.getController(modeController).getShape(model);
		if (shape.equals(NodeStyleModel.SHAPE_COMBINED)) {
			if (Controller.getCurrentModeController().getMapController().isFolded(model)) {
				shape= NodeStyleModel.STYLE_BUBBLE;
			}
			else {
				shape = NodeStyleModel.STYLE_FORK;
			}
		}
		else while(shape.equals(NodeStyleModel.SHAPE_AS_PARENT)){
			node = node.getParentView();
			if (node == null)
				shape = NodeStyleModel.STYLE_FORK;
			else
				shape = node.getMainView().getShape();
		}
		return shape;
	}

	/**
	 * Factory method which creates the right NodeView for the model.
	 */
	NodeView newNodeView(final NodeModel model, final MapView map, final Container parent, final int index) {
		final NodeView newView = new NodeView(model, map, parent);
		parent.add(newView, index);
		newView.setMainView(newMainView(newView));
		if(map.isDisplayable())
			updateNewView(newView);
		else
			newView.addHierarchyListener(new HierarchyListener() {
				public void hierarchyChanged(HierarchyEvent e) {
					NodeView view = (NodeView) e.getComponent();
					if(displayed(view, e)){
						view.removeHierarchyListener(this);
						updateNewView(view);
					}
					else if(removed(view, e)){
						view.removeHierarchyListener(this);
					}
				}

				private boolean removed(NodeView view, HierarchyEvent e) {
					return 0 != (e.getChangeFlags() & HierarchyEvent.PARENT_CHANGED) && view.getParent() == null;
				}

				private boolean displayed(NodeView view, HierarchyEvent e) {
					return 0 != (e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) && view.isDisplayable();
				}
			});
		return newView;
	}

	private void updateNewView(final NodeView newView) {
		newView.getModel().addViewer(newView);
		newView.setLayout(SelectableLayout.getInstance());
		updateNoteViewer(newView);
		newView.update();
        fireNodeViewCreated(newView);
        newView.addChildViews();
	}

	private static Map<Color, Icon> coloredNoteIcons  = new HashMap<Color, Icon>();
	private final Icon coloredIcon = createColoredIcon();
	private static final IMouseListener DETAILS_MOUSE_LISTENER = new DetailsViewMouseListener();
	private static final IMouseListener NOTE_MOUSE_LISTENER = new NoteViewMouseListener();

	public ZoomableLabel createNoteViewer() {
		final ZoomableLabel label = new ZoomableLabel();
		label.addMouseListener(NOTE_MOUSE_LISTENER);
		label.addMouseMotionListener(NOTE_MOUSE_LISTENER);
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
				final Color iconColor =  nodeView.getEdgeColor();
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

	void updateNoteViewer(NodeView nodeView) {
		ZoomableLabel note = (ZoomableLabel) nodeView.getContent(NodeView.NOTE_VIEWER_POSITION);
		String oldText = note != null ? note.getText() : null;
		String newText  = null;
		if (nodeView.getMap().showNotes()) {
			final NodeModel model = nodeView.getModel();
			final NoteModel extension = NoteModel.getNote(model);
            if (extension != null)
                newText = extension.getHtml();
		}
		if (oldText == null && newText == null) {
			return;
		}
		final ZoomableLabel view;
		if (oldText != null && newText != null) {
			view = (ZoomableLabel) nodeView.getContent(NodeView.NOTE_VIEWER_POSITION);
		}
		else if (oldText == null && newText != null) {
			view = NodeViewFactory.getInstance().createNoteViewer();
			nodeView.addContent(view, NodeView.NOTE_VIEWER_POSITION);
		}
		else {
			assert (oldText != null && newText == null);
			nodeView.removeContent(NodeView.NOTE_VIEWER_POSITION);
			return;
		}
		view.setFont(nodeView.getMap().getDefaultNoteFont());
		view.updateText(newText);

	}

	void updateDetails(NodeView nodeView) {
		final DetailTextModel detailText = DetailTextModel.getDetailText(nodeView.getModel());
		if (detailText == null) {
			nodeView.removeContent(NodeView.DETAIL_VIEWER_POSITION);
			return;
		}
		DetailsView detailContent = (DetailsView) nodeView.getContent(NodeView.DETAIL_VIEWER_POSITION);
		if (detailContent == null) {
			detailContent = createDetailView();
			nodeView.addContent(detailContent, NodeView.DETAIL_VIEWER_POSITION);
		}
		final MapView map = nodeView.getMap();
		if (detailText.isHidden()) {
			final ArrowIcon icon = new ArrowIcon(nodeView, true);
			detailContent.setIcon(icon);
			detailContent.setBackground(null);
			detailContent.updateText("");
			detailContent.setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
		}
		else {
			detailContent.setFont(map.getDetailFont());
			detailContent.setIcon(new ArrowIcon(nodeView, false));
			detailContent.updateText(detailText.getHtml());
			detailContent.setForeground(map.getDetailForeground());
			detailContent.setBackground(nodeView.getDetailBackground());
			detailContent.setPreferredSize(null);
		}
		detailContent.revalidate();
		map.repaint();
	}

	private DetailsView createDetailView() {
	    DetailsView detailContent =  new DetailsView();
	    final DefaultMapMouseListener mouseListener = new DefaultMapMouseListener();
	    detailContent.addMouseMotionListener(mouseListener);
	    detailContent.addMouseMotionListener(DETAILS_MOUSE_LISTENER);
	    detailContent.addMouseListener(DETAILS_MOUSE_LISTENER);
	    return detailContent;
    }

}
