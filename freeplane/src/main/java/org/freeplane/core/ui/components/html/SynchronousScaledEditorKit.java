package org.freeplane.core.ui.components.html;

import javax.swing.SizeRequirements;
import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.ImageView;
import javax.swing.text.html.InlineView;
import javax.swing.text.html.ParagraphView;

@SuppressWarnings("serial")
public class SynchronousScaledEditorKit extends ScaledEditorKit {
	private static ViewFactory synchronousFactory;
	private static ScaledEditorKit kit;
	static public ScaledEditorKit create() {
		if (kit == null) {
			synchronousFactory = new HTMLEditorKit.HTMLFactory(){
				public View create(Element elem) {
					View view = super.create(elem);
					if(elem.getName().equals("br"))
						return view;
                    if(view instanceof InlineView){ 
                        return new InlineView(elem){ 
                            public View breakView(int axis, int p0, float pos, float len) { 
                                View fragment = super.breakView(axis, p0, pos, len);
                                if(this != fragment)
                                    return fragment;
                                if(axis == View.X_AXIS) { 
                                    checkPainter(); 
                                    int p1 = getGlyphPainter().getBoundedPosition(this, p0, pos, len); 
                                    if(p0 == getStartOffset() && p1 == getEndOffset()) { 
                                        return this; 
                                    }
                                    return createFragment(p0, p1); 
                                } 
                                return this; 
                              } 
                          }; 
                    } 
                    else if (view instanceof ParagraphView) { 
                        return new ParagraphView(elem) { 
                            protected SizeRequirements calculateMinorAxisRequirements(int axis, SizeRequirements r) { 
                                if (r == null) { 
                                      r = new SizeRequirements(); 
                                } 
                                float pref = layoutPool.getPreferredSpan(axis); 
                                float min = layoutPool.getMinimumSpan(axis); 
                                // Don't include insets, Box.getXXXSpan will include them. 
                                  r.minimum = (int)min; 
                                  r.preferred = Math.max(r.minimum, (int) pref); 
                                  r.maximum = Integer.MAX_VALUE; 
                                  r.alignment = 0.5f; 
                                return r; 
                              } 

                          }; 
                      } 
                    else if (view instanceof ImageView) {
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
