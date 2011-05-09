package org.freeplane;

import org.freeplane.features.common.format.FormattedNumberTest;
import org.freeplane.features.common.format.FormattedObjectTest;
import org.freeplane.features.common.format.ParserTest;
import org.freeplane.features.common.format.PatternFormatTest;
import org.freeplane.features.common.format.ScannerTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ FormattedNumberTest.class //
        , FormattedObjectTest.class //
        , PatternFormatTest.class //
        , ParserTest.class //
        , ScannerTest.class })
public class AllFreeplaneUnitTests {
	//nothing
}
