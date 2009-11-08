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
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

import org.freeplane.core.controller.FreeplaneVersion;
import org.freeplane.core.resources.ResourceBundles;
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

	public FreeplaneSplashModern(JFrame frame) {
		super(frame);
		try {
			InputStream fontInputStream = ResourceController.getResourceController().getResource("/fonts/AUGUSTUS.TTF").openStream();
			versionTextFont = Font.createFont(Font.TRUETYPE_FONT, fontInputStream).deriveFont(16f);
			fontInputStream.close();
		} catch (Exception e) {
			versionTextFont = new Font("Arial", Font.PLAIN, 16);
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
		final int xCoordinate = (int) (getSize().getWidth() - mWidth1.intValue() - 40);
		final int yCoordinate = 32;
		g2.drawString(freeplaneNumber, xCoordinate, yCoordinate);
		g2.drawString(status, xCoordinate + (mWidth2 - mWidth1) / 2, yCoordinate + 16);
	}

	@Override
	public void setVisible(final boolean b) {
		super.setVisible(b);
		if (b) {
			getRootPane().paintImmediately(0, 0, getWidth(), getHeight());
		}
	}
}
