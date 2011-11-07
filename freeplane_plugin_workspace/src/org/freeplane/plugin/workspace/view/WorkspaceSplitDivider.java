/**
 * author: Marcel Genzmehr
 * 07.11.2011
 */
package org.freeplane.plugin.workspace.view;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

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

		addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {
			}

			public void mousePressed(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseClicked(MouseEvent e) {
				if (expanded) {
					if (e.getClickCount() % 2 == 0) {
						lastLocation = getLocation();
						splitPane.getLeftComponent().setVisible(false);
						splitPane.getLeftComponent().setSize(0, 0);
						splitPane.setDividerSize(7);
						dragDividerTo(0);
						finishDraggingTo(0);
						splitPane.setEnabled(false);
						expanded = false;
					}
				}
				else {
					splitPane.setDividerSize(6);
					splitPane.getLeftComponent().setVisible(true);
					splitPane.getLeftComponent().setSize(lastLocation.x, 0);
					dragDividerTo(lastLocation.x);
					finishDraggingTo(lastLocation.x);
					splitPane.setEnabled(true);
					expanded = true;
				}
			}
		});
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public void paint(Graphics g) {
		if (getLocation().x <= 1) {
			expanded = false;
		}
		else {
			expanded = true;
		}
		g.setColor(this.getForeground());
		g.fillPolygon(getArrow(getSize()));
	}

	private Polygon getArrow(Dimension size) {
		Polygon arrow = new Polygon();
		int center_y = size.height / 2;
		if (expanded) {
			arrow.addPoint(size.width - 1, center_y - 20);
			arrow.addPoint(size.width - 1, center_y + 20);
			arrow.addPoint(1, center_y);
		}
		else {
			arrow.addPoint(1, center_y - 20);
			arrow.addPoint(1, center_y + 20);
			arrow.addPoint(size.width - 1, center_y);
		}
		return arrow;
	}

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
