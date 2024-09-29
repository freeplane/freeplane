package org.freeplane.features.filter.condition;

import java.awt.FontMetrics;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.freeplane.core.ui.components.IconListComponent;
import org.freeplane.core.ui.components.ObjectIcon;
import org.freeplane.core.ui.components.TextIcon;
import org.freeplane.n3.nanoxml.XMLElement;


public abstract class ASelectableCondition  implements ICondition{
	public static final float STRING_MIN_MATCH_PROB = 0.7F;
	transient private String description;
	transient private JComponent renderer;
	private String userName;
	private static Method EQUALS;
	private static Method HASH;
	static{
		try{
			final ClassLoader classLoader = ASelectableCondition.class.getClassLoader();
			EQUALS = classLoader.loadClass("org.apache.commons.lang.builder.EqualsBuilder").getMethod("reflectionEquals", Object.class, Object.class);
			HASH = classLoader.loadClass("org.apache.commons.lang.builder.HashCodeBuilder").getMethod("reflectionHashCode", Object.class);
		}
		catch(Exception e){

		}
	}

	public ASelectableCondition() {
		super();
	}

	@Override
    public int hashCode() {
		if(HASH == null){
			return super.hashCode();
		}
		try {
	        return (Integer) HASH.invoke(null, this);
        }
        catch (Exception e) {
	        e.printStackTrace();
	        return super.hashCode();
        }
    }

	@Override
	public boolean equals(final Object obj) {
		if(EQUALS == null){
			return super.equals(obj);
		}
		try {
			return AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>() {
				@Override
				public Boolean run()
			            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
					return (Boolean) EQUALS.invoke(null, ASelectableCondition.this, obj);
				}
			}).booleanValue();
        }
        catch (Exception e) {
	        e.printStackTrace();
	        return super.equals(obj);
        }
    }
	protected abstract String createDescription();

	final public JComponent getListCellRendererComponent(FontMetrics  fontMetrics) {
		if (renderer == null) {
			this.renderer = createGraphicComponent(fontMetrics);
		}
		renderer.setToolTipText(description);
		return renderer;
	}

	public JComponent createGraphicComponent(FontMetrics  fontMetrics) {
		List<Icon> icons = createRenderedIcons(fontMetrics);
		if(userName != null){
		    List<Icon> iconsWithName = new ArrayList<Icon>(icons.size() + 1);
		    iconsWithName.add(new TextIcon(userName + " : ", fontMetrics));
		    iconsWithName.addAll(icons);
			return new IconListComponent(iconsWithName);
		}
		return new IconListComponent(icons);
	}

	protected List<Icon> createRenderedIcons(FontMetrics  fontMetrics) {
	    return Collections.singletonList(new ObjectIcon<>(this, ConditionFactory.createTextIcon(toString(), fontMetrics)));
    }

	@Override
    final public String toString() {
    	if (description == null) {
    		description = createDescription();
    	}
    	return description;
    }

	public void toXml(final XMLElement element) {
		final XMLElement child = new XMLElement();
		child.setName(getName());
		if(userName != null){
			child.setAttribute("user_name", userName);
		}
		fillXML(child);
		element.addChild(child);
	}

	protected void fillXML(XMLElement element){}

	abstract protected String getName();


	public void setUserName(String userName) {
		if(userName == this.userName || userName != null && userName.equals(this.userName))
			return;
	    this.userName = userName;
	    renderer = null;
    }


	public String getUserName() {
	    return userName;
    }


    protected List<Icon> createSmallRendererIcons(FontMetrics  fontMetrics) {
        if(userName == null){
            return createRenderedIcons(fontMetrics);
        }
        return Collections.singletonList(new ObjectIcon<>(this, new TextIcon('"' + userName + '"', fontMetrics)));
    }

    protected String createSmallDescription() {
        if(userName == null){
            return createDescription();
        }
        return '"' + userName + '"';
    }

    public boolean canBePersisted() {
        return true;
    }
}
