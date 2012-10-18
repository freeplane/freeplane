package spl;

import java.util.EventListener;


public interface JabRefEventListener extends EventListener {
	public void processEvent(JabRefEvent event);
}
