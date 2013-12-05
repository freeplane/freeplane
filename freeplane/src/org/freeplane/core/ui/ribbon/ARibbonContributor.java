package org.freeplane.core.ui.ribbon;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;


public abstract class ARibbonContributor {
	protected transient Comparator<ComparableContributorHull<?>> comparator = new Comparator<ComparableContributorHull<?>>() {
		public int compare(ComparableContributorHull<?> o1, ComparableContributorHull<?> o2) {
			if(o1.getOrderPriority() > o2.getOrderPriority()) {
				return 1;
			}
			else if(o1.getOrderPriority() < o2.getOrderPriority()) {
				return -1;
			} 
			return 0;
		}
	};
	
	public abstract String getKey();
	public abstract void contribute(RibbonBuildContext context, ARibbonContributor parent);
	public abstract void addChild(Object child, ChildProperties properties);
	
	public static int parseOrderSettings(String orderValue) {
		try {
			if("prepend".equals(orderValue.trim().toLowerCase())) {
				return StructureTree.PREPEND;
			}
			if("first".equals(orderValue.trim().toLowerCase())) {
				return StructureTree.FIRST;
			}
			if("last".equals(orderValue.trim().toLowerCase())) {
				return StructureTree.LAST;
			}
			
			return Integer.parseInt(orderValue.trim().toLowerCase());
		}
		catch (Exception e) {
		}
		
		return StructureTree.APPEND;
	}
	
	protected static class ComparableContributorHull<T> {
		private final T obj;
		private final int order;

		/***********************************************************************************
		 * CONSTRUCTORS
		 **********************************************************************************/

		public ComparableContributorHull(T obj, int order) {
			this.obj = obj;
			this.order = order;
		}
		
		/***********************************************************************************
		 * METHODS
		 **********************************************************************************/

		public T getObject() {
			return obj;
		}
		
		public int getOrderPriority() {
			return order;
		}
		
		/***********************************************************************************
		 * REQUIRED METHODS FOR INTERFACES
		 **********************************************************************************/
	}
	
	public static class ChildProperties {
		
		private final int orderPriority;
		private Map<Class<? extends Object>, Object> map;
		/***********************************************************************************
		 * CONSTRUCTORS
		 **********************************************************************************/
		public ChildProperties() {
			this(parseOrderSettings(null));
		}
		public ChildProperties(int orderPriority) {
			this.orderPriority = orderPriority;
		}
		/***********************************************************************************
		 * METHODS
		 **********************************************************************************/

		public int getOrderPriority() {
			return orderPriority;
		}
		
		public <T extends Object> T set(Class<T> key, Object value) {
			if(key == null) {
				throw new IllegalArgumentException("key is NULL");
			}
			
			if(value == null) {
				if(map != null) {
					return (T) map.remove(key);
				}
			}
			else {
				if(map == null) {
					map = new HashMap<Class<? extends Object>, Object>();
				}
				
				return (T) map.put(key, value);
			}
			return null;
		}
		
		public <T extends Object> T  get(Class<T> key) {
			if(key != null && map != null) {
				return (T) map.get(key);
			}
			return null;
		}
		/***********************************************************************************
		 * REQUIRED METHODS FOR INTERFACES
		 **********************************************************************************/
		
	}
}