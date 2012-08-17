package org.docear.plugin.core.util;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.prefs.Preferences;

import org.freeplane.core.util.LogUtils;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import com.sun.jna.platform.win32.WinReg.HKEY;

public class WinRegistry {
	
	public static final int HKEY_CLASSES_ROOT = 0x80000000;
	public static final int HKEY_CURRENT_USER = 0x80000001;
	  public static final int HKEY_LOCAL_MACHINE = 0x80000002;
	  public static final int HKEY_USERS = 0x80000003;
	  
	  public static final int REG_SUCCESS = 0;
	  public static final int REG_NOTFOUND = 2;
	  public static final int REG_ACCESSDENIED = 5;
	  
	  public static final int REG_SZ = 1;
	  public static final int REG_DWORD = 4;

			   

	  private static final int KEY_ALL_ACCESS = 0xf003f;
	  private static final int KEY_READ = 0x20019;
	  private static Preferences userRoot = Preferences.userRoot();
	  private static Preferences systemRoot = Preferences.systemRoot();
	  private static Class<? extends Preferences> userClass = userRoot.getClass();
	  private static Method regOpenKey = null;
	  private static Method regCloseKey = null;
	  private static Method regQueryValueEx = null;
	  private static Method regEnumValue = null;
	  private static Method regQueryInfoKey = null;
	  private static Method regEnumKeyEx = null;
	  private static Method regCreateKeyEx = null;
	  private static Method regSetValueEx = null;
	  private static Method regDeleteKey = null;
	  private static Method regDeleteValue = null;

	  static {
	    try {
	      regOpenKey = userClass.getDeclaredMethod("WindowsRegOpenKey",
	          new Class[] { int.class, byte[].class, int.class });
	      regOpenKey.setAccessible(true);
	      regCloseKey = userClass.getDeclaredMethod("WindowsRegCloseKey",
	          new Class[] { int.class });
	      regCloseKey.setAccessible(true);
	      regQueryValueEx = userClass.getDeclaredMethod("WindowsRegQueryValueEx",
	          new Class[] { int.class, byte[].class });
	      regQueryValueEx.setAccessible(true);
	      regEnumValue = userClass.getDeclaredMethod("WindowsRegEnumValue",
	          new Class[] { int.class, int.class, int.class });
	      regEnumValue.setAccessible(true);
	      regQueryInfoKey = userClass.getDeclaredMethod("WindowsRegQueryInfoKey1",
	          new Class[] { int.class });
	      regQueryInfoKey.setAccessible(true);
	      regEnumKeyEx = userClass.getDeclaredMethod(  
	          "WindowsRegEnumKeyEx", new Class[] { int.class, int.class,  
	              int.class });  
	      regEnumKeyEx.setAccessible(true);
	      regCreateKeyEx = userClass.getDeclaredMethod(  
	          "WindowsRegCreateKeyEx", new Class[] { int.class,  
	              byte[].class });  
	      regCreateKeyEx.setAccessible(true);  
	      regSetValueEx = userClass.getDeclaredMethod(  
	          "WindowsRegSetValueEx", new Class[] { int.class,  
	              byte[].class, byte[].class });  
	      regSetValueEx.setAccessible(true); 
	      regDeleteValue = userClass.getDeclaredMethod(  
	          "WindowsRegDeleteValue", new Class[] { int.class,  
	              byte[].class });  
	      regDeleteValue.setAccessible(true); 
	      regDeleteKey = userClass.getDeclaredMethod(  
	          "WindowsRegDeleteKey", new Class[] { int.class,  
	              byte[].class });  
	      regDeleteKey.setAccessible(true); 
	    }
	    catch (Exception e) {
	      e.printStackTrace();
	    }
	  }
	  
	  private WinRegistry() {  }

	  /**
	   * Read a value from key and value name
	   * @param hkey   HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
	   * @param key
	   * @param valueName
	   * @return the value
	   * @throws IllegalArgumentException
	   * @throws IllegalAccessException
	   * @throws InvocationTargetException
	   */
	  public static String readString(int hkey, String key, String valueName) 
		throws IllegalArgumentException, IllegalAccessException, 
		InvocationTargetException {
	    if (hkey == HKEY_LOCAL_MACHINE) {
	      return readString(systemRoot, hkey, key, valueName);
	    }
	    else if (hkey == HKEY_CURRENT_USER) {
	      return readString(userRoot, hkey, key, valueName);
	    }
	    else {
	      throw new IllegalArgumentException("hkey=" + hkey);
	    }
	  }

	  /**
	   * Read value(s) and value name(s) form given key 
	   * @param hkey  HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
	   * @param key
	   * @return the value name(s) plus the value(s)
	   * @throws IllegalArgumentException
	   * @throws IllegalAccessException
	   * @throws InvocationTargetException
	   */
	  public static Map<String, String> readStringValues(int hkey, String key) 
		throws IllegalArgumentException, IllegalAccessException, 
		InvocationTargetException {
	    if (hkey == HKEY_LOCAL_MACHINE) {
	      return readStringValues(systemRoot, hkey, key);
	    }
	    else if (hkey == HKEY_CURRENT_USER) {
	      return readStringValues(userRoot, hkey, key);
	    }
	    else {
	      throw new IllegalArgumentException("hkey=" + hkey);
	    }
	  }

	  /**
	   * Read the value name(s) from a given key
	   * @param hkey  HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
	   * @param key
	   * @return the value name(s)
	   * @throws IllegalArgumentException
	   * @throws IllegalAccessException
	   * @throws InvocationTargetException
	   */
	  public static List<String> readStringSubKeys(int hkey, String key) 
	    throws IllegalArgumentException, IllegalAccessException,
	    InvocationTargetException {
	    if (hkey == HKEY_LOCAL_MACHINE) {
	      return readStringSubKeys(systemRoot, hkey, key);
	    }
	    else if (hkey == HKEY_CURRENT_USER) {
	      return readStringSubKeys(userRoot, hkey, key);
	    }
	    else {
	      throw new IllegalArgumentException("hkey=" + hkey);
	    }
	  }

	  /**
	   * Create a key
	   * @param hkey  HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
	   * @param key
	   * @throws IllegalArgumentException
	   * @throws IllegalAccessException
	   * @throws InvocationTargetException
	   */
	  public static void createKey(int hkey, String key) 
	    throws IllegalArgumentException, IllegalAccessException,
	    InvocationTargetException {
	    int [] ret;
	    if (hkey == HKEY_LOCAL_MACHINE) {
	      ret = createKey(systemRoot, hkey, key);
	      regCloseKey.invoke(systemRoot, new Object[] { new Integer(ret[0]) });
	    }
	    else if (hkey == HKEY_CURRENT_USER) {
	      ret = createKey(userRoot, hkey, key);
	      regCloseKey.invoke(userRoot, new Object[] { new Integer(ret[0]) });
	    }
	    else {
	      throw new IllegalArgumentException("hkey=" + hkey);
	    }
	    if (ret[1] != REG_SUCCESS) {
	      throw new IllegalArgumentException("rc=" + ret[1] + "  key=" + key);
	    }
	  }

	  /**
	   * Write a value in a given key/value name
	   * @param hkey
	   * @param key
	   * @param valueName
	   * @param value
	   * @throws IllegalArgumentException
	   * @throws IllegalAccessException
	   * @throws InvocationTargetException
	   */
	  public static void writeStringValue
	    (int hkey, String key, String valueName, String value) 
	    throws IllegalArgumentException, IllegalAccessException,
	    InvocationTargetException {
	    if (hkey == HKEY_LOCAL_MACHINE) {
	      writeStringValue(systemRoot, hkey, key, valueName, value);
	    }
	    else if (hkey == HKEY_CURRENT_USER) {
	      writeStringValue(userRoot, hkey, key, valueName, value);
	    }
	    else {
	      throw new IllegalArgumentException("hkey=" + hkey);
	    }
	  }

	  /**
	   * Delete a given key
	   * @param hkey
	   * @param key
	   * @throws IllegalArgumentException
	   * @throws IllegalAccessException
	   * @throws InvocationTargetException
	   */
	  public static void deleteKey(int hkey, String key) 
	    throws IllegalArgumentException, IllegalAccessException,
	    InvocationTargetException {
	    int rc = -1;
	    if (hkey == HKEY_LOCAL_MACHINE) {
	      rc = deleteKey(systemRoot, hkey, key);
	    }
	    else if (hkey == HKEY_CURRENT_USER) {
	      rc = deleteKey(userRoot, hkey, key);
	    }
	    if (rc != REG_SUCCESS) {
	      throw new IllegalArgumentException("rc=" + rc + "  key=" + key);
	    }
	  }
	  
	  public static void exportKey(int hkey, String key, String file) throws IOException {		  
		 exportKey(hkey, key, new FileOutputStream(file)); 
	  }
	  
	  public static void exportKey(int hkey, String key, OutputStream stream) throws IOException {
		  try {
			  PrintStream printer = new PrintStream(stream);
			  keycount = 0;
			  printSubKey(hkey, key, printer);
			  printer.flush();
			  LogUtils.info("exported "+ keycount+" registry keys");
		  } catch (Exception e) {
			  throw new IOException(e);
		  }
	  }
	  static int keycount;
	  private static void printSubKey(int hkey, String key, PrintStream printer) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, IOException {
		  printer.println("["+getHKeyName(hkey)+"\\"+key+"]");
		  keycount++;
		  Set<Entry<String, Object>> entries = new HashSet<Entry<String, Object>>();
		  try{
			  entries = Advapi32Util.registryGetValues(getHKey(hkey), key).entrySet();
		  } 
		  catch(IllegalArgumentException e){}
		  
		  for(Entry<String, Object> entry : entries) {
			  if(entry.getValue() instanceof Integer) {
				  String value = Integer.toHexString(((Integer)entry.getValue()));
				  while(value.length() < 8) {
					  value = "0"+value;
				  }
				  printer.println("\""+entry.getKey()+"\"=dword:"+value+"");
			  }
			  else {
				  printer.println("\""+entry.getKey()+"\"=\""+entry.getValue()+"\"");
			  }
		  }		
		  
		  printer.println("");
		  for(String subKey : WinRegistry.readStringSubKeys(hkey, key)) {
			  printSubKey(hkey, key+"\\"+subKey, printer);
		  }
	  }

	  

	/**
	   * delete a value from a given key/value name
	   * @param hkey
	   * @param key
	   * @param value
	   * @throws IllegalArgumentException
	   * @throws IllegalAccessException
	   * @throws InvocationTargetException
	   */
	  public static void deleteValue(int hkey, String key, String value) 
	    throws IllegalArgumentException, IllegalAccessException,
	    InvocationTargetException {
	    int rc = -1;
	    if (hkey == HKEY_LOCAL_MACHINE) {
	      rc = deleteValue(systemRoot, hkey, key, value);
	    }
	    else if (hkey == HKEY_CURRENT_USER) {
	      rc = deleteValue(userRoot, hkey, key, value);
	    }
	    if (rc != REG_SUCCESS) {
	      throw new IllegalArgumentException("rc=" + rc + "  key=" + key + "  value=" + value);
	    }
	  }

	  // =====================

	  private static int deleteValue
	    (Preferences root, int hkey, String key, String value)
	    throws IllegalArgumentException, IllegalAccessException,
	    InvocationTargetException {
	    int[] handles = (int[]) regOpenKey.invoke(root, new Object[] {
	        new Integer(hkey), toCstr(key), new Integer(KEY_ALL_ACCESS) });
	    if (handles[1] != REG_SUCCESS) {
	      return handles[1];  // can be REG_NOTFOUND, REG_ACCESSDENIED
	    }
	    int rc =((Integer) regDeleteValue.invoke(root,  
	        new Object[] { 
	          new Integer(handles[0]), toCstr(value) 
	          })).intValue();
	    regCloseKey.invoke(root, new Object[] { new Integer(handles[0]) });
	    return rc;
	  }

	  private static int deleteKey(Preferences root, int hkey, String key) 
	    throws IllegalArgumentException, IllegalAccessException,
	    InvocationTargetException {
	    int rc =((Integer) regDeleteKey.invoke(root,  
	        new Object[] { new Integer(hkey), toCstr(key) })).intValue();
	    return rc;  // can REG_NOTFOUND, REG_ACCESSDENIED, REG_SUCCESS
	  }

	  private static String readString(Preferences root, int hkey, String key, String value)
	    throws IllegalArgumentException, IllegalAccessException,
	    InvocationTargetException {
	    int[] handles = (int[]) regOpenKey.invoke(root, new Object[] {
	        new Integer(hkey), toCstr(key), new Integer(KEY_READ) });
	    if (handles[1] != REG_SUCCESS) {
	      return null; 
	    }
	    byte[] valb = (byte[]) regQueryValueEx.invoke(root, new Object[] {
	        new Integer(handles[0]), toCstr(value) });
	    regCloseKey.invoke(root, new Object[] { new Integer(handles[0]) });
	    return (valb != null ? new String(valb).trim() : null);
	  }

	  private static Map<String,String> readStringValues
	    (Preferences root, int hkey, String key)
	    throws IllegalArgumentException, IllegalAccessException,
	    InvocationTargetException {
	    HashMap<String, String> results = new HashMap<String,String>();
	    int[] handles = (int[]) regOpenKey.invoke(root, new Object[] {
	        new Integer(hkey), toCstr(key), new Integer(KEY_READ) });
	    if (handles[1] != REG_SUCCESS) {
	      return null;
	    }
	    int[] info = (int[]) regQueryInfoKey.invoke(root,
	        new Object[] { new Integer(handles[0]) });

	    int count = info[2]; // count  
	    int maxlen = info[4]; // value length max
	    for(int index=0; index<count; index++)  {
	      byte[] name = (byte[]) regEnumValue.invoke(root, new Object[] {
	          new Integer
	            (handles[0]), new Integer(index), new Integer(maxlen + 1)});
	      if(name != null){
	    	  String value = readString(hkey, key, new String(name));
	    	  results.put(new String(name).trim(), value);
	      }
	    }
	    regCloseKey.invoke(root, new Object[] { new Integer(handles[0]) });
	    return results;
	  }

	  private static List<String> readStringSubKeys
	    (Preferences root, int hkey, String key)
	    throws IllegalArgumentException, IllegalAccessException,
	    InvocationTargetException {
	    List<String> results = new ArrayList<String>();
	    int[] handles = (int[]) regOpenKey.invoke(root, new Object[] {
	        new Integer(hkey), toCstr(key), new Integer(KEY_READ) 
	        });
	    if (handles[1] != REG_SUCCESS) {
	      return null;
	    }
	    int[] info = (int[]) regQueryInfoKey.invoke(root,
	        new Object[] { new Integer(handles[0]) });

	    int count = info[0]; // count  
	    int maxlen = info[3]; // value length max
	    for(int index=0; index<count; index++)  {
	      byte[] name = (byte[]) regEnumKeyEx.invoke(root, new Object[] {
	          new Integer
	            (handles[0]), new Integer(index), new Integer(maxlen + 1)
	          });
	      if(name != null){
	    	  results.add(new String(name).trim());
	      }
	    }
	    regCloseKey.invoke(root, new Object[] { new Integer(handles[0]) });
	    return results;
	  }

	  private static int [] createKey(Preferences root, int hkey, String key)
	    throws IllegalArgumentException, IllegalAccessException,
	    InvocationTargetException {
	    return  (int[]) regCreateKeyEx.invoke(root,
	        new Object[] { new Integer(hkey), toCstr(key) });
	  }

	  private static void writeStringValue 
	    (Preferences root, int hkey, String key, String valueName, String value) 
	    throws IllegalArgumentException, IllegalAccessException,
	    InvocationTargetException 
	  {
	    int[] handles = (int[]) regOpenKey.invoke(root, new Object[] {
	        new Integer(hkey), toCstr(key), new Integer(KEY_ALL_ACCESS) });

	    regSetValueEx.invoke(root,  
	        new Object[] { 
	          new Integer(handles[0]), toCstr(valueName), toCstr(value) 
	          }); 
	    regCloseKey.invoke(root, new Object[] { new Integer(handles[0]) });
	  }

	  // utility
	  private static byte[] toCstr(String str) {
	    byte[] result = new byte[str.length() + 1];

	    for (int i = 0; i < str.length(); i++) {
	      result[i] = (byte) str.charAt(i);
	    }
	    result[str.length()] = 0;
	    return result;
	  }

	public static Object[] parseKey(String key) throws IOException {
		Object[] result = new Object[2];
		if(key.toUpperCase().startsWith("HKEY_LOCAL_MACHINE\\")) {
			result[0] = new Integer(HKEY_LOCAL_MACHINE);
			result[1] = key.substring("HKEY_LOCAL_MACHINE\\".length());
		} 
		else if(key.toUpperCase().startsWith("HKEY_CURRENT_USER\\")) {
			result[0] = new Integer(HKEY_CURRENT_USER);
			result[1] = key.substring("HKEY_CURRENT_USER\\".length());
		} 
		else if(key.toUpperCase().startsWith("HKEY_CLASSES_ROOT\\")) {
			result[0] = new Integer(HKEY_CLASSES_ROOT);
			result[1] = key.substring("HKEY_CLASSES_ROOT\\".length());
		} 
		else if(key.toUpperCase().startsWith("HKEY_USERS\\")) {
			result[0] = new Integer(HKEY_USERS);
			result[1] = key.substring("HKEY_USERS\\".length());
		} 
		else {
			throw new IOException("missing hive key");
		}
		return result;
	}
	
	public static void writeIntValue(int hkey, String key, String name, int value) throws IOException {
		
		Advapi32Util.registrySetIntValue(getHKey(hkey), key, name, value);
			
	}
	
	private static HKEY getHKey(int hkey) throws IOException {
		switch(hkey) {
			case HKEY_CURRENT_USER: return WinReg.HKEY_CURRENT_USER;
			case HKEY_CLASSES_ROOT: return WinReg.HKEY_CLASSES_ROOT;
			case HKEY_LOCAL_MACHINE: return WinReg.HKEY_LOCAL_MACHINE;
			case HKEY_USERS: return WinReg.HKEY_USERS;
			
			default: {
				throw new IOException("invalid hive key");
			}
		}
	}
	
	private static String getHKeyName(int hkey) throws IOException {
		switch(hkey) {
			case HKEY_CURRENT_USER: return "HKEY_CURRENT_USER";
			case HKEY_CLASSES_ROOT: return "HKEY_CLASSES_ROOT";
			case HKEY_LOCAL_MACHINE: return "HKEY_LOCAL_MACHINE";
			case HKEY_USERS: return "HKEY_USERS";
			
			default: {
				throw new IOException("invalid hive key");
			}
		}
	}

	public static void importFile(String fileName) throws IOException {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String line = null;
			Object[] key = null;
			while( (line = reader.readLine()) != null) {
				if(line.startsWith("[")) {
					key = parseKey(line.substring(1, line.length()-1));
					continue;
				}
				int idx = line.indexOf("=");
				if((line.trim().length() > 0) && (idx > -1)) {
					String name = line.substring(1,idx-1);
					String value = line.substring(idx+1);
					if(value.startsWith("dword:")) {
						writeIntValue((Integer)key[0], key[1].toString(), name, Integer.parseInt(value.substring("dword:".length())));
					}
					else if(value.startsWith("\"")) {
						writeStringValue((Integer)key[0], key[1].toString(), name, value.substring(1,value.length()-1));
					}
				}
			}
		}
		catch (Exception e) {
			throw new IOException(e);
		}
				
	}
	  
	  /** 
	     * Java wrapper for Windows registry API RegSetValueEx()
	     */
//	    private static native int DocearRegSetValueEx(int hKey, int valueType, byte[] valueName, 
//	                                                         byte[] value);

}
