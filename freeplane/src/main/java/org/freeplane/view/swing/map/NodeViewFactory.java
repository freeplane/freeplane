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
import java.awt.Container;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.freeplane.core.ui.IMouseListener;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.nodestyle.NodeGeometryModel;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.nodestyle.NodeStyleModel;
import org.freeplane.features.note.NoteController;
import org.freeplane.features.note.NoteModel;
import org.freeplane.features.text.DetailModel;
import org.freeplane.features.text.TextController;
import org.freeplane.view.swing.ui.DefaultMapMouseListener;
import org.freeplane.view.swing.ui.DetailsViewMouseListener;

import com.jgoodies.common.base.Objects;

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
		final MainView view = new MainView();
		NodeGeometryModel shapeConfiguration = shapeConfiguration(node);
		final MainView oldView = node.getMainView();
		if(oldView != null && oldView.getShapeConfiguration().equals(shapeConfiguration))
			return oldView;
		final MainViewPainter shape = createViewPainter(view, shapeConfiguration);
		view.setPainter(shape);

		final ModeController modeController = node.getMap().getModeController();
		NodeTooltipManager toolTipManager = NodeTooltipManager.getSharedInstance(modeController);
		toolTipManager.registerComponent(view);
		return view;
	}

	void updateViewPainter(final NodeView node) {
		NodeGeometryModel shapeConfiguration = shapeConfiguration(node);
		final MainView mainView = node.getMainView();
		final MainViewPainter painter = createViewPainter(mainView, shapeConfiguration);
		mainView.setPainter(painter);
	}

	private MainViewPainter createViewPainter(final MainView view, NodeGeometryModel shapeConfiguration) {
		final MainViewPainter shape;
		switch(shapeConfiguration.getShape()){
		case fork:
			shape =  new ForkPainter(view);
			break;
		case bubble:
			shape =  new BubblePainter(view, shapeConfiguration);
			break;
		case oval:
			shape =  new OvalPainter(view, shapeConfiguration);
			break;
		case rectangle:
			shape =  new RectanglePainter(view, shapeConfiguration);
			break;
		case wide_hexagon:
			shape = new WideHexagonPainter(view, shapeConfiguration);
			break;
		case narrow_hexagon:
			shape = new NarrowHexagonPainter(view, shapeConfiguration);
			break;
		default:
			System.err.println("Tried to create a NodeView of unknown Style " + String.valueOf(shapeConfiguration.getShape()));
			shape = new ForkPainter(view);

		}
		return shape;
	}

	private NodeGeometryModel shapeConfiguration(NodeView node) {
		final ModeController modeController = node.getMap().getModeController();
		final NodeModel model = node.getModel();
		NodeGeometryModel shapeConfiguration = NodeStyleController.getController(modeController).getShapeConfiguration(model);
		if (shapeConfiguration.getShape().equals(NodeStyleModel.Shape.combined)) {
			if (node.isFolded()) {
				shapeConfiguration= shapeConfiguration.withShape(NodeStyleModel.Shape.bubble);
			}
			else {
				shapeConfiguration = NodeGeometryModel.FORK;
			}
		}
		else while(shapeConfiguration.getShape().equals(NodeStyleModel.Shape.as_parent)){
			NodeView parent = node.getParentView();
			if (parent == null)
				shapeConfiguration = NodeGeometryModel.DEFAULT_ROOT_OVAL;
			else if (parent.getParentView() == null)
				shapeConfiguration = NodeGeometryModel.FORK;
			else
				shapeConfiguration = parent.getMainView().getShapeConfiguration();
		}
		return shapeConfiguration;
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
				@Override
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
		newView.update();
        fireNodeViewCreated(newView);
        newView.addChildViews();
	}

	private static final IMouseListener DETAILS_MOUSE_LISTENER = new DetailsViewMouseListener();
	private static final IMouseListener NOTE_MOUSE_LISTENER = new NoteViewMouseListener();

	public ZoomableLabel createNoteViewer() {
		final ZoomableLabel label = new ZoomableLabel();
		label.addMouseListener(NOTE_MOUSE_LISTENER);
		label.addMouseMotionListener(NOTE_MOUSE_LISTENER);
		label.setIcon(NoteController.bwNoteIcon);
		label.setVerticalTextPosition(JLabel.TOP);
		return label;
	}



	void updateNoteViewer(NodeView nodeView, int minNodeWidth, int maxNodeWidth) {
		String newText  = null;
		Icon newIcon = null;
		MapView map = nodeView.getMap();
		if (map.showNotes()) {
			final NodeModel model = nodeView.getModel();
			final NoteModel note = NoteModel.getNote(model);
            if (note != null) {
    			String text = note.getText();
    			if(text != null) {
    			try {
    				TextController textController = map.getModeController().getExtension(TextController.class);
    				final Object transformedContent = textController.getTransformedObject(model, note, text);
    				newIcon = textController.getIcon(transformedContent);
    				
    				newText = transformedContent.toString();
    			}
    			catch (Throwable e) {
    				LogUtils.warn(e.getMessage());
    				newText = TextUtils.format("MainView.errorUpdateText", text, e.getLocalizedMessage());
    			}
    			}
			}
		}
		ZoomableLabel noteView = (ZoomableLabel) nodeView.getContent(NodeView.NOTE_VIEWER_POSITION);
		if (noteView == null && newText == null && newIcon == null
				|| noteView != null && newIcon == null 
				&& Objects.equals(newText, noteView.getText())
				&& noteView.getTextRenderingIcon() == null) {
			return;
		}
		if(noteView != null && newText == null && newIcon == null){
			nodeView.removeContent(NodeView.NOTE_VIEWER_POSITION);
			return;
		}
		if (noteView == null && (newText != null || newIcon != null)) {
			noteView = NodeViewFactory.getInstance().createNoteViewer();
			nodeView.addContent(noteView, NodeView.NOTE_VIEWER_POSITION);
		}
		noteView.setFont(map.getNoteFont());
		noteView.setForeground(map.getNoteForeground());
		final Color noteBackground = map.getNoteBackground();
		noteView.setBackground(noteBackground != null ? noteBackground : map.getBackground());
		noteView.setHorizontalAlignment(map.getNoteHorizontalAlignment());
		noteView.updateText(newText);
		noteView.setTextRenderingIcon(newIcon);
		noteView.setMinimumWidth(minNodeWidth);
		noteView.setMaximumWidth(maxNodeWidth);
		noteView.revalidate();
		map.repaint();

	}

	void updateDetails(NodeView nodeView, int minNodeWidth, int maxNodeWidth) {
		NodeModel node = nodeView.getModel();
		String detailTextText = DetailModel.getDetailText(node);
		if (detailTextText == null) {
			nodeView.removeContent(NodeView.DETAIL_VIEWER_POSITION);
			return;
		}
		final DetailModel detailText = DetailModel.getDetail(node);
		DetailsView detailContent = (DetailsView) nodeView.getContent(NodeView.DETAIL_VIEWER_POSITION);
		if (detailContent == null) {
			detailContent = createDetailView();
			nodeView.addContent(detailContent, NodeView.DETAIL_VIEWER_POSITION);
		}
		final MapView map = nodeView.getMap();
		if (detailText.isHidden()) {
			final ArrowIcon icon = new ArrowIcon(nodeView, true);
			detailContent.setIcon(icon);
			detailContent.updateText("");
		}
		else {
			detailContent.setFont(map.getDetailFont());
			detailContent.setHorizontalAlignment(map.getDetailHorizontalAlignment());
			detailContent.setIcon(new ArrowIcon(nodeView, false));
			String text;
			try {
				TextController textController = map.getModeController().getExtension(TextController.class);
				final Object transformedContent = textController.getTransformedObject(node, detailText, detailTextText);
				Icon icon = textController.getIcon(transformedContent);
				detailContent.setTextRenderingIcon(icon);
				text = transformedContent.toString();
			}
			catch (Throwable e) {
				LogUtils.warn(e.getMessage());
				text = TextUtils.format("MainView.errorUpdateText", detailTextText, e.getLocalizedMessage());
			}
			detailContent.updateText(text);
		}
		detailContent.setForeground(map.getDetailForeground());
		detailContent.setBackground(map.getDetailBackground());
		detailContent.setMinimumWidth(minNodeWidth);
		detailContent.setMaximumWidth(maxNodeWidth);
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
