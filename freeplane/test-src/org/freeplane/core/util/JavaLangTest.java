package org.freeplane.core.util;

import static org.junit.Assert.*;

import org.junit.Test;


/**
 * @author robert.ladstaetter
 */
public class JavaLangTest {
	
	@Test
	public void testSplit() {
		assertEquals(2,"2;2".split(";").length);
		assertEquals(2,"2;2;".split(";").length); // aha? ;)
	}
}
