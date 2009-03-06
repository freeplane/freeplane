package org.freeplane.core.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author robert.ladstaetter
 */
public class JavaLangTest {
	@Test
	public void testSplit() {
		Assert.assertEquals(2, "2;2".split(";").length);
		Assert.assertEquals(2, "2;2;".split(";").length); // aha? ;)
	}
}
