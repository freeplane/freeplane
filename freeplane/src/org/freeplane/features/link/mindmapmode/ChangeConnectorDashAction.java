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
package org.freeplane.features.link.mindmapmode;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.util.Arrays;

import javax.swing.Icon;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.features.link.ConnectorModel;
import org.freeplane.features.link.LinkController;

class ChangeConnectorDashAction extends AFreeplaneAction {
	private static final int ICON_HEIGHT = 6;
	private static final int ICON_WIDTH = 60;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ConnectorModel arrowLink;
	private final int[] dash;

	public ChangeConnectorDashAction(final MLinkController linkController,
	                                   final ConnectorModel arrowLink, final int[] dash) {
		super("ChangeConnectorDashAction", "", createIcon(dash));
		this.arrowLink = arrowLink;
		this.dash = dash;
		final int[] dash2 = arrowLink.getDash();
		final boolean selected = dash2 == dash || dash != null && Arrays.equals(dash, dash2);
		setSelected(selected);
	}

	public void actionPerformed(final ActionEvent e) {
		final MLinkController linkController = (MLinkController) LinkController.getController();
		linkController.setConnectorDash(arrowLink, dash);
	}
	
	private static Icon createIcon(final int[] dash){
		final BasicStroke stroke = UITools.createStroke(2, dash);
		return new Icon() {
			public void paintIcon(Component c, Graphics g, int x, int y) {
				Graphics2D g2 = (Graphics2D) g;
				g2.setColor(Color.BLACK);
				Stroke oldStroke = g2.getStroke();
				g2.setStroke(stroke);
				g2.drawLine(x, y+ICON_HEIGHT / 2, x+ICON_WIDTH, y+ICON_HEIGHT / 2);
				g2.setStroke(oldStroke);
			}
			
			public int getIconWidth() {
				return ICON_WIDTH;
			}
			
			public int getIconHeight() {
				return ICON_HEIGHT;
			}
		};
	}
}
