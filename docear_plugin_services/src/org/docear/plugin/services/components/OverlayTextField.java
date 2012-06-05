package org.docear.plugin.services.components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;

public class OverlayTextField extends JTextField implements FocusListener {
	
	private static final long serialVersionUID = 1L;
	private final String startText;

	public OverlayTextField(String initText) {
		super();
		this.startText = initText;
		addFocusListener(this);
		setGhostText();
	}
	
	public String getText() {
		if(getTextInternal().equals(startText)) {
			return "";
		}
		return super.getText();
	}
			
	public void focusGained(FocusEvent e) {
		removeGhostText();
	}

	public void focusLost(FocusEvent e) {
		setGhostText();
	}
	
	public void paint(Graphics g) {
		setGhostText();
		super.paint(g);
	}
	
	boolean inGhostSet = false;
	private void setGhostText() {
		if(inGhostSet) return;
		inGhostSet = true;
		if(!hasFocus() && "".equals(getTextInternal().trim()) ) {
			setForeground(new Color(0x88FFFFFF&getForeground().getRGB(), true));
			setText(startText);
		}
		inGhostSet = false;
	}
	
	private void removeGhostText() {
		setForeground(new Color(getForeground().getRGB(), false));
		if(startText.equals(getTextInternal())) {
			setText("");
		}
		revalidate();
		repaint();
	}
	
	private String getTextInternal() {
		return super.getText();
	}

	
}