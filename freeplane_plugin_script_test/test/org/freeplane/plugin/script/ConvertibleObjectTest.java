package org.freeplane.plugin.script;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.freeplane.features.common.map.NodeModel;
import org.freeplane.main.application.FreeplaneStarter;
import org.freeplane.plugin.script.proxy.ConversionException;
import org.freeplane.plugin.script.proxy.ConvertibleObject;
import org.junit.BeforeClass;
import org.junit.Test;

public class ConvertibleObjectTest {
	@BeforeClass
	public static void initStatics() {
		// FIXME: we have to start Freeplane to create a Controller for script execution
		new FreeplaneStarter().createController();
		// the package of this test is choosen in order to be allowed to access this method:
		ScriptingEngine.setNoUserPermissionRequired(true);
	}

	@Test
	public void testGetNum() throws ConversionException {
		assertEquals(new Long(20), convertibleObject("20").getNum());
		assertEquals(new Double(0.00001), ((Double) convertibleObject("0.00001").getNum()), 1e-9);
		// parse error
		boolean caughtException = false;
		final String notANumber = "xyz";
		try {
			convertibleObject(notANumber).getNum();
		}
		catch (ConversionException e) {
			caughtException = true;
		}
		assertTrue("not a number: " + notANumber, caughtException);
	}

	private ConvertibleObject convertibleObject(String text) {
		NodeModel nodeModel = new NodeModel(null);
		nodeModel.setText(text);
	    return new ConvertibleObject(nodeModel, nodeModel.getText());
    }

	@Test
	public void testGetDate() throws ConversionException {
		testOneDatePattern("2010-08-16 22:31:55", "2010-08-16 22:31:55");
		testOneDatePattern("2010-08-16 22:31:00", "2010-08-16 22:31");
		testOneDatePattern("2010-08-16 00:00:00", "2010-08-16");
		//
		testOneDatePattern("2010-08-16 22:31:55", "2010-08-16T22:31:55");
		testOneDatePattern("2010-08-16 22:31:00", "2010-08-16T22:31");
		testOneDatePattern("2010-08-16 00:00:00", "2010-08-16");
		//
		testOneDatePattern("2010-08-16 22:31:55", "20100816223155");
		testOneDatePattern("2010-08-16 22:31:00", "201008162231");
		testOneDatePattern("2010-08-16 00:00:00", "20100816");
		//
		testOneDatePattern("2010-08-16 22:31:55", "20100816T223155");
		testOneDatePattern("2010-08-16 22:31:00", "20100816T2231");
		testOneDatePattern("2010-08-16 00:00:00", "20100816");
		// parse errors
		// parse error
		boolean caughtException = false;
		final String notADate = "2010-08-";
		try {
			convertibleObject(notADate).getDate();
		}
		catch (ConversionException e) {
			caughtException = true;
		}
		assertTrue("not a date: " + notADate, caughtException);
	}

	private void testOneDatePattern(String expected, String testInput) throws ConversionException {
		assertEquals(date(expected), convertibleObject(testInput).getDate());
		assertEquals(calendar(expected), convertibleObject(testInput).getCalendar());
	}

	private Calendar calendar(String expected) throws ConversionException {
		GregorianCalendar result = new GregorianCalendar(0, 0, 0);
		result.setTime(date(expected));
		return result;
	}

	private Date date(String string) throws ConversionException {
		try {
			return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(string);
		}
		catch (ParseException e) {
			throw new ConversionException(e);
		}
	}

	@Test
	public void testGetObject() throws ConversionException {
		assertEquals(new Integer(3), convertibleObject("= 1 + 2").getObject());
		assertEquals(new Long(1234567890), convertibleObject("1234567890").getObject());
		assertEquals(new Double(0.00001), convertibleObject("0.00001").getObject());
		assertEquals(date("2010-12-24 23:59:59"), convertibleObject("2010-12-24 23:59:59").getObject());
		assertEquals("a text", convertibleObject("a text").getObject());
	}
}
