package org.freeplane.plugin.script.proxy;



import org.freeplane.core.controller.Controller;
import org.freeplane.plugin.script.proxy.Proxy;

class ControllerProxy implements Proxy.Controller {

	final private Controller controller;

	public ControllerProxy(Controller controller) {
		this.controller = controller;
	}

	public Proxy.Selection getSelection() {
		return new SelectionProxy();
	}

	public Proxy.View getView() {
		return new ViewProxy();
	}

}
