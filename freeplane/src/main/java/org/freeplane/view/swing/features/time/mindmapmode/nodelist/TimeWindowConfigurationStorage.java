package org.freeplane.view.swing.features.time.mindmapmode.nodelist;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JDialog;

import org.freeplane.core.resources.WindowConfigurationStorage;
import org.freeplane.n3.nanoxml.XMLElement;

class TimeWindowConfigurationStorage extends WindowConfigurationStorage {
	public static TimeWindowConfigurationStorage decorateDialog(final String marshalled, final JDialog dialog) {
		final TimeWindowConfigurationStorage storage = new TimeWindowConfigurationStorage();
		final XMLElement xml = storage.unmarschall(marshalled, dialog);
		if (xml != null) {
			final Iterator<XMLElement> iterator = xml.getChildren().iterator();
			while (iterator.hasNext()) {
				storage.addTimeWindowColumnSetting(TimeWindowColumnSetting.create(iterator.next()));
			}
			return storage;
		}
		return null;
	}

	public TimeWindowConfigurationStorage() {
	    super("time_window_configuration_storage");
    }

	private List<TimeWindowColumnSetting> timeWindowColumnSettingList = new ArrayList<TimeWindowColumnSetting>();

	public void addTimeWindowColumnSetting(final TimeWindowColumnSetting timeWindowColumnSetting) {
		timeWindowColumnSettingList.add(timeWindowColumnSetting);
	}

	public List<TimeWindowColumnSetting> getListTimeWindowColumnSettingList() {
		return java.util.Collections.unmodifiableList(timeWindowColumnSettingList);
	}

	@Override
	protected void marshallSpecificElements(final XMLElement xml) {
		final Iterator<TimeWindowColumnSetting> iterator = timeWindowColumnSettingList.iterator();
		while (iterator.hasNext()) {
			iterator.next().marschall(xml);
		}
	}
}
