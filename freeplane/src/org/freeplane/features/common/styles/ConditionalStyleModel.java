package org.freeplane.features.common.styles;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.common.filter.condition.ISelectableCondition;
import org.freeplane.features.common.map.ModeController;
import org.freeplane.features.common.map.NodeModel;

public class ConditionalStyleModel implements IExtension, Iterable<ConditionalStyleModel.Item>{
	public class Item{
		private ISelectableCondition condition;
		private Object style;
		private boolean isActive;
		private Item(boolean isActive, ISelectableCondition condition, Object style) {
	        super();
	        this.isActive = isActive;
	        this.condition = condition;
	        this.style = style;
        }
		public void setCondition(ISelectableCondition condition) {
	        this.condition = condition;
        }
		public ISelectableCondition getCondition() {
	        return condition;
        }
		public void setStyle(Object style) {
	        this.style = style;
        }
		public Object getStyle() {
	        return style;
        }
		public void setActive(boolean isActive) {
	        this.isActive = isActive;
        }
		public boolean isActive() {
	        return isActive;
        }
		
	}
	private ArrayList<Item> styles;
	public ConditionalStyleModel() {
	    super();
	    this.styles = new ArrayList<Item>();
    }
	private boolean recursiveCall;
	
	public Object getStyle(ModeController modeController, NodeModel node){
		if(recursiveCall){
			return null;
		}
		try{
			recursiveCall = true;
		for(Item item : styles){
			final ISelectableCondition condition = item.getCondition();
			if(condition != null && item.isActive() && condition.checkNode(modeController, node)){
				return item.style;
			}
		}
		return null;
		}
		finally{
			recursiveCall = false;
		}
	}
	
	void addCondition(boolean isActive, ISelectableCondition condition, Object style){
		styles.add(new Item(isActive, condition, style));
		if(table == null){
			return;
		}
		int index = styles.size() - 1;
		table.fireTableRowsInserted(index, index);
	}
	
	void insertCondition(int index, boolean isActive, ISelectableCondition condition, Object style){
		styles.add(index, new Item(isActive, condition, style));
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
						default:
							throw new ArrayIndexOutOfBoundsException();
				}
			}
			
			public int getRowCount() {
				return styles.size();
			}
			
			public int getColumnCount() {
				return 3;
			}
			
			

			@Override
            public Class<?> getColumnClass(int columnIndex) {
				switch(columnIndex){
					case 0: return Boolean.class;
					case 1: return ISelectableCondition.class;
				}
				return super.getColumnClass(columnIndex);
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
						 item.setStyle(aValue);
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
