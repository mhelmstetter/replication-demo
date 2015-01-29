package com.mongodb.replication.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.table.TableCellRenderer;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.mongodb.BasicDBObject;
import com.mongodb.gui.DataTableModel;
import com.mongodb.gui.util.EclipseIcons;
import com.mongodb.replication.domain.ReplicationSource;
import com.mongodb.replication.domain.ReplicationSourceStatus;
import com.mongodb.replication.repository.ReplicationConfigRepository;

public class ReplicationDisplay extends JPanel {

    @Autowired
    private EclipseIcons eclipseIcons;

    @Autowired
    private ReplicationConfigRepository replicationSourceRepository;

    private JDesktopPane desktop;

    private JFrame frame;


    JTable replicationSourcesTable;
    
    //List replicationSourcesData = new ArrayList();
    
    ReplicationStatusTableModel replicationStatusTableModel = new ReplicationStatusTableModel();

    public ReplicationDisplay() {
        frame = new JFrame();
        frame.setSize(2048, 1024);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    @PostConstruct
    public void init() throws IOException {
        buildGUI();

//        ReplicationSource source1 = new ReplicationSource();
//        source1.setHostname("localhost");
//        source1.setPort(37017);
//        replicationSourceRepository.save(source1);
//
//        ReplicationSource source2 = new ReplicationSource();
//        source2.setHostname("localhost");
//        source2.setPort(47017);
//        replicationSourceRepository.save(source2);

//        Iterator<ReplicationSource> i = replicationSourceRepository.findAll().iterator();
//        while (i.hasNext()) {
//            ReplicationSource replicationSource = i.next();
//            addReplicationSource(replicationSource);
//            
//            
//            
//            //replicationSourcesData.add(replicationSource.getHostname());
//            //replicationSourcesData.add(replicationSource.getPort());
//            
//            
//        }

    }

    private void buildGUI() throws IOException {

//        try {
//            UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticLookAndFeel");
//        } catch (Exception e) {
//        }

        desktop = new JDesktopPane();
        this.setLayout(new GridLayout());
        Dimension d = new Dimension(600, 450);
        this.setPreferredSize(d);
        this.setMinimumSize(d);
        desktop.setBounds(0, 0, d.width, d.height);
        // desktop.setBackground(Color.WHITE);
        this.add(desktop);
        // this.addComponentListener(new ComponentAdapter() {
        // public void componentResized(ComponentEvent e) {
        // JDesktopPaneUtils.tileInternalFrames(desktop);
        // }
        // });

        makeReplicasFrame();
        makeMapFrame();

        frame.getContentPane().add(this);
        frame.pack();
        frame.setVisible(true);
    }

    public void addReplicationSource(ReplicationSource replicationSource) {

        List columnNames = Arrays.asList(new String[] { "Date", "Namespace", "Operation", "Object", "o2"});
        List data = new ArrayList();
        Class[] classes = new Class[] {DateTime.class, String.class, String.class, BasicDBObject.class, BasicDBObject.class};
        //Arrays.fill(classes, String.class);
        DataTableModel tableModel = new DataTableModel(classes, columnNames, data);
        // MyTableCellRenderer renderer = new MyTableCellRenderer();
        JTable oplogEntryTable = new JTable(tableModel) {
            public java.awt.Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int vColIndex) {
                java.awt.Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
                if (rowIndex % 2 == 0 && !isCellSelected(rowIndex, vColIndex)) {
                    c.setBackground(Color.decode("#EEE9E9"));
                } else {
                    // If not shaded, match the table's background
                    c.setBackground(getBackground());
                }
                return c;

            }
        };

        // JTableUtils.initColumnSizes(table, new String[] { "999",
        // "1234567890123456", "1234567890", "1234567890",
        // "1234567890", "2014-03-01 00:00:00.000" });

        JInternalFrame frame1 = makeOplogFrame(replicationSource);
        JScrollPane scrollPane = new JScrollPane(oplogEntryTable);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        frame1.getContentPane().add(scrollPane);
        frame1.show();
        //JDesktopPaneUtils.tileInternalFrames(desktop);
        
        ReplicationSourceStatus replicationStatus = new ReplicationSourceStatus(replicationSource);
        replicationStatusTableModel.add(replicationStatus);
        
        DisplayOplogRecordProcessor processor = new DisplayOplogRecordProcessor(oplogEntryTable, replicationStatus, replicationStatusTableModel);
        
        //ReplicationUtil util = new ReplicationUtil(replicationStatus, "localhost", 27017, processor);
        //ReplicationUtil util = new ReplicationUtil(replicationStatus, "localhost", 27017);
    }

    private JToolBar makeFrameToolBar() {
        JToolBar toolBar = new JToolBar();

        JButton removeAll = makeToolBarButton("Remove All", eclipseIcons.REMOVE_ALL);
        // removeAll.addActionListener(new RemoveAllAction(sr.getTableModel()));
        JToggleButton scrollLock = new JToggleButton(eclipseIcons.LOCK);
        scrollLock.setBorder(null);
        scrollLock.setToolTipText("Scroll Lock");
        // scrollLock.addActionListener(new ScrollLockAction(sr));

        toolBar.add(removeAll);
        toolBar.add(scrollLock);
        return toolBar;
    }

    public static JButton makeToolBarButton(String toolTip, ImageIcon icon) {
        JButton button = new JButton();
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setBorder(null);
        // button.setFocusPainted(false);
        button.setBorderPainted(false);
        // button.setFocusable(false);

        // button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        // button.setRolloverEnabled(true);

        button.setToolTipText(toolTip);
        button.setIcon(icon);
        return button;
    }

    private void makeReplicasFrame() {
        JInternalFrame jframe = new JInternalFrame("Replication Sources");

        JPanel frameContent = new JPanel(new BorderLayout());

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        JButton removeAll = makeToolBarButton("Add Replication Source", eclipseIcons.PLUS);
        removeAll.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                MongoConnectionDialog d = new MongoConnectionDialog(frame);
                d.setVisible(true);
            }
        });
        toolBar.add(removeAll);

         
        // MyTableCellRenderer renderer = new MyTableCellRenderer();
        replicationSourcesTable = new JTable(replicationStatusTableModel);
        replicationSourcesTable.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(replicationSourcesTable);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        frameContent.add(toolBar, BorderLayout.PAGE_START);
        frameContent.add(scrollPane, BorderLayout.CENTER);

        jframe.setBounds(0, 210, 400, 200);
        jframe.getContentPane().add(frameContent);
        jframe.setResizable(true);
        jframe.setVisible(true);

        desktop.add(jframe);
        // JDesktopPaneUtils.tileInternalFrames(desktop);

    }
    
    private void makeMapFrame() throws IOException {
//        JInternalFrame jframe = new JInternalFrame("Map XXX");
//        jframe.setBounds(0, 0, 640, 480);
//        FlightMap map = new FlightMap(jframe);
//        
//        Dimension preferredSize = new Dimension(640, 480);
//        map.setPreferredSize(preferredSize);
//        
//        jframe.getContentPane().add(map);
//        jframe.setResizable(true);
//        jframe.setVisible(true);
//
//        desktop.add(jframe);
//        // JDesktopPaneUtils.tileInternalFrames(desktop);

    }
    
    private int y = 0;

    private JInternalFrame makeOplogFrame(ReplicationSource source) {
        // JToolBar toolBar = makeFrameToolBar(sr);
        //DockedInternalFrame frame1 = new DockedInternalFrame(source.getHostname() + ":" + source.getPort(),
        //        makeFrameToolBar());
        JInternalFrame frame1 = new JInternalFrame(source.getHostname() + ":" + source.getPort());

        frame1.setBounds(0, y, 600, 200);
        y+=200;
        desktop.add(frame1);
        frame1.setFrameIcon(null);
        //frame1.setMoveEnabled(false);
        
        //JDesktopPaneUtils.tileInternalFrames(desktop);
        
        return frame1;
    }

    public static void main(String[] args) {

        ApplicationContext ctx = new ClassPathXmlApplicationContext("spring-context.xml");

        ReplicationDisplay panel = ctx.getBean(ReplicationDisplay.class);

        // OplogDisplay panel = new OplogDisplay();

    }

}
