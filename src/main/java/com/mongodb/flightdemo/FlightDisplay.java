package com.mongodb.flightdemo;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

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
import com.mongodb.oplog.OplogEventListener;
import com.mongodb.replication.ReplicationManager;


@Component
public class FlightDisplay extends JMapPane implements OplogEventListener {

    @Autowired
    private ReplicationManager replicationManager;

    protected static final Logger logger = LoggerFactory.getLogger(FlightDisplay.class);

    private Map<String, DrawingContext> drawingContexts = new HashMap<String, DrawingContext>();

    private CoordinateReferenceSystem crs;



    public FlightDisplay() throws IOException {

        URL url = FlightDisplay.class.getResource("/data/shapefiles/countries.shp");
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


    // This is the top-level animation method. It erases
    // the sprite (if showing), updates its position and then
    // draws it.
    private void drawSprite(DrawingContext context) {

        Graphics2D gr2D = (Graphics2D) getGraphics();

        Rectangle bounds = context.getSpriteScreenPos();
        Raster background = null;
        if (bounds != null) {
            background = getBaseImage().getData(context.getSpriteScreenPos());
        }

        context.setSpritePosition(getWorldToScreenTransform(), crs, background);
        // context.setSpriteBackground();
        eraseSprite(gr2D, context);

        // moveSprite(context);
        context.paintSprite(gr2D);
        // logger.debug("draw finished =======================");

    }

    // Erase the sprite by replacing the background map section that
    // was cached when the sprite was last drawn.
    private void eraseSprite(Graphics2D gr2D, DrawingContext context) {
        if (context.getSpriteBackground() != null) {

            Rectangle rect = context.getSpriteBackground().getBounds();
            //logger.debug("erase() " + rect);
            BufferedImage image = new BufferedImage(rect.width, rect.height, BufferedImage.TYPE_INT_ARGB);

            Raster child = context.getSpriteBackground().createChild(rect.x, rect.y, rect.width, rect.height, 0, 0,
                    null);

            image.setData(child);

            gr2D.setBackground(getBackground());
            gr2D.clearRect(rect.x, rect.y, rect.width, rect.height);
            gr2D.drawImage(image, rect.x, rect.y, null);
            // context.setSpriteBackground(null);
        } else {
            //logger.debug(context + " eraseSprite() null background");
        }
    }


    private Image rotateIcon(Image icon, int angle) {
        int w = icon.getWidth(null);
        int h = icon.getHeight(null);
        int type = BufferedImage.TYPE_INT_RGB; // other options, see api
        BufferedImage image = new BufferedImage(h, w, type);
        Graphics2D g2 = image.createGraphics();
        double x = (h - w) / 2.0;
        double y = (w - h) / 2.0;
        AffineTransform at = AffineTransform.getTranslateInstance(x, y);
        at.rotate(Math.toRadians(angle), w / 2.0, h / 2.0);
        g2.drawImage(icon, at, null);
        g2.dispose();
        icon = new ImageIcon(image).getImage();
        return icon;
    }

    // Main application method
    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring-context.xml");
        context.registerShutdownHook();

        JFrame frame = new JFrame("Animation example");
        // FlightMap flightMap = new FlightMap();
        FlightDisplay flightMap = context.getBean(FlightDisplay.class);

        frame.getContentPane().add(flightMap);

        // frame.setSize(640, 480);
        frame.setSize(2048, 1024);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        Thread.sleep(5000);
        flightMap.init();
    }

    private void init() throws IOException {
        replicationManager.start();
    }

    @Override
    public void processRecord(BasicDBObject x) throws Exception {
        DBObject obj = (DBObject) x.get("o");
        String flightNum = (String) obj.get("flightNum");
        BasicDBList positions = (BasicDBList) obj.get("position");

        FlightInfo flightInfo = new FlightInfo(flightNum, (double) positions.get(0), (double) positions.get(1));

        final DrawingContext context = drawingContexts.get(flightNum);
        if (context != null) {
            // logger.debug("Moving " + context);
            context.changePosition(flightInfo);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    drawSprite(context);
                }
            });

        } else {
            final DrawingContext newContext = new DrawingContext(flightInfo);
            // logger.debug("New context " + context);
            drawingContexts.put(flightNum, newContext);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    drawSprite(newContext);
                }
            });

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