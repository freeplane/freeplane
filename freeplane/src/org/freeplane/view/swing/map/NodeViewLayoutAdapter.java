/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
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
import java.awt.Point;

import javax.swing.JComponent;

import org.freeplane.core.model.NodeModel;

abstract public class NodeViewLayoutAdapter implements INodeViewLayout {
	private static Dimension minDimension;
	private int childCount;
	private JComponent content;
	protected final int LISTENER_VIEW_WIDTH = 10;
	protected Point location = new Point();
	private NodeModel model;
	private int spaceAround;
	private int vGap;
	private NodeView view;

	public void addLayoutComponent(final String arg0, final Component arg1) {
	}

	protected int getChildContentHeight(final boolean isLeft) {
		final int childCount = getChildCount();
		if (childCount == 0) {
			return 0;
		}
		int height = 0;
		int count = 0;
		for (int i = 0; i < childCount; i++) {
			final NodeView child = (NodeView) getView().getComponent(i);
			if (child.isLeft() == isLeft) {
				final int additionalCloudHeigth = child.getAdditionalCloudHeigth();
				final int contentHeight = child.getContent().getHeight();
				height += contentHeight + additionalCloudHeigth;
				if (child.getHeight() - 2 * getSpaceAround() != 0) {
					count++;
				}
			}
		}
		if (count <= 1) {
			return height;
		}
		final int contentHeight = height + getVGap() * (count - 1);
		return contentHeight;
	}

	/**
	 * @return Returns the childCount.
	 */
	protected int getChildCount() {
		return childCount;
	}

	protected int getChildHorizontalShift() {
		if (getChildCount() == 0) {
			return 0;
		}
		int shift = 0;
		for (int i = 0; i < getChildCount(); i++) {
			final NodeView child = (NodeView) getView().getComponent(i);
			int shiftCandidate;
			if (child.isLeft()) {
				shiftCandidate = -child.getContent().getX() - child.getContent().getWidth();
				if (child.isContentVisible()) {
					shiftCandidate -= child.getHGap() + child.getAdditionalCloudHeigth() / 2;
				}
			}
			else {
				shiftCandidate = -child.getContent().getX();
				if (child.isContentVisible()) {
					shiftCandidate += child.getHGap();
				}
			}
			shift = Math.min(shift, shiftCandidate);
		}
		return shift;
	}

	protected int getChildVerticalShift(final boolean isLeft) {
		if (getChildCount() == 0) {
			return 0;
		}
		int shift = 0;
		for (int i = 0; i < getChildCount(); i++) {
			final NodeView child = (NodeView) getView().getComponent(i);
			if (child.isLeft() == isLeft) {
				final int childShift = child.getShift();
				if (childShift < 0 || i == 0) {
					shift += childShift;
				}
				shift -= (child.getContent().getY() - getSpaceAround());
			}
		}
		return shift - getSpaceAround();
	}

	/**
	 * @return Returns the content.
	 */
	protected JComponent getContent() {
		return content;
	}

	/**
	 * @return Returns the model.
	 */
	protected NodeModel getModel() {
		return model;
	}

	/**
	 * @return Returns the spaceAround.
	 */
	int getSpaceAround() {
		return spaceAround;
	}

	/**
	 * @return Returns the vGap.
	 */
	int getVGap() {
		return vGap;
	}

	/**
	 * @return Returns the view.
	 */
	protected NodeView getView() {
		return view;
	}

	abstract protected void layout();

	public void layoutContainer(final Container c) {
		setUp(c);
		layout();
		shutDown();
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.LayoutManager#minimumLayoutSize(java.awt.Container)
	 */
	public Dimension minimumLayoutSize(final Container arg0) {
		if (NodeViewLayoutAdapter.minDimension == null) {
			NodeViewLayoutAdapter.minDimension = new Dimension(0, 0);
		}
		return NodeViewLayoutAdapter.minDimension;
	}

	protected void placeLeftChildren(final int childVerticalShift) {
		final int baseX = getContent().getX();
		int y = getContent().getY() + childVerticalShift;
		int right = baseX + getContent().getWidth() + getSpaceAround();
		NodeView child = null;
		for (int i = 0; i < getChildCount(); i++) {
			final NodeView component = (NodeView) getView().getComponent(i);
			if (!component.isLeft()) {
				continue;
			}
			child = component;
			final int additionalCloudHeigth = child.getAdditionalCloudHeigth() / 2;
			y += additionalCloudHeigth;
			final int shiftY = child.getShift();
			final int childHGap = child.getContent().isVisible() ? child.getHGap() : 0;
			final int x = baseX - childHGap - child.getContent().getX() - child.getContent().getWidth();
			if (shiftY < 0) {
				child.setLocation(x, y);
				y -= shiftY;
			}
			else {
				y += shiftY;
				child.setLocation(x, y);
			}
			final int childHeight = child.getHeight() - 2 * getSpaceAround();
			if (childHeight != 0) {
				y += childHeight + getVGap() + additionalCloudHeigth;
			}
			right = Math.max(right, x + child.getWidth());
		}
		final int bottom = getContent().getY() + getContent().getHeight() + getSpaceAround();
		if (child != null) {
			getView().setSize(right,
			    Math.max(bottom, child.getY() + child.getHeight() + child.getAdditionalCloudHeigth() / 2));
		}
		else {
			getView().setSize(right, bottom);
		}
	}

	protected void placeRightChildren(final int childVerticalShift) {
		final int baseX = getContent().getX() + getContent().getWidth();
		int y = getContent().getY() + childVerticalShift;
		int right = baseX + getSpaceAround();;
		NodeView child = null;
		for (int i = 0; i < getChildCount(); i++) {
			final NodeView component = (NodeView) getView().getComponent(i);
			if (component.isLeft()) {
				continue;
			}
			child = component;
			final int additionalCloudHeigth = child.getAdditionalCloudHeigth() / 2;
			y += additionalCloudHeigth;
			final int shiftY = child.getShift();
			final int childHGap = child.getContent().isVisible() ? child.getHGap() : 0;
			final int x = baseX + childHGap - child.getContent().getX();
			if (shiftY < 0) {
				child.setLocation(x, y);
				y -= shiftY;
			}
			else {
				y += shiftY;
				child.setLocation(x, y);
			}
			final int childHeight = child.getHeight() - 2 * getSpaceAround();
			if (childHeight != 0) {
				y += childHeight + getVGap() + additionalCloudHeigth;
			}
			right = Math.max(right, x + child.getWidth() + additionalCloudHeigth);
		}
		final int bottom = getContent().getY() + getContent().getHeight() + getSpaceAround();
		if (child != null) {
			getView().setSize(right,
			    Math.max(bottom, child.getY() + child.getHeight() + child.getAdditionalCloudHeigth() / 2));
		}
		else {
			getView().setSize(right, bottom);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.LayoutManager#preferredLayoutSize(java.awt.Container)
	 */
	public Dimension preferredLayoutSize(final Container c) {
		if (!c.isValid()) {
			c.validate();
		}
		return c.getSize();
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.LayoutManager#removeLayoutComponent(java.awt.Component)
	 */
	public void removeLayoutComponent(final Component arg0) {
	}

	protected void setUp(final Container c) {
		final NodeView localView = (NodeView) c;
		localView.syncronizeAttributeView();
		final int localChildCount = localView.getComponentCount() - 1;
		for (int i = 0; i < localChildCount; i++) {
			final Component component = localView.getComponent(i);
			if (component instanceof NodeView) {
				((NodeView) component).validateTree();
			}
			else {
				component.validate();
			}
		}
		view = localView;
		model = localView.getModel();
		childCount = localChildCount;
		content = localView.getContent();
		if (getModel().isVisible()) {
			setVGap(getView().getVGap());
		}
		else {
			setVGap(getView().getVisibleParentView().getVGap());
		}
		spaceAround = view.getMap().getZoomed(NodeView.SPACE_AROUND);
	}

	protected void shutDown() {
		view = null;
		model = null;
		content = null;
		childCount = 0;
		setVGap(0);
		spaceAround = 0;
	}

	public void setVGap(final int vGap) {
		this.vGap = vGap;
	}
}
