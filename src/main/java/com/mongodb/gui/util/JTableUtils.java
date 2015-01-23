package com.mongodb.gui.util;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

public class JTableUtils {
    
    public static void initColumnSizes(JTable table, String[] prototypes) {
        TableModel model = (TableModel) table.getModel();
        int columnCount = model.getColumnCount();
        if (prototypes.length < columnCount) {
            throw new IllegalArgumentException(
                    "Fewer table cell prototypes were provided than there are table columns.");
        }
        TableColumn column = null;
        Component comp = null;
        int headerWidth = 0;
        int cellWidth = 0;
        TableCellRenderer headerRenderer = table.getTableHeader()
                .getDefaultRenderer();

        for (int i = 0; i < columnCount; i++) {
            column = table.getColumnModel().getColumn(i);

            comp = headerRenderer.getTableCellRendererComponent(null, column
                    .getHeaderValue(), false, false, 0, 0);
            headerWidth = comp.getPreferredSize().width;

            comp = table.getDefaultRenderer(model.getColumnClass(i))
                    .getTableCellRendererComponent(table, prototypes[i], false,
                            false, 0, i);
            cellWidth = comp.getPreferredSize().width;
            column.setPreferredWidth(Math.max(headerWidth, cellWidth));
        }
    }

}
