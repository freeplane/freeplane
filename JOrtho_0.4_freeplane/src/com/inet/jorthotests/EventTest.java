/*
 * Created on 14.10.2008
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.inet.jorthotests;

import java.awt.event.ActionEvent;

import javax.swing.*;

import com.inet.jortho.*;

import junit.framework.TestCase;

public class EventTest extends TestCase {

    static {
        AllTests.init();
    }

    public void testChangeLanguage() throws Exception{
        JMenu menu1 = SpellChecker.createLanguagesMenu();
        JMenu menu2 = SpellChecker.createLanguagesMenu();

        assertEquals( "Menucount", menu1.getItemCount(), menu2.getItemCount() );
        assertTrue( "2 languages requied:" + menu1.getItemCount(), menu1.getItemCount() >= 2 );

        JRadioButtonMenuItem item1_1 = (JRadioButtonMenuItem)menu1.getItem( 0 );
        JRadioButtonMenuItem item1_2 = (JRadioButtonMenuItem)menu1.getItem( 1 );

        JRadioButtonMenuItem item2_1 = (JRadioButtonMenuItem)menu2.getItem( 0 );
        JRadioButtonMenuItem item2_2 = (JRadioButtonMenuItem)menu2.getItem( 1 );

        assertEquals( "Item 1", item1_1, item2_1 );
        assertEquals( "Item 2", item1_2, item2_2 );

        //Change the selected language
        JRadioButtonMenuItem notSelected = item1_1.isSelected() ? item1_2 : item1_1;
        JRadioButtonMenuItem selected = item1_1.isSelected() ? item1_1 : item1_2;
        assertFalse( "Selected", notSelected.isSelected() );
        assertTrue( "Selected", selected.isSelected() );
        notSelected.doClick(0);
        assertTrue( "Selected", notSelected.isSelected() );
        assertFalse( "Selected", selected.isSelected() );

        assertEquals( "Item 1", item1_1, item2_1 );
        assertEquals( "Item 2", item1_2, item2_2 );
        
        Thread.sleep( 10 ); // for loading thread
        
        notSelected = item2_1.isSelected() ? item2_2 : item2_1;
        selected = item2_1.isSelected() ? item2_1 : item2_2;        
        assertFalse( "Selected", notSelected.isSelected() );
        assertTrue( "Selected", selected.isSelected() );
        notSelected.doClick(0);
        assertTrue( "Selected", notSelected.isSelected() );
        assertFalse( "Selected", selected.isSelected() );

        assertEquals( "Item 1", item1_1, item2_1 );
        assertEquals( "Item 2", item1_2, item2_2 );
        
        Thread.sleep( 10 ); // for loading thread
        
    }

    /**
     * Compare 2 JRadioButtonMenuItem
     */
    private void assertEquals( String description, JRadioButtonMenuItem item1, JRadioButtonMenuItem item2 ) {
        assertEquals( description + ": Name", item1.getName(), item2.getName() );
        assertEquals( description + ": Selected", item1.isSelected(), item2.isSelected() );
    }
}
