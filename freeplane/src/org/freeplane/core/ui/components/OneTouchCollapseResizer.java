/**
 * author: Marcel Genzmehr
 * 29.11.2011
 */
package org.freeplane.core.ui.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.freeplane.core.util.LogUtils;

/**
 * 
 */
public class OneTouchCollapseResizer extends JResizer {
	private static final long serialVersionUID = 3836146387249880446L;
	public static final String COLLAPSED = OneTouchCollapseResizer.class.getPackage().getName()+".collapsed";
	private static final String ALREADY_IN_PAINT = OneTouchCollapseResizer.class.getPackage().getName()+".ALREADY_PAINTING";
	
	public enum CollapseDirection {COLLAPSE_LEFT, COLLAPSE_RIGHT};
	
	private Dimension lastComponentSize;
	protected boolean expanded = true;
	private JPanel hotspot;
	private CollapseDirection collapseDirection;
	private int inset = 2;
	private final Direction direction;
	private Integer resizeComponentIndex;
	
	private Set<ComponentCollapseListener> collapseListener = new LinkedHashSet<ComponentCollapseListener>();

	
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	/**
	 * @param d
	 */
	public OneTouchCollapseResizer(final Direction d, final CollapseDirection collapseDirection) {
		super(d);
		direction = d;
		this.setDividerSize(7);
		this.collapseDirection = collapseDirection;
		
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
			
			public void mouseReleased(MouseEvent e) {
			}

			public void mousePressed(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
				if(e.getComponent() == getHotSpot()) {
					resetCursor();
				}
				if(isExpanded()) {
					resetCursor();
				}
			}

			public void mouseEntered(MouseEvent e) {
				if(e.getComponent() == getHotSpot()) {
					getHotSpot().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				}
				if(!isExpanded()) {
					e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				}
			}

			public void mouseClicked(MouseEvent e) {
				final JComponent parent = (JComponent) getParent();
				final Component resizedComponent = getResizedParent();
				if(e.getComponent() == getHotSpot()) {					
					final Dimension size = new Dimension(resizedComponent.getPreferredSize());
					
					if (isExpanded()) {
						getHotSpot().setEnabled(true);
						lastComponentSize = new Dimension(size);
						setExpanded(false);
					}
					else {						
						setExpanded(true);
					}				
					parent.revalidate();
					parent.repaint();
				} 
				else {
					if (!isExpanded()) {	
						setExpanded(true);
						parent.revalidate();
						parent.repaint();
					}
				}
			}
		};
		getHotSpot().addMouseListener(listener);
		addMouseListener(listener);
		
		add(getHotSpot());
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
		if(direction.equals(Direction.RIGHT)){
			w = size;
			h = 0;
		}
		else if(direction.equals(Direction.LEFT)){
			h = 0;
			w = size;
		}
		else if(direction.equals(Direction.UP)){
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
		if(direction.equals(Direction.RIGHT) || direction.equals(Direction.LEFT)){
			return getPreferredSize().width;
		}
		else /*Direction.DOWN || Direction.UP*/ {
			return getPreferredSize().height;
		}
	}
	
	public void setExpanded(boolean enabled) {
		if(this.expanded != enabled) {
			try {
				Component resizedComponent = getResizedParent();
				if(resizedComponent instanceof JComponent) {
					((JComponent) resizedComponent).putClientProperty(COLLAPSED, (enabled ? null : "true"));
				}
				if(enabled) {
					if(lastComponentSize != null) {
						resizedComponent.setPreferredSize(lastComponentSize);
					}
				}
				else {
					resizedComponent.setPreferredSize(new Dimension(0,0));
				}
				
				fireCollapseStateChanged(resizedComponent, enabled);
			}
			catch (Exception e) {
				// just ignore
			}
		}
		this.expanded = enabled;
	}
	
	private Component getResizedParent() {
		final JComponent parent = (JComponent) getParent();
		if(parent != null && resizeComponentIndex == null) {
			resizeComponentIndex = getIndex();
			lastComponentSize = new Dimension(parent.getComponent(resizeComponentIndex).getPreferredSize());
		}		
		return parent.getComponent(resizeComponentIndex);
	}
	
	public void paint(Graphics g) {
		if(getClientProperty(ALREADY_IN_PAINT) != null) {
			return;
		}
		putClientProperty(ALREADY_IN_PAINT, "true");
		super.paint(g);
		int center_y = getHeight()/2;
		int divSize = getDividerSize();
		getHotSpot().setBounds(0, center_y-15, divSize, 30);
		Dimension size = getResizedParent().getPreferredSize();
		if((direction.equals(Direction.RIGHT) || direction.equals(Direction.LEFT)) && size.width <= getDividerSize()) {
			setExpanded(false);
			
		}
		else if((direction.equals(Direction.UP) || direction.equals(Direction.DOWN)) && size.height <= getDividerSize()){
			setExpanded(false);
		}
		else {
			setExpanded(true);
			//getHotSpot().setBounds(0, 0, getDividerSize(), 24);
		}
		if(getResizedParent() instanceof JComponent) {
			((JComponent) getResizedParent()).putClientProperty(COLLAPSED, (isExpanded() ? null : "true"));
		}
		getHotSpot().paint(g.create(getHotSpot().getLocation().x, getHotSpot().getLocation().y, getHotSpot().getWidth(), getHotSpot().getHeight()));
		putClientProperty(ALREADY_IN_PAINT, null);
	}
	
	private Component getHotSpot() {
		if(hotspot == null) {
			hotspot = new JPanel() {
				private static final long serialVersionUID = -5321517835206976034L;

				public void paint(Graphics g) {
					if (isExpanded()) {
						drawCollapseLabel(g);
					}
					else {
						drawExpandLabel(g);			
					}
				}
			};
		}
		return hotspot;
	}
	
	private void drawCollapseLabel(Graphics g) {
		Dimension size = g.getClipBounds().getSize();
		int half_length = Math.round(g.getClipBounds().height*0.2f);
		int center_y = size.height / 2;

		g.setColor(getBackground());
		g.fillRect(0, 0, size.width, size.height-0);
		
		//g.setColor();
		if(this.collapseDirection.equals(CollapseDirection.COLLAPSE_LEFT)) {
			arrowLeft(g, size, half_length, center_y);
		} 
		else if(this.collapseDirection.equals(CollapseDirection.COLLAPSE_RIGHT)) {
			arrowRight(g, half_length, center_y);
		}
	}

	
	
	private void drawExpandLabel(Graphics g) {
		Dimension size = g.getClipBounds().getSize();
		int half_length = (g.getClipBounds().height-(inset*6))/2;
		int center_y = size.height / 2;
		
		g.setColor(getBackground());
		g.fillRect(0, 0, size.width, size.height-0);
		
		if(this.collapseDirection.equals(CollapseDirection.COLLAPSE_LEFT)) {			
			arrowRight(g, half_length, center_y);
		} 
		else if(this.collapseDirection.equals(CollapseDirection.COLLAPSE_RIGHT)) {
			arrowLeft(g, size, half_length, center_y);
		}
	}
	
	
	/**
	 * @param g
	 * @param size
	 * @param half_length
	 * @param center_y
	 */
	private void arrowLeft(Graphics g, Dimension size, int half_length, int center_y) {
		int[] x = new int[]{inset, size.width - inset, size.width - inset};
		int[] y = new int[]{center_y, center_y-half_length, center_y + half_length};
		g.setColor(Color.DARK_GRAY);
		g.fillPolygon(x, y, 3);
		g.setColor(Color.DARK_GRAY);
		g.drawLine(inset, center_y, size.width - inset, center_y - half_length);
		g.setColor(Color.GRAY);
		g.drawLine( size.width - inset, center_y + half_length, inset, center_y);
		g.setColor(Color.GRAY);
		g.drawLine( size.width - inset, center_y - half_length, size.width - inset, center_y + half_length);
	}

	/**
	 * @param g
	 * @param half_length
	 * @param center_y
	 */
	private void arrowRight(Graphics g, int half_length, int center_y) {
		int[] x = new int[]{inset, inset, getSize().width - inset};
		int[] y = new int[]{center_y+half_length, center_y-half_length, center_y};
		
		g.setColor( Color.DARK_GRAY);
		g.fillPolygon(x,y,3);
		g.setColor( Color.DARK_GRAY);
		g.drawLine( inset, center_y + half_length, inset, center_y - half_length);
		g.setColor(Color.GRAY);
		g.drawLine( inset, center_y - half_length, getSize().width - inset, center_y);
		g.setColor( Color.LIGHT_GRAY);
		g.drawLine( getSize().width - inset, center_y, inset, center_y + half_length);
	}
	
	private int getIndex() {
		final Container parent = getParent();
		for(int i = 0; i < parent.getComponentCount(); i++ ){
			if(OneTouchCollapseResizer.this.equals(parent.getComponent(i))){
				if(direction.equals(Direction.RIGHT)){
					return i + 1;
				}
				else if(direction.equals(Direction.LEFT)){
					return i - 1;
				}
				else if(direction.equals(Direction.UP)){
					return i - 1;
				}
				else if(direction.equals(Direction.DOWN)){
					return i + 1;
				}
			}
		}
		return -1;
    }

	public void addCollapseListener(ComponentCollapseListener listener) {
		if(listener == null) return;
		
		synchronized (collapseListener) {
			collapseListener.add(listener);
		}
		
	}
	
	public void removeCollapseListener(ComponentCollapseListener listener) {
		if(listener == null) return;
		
		synchronized (collapseListener) {
			collapseListener.remove(listener);
		}		
	}
	
	private void fireCollapseStateChanged(Component resizedComponent, boolean expanded) {
		ResizeEvent event = new ResizeEvent(resizedComponent);
		synchronized (this.collapseListener) {
			for(ComponentCollapseListener listener : collapseListener) {
				try {
					if(expanded) {
						listener.componentExpanded(event);
					}
					else {
						listener.componentCollapsed(event);
					}
				}
				catch (Exception e) {
					LogUtils.severe(e);
				}
			}
		}
		
	}
	
	public interface ComponentCollapseListener {
		public void componentCollapsed(ResizeEvent event);
		public void componentExpanded(ResizeEvent event);
	}
}
