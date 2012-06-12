package org.docear.plugin.core.util;


public class CompareVersion { 

    final public static int LESSER = -1; // versionA is lesser than versionB
    final public static int EQUALS = 0; // versionA equal to versionB
    final public static int GREATER = 1; // versionA is greater then versionB

    public static int compareVersions(String versionA, String versionB) throws NumberFormatException{
    	
    	if(versionA == null && versionB == null){
    		return EQUALS;
    	}
    	if(versionA == null && versionB != null){
    		return LESSER;
    	}
    	if(versionA != null && versionB == null){
    		return GREATER;
    	}
    	
        String[] a = versionA.split("\\.");
        String[] b = versionB.split("\\.");

        int i, j;
        int index = 0;
        
        while ((index < a.length) && (index < b.length)) {
            i = Integer.parseInt(a[index]);
            j = Integer.parseInt(b[index]);
            if (i > j) {
                return CompareVersion.GREATER;
            } else if (i < j) {
                return CompareVersion.LESSER;
            }
            index++;
        }
        if ((index < a.length) && (index == b.length)) {
            return CompareVersion.GREATER;
        } else if ((index == a.length) && (index < b.length)) {
            return CompareVersion.LESSER;
        } else {
            return CompareVersion.EQUALS;
        }
        
    }

}

