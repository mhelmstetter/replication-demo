package com.mongodb.flightdemo;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.JMapPane;
import org.geotools.swing.RenderingExecutorEvent;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.flightxml.FlightTrackGenerator;
import com.mongodb.oplog.OplogEventListener;
import com.mongodb.replication.ReplicationManager;
import com.mongodb.replication.domain.ReplicationSource;


@Component
public class FlightDisplay extends JMapPane implements OplogEventListener {

    @Autowired
    private ReplicationManager replicationManager;
    
    @Autowired
    private FlightTrackGenerator geoTrackGenerator;

    protected static final Logger logger = LoggerFactory.getLogger(FlightDisplay.class);

    private Map<String, DrawingContext> drawingContexts = new HashMap<String, DrawingContext>();

    private CoordinateReferenceSystem crs;

    private String region;
    
    private BufferedImage backBuffer;
    private Graphics2D backBufferGraphics;

    public FlightDisplay() throws IOException {

        URL url = FlightDisplay.class.getResource("/data/shapefiles/ne_110m_admin_0_countries.shp");
        FileDataStore store = FileDataStoreFinder.getDataStore(url);
        FeatureSource featureSource = store.getFeatureSource();

        MapContent map = new MapContent();
        // MapContent map = new DefaultMapContext(DefaultGeographicCRS.WGS84);
        Style style = SLD.createPolygonStyle(Color.DARK_GRAY, null, 1.0F);
        Layer layer = new FeatureLayer(featureSource, style);
        this.setBackground(Color.BLACK);

        map.addLayer(layer);

        this.setMapContent(map);
        crs = map.getMaxBounds().getCoordinateReferenceSystem();
        this.setRenderer(new StreamingRenderer());
    }

    // sets the location of the sprite component
    private void drawSprite(DrawingContext context) {
        context.setSpritePosition(getWorldToScreenTransform());
    }

    private void initGeneratorMenu(JFrame frame) {
        JMenuBar menuBar = new JMenuBar();
        JMenu dataMenu = new JMenu("Data");
        final JMenuItem startGenerator = new JMenuItem("Start Generator");
        final JMenuItem stopGenerator = new JMenuItem("Stop Generator");
        startGenerator.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopGenerator.setEnabled(true);
                startGenerator.setEnabled(false);
                geoTrackGenerator.startGenerator();
            }});
        dataMenu.add(startGenerator);
        
        stopGenerator.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopGenerator.setEnabled(false);
                startGenerator.setEnabled(true);
                geoTrackGenerator.stopGenerator();
            }});
        stopGenerator.setEnabled(false);
        dataMenu.add(stopGenerator);
        menuBar.add(dataMenu);
        frame.setJMenuBar(menuBar);
    }
    
    private void buildGui() {
        JFrame frame = new JFrame("MongoDB Replication Demo");
        frame.getContentPane().add(this);
        
        
        if (region != null) {
            initGeneratorMenu(frame);
            geoTrackGenerator.setRegion(region);
        }
        

        // frame.setSize(640, 480);
        frame.setSize(2048, 1024);
        

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
    
    @SuppressWarnings("static-access")
    void initializeAndParseCommandLineOptions(String[] args) {
        Options options = new Options();
        options.addOption(new Option( "help", "print this message" ));
        options.addOption(OptionBuilder.withArgName("region")
                .hasArg()
                .withDescription(  "Optional geographic region name (e.g. east or west)" )
                .create( "r" ));
        

        CommandLineParser parser = new GnuParser();
        CommandLine line = null;
        try {
            line = parser.parse(options, args);
            if (line.hasOption("help")) {
                printHelpAndExit(options); 
            }
            initOptions(line);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            printHelpAndExit(options);
        } catch (Exception e) {
            e.printStackTrace();
            printHelpAndExit(options);
        }
    }
    
    private void initOptions(CommandLine line) throws UnknownHostException {
        String region = line.getOptionValue("r");
        this.region = region;
    }

    private static void printHelpAndExit(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("flightDisplay", options);
        System.exit(-1);
    }

    // Main application method
    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring-context.xml");
        context.registerShutdownHook();
        
        try {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Replication Demo");
            //System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Name");
        } catch(Exception e) {
            // ignore
        }
        
        try {
            FlightDisplay flightMap = context.getBean(FlightDisplay.class);
            flightMap.initializeAndParseCommandLineOptions(args);
            flightMap.buildGui();
            // dirty hack to avoid NPEs presumably due to underlying async initialization of map components
            Thread.sleep(3000);
            flightMap.init();
        } catch (Exception e) {
            logger.error("Startup error", e);
            System.exit(-1);
        }
        logger.debug("Init complete");
        
    }

    private void init() throws IOException {
        //replicationManager.addOplogEventListener(this);
        
        ReplicationSource myOplog = new ReplicationSource();
        myOplog.setHostname("localhost");
        myOplog.setPort(27017);
        replicationManager.registerOplogEventListener(this, myOplog);
        
        replicationManager.start();

	this.repaint();
    }
    
    @Override
    public void onRenderingCompleted(RenderingExecutorEvent event) {
        logger.debug("onRenderingCompleted");
    }

    @Override
    public void processRecord(BasicDBObject x) throws Exception {
        DBObject obj = (DBObject) x.get("o");
        String flightNum = (String) obj.get("flightNum");
        final DrawingContext context = drawingContexts.get(flightNum);

	if (flightNum == null) {
	    return;
	}

        BasicDBList positions = (BasicDBList) obj.get("position");
        String airline = (String)obj.get("airline");

        FlightInfo flightInfo = new FlightInfo(flightNum, (double) positions.get(0), (double) positions.get(1));
        flightInfo.setAirline(airline);
        
        
        if (logger.isTraceEnabled()) {
            logger.trace("processRecord(): " + context);
        }
        if (context != null) {
            // logger.debug("Moving " + context);
            context.changePosition(flightInfo);
            drawSprite(context);
        } else {
            final DrawingContext newContext = new DrawingContext(flightInfo, crs);
    	    this.add(newContext);

            // logger.debug("New context " + context);
            drawingContexts.put(flightNum, newContext);
            drawSprite(newContext);

	    this.repaint();
        }

    }

    @Override
    public void close(String string) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void stats(long count, long skips, long duration, int lastTimestamp) {
        // TODO Auto-generated method stub

    }

}