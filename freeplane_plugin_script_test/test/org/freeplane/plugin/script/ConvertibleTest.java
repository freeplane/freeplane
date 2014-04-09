package org.freeplane.plugin.script;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.main.application.FreeplaneGUIStarter;
import org.freeplane.plugin.script.proxy.ConversionException;
import org.freeplane.plugin.script.proxy.Convertible;
import org.freeplane.plugin.script.proxy.ConvertibleHtmlText;
import org.freeplane.plugin.script.proxy.ConvertibleNoteText;
import org.freeplane.plugin.script.proxy.ConvertibleText;
import org.junit.BeforeClass;
import org.junit.Test;

public class ConvertibleTest {
	private static final String FREEPLANE_URI = "http://www.freeplane.org";

    /** provides an easy mean to create a Convertible with a null text. */
	public static final class TestConvertible extends Convertible {
		public TestConvertible(NodeModel nodeModel, String text) {
			super(FormulaUtils.evalIfScript(nodeModel, null, text));
		}
	}

	@BeforeClass
	public static void initStatics() {
		// we have to start Freeplane to create a Controller for script execution could we avoid that?
		System.setProperty("org.freeplane.nosplash", "true");
		final FreeplaneGUIStarter freeplaneGUIStarter = new FreeplaneGUIStarter();
        final Controller controller = freeplaneGUIStarter.createController();
		freeplaneGUIStarter.createModeControllers(controller);
		ResourceController.getResourceController().setProperty(ScriptingPermissions.RESOURCES_EXECUTE_SCRIPTS_WITHOUT_ASKING, true);
	}

	@Test
	public void testGetNum() throws ConversionException, ExecuteScriptException {
		assertEquals(null, convertible(null).getNum());
		assertEquals(new Long(20), convertible("20").getNum());
		assertEquals(new Double(0.00001), ((Double) convertible("0.00001").getNum()), 1e-9);
		assertEquals(new Long(31), convertible("0x1f").getNum());
		assertEquals(new Long(-31), convertible("-0x1F").getNum());
		assertEquals(new Long(31), convertible("#1F").getNum());
		assertEquals(new Long(-31), convertible("-#1f").getNum());
		assertEquals(new Long(23), convertible("027").getNum());
		assertEquals(new Long(-23), convertible("-027").getNum());
		assertThrowsNumberConversionException("");
		assertThrowsNumberConversionException("xyz");
		assertThrowsNumberConversionException(" 12");
	}

	private void assertThrowsNumberConversionException(String string) {
	    boolean caughtException = false;
		final String notANumber = string;
		try {
			convertible(notANumber).getNum();
		}
		catch (ConversionException e) {
			caughtException = true;
		}
		assertTrue("should have been detected as not-a-number: \"" + notANumber + '"', caughtException);
    }
	
	@Test
	public void testGetNum0() throws ConversionException, ExecuteScriptException {
		assertEquals(new Long(0), convertible(null).getNum0());
		assertEquals(new Long(20), convertible("20").getNum0());
		assertEquals(new Double(0.00001), ((Double) convertible("0.00001").getNum0()), 1e-9);
		assertEquals(new Long(31), convertible("0x1f").getNum0());
		assertEquals(new Long(-31), convertible("-0x1F").getNum0());
		assertEquals(new Long(31), convertible("#1F").getNum0());
		assertEquals(new Long(-31), convertible("-#1f").getNum0());
		assertEquals(new Long(23), convertible("027").getNum0());
		assertEquals(new Long(-23), convertible("-027").getNum0());
		// now the test the fallback on conversion errors
		assertEquals(new Long(0), convertible("xyz").getNum0());
		assertEquals(new Long(0), convertible("2010-08-16 22:31:55.123+0530").getNum0());
	}

	private Convertible convertible(String text) {
	    final MapModel mapModel = Controller.getCurrentModeController().getMapController().newModel();
		NodeModel nodeModel = new NodeModel(mapModel);
		return new TestConvertible(nodeModel, text);
	}

	@Test
	public void testGetDate() throws ConversionException, ExecuteScriptException {
		testOneDatePattern(null, null);
		testOneDatePattern("2010-08-16 22:31:55.123+0530", "2010-08-16 22:31:55.123+0530");
		// note that SimpleDateFormat uses local time zone for conversion - use DateFormatUtils instead!
		final String Z = getLocalTimeZoneOffsetString();
		testOneDatePattern("2010-08-16 22:31:55.123" + Z, "2010-08-16 22:31:55.123");
		testOneDatePattern("2010-08-16 22:31:55", "2010-08-16 22:31:55.000");
		testOneDatePattern("2010-08-16 22:31:55", "2010-08-16 22:31:55");
		testOneDatePattern("2010-08-16 22:31:00", "2010-08-16 22:31");
		testOneDatePattern("2010-08-16 00:00:00", "2010-08-16");
		//
		testOneDatePattern("2010-08-16 22:31:55", "2010-08-16T22:31:55.000");
		testOneDatePattern("2010-08-16 22:31:55", "2010-08-16T22:31:55");
		testOneDatePattern("2010-08-16 22:31:00", "2010-08-16T22:31");
		testOneDatePattern("2010-08-16 00:00:00", "2010-08-16");
		//
		testOneDatePattern("2010-08-16 22:31:55", "20100816223155.000");
		testOneDatePattern("2010-08-16 22:31:55", "20100816223155");
		testOneDatePattern("2010-08-16 22:31:00", "201008162231");
		testOneDatePattern("2010-08-16 00:00:00", "20100816");
		//
		testOneDatePattern("2010-08-16 22:31:55", "20100816T223155.000");
		testOneDatePattern("2010-08-16 22:31:55", "20100816T223155");
		testOneDatePattern("2010-08-16 22:31:00", "20100816T2231");
		testOneDatePattern("2010-08-16 00:00:00", "20100816");
		// parse errors
		// parse error
		boolean caughtException = false;
		final String notADate = "2010-08-";
		try {
			convertible(notADate).getDate();
		}
		catch (ConversionException e) {
			caughtException = true;
		}
		assertTrue("not a date: " + notADate, caughtException);
	}

	private void testOneDatePattern(String expected, String testInput) throws ConversionException,
	        ExecuteScriptException {
		assertEquals("expected: " + Convertible.toString(date(expected)) + "!=" + Convertible.toString(convertible(testInput).getDate()),
		    date(expected), convertible(testInput).getDate());
		assertEquals(calendar(expected), convertible(testInput).getCalendar());
	}

	private Calendar calendar(String expected) throws ConversionException {
		if (expected == null)
			return null;
		GregorianCalendar result = new GregorianCalendar(0, 0, 0);
		result.setTime(date(expected));
		return result;
	}

	private Date date(String string) throws ConversionException {
		try {
			if (string == null)
				return null;
			else if (string.matches(".*\\.\\d{3}[-+]\\d{4}"))
				return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ").parse(string);
			else if (string.matches(".*:\\d{2}\\.\\d{3}[-+]\\d{4}"))
			    return new SimpleDateFormat("yyyy-MM-dd HH:mmZ").parse(string);
            else if (string.matches(".* \\d{2}:\\d{2}:\\d{2}\\.\\d{3}"))
                return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(string);
            else if (string.matches(".* \\d{2}:\\d{2}:\\d{2}"))
                return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(string);
			else
				return new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(string);
		}
		catch (ParseException e) {
			throw new ConversionException(e);
		}
	}

	private String getLocalTimeZoneOffsetString() {
		final TimeZone timeZone = TimeZone.getDefault();
		int offsetMinutes = (timeZone.getRawOffset() + timeZone.getDSTSavings()) / 60000;
		return String.format("%+03d%02d", offsetMinutes / 60, offsetMinutes % 60);
	}

    @Test
    public void testGetUrl() throws ConversionException {
        assertEquals(null, convertible(null).getUri());
        assertEquals(uri(FREEPLANE_URI), convertible(FREEPLANE_URI).getUri());        
        // scheme: is mandatory
        assertThrowsUriConversionException("");
        assertThrowsUriConversionException("scheme:");
        assertEquals(uri("scheme:bla"), convertible("scheme:bla").getUri());
    }

    private void assertThrowsUriConversionException(String string) {
        boolean caughtException = false;
        final String notAnUrl = string;
        try {
            convertible(notAnUrl).getUri();
        }
        catch (ConversionException e) {
            caughtException = true;
        }
        assertTrue("should have been detected as not-an-url: \"" + notAnUrl + '"', caughtException);
    }

    private URI uri(String string) {
        try {
            return new URI(string);
        }
        catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

	@Test
	public void testGetObject() throws ConversionException, ExecuteScriptException {
		assertEquals(null, convertible(null).getObject());
		// Convertibles contain the evaluated text
		assertEquals(new Long(3), convertible("= 1 + 2").getObject());
		assertEquals(new Long(1234567890), convertible("1234567890").getObject());
		assertEquals(new Double(0.00001), convertible("0.00001").getObject());
		assertEquals(date("2010-12-24 23:59:59"), convertible("2010-12-24 23:59:59").getObject());
		assertEquals("a text", convertible("a text").getObject());
	}

	@Test
	public void testGetPlain() throws ConversionException, ExecuteScriptException {
		assertEquals(null, convertible(null).getPlain());
		assertEquals("12", convertible("12").getPlain());
		assertEquals("text", convertible("text").getPlain());
		// text must start with <html> to get transformed
		assertEquals("text", convertible("<html>text</html>").getPlain());
		assertEquals("text", convertible("<html>text").getPlain());
		assertEquals("text", convertible("<html><p>text<p></html>").getPlain());
		assertEquals("<p>text</p>", convertible("<p>text</p>").getPlain());
		assertEquals("<xyz>text</xyz>", convertible("<xyz>text</xyz>").getPlain());
		assertEquals("<?xml version=\"1.0\"?><p>text</p>", convertible("<?xml version=\"1.0\"?><p>text</p>").getPlain());
	}

	@Test
	public void testIsNum() throws ConversionException, ExecuteScriptException {
		assertFalse(convertible(null).isNum());
		assertTrue("Convertibles contain evaluated text", convertible("= 1 + 2").isNum());
// broken:
//		assertTrue(convertible("0x1f").isNum());
//		assertTrue(convertible("-0x1F").isNum());
		assertFalse("this is a known NumberUtils bug - use 0x encoding instead", convertible("#1F").isNum());
		assertFalse("this is a known NumberUtils bug - use 0x encoding instead", convertible("-#1f").isNum());
		assertTrue(convertible("027").isNum());
		assertTrue(convertible("-027").isNum());
		assertTrue(convertible("1234567890").isNum());
		assertTrue(convertible("0.00001").isNum());
		assertFalse(convertible("2010-12-24 23:59:59").isNum());
		assertFalse(convertible("a text").isNum());
		assertFalse(convertible("").isNum());
	}

	@Test
	public void testIsDate() throws ConversionException, ExecuteScriptException {
		assertFalse(convertible(null).isDate());
		assertFalse(convertible("").isDate());
		assertFalse(convertible("12").isDate());
		assertFalse(convertible("1.2").isDate());
		assertFalse(convertible("text").isDate());
		assertTrue(convertible("2010-08-16 22:31:55.123+0530").isDate());
		assertTrue(convertible("2010-08-16 22:31:55.123").isDate());
		assertTrue(convertible("2010-08-16 22:31:55").isDate());
		assertTrue(convertible("2010-08-16 22:31").isDate());
		assertTrue(convertible("2010-08-16").isDate());
		assertTrue(convertible("2010-08-16T22:31:55.123+0530").isDate());
		assertTrue(convertible("2010-08-16T22:31:55.123").isDate());
		assertTrue(convertible("2010-08-16T22:31:55").isDate());
		assertTrue(convertible("2010-08-16T22:31").isDate());
		assertTrue(convertible("2010-08-16").isDate());
		assertTrue(convertible("20100816223155.123+0530").isDate());
		assertTrue(convertible("20100816223155.123").isDate());
		assertTrue(convertible("20100816223155").isDate());
		assertTrue(convertible("201008162231").isDate());
		assertTrue(convertible("20100816").isDate());
		assertTrue(convertible("20100816T223155.123+0530").isDate());
		assertTrue(convertible("20100816T223155.123").isDate());
		assertTrue(convertible("20100816T223155").isDate());
		assertTrue(convertible("20100816T2231").isDate());
		assertTrue(convertible("20100816").isDate());
	}

	@Test
	public void testGetStringAndToString() throws ConversionException, ExecuteScriptException {
		assertGetStringAndToStringEqualsInputText(null);
		assertGetStringAndToStringEqualsInputText("2010-08-16 22:31:55");
		assertGetStringAndToStringEqualsInputText("12");
		assertGetStringAndToStringEqualsInputText("1.2");
		assertGetStringAndToStringEqualsInputText("text");
		assertGetStringAndToStringEqualsInputText("");
	}

	private void assertGetStringAndToStringEqualsInputText(String text) {
		assertEquals(text, convertible(text).toString());
		assertEquals(text, convertible(text).getString());
		assertEquals(text, convertible(text).getText());
	}

    @Test
    public void testGetProperty() throws ConversionException, ExecuteScriptException {
        assertEquals(null, convertible(null).getProperty("string"));
        assertEquals(null, convertible(null).getProperty("num"));
        assertEquals("12", convertible("12").getProperty("string"));
        assertEquals("12", convertible("12").getProperty("text"));
        assertEquals("12", convertible("12").getProperty("plain"));
        assertEquals(new Long(12), convertible("12").getProperty("num"));
        // "bytes" is a virtual property of class String (byte[] getBytes())
        assertEquals("12", new String((byte[]) convertible("12").getProperty("bytes")));
        assertEquals(date("2010-08-16 22:31"), convertible("2010-08-16T22:31").getProperty("date"));
        assertEquals(calendar("2010-08-16 22:31"), convertible("2010-08-16T22:31").getProperty("calendar"));
        assertEquals(uri(FREEPLANE_URI), convertible(FREEPLANE_URI).getProperty("uri"));
    }

    @Test
    public void testNullObject() {
        assertEquals(convertible(null), null);
        assertEquals(convertible(null), convertible(null));
    }

    @Test
	public void testInvokeMethod() throws ConversionException, ExecuteScriptException {
		assertEquals(null, convertible(null).invokeMethod("getString", null));
		assertEquals("12", convertible("12").invokeMethod("getString", null));
		assertEquals("12", convertible("12").invokeMethod("getText", null));
		assertEquals("12", convertible("12").invokeMethod("getPlain", null));
		assertEquals(new Long(12), convertible("12").invokeMethod("getNum", null));
		assertEquals(Boolean.TRUE, convertible("12").invokeMethod("startsWith", new Object[] { "1" }));
	}

	@Test
	public void testConvertibleNodeTextMayNotBeNull() throws ConversionException {
		boolean exceptionThrown = false;
		try {
			final NodeModel nodeModel = newNodeModel();
			nodeModel.setText(null);
			// not reached: new ConvertibleNodeText(nodeModel);
		}
		catch (NullPointerException e) {
			exceptionThrown = true;
		}
		assertTrue("you may not set a ConvertibleNodeText's text to null!", exceptionThrown);
	}

	@Test
	public void testConvertibleAttributeValuesMayBeNull() throws ConversionException, ExecuteScriptException {
		new ConvertibleText(newNodeModel(), null, null);
	}

	@Test
	public void testConvertibleNoteTextMayBeNull() throws ConversionException {
		try {
			// by default there are no notes, i.e. they are null
			assertNull(new ConvertibleNoteText(newNodeModel(), null, null).getText());
		}
		catch (Exception e) {
			fail("null texts are allowed for other Convertibles, but got " + e);
		}
	}

    @Test
    public void no_exception_on_invalid_formulas_in_ConvertibleHtmlText() throws ConversionException {
        try {
            assertEquals("===ERROR",
                new ConvertibleHtmlText(newNodeModel(), null, e("html", e("body", "===ERROR"))).getPlain());
            assertEquals("=hurz!",
                new ConvertibleHtmlText(newNodeModel(), null, e("html", e("body", "=hurz!"))).getPlain());
        }
        catch (Exception e) {
            fail("formula evaluation may not fail for ConvertibleHtmlText, but got " + e);
        }
    }

    @Test
    public void to_plain_and_text_return_evaluated_ConvertibleHtmlText() throws ConversionException {
        try {
            final String html = e("html", e("body", "=1+2"));
            assertEquals("3", new ConvertibleHtmlText(newNodeModel(), null, html).getPlain());
            assertEquals("3", new ConvertibleHtmlText(newNodeModel(), null, html).getText());
        }
        catch (Exception e) {
            fail("formula evaluation may not fail for ConvertibleHtmlText, but got " + e);
        }
    }
    
    @Test
    public void to_html_and_string_return_original_html_ConvertibleHtmlText() throws ConversionException {
        try {
            final String html = e("html", e("body", "=1+2"));
            assertEquals(html, new ConvertibleHtmlText(newNodeModel(), null, html).getHtml());
            assertEquals(html, new ConvertibleHtmlText(newNodeModel(), null, html).getString());
        }
        catch (Exception e) {
            fail("formula evaluation may not fail for ConvertibleHtmlText, but got " + e);
        }
    }

    private NodeModel newNodeModel() {
        final MapModel mapModel = Controller.getCurrentModeController().getMapController().newModel();
        return new NodeModel(mapModel);
    }
	
    @Test
    public void to_plain_evaluates_formulas_in_ConvertibleHtmlText() throws ConversionException {
        try {
            assertEquals("3",
                new ConvertibleHtmlText(newNodeModel(), null, e("html", e("body", "=1+2"))).getText());
        }
        catch (Exception e) {
            fail("formula evaluation may not fail for ConvertibleHtmlText, but got " + e);
        }
    }

    private String e(String tag, String content) {
        return String.format("<%s>%s</%s>", tag, content, tag);
    }

	@Test
	public void testSomethingToString() throws ConversionException, ExecuteScriptException {
		// this works but you may not set a Convertibles's text to null!
		assertEquals(null, Convertible.toString(null));
		testSomethingToStringImpl("12", 12L, "num");
		testSomethingToStringImpl("1.2", 1.2d, "num");
		final String Z = getLocalTimeZoneOffsetString();
		// default conversion doesn't contain milliseconds
		testSomethingToStringImpl("2010-08-16T22:31" + Z, date("2010-08-16 22:31" + Z), "date");
	}

	private void testSomethingToStringImpl(String expected, Object toConvert, String propertyName)
	        throws ConversionException, ExecuteScriptException {
		assertEquals(expected, Convertible.toString(toConvert));
		// the result of Convertible.toString(Xyz) must be a valid input to Convertible.getXyz()
		assertEquals(toConvert, convertible(expected).getProperty(propertyName));
	}

    @Test
    public void testGetBool() {
        assertTrue(new Convertible("true").getBool());
        assertTrue(new Convertible("True").getBool());
        assertTrue(new Convertible("TRUE").getBool());
        assertFalse(new Convertible("false").getBool());
        assertFalse(new Convertible("something").getBool());
        assertFalse(new Convertible(null).getBool());
    }
}
