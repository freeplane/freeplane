package org.freeplane.core.ui.components;

import java.awt.Rectangle;

import javax.swing.JTextArea;

public class InfoArea extends JTextArea {
    private static final long serialVersionUID = 1L;
    {
        setEditable(false);
    };
    @Override
    public void scrollRectToVisible(Rectangle aRect) {/**/}
}