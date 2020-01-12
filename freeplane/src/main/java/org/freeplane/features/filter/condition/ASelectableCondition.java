package org.freeplane.features.filter.condition;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.freeplane.features.map.NodeModel;
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
	
	public boolean checkNodeInFormulaContext(NodeModel node){
		return checkNode(node);
	}
	
	final public JComponent getListCellRendererComponent() {
		if (renderer == null) {
			this.renderer = createGraphicComponent();
		}
		return renderer;
	}

	public JComponent createGraphicComponent() {
		JComponent renderer = createRendererComponent();
		if(userName != null){
			final JCondition jCondition = new JCondition();
			jCondition.add(new JLabel(userName + " : "));
			jCondition.add(renderer);
			renderer = jCondition;
		}
		return renderer;
	}

	protected JComponent createRendererComponent() {
	    return ConditionFactory.createCellRendererComponent(toString());
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


	protected JComponent createShortRendererComponent() {
		if(userName == null){
			return createRendererComponent();
		}
		final JLabel label = new JLabel('"' + userName + '"');
		return label;
    }

    public boolean canBePersisted() {
        return true;
    }

}
