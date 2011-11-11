package net.sf.jabref.export;

public class DocearReferenceUpdateController {
	private static boolean locked;

	public static boolean isLocked() {
		return locked;
	}

	public static void lock() {
		locked = true;
	}
	
	public static void unlock() {
		locked = false;
	}
	
	
}
