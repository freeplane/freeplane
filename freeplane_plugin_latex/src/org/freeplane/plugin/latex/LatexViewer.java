/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2010 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is created by Stefan Ott in 2010.
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

package org.freeplane.plugin.latex;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.common.map.NodeModel;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;

class LatexViewer extends JComponent {
	private static final int DEFAULT_FONT_SIZE = 16;
	static String editorTitle = null;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private float zoom = 0f;
	@SuppressWarnings("unused")
    final private LatexNodeHook nodeHook;
	private LatexExtension model;
	private TeXFormula teXFormula;

	LatexViewer(final LatexNodeHook nodeHook, final LatexExtension latexExtension) {
		this.nodeHook = nodeHook;
		setBorder(BorderFactory.createLineBorder(Color.BLACK));
		setModel(latexExtension);
		if (LatexViewer.editorTitle == null) {
			LatexViewer.editorTitle = TextUtils.getText("plugins/latex/LatexNodeHook.editorTitle");
		}
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2
				        && Controller.getCurrentModeController().getModeName() == "MindMap") {
					//edit();
					final NodeModel node = Controller.getCurrentModeController().getMapController().getSelectedNode();
					final Container nodeView = SwingUtilities.getAncestorOfClass(NodeView.class, LatexViewer.this);
                    nodeHook.editLatexInEditor(node);
					nodeView.requestFocus();
					e.consume();
					return;
				}
			}
		});
	}

	private void calculateSize() {
		final MapView mapView = (MapView) SwingUtilities.getAncestorOfClass(MapView.class, this);
		final float mapZoom = mapView.getZoom();
		if (mapZoom == zoom) {
			return;
		}
		zoom = mapZoom;
		final Icon latexIcon = teXFormula.createTeXIcon(TeXConstants.STYLE_DISPLAY, DEFAULT_FONT_SIZE * zoom);
		final Insets insets = getInsets();
		final Dimension dimension = new Dimension(latexIcon.getIconWidth() + insets.left + insets.right,
		    latexIcon.getIconHeight() + insets.top + insets.bottom);
		setPreferredSize(dimension);
	}

	@Override
	public void paint(final Graphics g) {
		final Icon latexIcon = teXFormula.createTeXIcon(TeXConstants.STYLE_DISPLAY, DEFAULT_FONT_SIZE * zoom);
		final Insets insets = getInsets();
		latexIcon.paintIcon(this, g, insets.left, insets.top);
		super.paint(g);
	}

	public void setModel(final LatexExtension latexExtension) {
		model = latexExtension;
		try {
			teXFormula = new TeXFormula(model.getEquation());
			teXFormula.createTeXIcon(TeXConstants.STYLE_DISPLAY, DEFAULT_FONT_SIZE);
		}
		catch (final Exception e) {
			try {
				teXFormula = new TeXFormula("\\mbox{" + e.getMessage() + "}");
				teXFormula.createTeXIcon(TeXConstants.STYLE_DISPLAY, DEFAULT_FONT_SIZE);
			}
			catch (final Exception e1) {
				teXFormula = new TeXFormula("\\mbox{Can not parse given equation}");
			}
		}
		zoom = 0;
		revalidate();
		repaint();
	}

	@Override
	public Dimension getPreferredSize() {
		calculateSize();
		return super.getPreferredSize();
	}
}
