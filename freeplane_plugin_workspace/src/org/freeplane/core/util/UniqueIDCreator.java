package org.freeplane.core.util;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Locale;

public abstract class UniqueIDCreator {
	private static UniqueIDCreator idCreator;	
	
	public abstract String uniqueID();
		
	public static void setCreator(UniqueIDCreator creator) {
		idCreator = creator;
	}
	
	public static UniqueIDCreator getCreator() {
		if(idCreator == null) {
			idCreator = new UniqueIDCreator() {
				public String uniqueID() {
					SecureRandom random = new SecureRandom();
					String s = new BigInteger(16*8, random).toString(Character.MAX_RADIX);
					return (Long.toHexString(System.currentTimeMillis())+s).toUpperCase(Locale.ENGLISH);
				}
			};
		}
		return idCreator;
	}
}
