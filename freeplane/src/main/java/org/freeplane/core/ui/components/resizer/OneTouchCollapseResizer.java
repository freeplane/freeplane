/**
 * author: Marcel Genzmehr
 * 29.11.2011
 */
package org.freeplane.core.ui.components.resizer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;

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

	private boolean expanded = true;
	private final JComponent hotspot;
	private final int INSET = 2;
	private final Direction direction;
	private Integer resizeComponentIndex;

	private Dimension lastPreferredSize = null;


	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	/**
	 * @param d
	 * @param component 
	 */
	OneTouchCollapseResizer(final Direction d) {
		super(d);
		direction = d;
		this.setDividerSize((int)(UITools.FONT_SCALE_FACTOR * 10 + 0.5));
		
		ComponentAdapter sizeChangeListener = new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				setHotspotBounds();
			}
			
		};
		
		addComponentListener(sizeChangeListener);


		MouseListener listener = new MouseListener() {
			private void resetCursor() {
				if(d.equals(Direction.RIGHT)){
					setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
				}
				else if(d.equals(Direction.LEFT)){
					setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
				}
				else if(d.equals(Direction.UP)){
					setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
				}
				else /*Direction.DOWN*/ {
					setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				if(e.getComponent() == OneTouchCollapseResizer.this) {
					if(!isExpanded() || sliderLock) {
						e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					}
					else
						resetCursor();
				}
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				if((e.getComponent() == hotspot) || sliderLock) {

					if (isExpanded()) {
						hotspot.setEnabled(true);
						setExpanded(false);
					}
					else {
						setExpanded(true);
					}
				}
				else {
					if (!isExpanded()) {
						setExpanded(true);
					}
				}
			}
		};
		hotspot = new JComponent(){
			private static final long serialVersionUID = 1L;
		};
		hotspot.setOpaque(false);
		hotspot.addMouseListener(listener);
		hotspot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		addMouseListener(listener);

		add(hotspot);
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public boolean isExpanded() {
		return this.expanded;
	}

	public void setDividerSize(int size) {
		final int w;
		final int h;
		if(direction == Direction.RIGHT){
			w = size;
			h = 0;
		}
		else if(direction == Direction.LEFT){
			h = 0;
			w = size;
		}
		else if(direction == Direction.UP){
			h = size;
			w = 0;
		}
		else /*Direction.DOWN*/ {
			h = size;
			w = 0;
		}
		setPreferredSize(new Dimension(w, h));
	}

	public int getDividerSize() {
		if(direction == Direction.RIGHT || direction == Direction.LEFT){
			return getPreferredSize().width;
		}
		else /*Direction.DOWN || Direction.UP*/ {
			return getPreferredSize().height;
		}
	}

	public void setExpanded(boolean expanded) {
		if(this.expanded != expanded) {
			this.expanded = expanded;
			try {
				Component resizedComponent = getResizedComponent();
				if(expanded) {
					resizedComponent.setPreferredSize(lastPreferredSize);
				}
				else {
					lastPreferredSize = resizedComponent.isPreferredSizeSet() 
					        && direction.getPreferredSize(resizedComponent) > getDividerSize() ?  resizedComponent.getPreferredSize() : null;
					resizedComponent.setPreferredSize(new Dimension(0,0));
				}
				IMapViewManager mapViewManager = Controller.getCurrentController().getMapViewManager();
				mapViewManager.moveFocusFromDescendantToSelection(resizedComponent);
				resizedComponent.setVisible(expanded);

				fireCollapseStateChanged(resizedComponent, expanded);

				final JComponent parent = (JComponent) getParent();
				if(parent != null) {
					parent.revalidate();
					parent.repaint();
				}
			}
			catch (Exception e) {
				LogUtils.warn("Exception in org.freeplane.core.ui.components.OneTouchCollapseResizer.setExpanded(enabled): "+e);
			}
		}

	}

	private Component getResizedComponent() {
		final JComponent parent = (JComponent) getParent();
		if(parent != null && resizeComponentIndex == null) {
			resizeComponentIndex = getIndex();
		}
		return parent.getComponent(resizeComponentIndex);
	}

	@Override
    public void paintComponent(Graphics g) {
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		Graphics arrowGraphics = g.create(hotspot.getX(), hotspot.getY(), hotspot.getWidth(), hotspot.getHeight());
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
		g.setColor(Color.DARK_GRAY);
		g.fillPolygon(x, y, 3);
		g.setColor(Color.DARK_GRAY);
		g.drawLine(INSET, center_y, getSize().width - INSET, center_y - half_length);
		g.setColor(Color.GRAY);
		g.drawLine( getSize().width - INSET, center_y + half_length, INSET, center_y);
		g.setColor(Color.GRAY);
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

		g.setColor( Color.DARK_GRAY);
		g.fillPolygon(x,y,3);
		g.setColor( Color.DARK_GRAY);
		g.drawLine( INSET, center_y + half_length, INSET, center_y - half_length);
		g.setColor(Color.GRAY);
		g.drawLine( INSET, center_y - half_length, getSize().width - INSET, center_y);
		g.setColor( Color.LIGHT_GRAY);
		g.drawLine( getSize().width - INSET, center_y, INSET, center_y + half_length);
	}

	private void arrowUp(Graphics g, int half_length, int center_x) {
		int[] y = new int[]{INSET, getSize().height - INSET, getSize().height - INSET};
		int[] x = new int[]{center_x, center_x-half_length, center_x + half_length};

		g.setColor(Color.DARK_GRAY);
		g.fillPolygon(x, y, 3);

		g.setColor(Color.GRAY);
		g.drawLine(center_x + half_length, getSize().height - INSET, center_x, INSET);
		g.setColor(Color.DARK_GRAY);
		g.drawLine(center_x, INSET, center_x - half_length, getSize().height - INSET);
		g.setColor(Color.LIGHT_GRAY);
		g.drawLine(center_x - half_length, getSize().height - INSET, center_x + half_length, getSize().height - INSET);

	}

	private void arrowDown(Graphics g, int half_length, int center_x) {
		int[] y = new int[]{INSET, INSET, getSize().height - INSET};
		int[] x = new int[]{center_x+half_length, center_x-half_length, center_x};

		g.setColor( Color.DARK_GRAY);
		g.fillPolygon(x,y,3);

		g.setColor(Color.GRAY);
		g.drawLine( center_x - half_length, INSET, center_x, getSize().height- INSET);
		g.setColor( Color.DARK_GRAY);
		g.drawLine( center_x + half_length, INSET, center_x - half_length, INSET);
		g.setColor( Color.LIGHT_GRAY);
		g.drawLine(center_x,  getSize().height - INSET, center_x + half_length, INSET);
	}

	private int getIndex() {
		final Container parent = getParent();
		for(int i = 0; i < parent.getComponentCount(); i++ ){
			if(OneTouchCollapseResizer.this.equals(parent.getComponent(i))){
				if(direction == Direction.RIGHT){
					return i + 1;
				}
				else if(direction == Direction.LEFT){
					return i - 1;
				}
				else if(direction == Direction.UP){
					return i - 1;
				}
				else if(direction == Direction.DOWN){
					return i + 1;
				}
			}
		}
		return -1;
    }

	private void fireCollapseStateChanged(Component resizedComponent, boolean expanded) {
		UIComponentVisibilityDispatcher.of((JComponent) resizedComponent.getParent()).setProperty(expanded);
	}

	@Override
	void onSizeChanged(Component resizedComponent) {
		final UIComponentVisibilityDispatcher dispatcher = UIComponentVisibilityDispatcher.of((JComponent) resizedComponent.getParent());
		final String sizePropertyName = dispatcher.getPropertyName() +  ".size";
		ResourceController.getResourceController().setProperty(sizePropertyName, String.valueOf(direction.getPreferredSize(resizedComponent)));
	}



	private void setHotspotBounds() {
		if((direction == Direction.RIGHT || direction == Direction.LEFT)) {
			int center_y = getHeight()/2;
			int divSize = getDividerSize();
			hotspot.setBounds(0, center_y-divSize, divSize, 2 * divSize);
		}
		else {
			int center_x = getWidth()/2;
			int divSize = getDividerSize();
			hotspot.setBounds(center_x-divSize, 0, 2 * divSize, divSize);
		}
		Dimension size = getResizedComponent().getPreferredSize();
		if((direction == Direction.RIGHT || direction == Direction.LEFT) && size.width <= getDividerSize()) {
			setExpanded(false);

		}
		else if((direction == Direction.UP || direction == Direction.DOWN) && size.height <= getDividerSize()){
			setExpanded(false);
		}
		else {
			setExpanded(true);
		}
	}
}
