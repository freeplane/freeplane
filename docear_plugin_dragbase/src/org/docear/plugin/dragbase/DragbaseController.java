package org.docear.plugin.dragbase;

import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowStateListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.IMapChangeListener;
import org.freeplane.features.map.IMapSelectionListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.url.UrlManager;
import org.freeplane.features.url.mindmapmode.MFileManager;

public class DragbaseController {	
	
	private static Process dragbase = null;
	private static boolean startup = true;
	private static boolean dragbaserunning = true;
	private static dragbase.dragbase dragbaseWrapper = null;
	
	public static void startDragbasePlugin(ModeController modeController){
		
		if(Compat.isWindowsOS()){
			
			Controller.getCurrentController().getViewController().getJFrame().addWindowFocusListener(new WindowFocusListener() {
				
				public void windowLostFocus(WindowEvent e) {					
				}
				
				public void windowGainedFocus(WindowEvent e) {
					//dragbaseWrapper = new dragbase.dragbase();
					//dragbaseWrapper.Create();					
				}
			});
			
//			Controller.getCurrentController().getViewController().getJFrame().addWindowFocusListener(new WindowFocusListener() {
//				
//				public void windowLostFocus(WindowEvent e) {					
//				}
//				
//				public void windowGainedFocus(WindowEvent e) {					
//					if(startup && Controller.getCurrentController().getViewController().getFrame().isFocused()){
//						startup = false;
//						dragbaserunning = startDragbase();
//						checkDragbaseThread();
//						saveTempFile(Controller.getCurrentController().getMapViewManager().getModel());						
//					}
//					if(!dragbaserunning){
//						dragbaserunning = startDragbase();
//						checkDragbaseThread();
//					}
//				}
//			});
//			
//			Controller.getCurrentController().getViewController().getJFrame().addWindowStateListener(new WindowStateListener() {
//				
//				public void windowStateChanged(WindowEvent e) {
//					if(e.getID() == WindowEvent.WINDOW_CLOSING || e.getID() == WindowEvent.WINDOW_CLOSED){
//						stopDragbasePlugin();
//					}
//				}
//			});
//
//			modeController.getMapController().addMapChangeListener(new IMapChangeListener() {
//				
//				public void onPreNodeMoved(NodeModel oldParent, int oldIndex,
//						NodeModel newParent, NodeModel child, int newIndex) {					
//				}
//				
//				public void onPreNodeDelete(NodeModel oldParent, NodeModel selectedNode,
//						int index) {					
//				}
//				
//				public void onNodeMoved(NodeModel oldParent, int oldIndex,
//						NodeModel newParent, NodeModel child, int newIndex) {					
//				}
//				
//				public void onNodeInserted(NodeModel parent, NodeModel child, int newIndex) {					
//				}
//				
//				public void onNodeDeleted(NodeModel parent, NodeModel child, int index) {					
//				}
//				
//				public void mapChanged(MapChangeEvent event) {
//					if(!startup){
//						saveTempFile(event.getMap());
//					}
//				}
//			});
//			
//			Controller.getCurrentController().getMapViewManager().addMapSelectionListener(new IMapSelectionListener() {
//				
//				public void beforeMapChange(MapModel oldMap, MapModel newMap) {					
//				}
//				
//				public void afterMapClose(MapModel oldMap) {					
//				}
//				
//				public void afterMapChange(MapModel oldMap, MapModel newMap) {
//					if(!startup){
//						saveTempFile(newMap);
//					}
//				}
//			});
		}
		
	}
	
	public static void stopDragbasePlugin(){
		if(dragbase != null){
			dragbase.destroy();
		}
	}
	
	private static void checkDragbaseThread(){		
		ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(1));	
		executor.execute(new Runnable() {
			
			public void run() {
				if(dragbase != null){
					try {
						dragbase.waitFor();
						dragbaserunning = false;
					} catch (InterruptedException e) {										
					}
				}
			}
			
		});
	}

	private static boolean startDragbase() {
		// Try to start dragbase
		// could be in different directories
		String windir = System.getenv("windir");		
		if(windir == null || windir.length() <= 0) return false;		
		
		if(new File(windir + "\\system\\dragbase.exe").exists()){
			try {
				dragbase = new ProcessBuilder(windir + "\\system\\dragbase.exe").start();
			} catch (IOException e) {
				LogUtils.severe("Could not start Dragbase. Caused by IO Exception.", e);
				return false;
			}
			LogUtils.info("Started Dragbase for Dragbase plugin.");
			return true;
		}
		if(new File(windir + "\\system32\\dragbase.exe").exists()){
			try {
				dragbase = new ProcessBuilder(windir + "\\system32\\dragbase.exe").start();				
			} catch (IOException e) {
				LogUtils.severe("Could not start Dragbase. Caused by IO Exception.", e);
				return false;		
			}
			LogUtils.info("Started Dragbase for Dragbase plugin.");
			return true;
		}
		if(new File(windir + "\\sysWOW64\\dragbase.exe").exists()){
			try {
				dragbase = new ProcessBuilder(windir + "\\sysWOW64\\dragbase.exe").start();				
			} catch (IOException e) {
				LogUtils.severe("Could not start Dragbase. Caused by IO Exception.", e);
				return false;		
			}
			LogUtils.info("Started Dragbase for Dragbase plugin.");
			return true;
		}
		return false;
	}	
	
	private static boolean saveTempFile(final MapModel map)
	{		
		if(map == null) return false;
		
		SwingUtilities.invokeLater(new Runnable() {
			
			public void run() {
				// Get temp directory
				String temp_dir = System.getProperty("java.io.tmpdir");
				temp_dir += "tmp.mm";
				// Create file
				File file = new File(temp_dir);
				try {
					file.createNewFile();
					((MFileManager) UrlManager.getController()).writeToFile(map, file);			
				} catch (IOException e) {
					LogUtils.severe("Could not save map as tempfile. Caused by IO Exception.", e);
				}	
			}
		});		
		
		return true;
	}
	
	public static boolean dragbaseRunning(){
		return listRunningProcesses().contains("dragbase.exe");
	}
	
	 public static List<String> listRunningProcesses() {
	    List<String> processes = new ArrayList<String>();
	    try {
	      String line;
	      Process p = Runtime.getRuntime().exec("tasklist.exe /fo csv /nh");
	      BufferedReader input = new BufferedReader
	          (new InputStreamReader(p.getInputStream()));
	      while ((line = input.readLine()) != null) {
	          if (!line.trim().equals("")) {
	              // keep only the process name
	              line = line.substring(1);
	              processes.add(line.substring(0, line.indexOf(" ")));
	          }

	      }
	      input.close();
	    }
	    catch (Exception err) {
	      err.printStackTrace();
	    }
	    return processes;
	 }

}
