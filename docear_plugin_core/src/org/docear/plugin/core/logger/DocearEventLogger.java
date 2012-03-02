package org.docear.plugin.core.logger;

import java.io.File;
import java.net.URL;

import org.freeplane.core.util.LogUtils;

public class DocearEventLogger {
	
	public DocearEventLogger() {		
		
	}
	
	public void write(Object source, DocearLogEvent event) {
		this.appendToLog(source, event, "");
	}
	
	public void write(Object source, DocearLogEvent event, boolean eventdata) {
		this.write(source, event, (eventdata ? 1:0));
	}
	
	public void write(Object source, DocearLogEvent event, int eventdata) {
		this.appendToLog(source, event, ""+eventdata);
	}
	
	public void write(Object source, DocearLogEvent event, File eventdata) {
		this.appendToLog(source, event, eventdata.getAbsolutePath());
	}
	
	public void write(Object source, DocearLogEvent event, URL eventdata) {
		this.appendToLog(source, event, eventdata.toString());
	}
	
	public void write(Object source, DocearLogEvent event, String eventdata) {
		this.appendToLog(source, event, eventdata);
	}
	
	private void appendToLog(Object source, DocearLogEvent event, String eventdata) {
		LogUtils.info("write to logfile: "+System.currentTimeMillis()+" | "+source.getClass().getName()+" | "+event.getId()+" | "+eventdata);
		LogUtils.info("---");
	}
}
