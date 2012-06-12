package org.docear.plugin.services.components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JPasswordField;

public class OverlayPasswordField extends JPasswordField implements FocusListener {
	
	private static final long serialVersionUID = 1L;
	private final String startText;
	private char echoChar;
	
	public OverlayPasswordField(String initText) {
		super();
		this.startText = initText;
		addFocusListener(this);
		echoChar = getEchoChar();
		setGhostText();
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
	
	public char[] getPassword() {
		String pw = getTextInternal();
		if(startText.equals(pw)) {
			return new char[]{};
		}
		return super.getPassword();
	}
	
	
	boolean inGhostSet = false;
	private void setGhostText() {
		if(inGhostSet) return;
		inGhostSet = true;
		//setEchoChar(echoChar);
		if(!hasFocus() && "".equals(getTextInternal()) ) {
			setEchoChar((char) 0);
			setForeground(new Color(0x88FFFFFF&getForeground().getRGB(), true));
			setText(startText);
		}
		inGhostSet = false;
	}
	
	private String getTextInternal() {
		return new String(super.getPassword());
	}
	
	private void removeGhostText() {
		setForeground(new Color(getForeground().getRGB(), false));
		if(startText.equals(getTextInternal())) {
			setEchoChar(echoChar);
			setText("");
		}
		revalidate();
		repaint();
	}

	
}