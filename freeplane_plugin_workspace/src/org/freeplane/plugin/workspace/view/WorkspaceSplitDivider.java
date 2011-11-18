/**
 * author: Marcel Genzmehr
 * 07.11.2011
 */
package org.freeplane.plugin.workspace.view;

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

import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.WorkspacePreferences;

/**
 * 
 */
public class WorkspaceSplitDivider extends BasicSplitPaneDivider {
	private static final long serialVersionUID = 3680863351623884961L;
	private Point lastLocation;
	protected boolean expanded;
	protected boolean isMouseOver = false;
	private JPanel hotspot;

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	/**
	 * @param ui
	 */
	public WorkspaceSplitDivider(BasicSplitPaneUI ui) {
		super(ui);
		expanded = Controller.getCurrentController().getResourceController()
				.getBooleanProperty(WorkspacePreferences.SHOW_WORKSPACE_PROPERTY_KEY);
		lastLocation = new Point(Controller.getCurrentController().getResourceController()
				.getIntProperty(WorkspacePreferences.WORKSPACE_WIDTH_PROPERTY_KEY, 200), 0);
		
		if(lastLocation == null || lastLocation.x <= 1) {
			lastLocation = new Point(200,0);						
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
							lastLocation = getLocation();
							splitPane.getLeftComponent().setVisible(false);
							splitPane.getLeftComponent().setSize(0, 0);
							dragDividerTo(0);
							finishDraggingTo(0);
							splitPane.setEnabled(false);
							getHotSpot().setEnabled(true);
							expanded = false;
					}
					else {
						splitPane.getLeftComponent().setVisible(true);
						splitPane.getLeftComponent().setSize(lastLocation.x, 0);
						dragDividerTo(lastLocation.x);
						finishDraggingTo(lastLocation.x);
						splitPane.setEnabled(true);						
						expanded = true;
					}
				} 
				else {
					if (!expanded) {
						splitPane.getLeftComponent().setVisible(true);
						splitPane.getLeftComponent().setSize(lastLocation.x, 0);
						dragDividerTo(lastLocation.x);
						finishDraggingTo(lastLocation.x);
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
		if (getLocation().x <= 1) {
			expanded = false;
			splitPane.setEnabled(false);
			getHotSpot().setBounds(0, center_y-15, getDividerSize(), 30);
		}
		else {
			expanded = true;
			splitPane.setEnabled(true);
			getHotSpot().setBounds(0, 0, getDividerSize(), 24);
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
	
	int inset = 2;
	private void drawCollapseLabel(Graphics g) {
		Dimension size = g.getClipBounds().getSize();
		int half_length = Math.round(g.getClipBounds().height*0.2f);
		int center_y = size.height / 2;

		g.setColor(getBackground());
		g.fillRect(0, 0, size.width, size.height-0);
		
		//g.setColor();
		
		g.setColor(Color.DARK_GRAY);
		g.drawLine(inset, center_y, size.width - inset, center_y - half_length);
		g.setColor(Color.GRAY);
		g.drawLine( size.width - inset, center_y + half_length, inset, center_y);
		g.setColor(Color.GRAY);
		g.drawLine( size.width - inset, center_y - half_length, size.width - inset, center_y + half_length);
	}
	
	private void drawExpandLabel(Graphics g) {
		Dimension size = g.getClipBounds().getSize();
		int half_length = (g.getClipBounds().height-(inset*6))/2;
		int center_y = size.height / 2;
		
		g.setColor(getBackground());
		g.fillRect(0, 0, size.width, size.height-0);
		
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
