package org.freeplane.core.ui.components.html;

import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.ImageView;

@SuppressWarnings("serial")
public class SynchronousScaledEditorKit extends ScaledEditorKit {
	private static ViewFactory synchronousFactory;
	private static ScaledEditorKit kit;
	static public ScaledEditorKit create() {
		if (kit == null) {
			synchronousFactory = new HTMLEditorKit.HTMLFactory(){
				public View create(Element elem) {
					View view = super.create(elem);

					if (view instanceof ImageView) {
						((ImageView)view).setLoadsSynchronously(true);
					}
					return view;
			    }
			};
			kit = new SynchronousScaledEditorKit();
		}
		return kit;
	}
	
	@Override
	public ViewFactory getViewFactory() {
		return synchronousFactory;
	}
	
	

}
