/*
 * Created on 14.10.2008
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.inet.jorthotests;

import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;

import junit.framework.TestCase;

import com.inet.jortho.SpellChecker;

public class EventTest extends TestCase {
	static {
		AllTests.init();
	}

	/**
	 * Compare 2 JRadioButtonMenuItem
	 */
	private void assertEquals(final String description, final JRadioButtonMenuItem item1,
	                          final JRadioButtonMenuItem item2) {
		assertEquals(description + ": Name", item1.getName(), item2.getName());
		assertEquals(description + ": Selected", item1.isSelected(), item2.isSelected());
	}

	public void testChangeLanguage() throws Exception {
		final JMenu menu1 = SpellChecker.createLanguagesMenu();
		final JMenu menu2 = SpellChecker.createLanguagesMenu();
		assertEquals("Menucount", menu1.getItemCount(), menu2.getItemCount());
		assertTrue("2 languages requied:" + menu1.getItemCount(), menu1.getItemCount() >= 2);
		final JRadioButtonMenuItem item1_1 = (JRadioButtonMenuItem) menu1.getItem(0);
		final JRadioButtonMenuItem item1_2 = (JRadioButtonMenuItem) menu1.getItem(1);
		final JRadioButtonMenuItem item2_1 = (JRadioButtonMenuItem) menu2.getItem(0);
		final JRadioButtonMenuItem item2_2 = (JRadioButtonMenuItem) menu2.getItem(1);
		assertEquals("Item 1", item1_1, item2_1);
		assertEquals("Item 2", item1_2, item2_2);
		//Change the selected language
		JRadioButtonMenuItem notSelected = item1_1.isSelected() ? item1_2 : item1_1;
		JRadioButtonMenuItem selected = item1_1.isSelected() ? item1_1 : item1_2;
		assertFalse("Selected", notSelected.isSelected());
		assertTrue("Selected", selected.isSelected());
		notSelected.doClick(0);
		assertTrue("Selected", notSelected.isSelected());
		assertFalse("Selected", selected.isSelected());
		assertEquals("Item 1", item1_1, item2_1);
		assertEquals("Item 2", item1_2, item2_2);
		Thread.sleep(10); // for loading thread
		notSelected = item2_1.isSelected() ? item2_2 : item2_1;
		selected = item2_1.isSelected() ? item2_1 : item2_2;
		assertFalse("Selected", notSelected.isSelected());
		assertTrue("Selected", selected.isSelected());
		notSelected.doClick(0);
		assertTrue("Selected", notSelected.isSelected());
		assertFalse("Selected", selected.isSelected());
		assertEquals("Item 1", item1_1, item2_1);
		assertEquals("Item 2", item1_2, item2_2);
		Thread.sleep(10); // for loading thread
	}
}
