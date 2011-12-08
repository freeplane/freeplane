/**
 * author: Marcel Genzmehr
 * 07.12.2011
 */
package org.docear.plugin.core.ui.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;

import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

public class JCheckboxBorder extends TitledBorder {

	private static final long serialVersionUID = 1L;

	private Point textLocation = new Point();
	private JCheckBox checkbox = new JCheckBox();

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public JCheckboxBorder(String title) {
		this(null, title, LEADING, TOP, null, null);
	}

	/**
	 * @param border
	 * @param title
	 */
	public JCheckboxBorder(Border border, String title) {
		this(border, title, LEADING, TOP, null, null);
	}

	public JCheckboxBorder(Border border) {
		this(border, "", LEADING, TOP, null, null);
	}

	public JCheckboxBorder(Border border, String title, int titleJustification, int titlePosition) {
		this(border, title, titleJustification, titlePosition, null, null);
	}

	public JCheckboxBorder(Border border, String title, int titleJustification, int titlePosition, Font titleFont) {
		this(border, title, titleJustification, titlePosition, titleFont, null);
	}

	public JCheckboxBorder(Border border, String title, int titleJustification, int titlePosition, Font titleFont,
			Color titleColor) {
		super(border, title, titleJustification, titlePosition, titleFont, titleColor);
		checkbox.setText(title);
		checkbox.setEnabled(true);
		checkbox.setVisible(true);
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public void setCheckboxAction(Action action) {
		checkbox.setAction(action);
	}

	public boolean isCheckboxSelected() {
		return checkbox.isSelected();
	}

	public void setCheckboxSelected(boolean selected) {
		checkbox.setSelected(selected);
	}

	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
		Border border = getBorder();

		if (getTitle() == null || getTitle().equals("")) {
			if (border != null) {
				border.paintBorder(c, g, x, y, width, height);
			}
			return;
		}

		Rectangle grooveRect = new Rectangle(x + EDGE_SPACING, y + EDGE_SPACING, width - (EDGE_SPACING * 2), height
				- (EDGE_SPACING * 2));
		Font font = g.getFont();
		Color color = g.getColor();

		g.setFont(getFont(c));

		JComponent jc = (c instanceof JComponent) ? (JComponent) c : null;
		FontMetrics fm = getFontMetrics(jc, g, g.getFont());
		int fontHeight = fm.getHeight();
		int descent = fm.getDescent();
		int ascent = fm.getAscent();
		int stringWidth = checkbox.getUI().getPreferredSize(checkbox).width;
		int diff;
		Insets insets;

		if (border != null) {
			insets = border.getBorderInsets(c);
		}
		else {
			insets = new Insets(0, 0, 0, 0);
		}

		int titlePos = getTitlePosition();
		switch (titlePos) {
		case ABOVE_TOP:
			diff = ascent + descent + (Math.max(EDGE_SPACING, TEXT_SPACING * 2) - EDGE_SPACING);
			grooveRect.y += diff;
			grooveRect.height -= diff;
			textLocation.y = grooveRect.y - (descent + TEXT_SPACING);
			break;
		case TOP:
		case DEFAULT_POSITION:
			diff = Math.max(0, ((ascent / 2) + TEXT_SPACING) - EDGE_SPACING);
			grooveRect.y += diff;
			grooveRect.height -= diff;
			textLocation.y = (grooveRect.y - descent) + (insets.top + ascent + descent) / 2;
			break;
		case BELOW_TOP:
			textLocation.y = grooveRect.y + insets.top + ascent + TEXT_SPACING;
			break;
		case ABOVE_BOTTOM:
			textLocation.y = (grooveRect.y + grooveRect.height) - (insets.bottom + descent + TEXT_SPACING);
			break;
		case BOTTOM:
			grooveRect.height -= fontHeight / 2;
			textLocation.y = ((grooveRect.y + grooveRect.height) - descent) + ((ascent + descent) - insets.bottom) / 2;
			break;
		case BELOW_BOTTOM:
			grooveRect.height -= fontHeight;
			textLocation.y = grooveRect.y + grooveRect.height + ascent + TEXT_SPACING;
			break;
		}

		int justification = getTitleJustification();
		if (c.getComponentOrientation().isLeftToRight()) {
			if (justification == LEADING || justification == DEFAULT_JUSTIFICATION) {
				justification = LEFT;
			}
			else if (justification == TRAILING) {
				justification = RIGHT;
			}
		}
		else {
			if (justification == LEADING || justification == DEFAULT_JUSTIFICATION) {
				justification = RIGHT;
			}
			else if (justification == TRAILING) {
				justification = LEFT;
			}
		}

		switch (justification) {
		case LEFT:
			textLocation.x = grooveRect.x + TEXT_INSET_H + insets.left;
			break;
		case RIGHT:
			textLocation.x = (grooveRect.x + grooveRect.width) - (stringWidth + TEXT_INSET_H + insets.right);
			break;
		case CENTER:
			textLocation.x = grooveRect.x + ((grooveRect.width - stringWidth) / 2);
			break;
		}
		
		// If title is positioned in middle of border AND its fontsize
		// is greater than the border's thickness, we'll need to paint
		// the border in sections to leave space for the component's background
		// to show through the title.
		//
		Rectangle clipRect = new Rectangle();
		if (border != null) {
			if (((titlePos == TOP || titlePos == DEFAULT_POSITION) && (grooveRect.y > textLocation.y - ascent))
					|| (titlePos == BOTTOM && (grooveRect.y + grooveRect.height < textLocation.y + descent))) {

				// save original clip
				Rectangle saveClip = g.getClipBounds();

				// paint strip left of text
				clipRect.setBounds(saveClip);
				if (computeIntersection(clipRect, x, y, textLocation.x - 1 - x, height)) {
					g.setClip(clipRect);
					border.paintBorder(c, g, grooveRect.x, grooveRect.y, grooveRect.width, grooveRect.height);
				}

				// paint strip right of text
				clipRect.setBounds(saveClip);
				if (computeIntersection(clipRect, textLocation.x + stringWidth + 1, y, x + width - (textLocation.x + stringWidth + 1),
						height)) {
					g.setClip(clipRect);
					border.paintBorder(c, g, grooveRect.x, grooveRect.y, grooveRect.width, grooveRect.height);
				}

				if (titlePos == TOP || titlePos == DEFAULT_POSITION) {
					// paint strip below text
					clipRect.setBounds(saveClip);
					if (computeIntersection(clipRect, textLocation.x - 1, textLocation.y + descent, stringWidth + 2, y + height - textLocation.y
							- descent)) {
						g.setClip(clipRect);
						border.paintBorder(c, g, grooveRect.x, grooveRect.y, grooveRect.width, grooveRect.height);
					}

				}
				else { // titlePos == BOTTOM
						// paint strip above text
					clipRect.setBounds(saveClip);
					if (computeIntersection(clipRect, textLocation.x - 1, y, stringWidth + 2, textLocation.y - ascent - y)) {
						g.setClip(clipRect);
						border.paintBorder(c, g, grooveRect.x, grooveRect.y, grooveRect.width, grooveRect.height);
					}
				}

				// restore clip
				g.setClip(saveClip);

			}
			else {
				border.paintBorder(c, g, grooveRect.x, grooveRect.y, grooveRect.width, grooveRect.height);
			}
		}

		g.setColor(getTitleColor());
		checkbox.setBounds(c.getX()+textLocation.x+100, c.getY()+textLocation.y, stringWidth, fontHeight);
		checkbox.getUI().paint(g, checkbox);

		g.setFont(font);
		g.setColor(color);
	}

	private static boolean computeIntersection(Rectangle dest, int rx, int ry, int rw, int rh) {
		int x1 = Math.max(rx, dest.x);
		int x2 = Math.min(rx + rw, dest.x + dest.width);
		int y1 = Math.max(ry, dest.y);
		int y2 = Math.min(ry + rh, dest.y + dest.height);
		dest.x = x1;
		dest.y = y1;
		dest.width = x2 - x1;
		dest.height = y2 - y1;

		if (dest.width <= 0 || dest.height <= 0) {
			return false;
		}
		return true;
	}

	public FontMetrics getFontMetrics(JComponent c, Graphics g, Font font) {
		if (c != null) {
			// Note: We assume that we're using the FontMetrics
			// from the widget to layout out text, otherwise we can get
			// mismatches when printing.
			return c.getFontMetrics(font);
		}
		return Toolkit.getDefaultToolkit().getFontMetrics(font);
	}
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
