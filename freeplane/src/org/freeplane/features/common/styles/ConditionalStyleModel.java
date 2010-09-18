package org.freeplane.features.common.styles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.common.filter.condition.ISelectableCondition;
import org.freeplane.features.common.map.NodeModel;

public class ConditionalStyleModel implements IExtension, Iterable<ConditionalStyleModel.Item>{
	public class Item{
		private ISelectableCondition condition;
		private IStyle style;
		private boolean isActive;
		private boolean isLast;
		private Item(boolean isActive, ISelectableCondition condition, IStyle style, boolean isLast) {
	        super();
	        this.isActive = isActive;
	        this.condition = condition;
	        this.style = style;
	        this.setLast(isLast);
        }
		public void setCondition(ISelectableCondition condition) {
	        this.condition = condition;
        }
		public ISelectableCondition getCondition() {
	        return condition;
        }
		public void setStyle(IStyle style) {
	        this.style = style;
        }
		public IStyle getStyle() {
	        return style;
        }
		public void setActive(boolean isActive) {
	        this.isActive = isActive;
        }
		public boolean isActive() {
	        return isActive;
        }
		public void setLast(boolean isLast) {
	        this.isLast = isLast;
        }
		public boolean isLast() {
	        return isLast;
        }
		
	}
	private ArrayList<Item> styles;
	public ConditionalStyleModel() {
	    super();
	    this.styles = new ArrayList<Item>();
    }
	private boolean recursiveCall;
	
	public Collection<IStyle> getStyles(NodeModel node){
		if(recursiveCall){
			return null;
		}
		try{
			recursiveCall = true;
			Collection<IStyle> matchingStyles = new LinkedHashSet<IStyle>();
			for(Item item : styles){
				final ISelectableCondition condition = item.getCondition();
				if(condition != null && item.isActive() && condition.checkNode(node)){
					matchingStyles.add(item.style);
					if(item.isLast()){
						break;
					}
				}
			}
			return matchingStyles;
		}
		finally{
			recursiveCall = false;
		}
	}
	
	void addCondition(boolean isActive, ISelectableCondition condition, IStyle style, boolean isLast){
		styles.add(new Item(isActive, condition, style, isLast));
		if(table == null){
			return;
		}
		int index = styles.size() - 1;
		table.fireTableRowsInserted(index, index);
	}
	
	void insertCondition(int index, boolean isActive, ISelectableCondition condition, IStyle style, boolean isLast){
		styles.add(index, new Item(isActive, condition, style, isLast));
		if(table == null){
			return;
		}
		table.fireTableRowsInserted(index, index);
	}
	
	Item removeCondition(int index){
		final Item item = styles.remove(index);
		if(table == null){
			return item;
		}
		table.fireTableRowsDeleted(index, index);
		return item;
	}
	
	void swapConditions(int index1, int index2){
		final Item item1 = styles.get(index1);
		final Item item2 = styles.get(index2);
		styles.set(index1, item2);
		styles.set(index2, item1);
		if(table == null){
			return;
		}
		table.fireTableRowsUpdated(index1, index1);
		table.fireTableRowsUpdated(index2, index2);
	}
	
	void moveUp(int index){
		if(index == 0){
			return;
		}
		swapConditions(index, index - 1);
	}
	
	void moveDown(int index){
		if(index == styles.size() - 1){
			return;
		}
		swapConditions(index, index + 1);
	}
	
	void clear(){
		styles.clear();
	}

	public Iterator<Item> iterator() {
	    return styles.iterator();
    }
	private AbstractTableModel table;
	public TableModel asTableModel(){
		if(table != null){
			return table;
		}
		table = new AbstractTableModel() {
			/**
             * 
             */
            private static final long serialVersionUID = 1L;

			public Object getValueAt(int rowIndex, int columnIndex) {
				Item item = styles.get(rowIndex);
				switch(columnIndex){
					case 0:
						return item.isActive();
					case 1:
						return item.getCondition();
					case 2:
						return item.getStyle();
					case 3:
						return item.isLast();
						default:
							throw new ArrayIndexOutOfBoundsException();
				}
			}
			
			public int getRowCount() {
				return styles.size();
			}
			
			public int getColumnCount() {
				return 4;
			}
			
			

			@Override
            public Class<?> getColumnClass(int columnIndex) {
				switch(columnIndex){
					case 0:
					case 3: return Boolean.class;
					case 1: return ISelectableCondition.class;
				}
				return super.getColumnClass(columnIndex);
            }
			
			

			@Override
            public String getColumnName(int column) {
				switch(column){
					case 0: return TextUtils.getText("active");
					case 1: return TextUtils.getText("condition");
					case 2: return TextUtils.getText("style");
					case 3: return TextUtils.getText("stop_processing");
				}
				return super.getColumnName(column);
            }

			@Override
            public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
				Item item = styles.get(rowIndex);
				switch(columnIndex){
					case 0:
						item.setActive((Boolean) aValue);
						return;
					case 1:
						item.setCondition((ISelectableCondition) aValue);
						return;
					case 2:
						 item.setStyle((IStyle) aValue);
						 return;
					case 3:
						item.setLast((Boolean) aValue);
						return;
					default:
						throw new ArrayIndexOutOfBoundsException();
				}
            }

			@Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
	            return true;
            }
		};
		return table;
	}

	public int getStyleCount() {
	    return styles.size();
    }
}
