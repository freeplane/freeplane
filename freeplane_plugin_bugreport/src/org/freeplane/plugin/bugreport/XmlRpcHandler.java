package org.freeplane.plugin.bugreport;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;
import java.util.regex.Pattern;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.freeplane.core.controller.FreeplaneVersion;
import org.freeplane.core.resources.FpStringUtils;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogTool;

public class XmlRpcHandler extends StreamHandler{
	private static ByteArrayOutputStream out = null;
	private MessageDigest md = null;
	private boolean reportCollected = false;
	private String hash = null;
	private String log = null;
	final private ReportRegistry register;
	private boolean isRunning;
	
	static final private String OPTION="org.freeplane.plugin.bugreport";
	static final private String ALLOWED="org.freeplane.plugin.bugreport.allowed";
	static final private String DENIED="org.freeplane.plugin.bugreport.denied";
	static final private String ASK="org.freeplane.plugin.bugreport.ask";
	
	
	public XmlRpcHandler() {
	    super();
	    try {
	        setEncoding("UTF-8");
        }
        catch (SecurityException e) {
        }
        catch (UnsupportedEncodingException e) {
        }
	    setFormatter(new BugFormatter());
	    setLevel(Level.SEVERE);
	    register = new ReportRegistry();
    }


	public synchronized void publish(LogRecord record) {
		if(disabled || isRunning){
			return;
		}
		if(out == null){
			out = new ByteArrayOutputStream();
			setOutputStream(out);
		}
		if (!isLoggable(record)) {
		    return;
		}
		if(! reportCollected){
			reportCollected = true;
			EventQueue.invokeLater(new SubmitStarter());
		}
		super.publish(record);
	}

	private class SubmitStarter implements Runnable{ 
		public void run() {
			startSubmit();
		}
	}

	private void startSubmit() {
		isRunning = true;
		Thread submitterThread = new Thread(new SubmitRunner(), "RemoteLog");
		submitterThread.start();
	}

	private class SubmitRunner implements Runnable{ 
		public void run() {
			runSubmit();
		}
	}

	private void runSubmit() {
		try {
			close();
			createInfo();
			log = info + out.toString(getEncoding());
			if(log.equals("")){
				return;				
			}
			hash = calculateHash();
			if(hash == null){
				return;
			}
			if(register.isReportRegistered(hash)){
				return;
			}
			if (ALLOWED.equals(showBugReportDialog(log))){
				register.registerReport(hash);
				Map<String, String> report = new LinkedHashMap<String, String>();
				report.put("hash", hash);
				report.put("log", log);
				report.put("version", version);
				sendReport(report);
			}
		} catch (UnsupportedEncodingException e) {
			LogTool.severe(e);
		}
		finally{
			out = null;
			reportCollected = false;
			isRunning = false;
		}
	}


	private String showBugReportDialog(String log) {
	    final String option = ResourceController.getResourceController().getProperty(OPTION, ASK);
	    if(! option.equals(ASK)){
	    	return option;
	    }
	    final Box messagePane = Box.createVerticalBox();
	    final JLabel messageLabel = new JLabel(ResourceBundles.getText("org.freeplane.plugin.bugreport.question"));
	    messageLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
	    messagePane.add(messageLabel);
	    messagePane.add(Box.createVerticalStrut(10));
	    final JLabel messageLabel2 = new JLabel(ResourceBundles.getText("org.freeplane.plugin.bugreport.report"));
	    messageLabel2.setAlignmentX(JLabel.LEFT_ALIGNMENT);
	    messagePane.add(messageLabel2);	    
	    final JTextArea historyArea = new JTextArea(log);
	    historyArea.setEditable(false);
	    final JScrollPane historyPane = new JScrollPane(historyArea);
	    historyPane.setPreferredSize(new Dimension(500, 300));
	    historyPane.setAlignmentX(JLabel.LEFT_ALIGNMENT);
	    messagePane.add(historyPane);			
	    final Object[] options = new Object[]{
	    		ResourceBundles.getText("org.freeplane.plugin.bugreport.always_agree"),
	    		ResourceBundles.getText("org.freeplane.plugin.bugreport.agree"),
	    		ResourceBundles.getText("org.freeplane.plugin.bugreport.deny"),
	    		ResourceBundles.getText("org.freeplane.plugin.bugreport.always_deny")};
	    final int choice = JOptionPane.showOptionDialog(null, 
	    	messagePane, ResourceBundles.getText("org.freeplane.plugin.bugreport.dialog.title"), JOptionPane.DEFAULT_OPTION, 
	    	JOptionPane.INFORMATION_MESSAGE, null, options, options[1]);
	    final String decision;
	    switch(choice){
	    	case 0: 
	    		decision = ALLOWED; 
	    		ResourceController.getResourceController().setProperty(OPTION, decision);
	    		break;
			case 1:
				decision = ALLOWED;
				break;
			case 2:
				decision = DENIED;
				break;
			case 3:
				decision = DENIED;
				ResourceController.getResourceController().setProperty(OPTION, decision);
				break;
			default:
				decision = DENIED;
				break;
		}
	    return decision;
    }
	
	private void createInfo() {
	    if(info == null){
	    	StringBuilder sb = new StringBuilder();
	    	sb.append("freeplane_version = ");
	    	version = FreeplaneVersion.getVersion().toString();
			sb.append(version);
	    	sb.append("; freeplane_xml_version = ");
	    	sb.append(FreeplaneVersion.XML_VERSION);
	    	sb.append("\njava_version = ");
	    	sb.append(System.getProperty("java.version"));
	    	sb.append("; os_name = ");
	    	sb.append(System.getProperty("os.name"));
	    	sb.append("; os_version = ");
	    	sb.append(System.getProperty("os.version"));
	    	sb.append('\n');			
	    	info = sb.toString();
	    }
    }


	private String sendReport(Map <String, String> reportFields) {
		   try {
		        // Construct data
		        StringBuilder data = new StringBuilder();
		        for(Entry<String, String> entry: reportFields.entrySet()){
		        	if(data.length() != 0){
				        data.append('&');
		        	}
			        data.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
			        data.append('=');
			        data.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
		        }
		    
		        // Send data
				URL url = new URL(getBugTrackerUrl());
		        URLConnection conn = url.openConnection();
		        conn.setDoOutput(true);
		        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		        wr.write(data.toString());
		        wr.flush();
		    
		        // Get the response
		        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		        String line = rd.readLine();
		        if(line != "ok"){
		        	do {
		        		System.out.println(line);
		        		line = rd.readLine();
		        	} while (line != null);
		        }
		        wr.close();
		        rd.close();
		        return line;
		    } catch (Exception e) {
		    }
	    	return null;
    }


	private final static String BUG_TRACKER_REFERENCE_URL = "http://freeplane.sourceforge.net/info/bugtracker.ref.txt";
	private static String BUG_TRACKER_URL = null;
	static boolean disabled = false;
	private static String info;
	private static String version;
	private String getBugTrackerUrl() {
		if(BUG_TRACKER_URL != null){
			return BUG_TRACKER_URL;
		}
		try {
	        URL url = new URL(BUG_TRACKER_REFERENCE_URL);
	        BufferedReader in = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream()));
	        BUG_TRACKER_URL = in.readLine(); 
	        return BUG_TRACKER_URL;
        }
        catch (Exception e) {
        	disabled = true;
        	return null;
        }
	}
		    

	private String calculateHash() {
		final String[] lines = log.split("\n");
		StringBuffer hashInput  = new StringBuffer();
		hashInput.append(version);
		for(int i = 0; i < lines.length; i++){
			final String s = lines[i];
			if(s.startsWith("\tat org.freeplane.") || s.startsWith("missing key ")){
				hashInput.append(s);
			}
		}
		if(hashInput.length() == version.length()){
			return null;
		}
			try {
				return calculateHash(hashInput.toString().getBytes(getEncoding()));
			} catch (UnsupportedEncodingException e) {
				return null;
			}
	}

	private String calculateHash(byte[] byteArray) {
		try {
			if(md == null){
				md = MessageDigest.getInstance ("MD5");
			}
			final byte[] digest = md.digest(byteArray);
			return toHexString(digest); 
		} catch (Exception e) {
			LogTool.warn(e);
			return null;
		}
	}

	private static String toHexString(byte[] v) {
		final String HEX_DIGITS = "0123456789abcdef";
		StringBuffer sb = new StringBuffer(v.length * 2);
		for (int i = 0; i < v.length; i++) {
			int b = v[i] & 0xFF;
			sb.append(HEX_DIGITS.charAt(b >>> 4))
			.append(HEX_DIGITS.charAt(b & 0xF));
		}
		return sb.toString();
	}
}
