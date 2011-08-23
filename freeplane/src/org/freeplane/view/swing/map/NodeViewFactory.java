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
import java.awt.event.MouseEvent;
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

import org.freeplane.core.ui.AMouseListener;
import org.freeplane.core.ui.DelayedMouseListener;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.nodestyle.NodeStyleModel;
import org.freeplane.features.note.NoteController;
import org.freeplane.features.note.NoteModel;
import org.freeplane.features.text.DetailTextModel;
import org.freeplane.features.text.TextController;
import org.freeplane.features.text.mindmapmode.MTextController;
import org.freeplane.view.swing.ui.DefaultMapMouseListener;
import org.freeplane.view.swing.ui.DefaultMapMouseReceiver;

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
					final Dimension preferredCompSize = width == 0 ? new Dimension() : component.getPreferredSize();
					if (component instanceof MainView) {
						component.setBounds(0, y, width, preferredCompSize.height);
					}
					else {
						final int x = (int) (component.getAlignmentX() * (width - preferredCompSize.width));
						component.setBounds(x, y, preferredCompSize.width, preferredCompSize.height);
					}
					y += preferredCompSize.height;
					if (component instanceof ForkMainView){
						y += ((ForkMainView)component).getEdgeWidth();
					}
				}
				else{
					component.setBounds(0, y, 0, 0);
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
					if (component instanceof ForkMainView){
						prefSize.height += ((ForkMainView)component).getEdgeWidth();
					}
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

	MainView newMainView(NodeView node) {
		final ModeController modeController = node.getMap().getModeController();
		final NodeModel model = node.getModel();
		MainView view;
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

		if (shape == null || shape.equals(NodeStyleModel.STYLE_FORK)) {
			if (model.isRoot())
				view = new RootMainView(NodeStyleModel.STYLE_FORK);
			else
				view = new ForkMainView();
		}
		else if (shape.equals(NodeStyleModel.STYLE_BUBBLE)) {
			if (model.isRoot())
				view = new RootMainView(NodeStyleModel.STYLE_BUBBLE);
			else
				view =  new BubbleMainView();
		}
		else {
			System.err.println("Tried to create a NodeView of unknown Style " + String.valueOf(shape));
			view = new ForkMainView();
		}
		NodeTooltipManager toolTipManager = NodeTooltipManager.getSharedInstance(modeController);
		toolTipManager.registerComponent(view);
		return view;
	}

	/**
	 * Factory method which creates the right NodeView for the model.
	 */
	NodeView newNodeView(final NodeModel model, final int position, final MapView map, final Container parent) {
		final NodeView newView = new NodeView(model, position, map, parent);
		model.addViewer(newView);
		newView.setLayout(SelectableLayout.getInstance());
		newView.setMainView(newMainView(newView));
        updateNoteViewer(newView);
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
			final TextController textController = TextController.getController();
			final NodeModel model = nodeView.getModel();
			final NoteModel extension = NoteModel.getNote(model);
			if(extension != null){
				final String originalText = extension.getHtml();
				try {
					newText = textController.getTransformedTextNoThrow(originalText, model, extension);
					if (!NodeView.DONT_MARK_FORMULAS && newText != originalText)
						newText = colorize(newText, "green");
				}
				catch (Exception e) {
					newText = colorize(TextUtils.format("MainView.errorUpdateText", originalText, e.getLocalizedMessage())
						.replace("\n", "<br>"), "red");
				}
			}
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
	private String colorize(final String text, String color) {
		return "<span style=\"color:" + color + ";font-style:italic;\">" + text + "</span>";
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
		if (detailText.isHidden()) {
			detailContent.updateText("");
			detailContent.setBackground(null);
			detailContent.setIcon(new ArrowIcon(nodeView, true));
		}
		else {
			detailContent.updateText(detailText.getHtml());
			final MapView map = nodeView.getMap();
			detailContent.setFont(map.getDetailFont());
			detailContent.setForeground(map.getDetailForeground());
			detailContent.setBackground(nodeView.getDetailBackground());
			detailContent.setIcon(new ArrowIcon(nodeView, false));
		}
	}

	private DetailsView createDetailView() {
	    DetailsView detailContent =  new DetailsView();
	    final DefaultMapMouseReceiver mouseReceiver = new DefaultMapMouseReceiver();
	    final DefaultMapMouseListener mouseListener = new DefaultMapMouseListener(mouseReceiver);
	    detailContent.addMouseMotionListener(mouseListener);
	    detailContent.addMouseListener(new DelayedMouseListener(new AMouseListener() {
	    
	    	@Override
	        public void mousePressed(MouseEvent e) {
	    		mouseReceiver.mousePressed(e);
	        }
	    
	    	@Override
	        public void mouseReleased(MouseEvent e) {
	    		mouseReceiver.mouseReleased(e);
	        }
	    
	    	@Override
	        public void mouseClicked(MouseEvent e) {
	    		final NodeView nodeView = (NodeView)SwingUtilities.getAncestorOfClass(NodeView.class, e.getComponent());
	    		final NodeModel model = nodeView.getModel();
	    		TextController controller = TextController.getController();
	    		final ZoomableLabel component = (ZoomableLabel) e.getComponent();
	    		if(e.getX() < component.getIconWidth())
	    			controller.setDetailsHidden(model, ! DetailTextModel.getDetailText(model).isHidden());
	    		else if(controller instanceof MTextController && e.getClickCount() == 2){
	    			((MTextController) controller).editDetails(model, e, e.isAltDown());
	    		}
	        }
	    	
	    }, 2, MouseEvent.BUTTON1));
	    return detailContent;
    }

}
