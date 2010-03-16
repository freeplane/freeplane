/*
 * SimplyHTML, a word processor based on Java, HTML and CSS
 * Copyright (C) 2002 Ulrich Hilger
 * Copyright (C) 2006 Karsten Pawlik
 * Copyright (C) 2006 Dimitri Polivaev
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.freeplane.main.application;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JWindow;

import org.freeplane.core.controller.FreeplaneVersion;
import org.freeplane.core.resources.ResourceController;

/**
 * Class that displays a splash screen
 * Is run in a separate thread so that the applet continues to load in the background
 * @author Karsten Pawlik
 * 
 */
public class FreeplaneSplashModern extends JWindow {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Font versionTextFont = null;
	private final String description = "Free mind mapping and knowledge management software";
	private final String copyright = "\u00a9 2000-2010";

	public FreeplaneSplashModern(JFrame frame) {
		super(frame);
		InputStream fontInputStream = null;
		try {
			fontInputStream = ResourceController.getResourceController().getResource("/fonts/BPreplay.ttf").openStream();
			versionTextFont = Font.createFont(Font.TRUETYPE_FONT, fontInputStream).deriveFont(16f);
		} catch (Exception e) {
			versionTextFont = new Font("Arial", Font.PLAIN, 16);
		}
		finally{
			if (fontInputStream != null)
				try {
					fontInputStream.close();
				} catch (IOException e) {
				}
		}
		splashImage = new ImageIcon(ResourceController.getResourceController().getResource("/images/Freeplane_splash.png"));

		getRootPane().setOpaque(false);
		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		final Dimension labelSize = new Dimension(splashImage.getIconWidth(), splashImage.getIconHeight());
		setLocation(screenSize.width / 2 - (labelSize.width / 2), screenSize.height / 2 - (labelSize.height / 2));
		setSize(labelSize);
	}

	private Integer mWidth1;
	private ImageIcon splashImage;
	private Integer mWidth2;
	private Integer mWidth3;
	@Override
	public void paint(final Graphics g) {
		final Graphics2D g2 = (Graphics2D) g;
		splashImage.paintIcon(this, g2, 0, 0);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setFont(versionTextFont);
		g2.setColor(new Color(0x4a, 0x00, 0x65));
		FreeplaneVersion version = FreeplaneVersion.getVersion();
		final String freeplaneNumber = version.numberToString() ;
		String status = version.getType().toUpperCase();
		if (mWidth1 == null) {
			mWidth1 = new Integer(g2.getFontMetrics().stringWidth(freeplaneNumber));
			mWidth2 = new Integer(g2.getFontMetrics().stringWidth(status));
		}
		int xCoordinate = getSize().width - mWidth1.intValue() - 28;
		int yCoordinate = 32;
		g2.drawString(freeplaneNumber, xCoordinate, yCoordinate);
		g2.drawString(status, xCoordinate + (mWidth1 - mWidth2) / 2, yCoordinate + 16);
		g2.setFont(versionTextFont.deriveFont(10f));
		g2.setColor(Color.WHITE);
		xCoordinate = 10;
		yCoordinate = getSize().height - 10;
		g2.drawString(description, xCoordinate, yCoordinate);
		if (mWidth3 == null) {
			mWidth3 = new Integer(g2.getFontMetrics().stringWidth(copyright));
		}
		xCoordinate = getSize().width - mWidth3.intValue() - 10;
		g2.drawString(copyright, xCoordinate, yCoordinate);
	}

	@Override
	public void setVisible(final boolean b) {
		super.setVisible(b);
		if (b) {
			getRootPane().paintImmediately(0, 0, getWidth(), getHeight());
		}
	}
}
