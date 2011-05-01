package org.freeplane;

import org.freeplane.core.util.FormattedNumberTest;
import org.freeplane.core.util.FormattedObjectTest;
import org.freeplane.features.common.format.PatternFormatTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({FormattedNumberTest.class, FormattedObjectTest.class, PatternFormatTest.class})
public class AllFreeplaneUnitTests {
	//nothing
}
