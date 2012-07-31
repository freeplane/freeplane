package net.sf.jabref.export;

public class DocearReferenceUpdateController {
	private static Boolean locked = false;

	public static synchronized boolean isLocked() {
		return locked;
	}

	public static synchronized void lock() {
		locked = true;
	}
	
	public static synchronized void unlock() {
		locked = false;
	}
	
	
}
