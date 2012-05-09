package org.docear.plugin.core.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;

import sun.swing.SwingUtilities2;

public class MultiLineActionLabel extends JPanel implements SwingConstants, Accessible {

	private static final long serialVersionUID = 1L;

	private String text = "";

	private int verticalAlignment = CENTER;
	private int horizontalAlignment = LEADING;
	private int verticalTextPosition = CENTER;
	private int horizontalTextPosition = TRAILING;
	
	private Rectangle paintTextR = new Rectangle();
	private Rectangle paintIconR = new Rectangle();
	
	private List<ActionListener> actionListeners = new ArrayList<ActionListener>();
	private List<MultiLineActionLabel.ActionLabelItem> actionItems = new ArrayList<MultiLineActionLabel.ActionLabelItem>();
	private final MouseAdapter mouseAdapter = new MouseAdapter();
	
	public MultiLineActionLabel(String text) {
		setText(text);
		addMouseListener(mouseAdapter);
		addMouseMotionListener(mouseAdapter);
	}

	public void setText(String text) {

		String oldAccessibleName = null;
		if (accessibleContext != null) {
			oldAccessibleName = accessibleContext.getAccessibleName();
		}

		String oldValue = this.text;

		this.text = parsedString(text);
		
		BasicHTML.updateRenderer(this, getText());

		firePropertyChange("text", oldValue, getText());

		if ((accessibleContext != null) && (accessibleContext.getAccessibleName() != oldAccessibleName)) {
			accessibleContext.firePropertyChange(AccessibleContext.ACCESSIBLE_VISIBLE_DATA_PROPERTY, oldAccessibleName, accessibleContext.getAccessibleName());
		}
		if (getText() == null || oldValue == null || !getText().equals(oldValue)) {
			revalidate();
			repaint();
		}
	}

	private String parsedString(final String str) {
		StringBuilder builder = new StringBuilder();
		if (!BasicHTML.isHTMLString(text)) {
			builder.append("<html><body>");
		}
		int currentPos = -1;
		int lastPos = 0;
		String text = str.replaceAll("[\n]", "<br/>");
		actionItems.clear();
		while ((currentPos = text.indexOf("<action cmd=\"", (currentPos + 1))) > -1) {			
			builder.append(text.substring(lastPos, currentPos));
			builder.append("<span style=\"color: #0000FF;\">");
			lastPos = currentPos+"<action cmd=\"".length();
			String actionCommand = text.substring(lastPos, text.indexOf("\"", lastPos));			
			lastPos = text.indexOf(">", lastPos)+1;
			currentPos = text.indexOf("</action>", (currentPos + 1));			
			ActionLabelItem item = new ActionLabelItem(text.substring(lastPos, currentPos));
			item.setActionCommand(actionCommand);
			builder.append(item.getText());
			builder.append("</span>");
			lastPos = currentPos+"</action>".length();
			actionItems.add(item);
		}
		if(lastPos < text.length()) {
			builder.append(text.substring(lastPos));
		}

		if (!BasicHTML.isHTMLString(text)) {
			builder.append("</body></html>");
		}
		return builder.toString();
	}

	public String getText() {
		return text;
	}

	public void paint(Graphics g) {
		super.paint(g);
		String text = getText();

		if (text == null) {
			return;
		}

		FontMetrics fm = SwingUtilities2.getFontMetrics(this, g);
		layout(fm, getWidth(), getHeight());
		
		
		if (text != null) {
			View view = (View) getClientProperty(BasicHTML.propertyKey);
			if (view != null) {
				try {
					String clippedText = view.getDocument().getText(1, view.getDocument().getLength());					
					identifyActionAreas(clippedText, fm, SwingUtilities2.getFontMetrics(this, g, fm.getFont().deriveFont(Font.BOLD)));
					view.paint(g, paintTextR);
				} catch (Exception e) {
				}				
			} 
		}
//		g.setColor(Color.black);
//		for(ActionLabelItem item : actionItems) {
//			g.drawRect(item.getHotArea().x, item.getHotArea().y, item.getHotArea().width, item.getHotArea().height);
//		}
	}

	private void identifyActionAreas(final String text, FontMetrics fmDefault, FontMetrics fmAction) {
		int lastPos = 0;
		for(ActionLabelItem item : actionItems) {
			Rectangle rect = item.getHotArea();
			int textPos = text.indexOf(item.getText(), lastPos);
			if(textPos > -1) {
				String sub = text.substring(0, textPos);
				rect.x = paintTextR.x + fmDefault.stringWidth(sub);
				rect.y = paintTextR.y;
				rect.width = fmDefault.stringWidth(item.getText());
				rect.height = fmDefault.getHeight();
				lastPos = textPos+item.getText().length();
			}
		}		
	}

	

	private String layout(FontMetrics fm, int width, int height) {
		Insets insets = getInsets(null);
		String text = getText();
		Rectangle paintViewR = new Rectangle();
		paintViewR.x = insets.left;
		paintViewR.y = insets.top;
		paintViewR.width = width - (insets.left + insets.right);
		paintViewR.height = height - (insets.top + insets.bottom);
		paintIconR.x = paintIconR.y = paintIconR.width = paintIconR.height = 0;
		paintTextR.x = paintTextR.y = paintTextR.width = paintTextR.height = 0;
		return layoutCL(fm, text, null, paintViewR, paintIconR, paintTextR);
	}

	protected String layoutCL(FontMetrics fontMetrics, String text, Icon icon, Rectangle viewR, Rectangle iconR, Rectangle textR) {
		return SwingUtilities.layoutCompoundLabel(this, fontMetrics, text, icon, verticalAlignment, horizontalAlignment, verticalTextPosition,
				horizontalTextPosition, viewR, iconR, textR, 4);
	}

	public Dimension getPreferredSize() {
		String text = getText();

		Insets insets = getInsets(null);
		Font font = getFont();

		int dx = insets.left + insets.right;
		int dy = insets.top + insets.bottom;

		if (((text == null) || ((text != null) && (font == null)))) {
			return new Dimension(dx, dy);
		} else {
			FontMetrics fm = getFontMetrics(font);

			Rectangle iconR = new Rectangle();
			Rectangle textR = new Rectangle();
			Rectangle viewR = new Rectangle();
			iconR.x = iconR.y = iconR.width = iconR.height = 0;
			textR.x = textR.y = textR.width = textR.height = 0;
			viewR.x = dx;
			viewR.y = dy;
			viewR.width = viewR.height = Short.MAX_VALUE;

			layoutCL(fm, text, null, viewR, iconR, textR);
			int x1 = Math.min(iconR.x, textR.x);
			int x2 = Math.max(iconR.x + iconR.width, textR.x + textR.width);
			int y1 = Math.min(iconR.y, textR.y);
			int y2 = Math.max(iconR.y + iconR.height, textR.y + textR.height);
			Dimension rv = new Dimension(x2 - x1, y2 - y1);

			rv.width += dx;
			rv.height += dy;
			return rv;
		}
	}

	/**
	 * @return getPreferredSize()
	 */
	public Dimension getMinimumSize() {
		Dimension d = getPreferredSize();
		View view = (View) getClientProperty(BasicHTML.propertyKey);
		if (view != null) {
			d.width -= view.getPreferredSpan(View.X_AXIS) - view.getMinimumSpan(View.X_AXIS);
		}
		return d;
	}

	/**
	 * @return getPreferredSize()
	 */
	public Dimension getMaximumSize() {
		Dimension d = getPreferredSize();
		View view = (View) getClientProperty(BasicHTML.propertyKey);
		if (view != null) {
			d.width += view.getMaximumSpan(View.X_AXIS) - view.getPreferredSpan(View.X_AXIS);
		}
		return d;
	}

	public int getBaseline(int width, int height) {
		super.getBaseline(width, height);
		String text = getText();
		if (text == null || "".equals(text) || getFont() == null) {
			return -1;
		}
		FontMetrics fm = getFontMetrics(getFont());
		layout(fm, width, height);
		return getHTMLBaseline(paintTextR.y, fm.getAscent(), paintTextR.width, paintTextR.height);
	}
	
	public void addActionListener(ActionListener listener) {
		if(!actionListeners.contains(listener)) {
			this.actionListeners.add(listener);
		}
	}
	
	public void removeActionListener(ActionListener listener) {
		this.actionListeners.remove(listener);
	}

	private int getHTMLBaseline(int y, int ascent, int width, int height) {
		View view = (View) getClientProperty(BasicHTML.propertyKey);
		if (view != null) {
			int baseline = BasicHTML.getHTMLBaseline(view, width, height);
			if (baseline < 0) {
				return baseline;
			}
			return y + baseline;
		}
		return y + ascent;

	}
	
	protected void fireActionEvent(String actionCommand) {
		ActionEvent event = new ActionEvent(this, 0, actionCommand);
		for(ActionListener listener : actionListeners) {
			listener.actionPerformed(event);
		}
		
	}
	
	protected ActionLabelItem getIntersectingItem(Point point) {
		for(ActionLabelItem item : actionItems) {
			if(item.getHotArea().contains(point)) {
				return item;
			}
		}
		return null;
	}
	
	protected ActionLabelItem getActionItem(Rectangle rect) {
		// TODO Auto-generated method stub
		return null;
	}
	
	class MouseAdapter implements MouseListener, MouseMotionListener {
	
		public void mouseMoved(MouseEvent e) {			
			ActionLabelItem item = getIntersectingItem(e.getPoint());
			if(item != null) {
				MultiLineActionLabel.this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			} else {
				MultiLineActionLabel.this.setCursor(Cursor.getDefaultCursor());
			}
		}

		public void mouseClicked(MouseEvent e) {
			ActionLabelItem item = getIntersectingItem(e.getPoint());
			if(item != null) {
				fireActionEvent(item.getActionCommand());
			}
		}

		public void mouseDragged(MouseEvent e) {}
		public void mousePressed(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) {}
		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}		
	}

	class ActionLabelItem {
		private Rectangle hotArea = null;
		private String actionCommand;
		private final String text;
		
		public ActionLabelItem(String text) {
			this.text = text;
		}		

		public String getText() {
			return text;
		}

		public void setActionCommand(String actionCommand) {
			this.actionCommand = actionCommand;			
		}

		public String getActionCommand() {
			return actionCommand;
		}
		
		public Rectangle getHotArea() {
			if(hotArea == null) {
				hotArea = new Rectangle();
			}
			return hotArea;
		}

		public void setHotArea(Rectangle hotArea) {
			this.hotArea = hotArea;
		}
		
	}

}
