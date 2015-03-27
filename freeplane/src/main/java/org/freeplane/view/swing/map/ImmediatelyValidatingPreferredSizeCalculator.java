package org.freeplane.view.swing.map;

import java.awt.Container;
import java.awt.Dimension;

public class ImmediatelyValidatingPreferredSizeCalculator {
    public static final ImmediatelyValidatingPreferredSizeCalculator INSTANCE  = new ImmediatelyValidatingPreferredSizeCalculator();

/*
    * (non-Javadoc)
    * @see java.awt.LayoutManager#preferredLayoutSize(java.awt.Container)
    */
   public Dimension preferredLayoutSize(final Container c) {
       if (!c.isValid()) {
           c.validate();
       }
       return c.getSize();
   }

}
