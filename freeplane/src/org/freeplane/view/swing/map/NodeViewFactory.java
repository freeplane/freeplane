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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

import javax.swing.JComponent;

import org.freeplane.core.model.NodeModel;
import org.freeplane.features.common.edge.EdgeController;
import org.freeplane.features.common.edge.EdgeStyle;
import org.freeplane.features.common.nodestyle.NodeStyleController;
import org.freeplane.features.common.nodestyle.NodeStyleModel;
import org.freeplane.view.swing.map.edge.BezierEdgeView;
import org.freeplane.view.swing.map.edge.EdgeView;
import org.freeplane.view.swing.map.edge.HiddenEdgeView;
import org.freeplane.view.swing.map.edge.LinearEdgeView;
import org.freeplane.view.swing.map.edge.SharpBezierEdgeView;
import org.freeplane.view.swing.map.edge.SharpLinearEdgeView;

class NodeViewFactory {
	private static class ContentPane extends JComponent {
		/**
         * 
         */
        private static final long serialVersionUID = 1L;
		static private LayoutManager layoutManager = new ContentPaneLayout();

		ContentPane() {
			setLayout(ContentPane.layoutManager);
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

	private EdgeView bezierEdgeView;
	private HiddenEdgeView hiddenEdgeView;
	private EdgeView linearEdgeView;
	private EdgeView sharpBezierEdgeView;
	private EdgeView sharpLinearEdgeView;

	private NodeViewFactory() {
	}

	private void fireNodeViewCreated(final NodeView newView) {
		newView.getMap().getModeController().onViewCreated(newView);
	}

	private EdgeView getBezierEdgeView() {
		if (bezierEdgeView == null) {
			bezierEdgeView = new BezierEdgeView();
		}
		return bezierEdgeView;
	}

	EdgeView getEdge(final NodeView newView) {
		final NodeModel model = newView.getModel();
		final String edgeStyle = EdgeController.getController(newView.getMap().getModeController()).getStyle(model);
		if (edgeStyle.equals(EdgeStyle.EDGESTYLE_LINEAR)) {
			return getLinearEdgeView();
		}
		else if (edgeStyle.equals(EdgeStyle.EDGESTYLE_BEZIER)) {
			return getBezierEdgeView();
		}
		else if (edgeStyle.equals(EdgeStyle.EDGESTYLE_SHARP_LINEAR)) {
			return getSharpEdgeView();
		}
		else if (edgeStyle.equals(EdgeStyle.EDGESTYLE_SHARP_BEZIER)) {
			return getSharpBezierEdgeView();
		}
		else if (edgeStyle.equals(EdgeStyle.EDGESTYLE_HIDDEN)) {
			return getHiddenEdgeView();
		}
		else {
			System.err.println("Unknown Edge Type.");
			return getLinearEdgeView();
		}
	}

	private EdgeView getHiddenEdgeView() {
		if (hiddenEdgeView == null) {
			hiddenEdgeView = new HiddenEdgeView();
		}
		return hiddenEdgeView;
	}

	private EdgeView getLinearEdgeView() {
		if (linearEdgeView == null) {
			linearEdgeView = new LinearEdgeView();
		}
		return linearEdgeView;
	}

	private EdgeView getSharpBezierEdgeView() {
		if (sharpBezierEdgeView == null) {
			sharpBezierEdgeView = new SharpBezierEdgeView();
		}
		return sharpBezierEdgeView;
	}

	private EdgeView getSharpEdgeView() {
		if (sharpLinearEdgeView == null) {
			sharpLinearEdgeView = new SharpLinearEdgeView();
		}
		return sharpLinearEdgeView;
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
			System.err.println("Tried to create a NodeView of unknown Style.");
			return new ForkMainView();
		}
	}

	/**
	 * Factory method which creates the right NodeView for the model.
	 */
	NodeView newNodeView(final NodeModel model, final int position, final MapView map, final Container parent) {
		final NodeView newView = new NodeView(model, position, map, parent);
		if (model.isRoot()) {
			final MainView mainView = new RootMainView();
			newView.setMainView(mainView);
			newView.setLayout(VerticalRootNodeViewLayout.getInstance());
		}
		else {
			newView.setMainView(newMainView(newView));
			if (newView.isLeft()) {
				newView.setLayout(LeftNodeViewLayout.getInstance());
			}
			else {
				newView.setLayout(RightNodeViewLayout.getInstance());
			}
		}
		model.addViewer(newView);
		newView.update();
		fireNodeViewCreated(newView);
		return newView;
	}
}
