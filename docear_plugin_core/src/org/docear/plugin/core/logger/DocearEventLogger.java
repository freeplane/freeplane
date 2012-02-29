package org.docear.plugin.core.logger;

import org.freeplane.core.util.LogUtils;

public class DocearEventLogger {
	
	public enum DocearEvent {
		APPLICATION_STARTED (101),
		APPLICATION_CLOSED (102),
		APPLICATION_MINIMIZED (103),
		APPLICATION_MAXIMIZED (104),
		
		FILE_OPENED (201),		//eventdata: filename
		FILE_CLOSED (202),		//eventdata: filename
		FILE_SAVED (203),		//eventdata: filename
		FILE_AUTO_SAVED (204),	//eventdata: filename
		FILE_NEW (205);
		
		private final int id;
		
		DocearEvent(int id) {
			this.id = id;
		}
		
		public final int getId() {
			return this.id;
		}
	}
	
	
	
	public DocearEventLogger() {		
		
	}
	
	public void write(Object source, DocearEvent event, String message) {
		LogUtils.info("write to logfile: "+System.currentTimeMillis()+" | "+source.getClass().getName()+" | "+event.getId()+" | "+message);
		LogUtils.info("---");
	}
}
