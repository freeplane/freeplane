/**
 * author: Marcel Genzmehr
 * 29.11.2011
 */
package org.freeplane.core.ui.components.resizer;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.Box;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.IMapViewManager;

/**
 *
 */
class OneTouchCollapseResizer extends JResizer {
	private static final long serialVersionUID = 1;

	private boolean expanded = false;
	private final int INSET = 2;
	private final String sizePropertyName;
	private final ComponentAdapter sizeChangeListener;





	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	/**
	 * @param d
	 * @param component 
	 * @param component 
	 */
	OneTouchCollapseResizer(final Direction d, Component resizedComponent, String propertyNameBase) {
		super(d, resizedComponent);
		this.sizePropertyName = propertyNameBase + ".size";
		this.setDividerSize((int)(UITools.FONT_SCALE_FACTOR * 5 + 0.5));
		
		sizeChangeListener = new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				setHotspotBounds();
			}
			
		};
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				Component resizedComponent = getResizedComponent();
				if (resizedComponent.isPreferredSizeSet()) {
					int preferredSize = direction.getPreferredSize(resizedComponent);
					if (preferredSize>= minimumCollapseSize()) {
						ResourceController.getResourceController().setProperty(sizePropertyName, preferredSize);
					}
				}
			}
			@Override
			public void mouseClicked(MouseEvent e) {
				if (expanded) {
					setExpanded(false);
				}
				else {
					setExpanded(true);
				}
			}
		});
		
		addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
               	if(sliderLock || !expanded)
					setExpanded(true);
            }
		});
		final UIComponentVisibilityDispatcher dispatcher = UIComponentVisibilityDispatcher.install(parentBox, propertyNameBase);
		dispatcher.setResizer(this);
		expanded = ! dispatcher.isVisible();
		setExpanded(! expanded);
		parentBox.addComponentListener(sizeChangeListener);

	}

	Box getParentBox() {
		return parentBox;
	}

	private void setDividerSize(int size) {
		final int w;
		final int h;
		if(direction == Direction.RIGHT){
			w = size;
			h = size * 2;
		}
		else if(direction == Direction.LEFT){
			h = size * 2;
			w = size;
		}
		else if(direction == Direction.UP){
			h = size;
			w = size * 2;
		}
		else /*Direction.DOWN*/ {
			h = size;
			w = size * 2;
		}
		setPreferredSize(new Dimension(w, h));
	}

	private int getDividerSize() {
		if(direction == Direction.RIGHT || direction == Direction.LEFT){
			return getPreferredSize().width;
		}
		else /*Direction.DOWN || Direction.UP*/ {
			return getPreferredSize().height;
		}
	}

	void setExpanded(boolean expanded) {
		if(this.expanded != expanded) {
			this.expanded = expanded;
			try {
				Component resizedComponent = getResizedComponent();
				if(expanded) {
					if(!sliderLock){
						int size = ResourceController.getResourceController().getIntProperty(sizePropertyName, 0);
						if(size >= minimumCollapseSize()) {
							direction.setPreferredSize(resizedComponent, size);
						}
						else
							resizedComponent.setPreferredSize(null);
					}
				}
				else {
					resizedComponent.setPreferredSize(new Dimension(0,0));
					IMapViewManager mapViewManager = Controller.getCurrentController().getMapViewManager();
					mapViewManager.moveFocusFromDescendantToSelection(resizedComponent);
				}

				resizedComponent.setVisible(expanded);

				UIComponentVisibilityDispatcher.of(parentBox).setProperty(expanded);

				parentBox.revalidate();
				parentBox.repaint();
			}
			catch (Exception e) {
				LogUtils.warn("Exception in org.freeplane.core.ui.components.OneTouchCollapseResizer.setExpanded(enabled): "+e);
			}
		}

	}

	private int minimumCollapseSize() {
		return 3 * getDividerSize();
	}

	private Component getResizedComponent() {
		return resizedComponent;
	}

	@Override
    public void paintComponent(Graphics g) {
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		int hotspotX;
		int hotspotY;
		int hotspotWidth;
		int hotspotHeight;
		int dividerSize = getDividerSize();
		if((direction == Direction.RIGHT || direction == Direction.LEFT)) {
			int centerY = getHeight()/2;
			hotspotX =0;
			hotspotY = centerY-dividerSize;
			hotspotWidth = dividerSize;
			hotspotHeight = 2 * 2 * dividerSize;
		}
		else {
			int centerX = getWidth()/2;
			hotspotX =centerX-dividerSize;
			hotspotY = 0;
			hotspotWidth = 2 * 2 * dividerSize;
			hotspotHeight = dividerSize;
		}

		Graphics arrowGraphics = g.create(hotspotX, hotspotY, hotspotWidth, hotspotHeight);
		arrowGraphics.setColor(getForeground());
		drawControlArrow(arrowGraphics);
		arrowGraphics.dispose();
	}

	private void drawControlArrow(Graphics g) {
		Dimension size = g.getClipBounds().getSize();
		int half_length = (size.height-(INSET*6))/2;
		int center_y = size.height / 2;

		int half_width = (size.width-(INSET*6))/2;
		int center_x = size.width / 2;

		if(expanded && direction == Direction.RIGHT || ! expanded && direction == Direction.LEFT) {
			arrowRight(g, half_length, center_y);
		}
		else if(expanded && direction == Direction.LEFT || ! expanded && direction == Direction.RIGHT) {
			arrowLeft(g, half_length, center_y);
		}
		else if(expanded && direction == Direction.DOWN || ! expanded && direction == Direction.UP) {
			arrowDown(g, half_width, center_x);
		}
		else if(expanded && direction == Direction.UP || ! expanded && direction == Direction.DOWN) {
			arrowUp(g, half_width, center_x);
		}
	}


	/**
	 * @param g
	 * @param half_length
	 * @param center_y
	 */
	private void arrowLeft(Graphics g, int half_length, int center_y) {
		int[] x = new int[]{INSET, getSize().width - INSET, getSize().width - INSET};
		int[] y = new int[]{center_y, center_y-half_length, center_y + half_length};
		g.fillPolygon(x, y, 3);
		g.drawLine(INSET, center_y, getSize().width - INSET, center_y - half_length);
		g.drawLine( getSize().width - INSET, center_y + half_length, INSET, center_y);
		g.drawLine( getSize().width - INSET, center_y - half_length, getSize().width - INSET, center_y + half_length);
	}

	/**
	 * @param g
	 * @param half_length
	 * @param center_y
	 */
	private void arrowRight(Graphics g, int half_length, int center_y) {
		int[] x = new int[]{INSET, INSET, getSize().width - INSET};
		int[] y = new int[]{center_y+half_length, center_y-half_length, center_y};

		g.fillPolygon(x,y,3);
		g.drawLine( INSET, center_y + half_length, INSET, center_y - half_length);
		g.drawLine( INSET, center_y - half_length, getSize().width - INSET, center_y);
		g.drawLine( getSize().width - INSET, center_y, INSET, center_y + half_length);
	}

	private void arrowUp(Graphics g, int half_length, int center_x) {
		int[] y = new int[]{INSET, getSize().height - INSET, getSize().height - INSET};
		int[] x = new int[]{center_x, center_x-half_length, center_x + half_length};

		g.fillPolygon(x, y, 3);
		g.drawLine(center_x + half_length, getSize().height - INSET, center_x, INSET);
		g.drawLine(center_x, INSET, center_x - half_length, getSize().height - INSET);
		g.drawLine(center_x - half_length, getSize().height - INSET, center_x + half_length, getSize().height - INSET);

	}

	private void arrowDown(Graphics g, int half_length, int center_x) {
		int[] y = new int[]{INSET, INSET, getSize().height - INSET};
		int[] x = new int[]{center_x+half_length, center_x-half_length, center_x};

		g.fillPolygon(x,y,3);
		g.drawLine( center_x - half_length, INSET, center_x, getSize().height- INSET);
		g.drawLine( center_x + half_length, INSET, center_x - half_length, INSET);
		g.drawLine(center_x,  getSize().height - INSET, center_x + half_length, INSET);
	}

	private void setHotspotBounds() {
		if(direction.getPreferredSize(getResizedComponent()) < minimumCollapseSize()) {
			setExpanded(false);
		}
	}
}
