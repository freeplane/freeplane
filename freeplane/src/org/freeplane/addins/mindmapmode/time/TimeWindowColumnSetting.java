package org.freeplane.addins.mindmapmode.time;

import org.freeplane.io.xml.n3.nanoxml.IXMLElement;
import org.freeplane.io.xml.n3.nanoxml.XMLElement;

class TimeWindowColumnSetting {
	protected int columnWidth;
	protected int columnSorting;

	public int getColumnWidth() {
		return this.columnWidth;
	}

	public void setColumnWidth(int columnWidth) {
		this.columnWidth = columnWidth;
	}

	public int getColumnSorting() {
		return this.columnSorting;
	}

	public void setColumnSorting(int columnSorting) {
		this.columnSorting = columnSorting;
	}

	void marschall(IXMLElement xml) {
		IXMLElement child = new XMLElement("time_window_column_setting");
		child.setAttribute("column_sorting", Integer.toString(columnSorting));
		child.setAttribute("column_width", Integer.toString(columnWidth));
		xml.addChild(child);
	}

	static TimeWindowColumnSetting create(IXMLElement xml) {
		final TimeWindowColumnSetting timeWindowColumnSetting = new TimeWindowColumnSetting();
		timeWindowColumnSetting.columnSorting = Integer.parseInt(xml.getAttribute("column_sorting", null));
		timeWindowColumnSetting.columnWidth = Integer.parseInt(xml.getAttribute("column_width", null));
		return timeWindowColumnSetting;
    }
}
