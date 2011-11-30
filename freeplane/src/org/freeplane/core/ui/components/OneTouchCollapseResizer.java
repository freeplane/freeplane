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

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * 
 */
public class OneTouchCollapseResizer extends JResizer {

	private static final long serialVersionUID = 3836146387249880446L;
	
	public enum CollapseDirection {COLLAPSE_LEFT, COLLAPSE_RIGHT};
	
	private Dimension lastComponentSize;
	protected boolean expanded;
	private JPanel hotspot;
	private CollapseDirection collapseDirection;
	private int inset = 2;
	private final Direction direction;
	private Integer resizeComponentIndex;
	
	
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
				if(expanded) {
					resetCursor();
				}
			}

			public void mouseEntered(MouseEvent e) {
				if(e.getComponent() == getHotSpot()) {
					getHotSpot().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				}
				if(!expanded) {
					e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				}
			}

			public void mouseClicked(MouseEvent e) {
				final JComponent parent = (JComponent) getParent();
				final Component resizedComponent = parent.getComponent(resizeComponentIndex);
				if(e.getComponent() == getHotSpot()) {					
					final Dimension size = new Dimension(resizedComponent.getPreferredSize());
					
					if (expanded) {
						getHotSpot().setEnabled(true);
						lastComponentSize = new Dimension(size);
//						if(d.equals(Direction.RIGHT) || d.equals(Direction.LEFT)){
//							size.width = getDivederSize();
//						}
//						else if(d.equals(Direction.UP) || d.equals(Direction.DOWN)){
//							size.height = getDivederSize();
//						}						
						resizedComponent.setPreferredSize(new Dimension(0,0));
						expanded = false;
					}
					else {
						resizedComponent.setPreferredSize(lastComponentSize);						
						expanded = true;
					}				
					parent.revalidate();
					parent.repaint();
				} 
				else {
					if (!expanded) {
						resizedComponent.setPreferredSize(lastComponentSize);	
						expanded = true;
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
	
	
	
	public void setDividerSize(int size) {
		final int w;
		final int h;
		if(direction.equals(Direction.RIGHT)){
			w = size;
			h = 0;
		}
		else if(direction.equals(Direction.LEFT)){
			h = size;
			w = 0;
		}
		else if(direction.equals(Direction.UP)){
			h = 0;
			w = size;
		}
		else /*Direction.DOWN*/ {
			h = 0;
			w = size;
		}		
		setPreferredSize(new Dimension(w, h));
	}
	
	public int getDivederSize() {
		if(direction.equals(Direction.RIGHT) || direction.equals(Direction.LEFT)){
			return getPreferredSize().width;
		}
		else /*Direction.DOWN || Direction.UP*/ {
			return getPreferredSize().height;
		}
	}
	
	private Component getResizedParent() {
		final JComponent parent = (JComponent) getParent();
		return parent.getComponent(resizeComponentIndex);
	}
	
	public void paint(Graphics g) {
		if(resizeComponentIndex == null) {
			resizeComponentIndex = getIndex();			
			lastComponentSize = new Dimension(getResizedParent().getPreferredSize());
		}
		super.paint(g);
		int center_y = getHeight()/2;
		int divSize = getDivederSize();
		getHotSpot().setBounds(0, center_y-15, divSize, 30);
		Dimension size = getResizedParent().getPreferredSize();
		if((direction.equals(Direction.RIGHT) || direction.equals(Direction.LEFT)) && size.width <= getDivederSize()) {
			expanded = false;
		}
		else if((direction.equals(Direction.UP) || direction.equals(Direction.DOWN)) && size.height <= getDivederSize()){
			expanded = false;
		}
		else {
			expanded = true;
			//getHotSpot().setBounds(0, 0, getDividerSize(), 24);
		}
		getHotSpot().paint(g.create(getHotSpot().getLocation().x, getHotSpot().getLocation().y, getHotSpot().getWidth(), getHotSpot().getHeight()));
	}
	
	private Component getHotSpot() {
		if(hotspot == null) {
			hotspot = new JPanel() {
				private static final long serialVersionUID = -5321517835206976034L;

				public void paint(Graphics g) {
					if (expanded) {
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

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
