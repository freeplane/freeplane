package org.freeplane.plugin.script;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.freeplane.features.common.map.NodeModel;
import org.freeplane.main.application.FreeplaneStarter;
import org.freeplane.plugin.script.proxy.ConversionException;
import org.freeplane.plugin.script.proxy.Convertible;
import org.freeplane.plugin.script.proxy.ConvertibleAttributeValue;
import org.freeplane.plugin.script.proxy.ConvertibleNoteText;
import org.freeplane.plugin.script.proxy.FormulaUtils;
import org.junit.BeforeClass;
import org.junit.Test;

public class ConvertibleTest {
	/** provides an easy mean to create a Convertible with a null text. */
	public static final class TestConvertible extends Convertible {
		private NodeModel nodeModel;

		public TestConvertible(NodeModel nodeModel, String text) {
			super(text);
			this.nodeModel = nodeModel;
		}

		public Convertible getValue() {
			// some eval method that allows to directly hand in the text
			return new Convertible(FormulaUtils.evalAttributeText(nodeModel, getText()));
		}
	}

	@BeforeClass
	public static void initStatics() {
		// FIXME: we have to start Freeplane to create a Controller for script execution
		new FreeplaneStarter().createController();
		// the package of this test is choosen in order to be allowed to access this method:
		ScriptingEngine.setNoUserPermissionRequired(true);
	}

	@Test
	public void testGetNum() throws ConversionException {
		assertEquals(null, convertible(null).getNum());
		assertEquals(new Long(20), convertible("20").getNum());
		assertEquals(new Double(0.00001), ((Double) convertible("0.00001").getNum()), 1e-9);
		assertEquals(new Long(31), convertible("0x1f").getNum());
		assertEquals(new Long(-31), convertible("-0x1F").getNum());
		assertEquals(new Long(31), convertible("#1F").getNum());
		assertEquals(new Long(-31), convertible("-#1f").getNum());
		assertEquals(new Long(23), convertible("027").getNum());
		assertEquals(new Long(-23), convertible("-027").getNum());
		// parse error
		boolean caughtException = false;
		final String notANumber = "xyz";
		try {
			convertible(notANumber).getNum();
		}
		catch (ConversionException e) {
			caughtException = true;
		}
		assertTrue("not a number: " + notANumber, caughtException);
	}

	private Convertible convertible(String text) {
		NodeModel nodeModel = new NodeModel(null);
		return new TestConvertible(nodeModel, text);
	}

	@Test
	public void testGetDate() throws ConversionException {
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

	private void testOneDatePattern(String expected, String testInput) throws ConversionException {
		assertEquals(date(expected), convertible(testInput).getDate());
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
			else if (string.matches(".*[-+]\\d{4}"))
				return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ").parse(string);
			else
				return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(string);
		}
		catch (ParseException e) {
			throw new ConversionException(e);
		}
	}

	private String getLocalTimeZoneOffsetString() {
		final Calendar calendar = Calendar.getInstance();
		int offsetMinutes = (calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET)) / 60000;
		return String.format("%+03d%02d", offsetMinutes / 60, offsetMinutes % 60);
	}

	@Test
	public void testGetObject() throws ConversionException {
		assertEquals(null, convertible(null).getObject());
		assertEquals(new Integer(3), convertible("= 1 + 2").getObject());
		assertEquals(new Long(1234567890), convertible("1234567890").getObject());
		assertEquals(new Double(0.00001), convertible("0.00001").getObject());
		assertEquals(date("2010-12-24 23:59:59"), convertible("2010-12-24 23:59:59").getObject());
		assertEquals("a text", convertible("a text").getObject());
	}

	@Test
	public void testGetPlain() throws ConversionException {
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
	public void testIsNum() throws ConversionException {
		assertFalse(convertible(null).isNum());
		assertFalse(convertible("= 1 + 2").isNum());
		assertTrue(convertible("0x1f").isNum());
		assertTrue(convertible("-0x1F").isNum());
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
	public void testIsDate() throws ConversionException {
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
	public void testGetStringAndToString() throws ConversionException {
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
	public void testGetProperty() throws ConversionException {
		assertEquals(null, convertible(null).getProperty("string"));
		assertEquals(null, convertible(null).getProperty("num"));
		assertEquals("12", convertible("12").getProperty("string"));
		assertEquals("12", convertible("12").getProperty("text"));
		assertEquals("12", convertible("12").getProperty("plain"));
		assertEquals(new Long(12), convertible("12").getProperty("num"));
		// "bytes" is a virtual property of class String (byte[] getBytes())
		assertEquals("12", new String((byte[]) convertible("12").getProperty("bytes")));
		assertEquals(date("2010-08-16 22:31:55"), convertible("2010-08-16T22:31:55").getProperty("date"));
		assertEquals(calendar("2010-08-16 22:31:55"), convertible("2010-08-16T22:31:55").getProperty("calendar"));
	}

	@Test
	public void testInvokeMethod() throws ConversionException {
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
			final NodeModel nodeModel = new NodeModel(null);
			nodeModel.setText(null);
			// not reached: new ConvertibleNodeText(nodeModel);
		}
		catch (NullPointerException e) {
			exceptionThrown = true;
		}
		assertTrue("you may not set a ConvertibleNodeText's text to null!", exceptionThrown);
	}

	@Test
	public void testConvertibleAttributeValuesMayNotBeNull() throws ConversionException {
		boolean exceptionThrown = false;
		try {
			new ConvertibleAttributeValue(new NodeModel(null), null);
		}
		catch (NullPointerException e) {
			exceptionThrown = true;
		}
		assertTrue("you may not set a ConvertibleAttributeValuesMayNotBeNull's text to null!", exceptionThrown);
	}
	
	@Test
	public void testOtherConvertibleNoteTextMayBeNull() throws ConversionException {
		try {
			// by default there are no notes, i.e. they are null
			assertNull(new ConvertibleNoteText(new NodeModel(null)).getText());
		}
		catch (Exception e) {
			fail("null texts are allowed for other Convertibles");
		}
	}

	@Test
	public void testSomethingToString() throws ConversionException {
		// this works but you may not set a Convertibles's text to null!
		assertEquals(null, Convertible.toString(null));
		testSomethingToStringImpl("12", 12L, "num");
		testSomethingToStringImpl("1.2", 1.2d, "num");
		testSomethingToStringImpl("2010-08-16T22:31:55.123+0000", date("2010-08-16 22:31:55.123+0000"), "date");
	}

	private void testSomethingToStringImpl(String expected, Object toConvert, String propertyName)
	        throws ConversionException {
		assertEquals(expected, Convertible.toString(toConvert));
		// the result of Convertible.toString(Xyz) must be a valid input to Convertible.getXyz()
		assertEquals(toConvert, convertible(expected).getProperty(propertyName));
	}
}
