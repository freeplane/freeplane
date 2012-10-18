package spl;

import net.sf.jabref.JabRefFrame;

public abstract class JabRefEvent {
	private boolean consumed = false;
	private final JabRefFrame frame;

	protected JabRefEvent(JabRefFrame frame) {
		if(frame == null) {
			throw new IllegalArgumentException("NULL");
		}
		this.frame = frame;
	}
	
	public void consume() {
		this.consumed  = true;
	}
	
	public boolean consumed() {
		return this.consumed;
	}

	public JabRefFrame getJabRefFrame() {
		return frame;
	}
	
}
