package com.mongodb.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.table.AbstractTableModel;


public class DataTableModel extends AbstractTableModel {

    protected List columnNames;

    protected List data;

    protected Class[] classes;
    
    protected boolean isCellsEditable;

    protected DataTableModel() {}
    
    public DataTableModel(Class[] classes, List columnNames, List data) {
        this.classes = classes;
        this.columnNames = columnNames;
        this.data = data;
    }

    /**
     * @param columnNames2
     * @param data2
     */
    public DataTableModel(List columnNames, List data) {
        Class[] classes = new Class[columnNames.size()];
        Arrays.fill(classes, String.class);
        this.classes = classes;
        this.columnNames = columnNames;
        this.data = data;
    }
    
    public DataTableModel(String[] columnNames, List data) {
        Class[] classes = new Class[columnNames.length];
        Arrays.fill(classes, String.class);
        this.classes = classes;
        this.columnNames = Arrays.asList(columnNames);
        this.data = data;
    }
    
    public DataTableModel(String[] columnNames) {
        this(columnNames, new ArrayList());
    }

    public void addElement(Object newElement) {
        data.add(newElement);
    }

    public void fireTableRowsInserted() {
        int rowCount = getRowCount();
        fireTableRowsInserted(rowCount - 1, rowCount - 1);
    }

    public void removeAllElements() {
        int rowCount = getRowCount();
        if (rowCount > 0) {
            data.clear();
            fireTableRowsDeleted(0, rowCount - 1);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount() {
        return columnNames.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount() {
        if (columnNames.size() == 0) {
            return 0;
        }
        return data.size() / columnNames.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
        int index = columnNames.size() * rowIndex + columnIndex;
        return data.get(index);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.TableModel#getColumnName(int)
     */
    public String getColumnName(int column) {
        return columnNames.get(column).toString();    
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.TableModel#getColumnClass(int)
     */
    public Class getColumnClass(int columnIndex) {
        return classes[columnIndex];
    }
    
    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#isCellEditable(int, int)
     */
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return isCellsEditable;
    }
    /**
     * @return Returns the isCellsEditable.
     */
    public boolean isCellsEditable() {
        return isCellsEditable;
    }
    /**
     * @param isCellsEditable The isCellsEditable to set.
     */
    public void setCellsEditable(boolean isCellsEditable) {
        this.isCellsEditable = isCellsEditable;
    }
    
    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int, int)
     */
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        int index = columnNames.size() * rowIndex + columnIndex;
        data.set(index, aValue);
    }
}