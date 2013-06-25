/*
 * Preview Dialog - A Preview Dialog for your Swing Applications Copyright (C)
 * 2003 Jens Kaiser. Written by: 2003 Jens Kaiser <jens.kaiser@web.de> This
 * program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Library General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version. This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library
 * General Public License for more details. You should have received a copy of
 * the GNU Library General Public License along with this program; if not, write
 * to the Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139,
 * USA.
 */
package org.freeplane.features.print;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JLabel;

class BrowseAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final private Runnable pageIndexPainter;
	final private JLabel pageNumber;
	protected int pageStep;
	protected Preview preview;

	public BrowseAction(final Preview preview, final JLabel pageNumber, final int pageStep) {
		super();
		this.preview = preview;
		this.pageStep = pageStep;
		this.pageNumber = pageNumber;
		pageIndexPainter = new Runnable() {
			public void run() {
				paintPageIndex();
			}
		};
	}

	public void actionPerformed(final ActionEvent e) {
		preview.moveIndex(pageStep);
		paintPageIndex();
		preview.repaint();
		EventQueue.invokeLater(pageIndexPainter);
	}

	private String getPageIndexText() {
		return "- " + String.valueOf(1 + preview.getPageIndex()) + " -";
	}

	private void paintPageIndex() {
		pageNumber.setText(getPageIndexText());
		pageNumber.paintImmediately(0, 0, pageNumber.getWidth(), pageNumber.getHeight());
	}
}
