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
	
	public static String createRandomString(int numBits) {
		SecureRandom random = new SecureRandom();
		String s = new BigInteger(numBits, random).toString(Character.MAX_RADIX);
		return s;
	}
	
}
