package org.docear.plugin.dragbase;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mapio.MapIO;
import org.freeplane.features.mapio.mindmapmode.MMapIO;
import org.freeplane.features.mode.Controller;

public class DragBaseWrapper {
	
	// Reference to the library
	static
	{
		if(Compat.isWindowsOS()){
			try{
				String processor = System.getProperty("org.osgi.framework.processor");
				if(processor.equalsIgnoreCase("x86")){
					System.loadLibrary("dragbase32");
					libraryLoaded = true;					
				}
				else if(processor.equalsIgnoreCase("x86-64")){
					System.loadLibrary("dragbase64");
					libraryLoaded = true;
				}
			}catch(Exception e){
				LogUtils.warn(e);
			}
		}
	}
	
	static boolean libraryLoaded = false;
	
	/* Intializes dragbase
	 * Parameters:
	 * frame    - Window which is used for dragbase
	 * si       - Class implementing the SaveInterface, Save is always called
	 *            when dragbase needs to save a file.
	 * app_name - short string identifier of your application
	 * 
	 * Return: 0 if successful, otherwise an error code (see documentation)
	 */
	public int Create()	{		
		if(!libraryLoaded) return 0;
		
		String title = Controller.getCurrentController().getViewController().getFrame().getTitle();
		
		Properties freeplaneProperties = new Properties();
		try {
			freeplaneProperties.load(Compat.class.getClassLoader().getResourceAsStream(ResourceController.FREEPLANE_PROPERTIES));
		} catch (IOException e) {
			LogUtils.warn(e);
		}
		String applicationName = freeplaneProperties.getProperty("ApplicationName", "freeplane").toLowerCase(Locale.ENGLISH);
		
		SaveInterface saveInterface = new SaveInterface() {
			
			public int Save(String filename, String directory) {
				final MMapIO mapIO = (MMapIO) Controller.getCurrentModeController().getExtension(MapIO.class);
				final MapModel map = Controller.getCurrentController().getMap();
				try {					
					mapIO.writeToFile(map, new File(filename));
				}
				catch (Exception e) {
					LogUtils.severe("Could not create '"+filename+"' for dragbase.",e);
					return 0;
				}	
				return 1;
			}
		};
		
		
		int ret = Create_(title, saveInterface, applicationName);
		System.out.println("dragbase return: " + ret);
		return ret;
	}
	
	/* Specifies a new file name used by dragbase
	 * Parameters:
	 * filename - The file name, including the path.
	 */
	public native void SetFilename(String filename);
	
	private native int Create_(String window_title, SaveInterface si, String app_name);

}

//Interface which is used by dragbase
interface SaveInterface {
	
	/* Parameters:
	 * filename - The file name, including the path.
	 * directory - The directory where the file is stored
	 * Return 1 if successful and 0 if not
	 */
	
	public int Save(String filename,String directory);
}
