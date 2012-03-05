package org.docear.plugin.core.logger;

public enum DocearLogEvent {
	
	//Application
	APPLICATION_STARTED (101),
	APPLICATION_CLOSED (102),
	APPLICATION_MINIMIZED (103),
	APPLICATION_MAXIMIZED (104),
	
	//File actions
	MAP_OPENED (201),		//eventdata: filename
	MAP_CLOSED (202),		//eventdata: filename
	MAP_SAVED (203),		//eventdata: filename
	//MAP_AUTO_SAVED (204),	//eventdata: filename
	MAP_NEW (205),			
	FILE_OPENED (206),		//eventdata: testname
	
	//Software settings
	
	//Software usage
	//UPDATE_REFERENCES (401), //eventdata: <count changed keys>;<count new keys>
	OPEN_PREFERENCES (402),
	//VIEW_LOG_FILES (403),
	SHOW_HELP (404),
	//SHOW_CONTACT_AND_FEEDBACK (405),
	//IMPORT_BOOKMARS (406),
	//ADD_REFERENCE (407),
	
	MONITORING_FOLDER_ADD (412),
	MONITORING_FOLDER_REMOVE (413),
	MONITORING_FOLDER_EDIT (414),
	MONITORING_FOLDER_READ (415),
	//!!!USERNAME_CHANGE (416),
	MAP_CHANGE_ACTIVE (417),
	MONITORING_FOLDER_READ_ABORTED (418),
	OPEN_URL (419),			//eventdata: url
	
	
	//Operating system	
	OS_SCREEN_RESOLUTION (501),
	OS_OPERATING_SYSTEM (502),
	OS_LANGUAGE_CODE (503),
	OS_COUNTRY_CODE (504),
	OS_TIME_ZONE (505);
	private final int id;
	
	DocearLogEvent(int id) {
		this.id = id;
	}
	
	public final int getId() {
		return this.id;
	}
}