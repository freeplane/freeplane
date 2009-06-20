package org.freeplane.core.ui.components;

import java.awt.*;

import javax.swing.JComponent;
import javax.swing.JToolTip;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicToolTipUI;


public class LimitedWidthTooltipUI extends BasicToolTipUI {

    private static int maximumWidth = Integer.MAX_VALUE;
    static LimitedWidthTooltipUI singleton = new LimitedWidthTooltipUI();

    public static void initialize() {
        // don't hardcode class name
        String key = "ToolTipUI";
        Class cls = singleton.getClass();
        String name = cls.getName();
        UIManager.put(key,name);
        UIManager.put(name,cls);
    }

    private LimitedWidthTooltipUI() {
        super();
    }

    public static ComponentUI createUI(JComponent c) {
        return singleton;
    }

    /**
     *  set maximum width
     *  0 = no maximum width
     */
    public static void setMaximumWidth(int width){
        maximumWidth = width;
    }


    public Dimension getPreferredSize(JComponent c) {
    	Dimension preferredSize = super.getPreferredSize(c);
    	if(preferredSize.width < maximumWidth){
        	return preferredSize;
    	}
    	String tipText = ((JToolTip)c).getTipText();
    	final String TABLE_START = "<html><table>";
    	if(! tipText.startsWith(TABLE_START)){
    		return preferredSize;
    	}
    	tipText = "<html><table width=\"" + maximumWidth + "\">" + tipText.substring(TABLE_START.length());
    	((JToolTip)c).setTipText(tipText);
       	preferredSize = super.getPreferredSize(c);
    	if(preferredSize.width > maximumWidth){
    		preferredSize.width = maximumWidth;
    	}
    	return preferredSize;
    }

}