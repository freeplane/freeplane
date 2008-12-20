package org.freeplane.addins.mindmapmode.time;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JDialog;

import org.freeplane.controller.resources.WindowConfigurationStorage;
import org.freeplane.io.xml.n3.nanoxml.IXMLElement;



class TimeWindowConfigurationStorage extends WindowConfigurationStorage {
public void addTimeWindowColumnSetting(TimeWindowColumnSetting timeWindowColumnSetting) {
    timeWindowColumnSettingList.add(timeWindowColumnSetting);
  }

  public void addAtTimeWindowColumnSetting(int position, TimeWindowColumnSetting timeWindowColumnSetting) {
    timeWindowColumnSettingList.add(position, timeWindowColumnSetting);
  }

  public TimeWindowColumnSetting getTimeWindowColumnSetting(int index) {
    return (TimeWindowColumnSetting)timeWindowColumnSettingList.get( index );
  }

  public int sizeTimeWindowColumnSettingList() {
    return timeWindowColumnSettingList.size();
  }

  public void clearTimeWindowColumnSettingList() {
    timeWindowColumnSettingList.clear();
  }

  public java.util.List getListTimeWindowColumnSettingList() {
    return java.util.Collections.unmodifiableList(timeWindowColumnSettingList);
  }

  protected ArrayList<TimeWindowColumnSetting> timeWindowColumnSettingList = new ArrayList();

@Override
protected void marschallSpecificElements(IXMLElement xml) {
	xml.setName("time_window_configuration_storage");
	final Iterator<TimeWindowColumnSetting> iterator = timeWindowColumnSettingList.iterator();
	while(iterator.hasNext()){
		iterator.next().marschall(xml);
	}
}
public static TimeWindowConfigurationStorage decorateDialog(String marshalled,
                                                                    JDialog dialog) {
	TimeWindowConfigurationStorage storage = new TimeWindowConfigurationStorage();
	IXMLElement xml = storage.unmarschall(marshalled, dialog);
	if(xml != null){
		final Iterator iterator = xml.getChildren().iterator();
		while(iterator.hasNext()){
			storage.addTimeWindowColumnSetting(TimeWindowColumnSetting.create((IXMLElement)iterator.next()));
		}
		return storage;
	}
	return null;
      }
}
