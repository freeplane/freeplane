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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.freeplane.main.application;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.InputStream;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.JWindow;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.FileUtils;
import org.freeplane.core.util.FreeplaneVersion;
import org.freeplane.features.mode.Controller;

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
	private static final String FREEPLANE_SPLASH_PNG = "/images/Freeplane_splash.png";
	private Font versionTextFont = null;

	public FreeplaneSplashModern(final JFrame frame) {
		super(frame);
		splashResource = ResourceController.getResourceController().getResource(FREEPLANE_SPLASH_PNG);
		splashImage = new ImageIcon(splashResource);
		setBackground(new Color(0x57, 0xbf, 0x5e));
		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		final Dimension labelSize = new Dimension(splashImage.getIconWidth(), splashImage.getIconHeight());
		setLocation(screenSize.width / 2 - (labelSize.width / 2), screenSize.height / 2 - (labelSize.height / 2));
		setSize(labelSize);
		RootPane rootPane = new RootPane();
		rootPane.setSize(labelSize);
		setRootPane(rootPane);
	}

	private void createVersionTextFont() {
		if(versionTextFont != null){
			return;
		}
	    InputStream fontInputStream = null;
		try {
			
			//fontInputStream = ResourceController.getResourceController().getResource("/fonts/intuitive-subset.ttf")
			//fontInputStream = ResourceController.getResourceController().getResource("/fonts/WarnockPro-SemiboldCapt.otf").openStream();
			//fontInputStream = ResourceController.getResourceController().getResource("/fonts/WarnockPro-BoldCapt.otf").openStream();
			fontInputStream = ResourceController.getResourceController().getResource("/fonts/MinionPro-BoldCapt.otf").openStream();
			
			versionTextFont = Font.createFont(Font.TRUETYPE_FONT, fontInputStream);
		}
		catch (final Exception e) {
			versionTextFont = new Font("Arial", Font.PLAIN, 12);
		}
		finally {
			FileUtils.silentlyClose(fontInputStream);
		}
    }

	private final ImageIcon splashImage;
	private final URL splashResource;

	@SuppressWarnings("serial")
    private class RootPane extends JRootPane{

		public RootPane() {
			setDoubleBuffered(false);
		}

		@Override
		public void paintComponent(final Graphics g) {
			final Graphics2D g2 = (Graphics2D) g;
			splashImage.paintIcon(this, g2, 0, 0);
			final String splashResourceType = splashResource.getProtocol();
			final String classResourceType = getClass().getResource(FreeplaneSplashModern.class.getSimpleName() + ".class").getProtocol();
			if(! splashResourceType.equals(classResourceType))
				return;
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			final FreeplaneVersion version = FreeplaneVersion.getVersion();
			final String versionString = getVersionText(version);
			g2.setColor(Color.WHITE);
			createVersionTextFont();
			final float versionFontSize= 14;
			g2.setFont(versionTextFont.deriveFont(versionFontSize));
			int versionStringWidth = g2.getFontMetrics().stringWidth(versionString);
			final int xCoordinate = splashImage.getIconWidth() - versionStringWidth - 20;
			final int yCoordinate = 20;
			g2.drawString(versionString, xCoordinate, yCoordinate);
		}

		@Override
		public void paintChildren(final Graphics g) {
		}
	}

	private String getVersionText(final FreeplaneVersion version) {
	    final String freeplaneNumber = version.numberToString();
		final String status = version.getType().toLowerCase();
		if("".equals(status))
			return freeplaneNumber;
		else{
			final String versionString = freeplaneNumber + " " + status;
			return versionString;
		}
    }

	@Override
	public void setVisible(final boolean b) {
		super.setVisible(b);
		if (b) {
			paintImmediately();
		}
	}

	public void paintImmediately() {
	    ((JComponent) getRootPane()).paintImmediately(0, 0, getWidth(), getHeight());
    }

	static public void main(String[] args){
		try {
			if (System.getProperty("os.name", "").startsWith("Mac OS")) {
				UIManager.setLookAndFeel("org.violetlib.aqua.AquaLookAndFeel");
			}
		} catch (Exception e) {}

		setFonts();
		ApplicationResourceController applicationResourceController = new ApplicationResourceController();
		Controller controller = new Controller(applicationResourceController);
		Controller.setCurrentController(controller);
		FreeplaneSplashModern freeplaneSplashModern = new FreeplaneSplashModern(null);
		freeplaneSplashModern.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				System.out.println("x = " + e.getX() + " y = " + e.getY());
				if(e.getClickCount() == 2)
					System.exit(0);
			}
		});
		freeplaneSplashModern.setVisible(true);
	}

	static public void setFonts() {
		setFont(new FontUIResource(new Font("SF UI Text", Font.PLAIN, 11)));
	}

	private static void setFont(FontUIResource myFont) {
		UIManager.put("CheckBoxMenuItem.acceleratorFont", myFont);
		UIManager.put("Button.font", myFont);
		UIManager.put("ToggleButton.font", myFont);
		UIManager.put("RadioButton.font", myFont);
		UIManager.put("CheckBox.font", myFont);
		UIManager.put("ColorChooser.font", myFont);
		UIManager.put("ComboBox.font", myFont);
		UIManager.put("Label.font", myFont);
		UIManager.put("List.font", myFont);
		UIManager.put("MenuBar.font", myFont);
		UIManager.put("Menu.acceleratorFont", myFont);
		UIManager.put("RadioButtonMenuItem.acceleratorFont", myFont);
		UIManager.put("MenuItem.acceleratorFont", myFont);
		UIManager.put("MenuItem.font", myFont);
		UIManager.put("RadioButtonMenuItem.font", myFont);
		UIManager.put("CheckBoxMenuItem.font", myFont);
		UIManager.put("OptionPane.buttonFont", myFont);
		UIManager.put("OptionPane.messageFont", myFont);
		UIManager.put("Menu.font", myFont);
		UIManager.put("PopupMenu.font", myFont);
		UIManager.put("OptionPane.font", myFont);
		UIManager.put("Panel.font", myFont);
		UIManager.put("ProgressBar.font", myFont);
		UIManager.put("ScrollPane.font", myFont);
		UIManager.put("Viewport.font", myFont);
		UIManager.put("TabbedPane.font", myFont);
		UIManager.put("Slider.font", myFont);
		UIManager.put("Table.font", myFont);
		UIManager.put("TableHeader.font", myFont);
		UIManager.put("TextField.font", myFont);
		UIManager.put("Spinner.font", myFont);
		UIManager.put("PasswordField.font", myFont);
		UIManager.put("TextArea.font", myFont);
		UIManager.put("TextPane.font", myFont);
		UIManager.put("EditorPane.font", myFont);
		UIManager.put("TabbedPane.smallFont", myFont);
		UIManager.put("TitledBorder.font", myFont);
		UIManager.put("ToolBar.font", myFont);
		UIManager.put("ToolTip.font", myFont);
		UIManager.put("Tree.font", myFont);
		UIManager.put("FormattedTextField.font", myFont);
		UIManager.put("IconButton.font", myFont);
		UIManager.put("InternalFrame.optionDialogTitleFont", myFont);
		UIManager.put("InternalFrame.paletteTitleFont", myFont);
		UIManager.put("InternalFrame.titleFont", myFont);
	}

}
