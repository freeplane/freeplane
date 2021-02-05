package org.freeplane.core.resources.components;

import javax.swing.JPanel;
import javax.swing.border.Border;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class PropertyPane extends JPanel{

    private static final long serialVersionUID = 1L;
    
    private static final String DEFAULT_LAYOUT_FORMAT = "right:max(40dlu;p), 4dlu, 200dlu:grow, 7dlu";

    @SuppressWarnings("deprecation")
    private DefaultFormBuilder formBuilder;

    public PropertyPane() {
        this(DEFAULT_LAYOUT_FORMAT);
    }
    
    public PropertyPane(String layourFormat) {
        this(new FormLayout(layourFormat, ""));
    }
    
    public PropertyPane(FormLayout formLayout) {
        super(formLayout);
        formBuilder = new DefaultFormBuilder(formLayout, this);
    }
    
    public void addProperty(IPropertyControl control) {
        control.appendToForm(formBuilder);
    }

    public DefaultFormBuilder border(Border border) {
        return formBuilder.border(border);
    }
    
    

}
