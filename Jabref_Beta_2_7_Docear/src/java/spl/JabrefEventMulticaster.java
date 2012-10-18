package spl;

/**
 * 
 * @author mag
 *
 * <p>see: {@link java.awt.AWTEventMulticaster}</p>
 */
public class JabrefEventMulticaster implements JabRefEventListener {

	protected final JabRefEventListener a, b;

	protected JabrefEventMulticaster(JabRefEventListener a, JabRefEventListener b) {
		this.a = a;
		this.b = b;
	}

	public static JabRefEventListener add(JabRefEventListener a, JabRefEventListener b) {
		return addInternal(a, b);
	}

	public static JabRefEventListener remove(JabRefEventListener l, JabRefEventListener oldl) {
		return removeInternal(l, oldl);
	}

	private static JabRefEventListener addInternal(JabRefEventListener a, JabRefEventListener b) {
		if (a == null) {
			return b;
		}
		if (b == null) {
			return a;
		}
		
		if(b instanceof JabrefEventMulticaster) {
			if(((JabrefEventMulticaster) b).containsListener(a)) {
				return b;
			}
		}
		return new JabrefEventMulticaster(a, b);
	}

	private static JabRefEventListener removeInternal(JabRefEventListener l, JabRefEventListener oldl) {
		if (l == oldl || l == null) {
			return null;
		} else if (l instanceof JabrefEventMulticaster) {
			return ((JabrefEventMulticaster) l).remove(oldl);
		} else {
			return l; // it's not here
		}
	}

	private JabRefEventListener remove(JabRefEventListener oldl) {
		if (oldl == a)
			return b;
		if (oldl == b)
			return a;
		JabRefEventListener a2 = removeInternal(a, oldl);
		JabRefEventListener b2 = removeInternal(b, oldl);
		if (a2 == a && b2 == b) {
			return this; // it's not here
		}
		return addInternal(a2, b2);
	}
	
	protected boolean containsListener(JabRefEventListener l) {
		if(l !=  null) {
			if(a != null && (l == a || (a instanceof JabrefEventMulticaster && ((JabrefEventMulticaster) a).containsListener(l)))) {
				return true;
			}
			if(b != null && (l == b || (b instanceof JabrefEventMulticaster && ((JabrefEventMulticaster) b).containsListener(l)))) {
				return true;
			}
		}
		return false;
	}

	public void processEvent(JabRefEvent event) {
		if(event.consumed()) return;
		if(a != null) {
			(a).processEvent(event);
		}
		if(event.consumed()) return;
		if(b != null) {
			(b).processEvent(event);
		}
	}

}
