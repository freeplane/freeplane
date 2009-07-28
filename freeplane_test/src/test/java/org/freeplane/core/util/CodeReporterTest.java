package org.freeplane.core.util;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.IFreeplaneAction;
import org.junit.Test;

public class CodeReporterTest {
	@Test
	public void testGetActions() {
		CodeReporter r = new CodeReporter("../freeplane/");
		Class<?>[] classes = IFreeplaneAction.class.getInterfaces();	
	System.out.println(classes);	
	}
}
