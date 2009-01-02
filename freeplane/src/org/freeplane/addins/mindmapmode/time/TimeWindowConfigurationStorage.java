package org.freeplane.addins.mindmapmode.time;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JDialog;

import org.freeplane.core.io.IXMLElement;
import org.freeplane.core.resources.WindowConfigurationStorage;

class TimeWindowConfigurationStorage extends WindowConfigurationStorage {
	public static TimeWindowConfigurationStorage decorateDialog(final String marshalled,
	                                                            final JDialog dialog) {
		final TimeWindowConfigurationStorage storage = new TimeWindowConfigurationStorage();
		final IXMLElement xml = storage.unmarschall(marshalled, dialog);
		if (xml != null) {
			final Iterator iterator = xml.getChildren().iterator();
			while (iterator.hasNext()) {
				storage.addTimeWindowColumnSetting(TimeWindowColumnSetting
				    .create((IXMLElement) iterator.next()));
			}
			return storage;
		}
		return null;
	}

	protected ArrayList<TimeWindowColumnSetting> timeWindowColumnSettingList = new ArrayList();

	public void addAtTimeWindowColumnSetting(final int position,
	                                         final TimeWindowColumnSetting timeWindowColumnSetting) {
		timeWindowColumnSettingList.add(position, timeWindowColumnSetting);
	}

	public void addTimeWindowColumnSetting(final TimeWindowColumnSetting timeWindowColumnSetting) {
		timeWindowColumnSettingList.add(timeWindowColumnSetting);
	}

	public void clearTimeWindowColumnSettingList() {
		timeWindowColumnSettingList.clear();
	}

	public java.util.List getListTimeWindowColumnSettingList() {
		return java.util.Collections.unmodifiableList(timeWindowColumnSettingList);
	}

	public TimeWindowColumnSetting getTimeWindowColumnSetting(final int index) {
		return timeWindowColumnSettingList.get(index);
	}

	@Override
	protected void marschallSpecificElements(final IXMLElement xml) {
		xml.setName("time_window_configuration_storage");
		final Iterator<TimeWindowColumnSetting> iterator = timeWindowColumnSettingList.iterator();
		while (iterator.hasNext()) {
			iterator.next().marschall(xml);
		}
	}

	public int sizeTimeWindowColumnSettingList() {
		return timeWindowColumnSettingList.size();
	}
}
