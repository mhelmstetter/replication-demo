package com.mongodb.replication.gui;

import java.io.IOException;

import javax.swing.JTable;
import javax.swing.SwingUtilities;

import org.bson.types.BSONTimestamp;
import org.joda.time.DateTime;

import com.mongodb.BasicDBObject;
import com.mongodb.gui.DataTableModel;
import com.mongodb.gui.util.JTableScrolling;
import com.mongodb.oplog.OplogEventListener;
import com.mongodb.replication.domain.ReplicationSourceStatus;

public class DisplayOplogRecordProcessor implements OplogEventListener {
    
    private DataTableModel tableModel;
    private JTable table;
    
    private ReplicationSourceStatus replicationStatus;
    
    private ReplicationStatusTableModel replicationStatusTableModel;
    
    public DisplayOplogRecordProcessor(JTable table, ReplicationSourceStatus replicationStatus, ReplicationStatusTableModel replicationStatusTableModel) {
        this.table = table;
        this.tableModel = (DataTableModel)table.getModel();
        this.replicationStatus = replicationStatus;
        this.replicationStatusTableModel = replicationStatusTableModel;
    }
    

    @Override
    public void processRecord(final BasicDBObject dbo) throws Exception {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                BSONTimestamp ts = (BSONTimestamp) dbo.get("ts");
                DateTime timestamp = new DateTime(ts.getTime() * 1000l);
                String namespace = (String) dbo.get("ns");
                String op = (String)dbo.get("op");
                BasicDBObject data = (BasicDBObject) dbo.get("o");
                BasicDBObject o2 = (BasicDBObject) dbo.get("o2");

                tableModel.addElement(timestamp);
                tableModel.addElement(namespace);
                tableModel.addElement(op);
                tableModel.addElement(data);
                tableModel.addElement(o2);
                tableModel.fireTableRowsInserted();
                
                // if (!sensorData.scrollLock) {
                JTableScrolling.makeRowVisible(table, tableModel.getRowCount() - 1);
                // }
            }
        });
        
    }

    @Override
    public void close(String string) throws IOException {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void stats(long count, long skips, long duration, int lastTimestamp) {
        replicationStatus.update(count, skips, duration, lastTimestamp);
        replicationStatusTableModel.fireTableRowsUpdated(replicationStatus);
    }

}
