package org.docear.plugin.core.util;

import java.io.File;
import java.math.BigInteger;
import java.net.URI;
import java.security.SecureRandom;

import org.freeplane.plugin.workspace.WorkspaceUtils;

public class CoreUtils {
	public static File resolveURI(final URI uri) {
		return WorkspaceUtils.resolveURI(uri);
	}
	
	public static String createRandomString(int length) {
		SecureRandom random = new SecureRandom();
		String s = new BigInteger(length*8, random).toString(Character.MAX_RADIX);
		return s;
	}

	public static boolean isEmpty(Object o) {
		return o==null || o.toString().length()==0;		
	}
	
}
