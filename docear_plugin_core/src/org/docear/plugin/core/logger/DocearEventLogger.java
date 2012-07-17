package org.docear.plugin.core.logger;

import java.io.File;

public class DocearEventLogger {
	
	public DocearEventLogger() {		
		
	}

	@SuppressWarnings("unused")
	private String getString(final Object eventdata) {
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
//		String s = "";
//		
//		if (eventdata != null && eventdata.length>0) {
//			s += getString(eventdata[0]);
//			for (int i = 1; i<eventdata.length; i++) {
//				s += ";" + getString(eventdata[i]);
//			}			
//		}
//		
//		LogUtils.info("write to logfile: "+System.currentTimeMillis()+" | "+source.getClass().getName()+" | "+event.getId()+" | "+s);
//		LogUtils.info("---");
	}

	public void appendToLog(Object source, DocearLogEvent event) {
		appendToLog(source, event, (Object) null);		
	}
	
	
}
