package com.mongodb.replication.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import com.mongodb.replication.domain.ReplicationSourceStatus;

public class ReplicationStatusTableModel extends AbstractTableModel {
    
    private List<ReplicationSourceStatus> replicationSources = new ArrayList<ReplicationSourceStatus>();
    private Map<ReplicationSourceStatus, Integer> rowIndexMap = new HashMap<ReplicationSourceStatus, Integer>();
    int rowNum = 0;
    
    private String[] columnNames = {"host", "port", "count", "rate", "lag seconds"};

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public int getRowCount() {
        return replicationSources.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ReplicationSourceStatus rs = replicationSources.get(rowIndex);
        if (columnIndex == 0) {
            return rs.getReplicationSource().getHostname();
        } else if (columnIndex == 1) {
            return rs.getReplicationSource().getPort();
        } else if (columnIndex == 2) {
            return rs.getCount();
        } else if (columnIndex == 3) {
            return rs.getRate();
        } else {
            int lastTimestamp = rs.getLastTimestamp();
            int currentTimestamp = (int) (System.currentTimeMillis() / 1000L);
            return Math.max(currentTimestamp - lastTimestamp, 0);
        }
        
    }

    public void add(ReplicationSourceStatus replicationStatus) {
        replicationSources.add(replicationStatus);
        rowIndexMap.put(replicationStatus, new Integer(rowNum++));
        
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    public void fireTableRowsUpdated(ReplicationSourceStatus replicationStatus) {
        Integer rowNum = rowIndexMap.get(replicationStatus);
        super.fireTableRowsUpdated(rowNum, rowNum);
        
    }

}
