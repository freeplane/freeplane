package org.freeplane.features.text.mindmapmode;

import static org.freeplane.features.text.mindmapmode.SplitInRowsAction.PatternMaker.escape;
import static org.junit.Assert.*;

import org.junit.Test;

public class SplitToRowsActionShould {
@Test
public void testEscape() throws Exception {
	assertTrue("&-^\\[]".matches("[\\[\\]\\-\\&\\\\\\^]+"));
	assertEquals(",\\-", escape(", -")); 
	assertEquals("\\]", escape("]")); 
	assertEquals("\\^", escape("^")); 
}
}
