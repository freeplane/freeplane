/**
 * author: Marcel Genzmehr
 * 07.11.2011
 */
package org.freeplane.main.application.docear;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

/**
 * 
 */
public class FreeplaneOneTouchSplitDivider extends BasicSplitPaneDivider {
	private static final long serialVersionUID = -7634197376851132336L;
	
	// FIXME: DOCEAR - impl top/bottom collapse in next version
//	public static final int COLLAPSE_TOP = 0x1;
//	public static final int COLLAPSE_BOTTOM = 0x2;
	public static final int COLLAPSE_LEFT = 0x4;
	public static final int COLLAPSE_RIGHT = 0x8;
	
	private Point lastLocation;
	protected boolean expanded;
	protected boolean isMouseOver = false;
	private JPanel hotspot;
	private int collapseDirection;
	private int inset = 2;

	

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	/**
	 * @param ui
	 * @param collapseDirection 
	 */
	public FreeplaneOneTouchSplitDivider(BasicSplitPaneUI ui, final int collapseDirection) {
		super(ui);
		this.collapseDirection = collapseDirection;
		
		if(lastLocation == null || lastLocation.x <= 1) {
			lastLocation = 	new Point(splitPane.getDividerLocation(), 0);					
		}

		MouseListener listener = new MouseListener() {
			public void mouseReleased(MouseEvent e) {
			}

			public void mousePressed(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
				isMouseOver  = false;
				if(e.getComponent() == getHotSpot()) {
					orientation = splitPane.getOrientation();
			        setCursor((orientation == JSplitPane.HORIZONTAL_SPLIT) ?
			                  Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR) :
			                  Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));   
				}
				if(expanded) {
					e.getComponent().setCursor((orientation == JSplitPane.HORIZONTAL_SPLIT) ?
			                  Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR) :
			                  Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
				}
				repaintLabelArea();
			}

			public void mouseEntered(MouseEvent e) {
				if(e.getComponent() == getHotSpot()) {
					getHotSpot().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				}
				if(!expanded) {
					e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				}
				repaintLabelArea();
			}

			public void mouseClicked(MouseEvent e) {
				if(e.getComponent() == getHotSpot()) {
					if (expanded) {
							if(collapseDirection == COLLAPSE_LEFT) {
								lastLocation = getLocation();
								splitPane.getLeftComponent().setVisible(false);
								splitPane.getLeftComponent().setSize(0, 0);
								dragDividerTo(0);
								finishDraggingTo(0);
							} 
							else if(collapseDirection == COLLAPSE_RIGHT) {
								lastLocation = getLocation();
								splitPane.getRightComponent().setVisible(false);
								splitPane.getRightComponent().setSize(0, 0);
								dragDividerTo(splitPane.getWidth());
								finishDraggingTo(splitPane.getWidth());
							}
							splitPane.setEnabled(false);
							getHotSpot().setEnabled(true);
							expanded = false;
					}
					else {
						if(collapseDirection == COLLAPSE_LEFT) {
							splitPane.getLeftComponent().setVisible(true);
							splitPane.getLeftComponent().setSize(lastLocation.x, 0);
							dragDividerTo(lastLocation.x);
							finishDraggingTo(lastLocation.x);
						} 
						else if(collapseDirection == COLLAPSE_RIGHT) {
							splitPane.getRightComponent().setVisible(true);
							splitPane.getRightComponent().setSize(lastLocation.x, 0);
							dragDividerTo(lastLocation.x);
							finishDraggingTo(lastLocation.x);
						}
						splitPane.setEnabled(true);						
						expanded = true;
					}
				} 
				else {
					if (!expanded) {
						if(collapseDirection == COLLAPSE_LEFT) {
							splitPane.getLeftComponent().setVisible(true);
							splitPane.getLeftComponent().setSize(lastLocation.x, 0);
							dragDividerTo(lastLocation.x);
							finishDraggingTo(lastLocation.x);
						} 
						else if(collapseDirection == COLLAPSE_RIGHT) {
							splitPane.getRightComponent().setVisible(true);
							splitPane.getRightComponent().setSize(lastLocation.x, 0);
							dragDividerTo(lastLocation.x);
							finishDraggingTo(lastLocation.x);
						}
						splitPane.setEnabled(true);
						expanded = true;
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

	public void paint(Graphics g) {
		super.paint(g);
		int center_y = getHeight()/2;
		getHotSpot().setBounds(0, center_y-15, getDividerSize(), 30);
		if (getLocation().x <= 1) {
			expanded = false;
			splitPane.setEnabled(false);
			
		}
		else {
			expanded = true;
			splitPane.setEnabled(true);
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
	
	protected final void repaintLabelArea() {
//		if (expanded) {
//			splitPane.repaint();
//		} 
//		else {
//			splitPane.repaint();
//		}
	}
	
	public void paintSpecial(Graphics g) {
//		int center_y = getHeight() / 2;
//				
//		if (expanded) {
//			drawCollapseLabel(g.create(splitPane.getDividerLocation()-12, center_y-15, 12, 30));
//		}
//		else {
//			drawExpandLabel(g.create(splitPane.getDividerLocation()+getDividerSize(), center_y-15, 12, 30));			
//		}
	}
	
	public boolean isMouseOver(JSplitPane parent) {		
		return false; //isMouseOver;
	}
	
	private void drawCollapseLabel(Graphics g) {
		Dimension size = g.getClipBounds().getSize();
		int half_length = Math.round(g.getClipBounds().height*0.2f);
		int center_y = size.height / 2;

		g.setColor(getBackground());
		g.fillRect(0, 0, size.width, size.height-0);
		
		//g.setColor();
		if(this.collapseDirection == COLLAPSE_LEFT) {
			arrowLeft(g, size, half_length, center_y);
		} 
		else if(this.collapseDirection == COLLAPSE_RIGHT) {
			arrowRight(g, half_length, center_y);
		}
	}

	
	
	private void drawExpandLabel(Graphics g) {
		Dimension size = g.getClipBounds().getSize();
		int half_length = (g.getClipBounds().height-(inset*6))/2;
		int center_y = size.height / 2;
		
		g.setColor(getBackground());
		g.fillRect(0, 0, size.width, size.height-0);
		
		if(this.collapseDirection == COLLAPSE_LEFT) {			
			arrowRight(g, half_length, center_y);
		} 
		else if(this.collapseDirection == COLLAPSE_RIGHT) {
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

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
