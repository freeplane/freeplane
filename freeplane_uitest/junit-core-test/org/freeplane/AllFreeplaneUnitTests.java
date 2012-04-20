package org.freeplane;

import org.freeplane.features.format.FormattedFormulaTest;
import org.freeplane.features.format.FormattedNumberTest;
import org.freeplane.features.format.FormattedObjectTest;
import org.freeplane.features.format.ParserTest;
import org.freeplane.features.format.PatternFormatTest;
import org.freeplane.features.format.ScannerTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ FormattedNumberTest.class //
        , FormattedObjectTest.class //
        , FormattedFormulaTest.class //
        , PatternFormatTest.class //
        , ParserTest.class //
        , ScannerTest.class })
public class AllFreeplaneUnitTests {
	//nothing
}
