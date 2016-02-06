package org.freeplane.features.text.mindmapmode;

import static org.freeplane.features.text.mindmapmode.SplitToWordsAction.PatternMaker.escape;
import static org.junit.Assert.*;

import org.junit.Test;

public class SplitToWordsActionShould {
@Test
public void testEscape() throws Exception {
	assertTrue("&-^\\[]".matches("[\\[\\]\\-\\&\\\\\\^]+"));
	assertEquals(",\\-", escape(", -")); 
	assertEquals("\\]", escape("]")); 
	assertEquals("\\^", escape("^")); 
}
}
