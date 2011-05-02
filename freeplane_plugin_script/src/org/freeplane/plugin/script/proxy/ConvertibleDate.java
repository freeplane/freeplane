package org.freeplane.plugin.script.proxy;

import java.util.Calendar;
import java.util.Date;

import org.freeplane.features.common.format.FormattedDate;

public class ConvertibleDate extends Convertible {
	final private Date date;

	public ConvertibleDate(Date date) {
	    super(FormattedDate.toStringISO(date));
	    this.date = date;
    }

	@Override
    public Date getDate() throws ConversionException {
	    return date;
    }

	@Override
    public Calendar getCalendar() throws ConversionException {
	    final Calendar calendar = Calendar.getInstance();
	    calendar.setTime(date);
		return calendar;
    }

	@Override
    public Object getObject() {
	    return date;
    }

	@Override
    public boolean isNum() {
		return false;
    }

	@Override
    public boolean isDate() {
		return true;
    }
}
