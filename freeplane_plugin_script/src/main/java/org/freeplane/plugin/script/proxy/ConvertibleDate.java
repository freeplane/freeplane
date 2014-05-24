package org.freeplane.plugin.script.proxy;

import java.util.Calendar;
import java.util.Date;

import org.freeplane.features.format.FormattedDate;

public class ConvertibleDate extends Convertible {
	final private Date date;

	public ConvertibleDate(final Date date) {
	    super(FormattedDate.toStringISO(date));
	    this.date = date;
    }

	@Override
    public Date getDate() {
	    return date;
    }

	@Override
    public Calendar getCalendar() {
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
