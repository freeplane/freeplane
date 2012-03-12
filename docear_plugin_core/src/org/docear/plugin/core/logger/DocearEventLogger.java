package org.docear.plugin.core.logger;

import java.io.File;
import java.net.URL;

import org.freeplane.core.util.LogUtils;

public class DocearEventLogger {
	
	public DocearEventLogger() {		
		
	}
	
//	public void write(Object source, DocearLogEvent event) {
//		this.appendToLog(source, event, "");
//	}
//	
//	public void write(Object source, DocearLogEvent event, Boolean eventdata) {
//		this.write(source, event, (eventdata ? 1:0));
//	}
//	
//	public void write(Object source, DocearLogEvent event, Integer eventdata) {
//		this.appendToLog(source, event, ""+eventdata);
//	}
//	
//	public void write(Object source, DocearLogEvent event, File eventdata) {
//		this.appendToLog(source, event, eventdata.getAbsolutePath());
//	}
//	
//	public void write(Object source, DocearLogEvent event, URL eventdata) {
//		this.appendToLog(source, event, eventdata.toString());
//	}
//	
//	public void write(Object source, DocearLogEvent event, String eventdata) {
//		this.appendToLog(source, event, eventdata);
//	}
	
//	private void appendToLog(Object source, DocearLogEvent event, String eventdata) {
//		LogUtils.info("write to logfile: "+System.currentTimeMillis()+" | "+source.getClass().getName()+" | "+event.getId()+" | "+eventdata);
//		LogUtils.info("---");
//	}
//	
	private String getString(Object eventdata) {
		if (eventdata == null) {
			return "";
		}
		
		if (eventdata instanceof Boolean) {
			return ((Boolean) eventdata ? "1":"0");
		}
		else if (eventdata instanceof File) {
			return ((File) eventdata).getAbsolutePath();
		}
		
		
		return eventdata.toString();
	}
	
	public void appendToLog(Object source, DocearLogEvent event, Object... eventdata) {
		String s = "";
		
		if (eventdata != null && eventdata.length>0) {
			s += getString(eventdata[0]);
			for (int i = 1; i<eventdata.length; i++) {
				s += ";" + getString(eventdata[i]);
			}			
		}
		
		LogUtils.info("write to logfile: "+System.currentTimeMillis()+" | "+source.getClass().getName()+" | "+event.getId()+" | "+s);
		LogUtils.info("---");
	}

	public void appendToLog(Object source, DocearLogEvent event) {
		appendToLog(source, event, (Object) null);		
	}
	
	
}
