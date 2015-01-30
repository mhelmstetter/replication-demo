/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 *
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This file is hereby placed into the Public Domain. This means anyone is
 *    free to do whatever they wish with this file. Use it well and enjoy!
 */

package com.mongodb.replication.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.RootPaneContainer;
import javax.swing.Timer;

import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.map.MapViewport;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.JMapPane;
import org.geotools.swing.RenderingExecutorEvent;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Simple example of an animated object (known as a 'sprite')
 * moving over a map. The object is a flying saucer (actually
 * the GeoTools logo) moving over a map of country boundaries.
 *
 * @author Michael Bedward
 */
public class FlyingSaucer extends JMapPane {

    private static final ReferencedEnvelope SMALL_WORLD =
            new ReferencedEnvelope(149, 153, -32, -36, DefaultGeographicCRS.WGS84);
    
    private static final Random rand = new Random();

    // Arbitrary distance to move at each step of the animation
    // in world units.
    private double movementDistance = 0.1;

    private boolean firstDisplay = true;
    
    class DrawingContext {
        private Raster spriteBackground;
        private int xdir = 1;
        private int ydir = 1;
        // The rectangle (in world coordinates) that defines the flying
        // saucer's current position
        private ReferencedEnvelope spriteEnv;
        private Image image = new ImageIcon(FlyingSaucer.class.getResource("/images/plane.png")).getImage();
    }
    private List<DrawingContext> drawingContexts = new ArrayList<DrawingContext>();


    // This animation will be driven by a timer which fires
    // every 200 milliseconds. Each time it fires the drawSprite
    // method is called to update the animation
    private Timer animationTimer = new Timer(200, new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            drawSprite();
        }
    });


    public FlyingSaucer(RootPaneContainer frame) throws IOException {
        frame.getContentPane().add(this);
        URL url = FlyingSaucer.class.getResource("/data/shapefiles/countries.shp");
        FileDataStore store = FileDataStoreFinder.getDataStore(url);
        FeatureSource featureSource = store.getFeatureSource();

        final MapContent mapContent = new MapContent();
        //MapContent map = new DefaultMapContext(DefaultGeographicCRS.WGS84);
        Style style = SLD.createPolygonStyle(Color.DARK_GRAY, null, 1.0F);
        Layer layer = new FeatureLayer(featureSource, style);
        this.setBackground(Color.BLACK);
        
        
        mapContent.addLayer(layer);

        this.setMapContent(mapContent);
        
//        ReferencedEnvelope worldBounds = mapContent.getMaxBounds();
//        CoordinateReferenceSystem crs = worldBounds.getCoordinateReferenceSystem();
//        double x1 = 38.8951;
//        double y1 = -77.0367;
//        double x2 = x1+10;
//        double y2 = y1-10;
//        ReferencedEnvelope bbox = new ReferencedEnvelope( x1,y1, x2, y2, crs );
//        MapViewport viewport = new MapViewport();
//       
//        viewport.setBounds(SMALL_WORLD);
//        mapContent.setViewport(viewport);
        
        this.setRenderer(new StreamingRenderer());
        
        for (int i = 0; i < 1; i++) {
            drawingContexts.add(new DrawingContext());
        }
        
        //MapContent content = mapContent.getMapContent();
        MapViewport viewport = mapContent.getViewport();
        ReferencedEnvelope maxBounds = null;
        //CoordinateReferenceSystem mapCRS = viewport.getCoordianteReferenceSystem();

//        for (Layer xxx : mapContent.layers()) {
//            ReferencedEnvelope dataBounds = xxx.getBounds();
//            if (maxBounds == null) {
//                maxBounds = dataBounds;
//            } else {
//                maxBounds.expandToInclude(dataBounds);
//            }
//        }
//        viewport.setBounds( maxBounds );
//        
//        this.addMouseWheelListener(new MouseWheelListener() {
//
//            @Override
//            public void mouseWheelMoved(MouseWheelEvent e) {
//                int clicks = e.getWheelRotation();
//                // -ve means wheel moved up, +ve means down
//                int sign = (clicks < 0 ? -1 : 1);
//                
//                ReferencedEnvelope env = mapContent.getMaxBounds();
//                double width = env.getWidth();
//                double delta = width * 0.1 * sign;
//
//                env.expandBy(delta);
//                System.out.println(env);
//                mapContent.getViewport().setBounds(env);
//                
//                FlyingSaucer.this.repaint();
//                
//            }});
        
    }

    // We override the JMapPane paintComponent method so that when
    // the map needs to be redrawn (e.g. after the frame has been
    // resized) the animation is stopped until rendering is complete.
    @Override
    protected void paintComponent(Graphics g) {
        //animationTimer.stop();
        super.paintComponent(g);
    }

 // We override the JMapPane onRenderingCompleted method to
    // restart the animation after the map has been drawn.
    @Override
    public void onRenderingCompleted(RenderingExecutorEvent event) {
        //System.out.println("****");
        super.onRenderingCompleted(event);
        for (DrawingContext context : drawingContexts) {
            context.spriteBackground = null;
        }
        
        //animationTimer.start();
    }

    // This is the top-level animation method. It erases
    // the sprite (if showing), updates its position and then
    // draws it.
    private void drawSprite() {
        

        Graphics2D gr2D = (Graphics2D) getGraphics();
        
        for (DrawingContext context : drawingContexts) {
            if (firstDisplay) {
                setSpritePosition(context);
            }
            eraseSprite(gr2D, context);
            moveSprite(context);
            paintSprite(gr2D, context);
        }
        firstDisplay = false;    
    }

    // Erase the sprite by replacing the background map section that
    // was cached when the sprite was last drawn.
    private void eraseSprite(Graphics2D gr2D, DrawingContext context) {
        if (context.spriteBackground != null) {
            Rectangle rect = context.spriteBackground.getBounds();

            BufferedImage image = new BufferedImage(
                    rect.width, rect.height, BufferedImage.TYPE_INT_ARGB);

            Raster child = context.spriteBackground.createChild(
                    rect.x, rect.y, rect.width, rect.height, 0, 0, null);

            image.setData(child);

            gr2D.setBackground(getBackground());
            gr2D.clearRect(rect.x, rect.y, rect.width, rect.height);
            gr2D.drawImage(image, rect.x, rect.y, null);
            context.spriteBackground = null;
        }
    }

    // Update the sprite's location. In this example we are simply
    // moving at 45 degrees to the map edges and bouncing off when an
    // edge is reached.
    private void moveSprite(DrawingContext context) {
        ReferencedEnvelope displayArea = getDisplayArea();

        DirectPosition2D lower = new DirectPosition2D();
        DirectPosition2D upper = new DirectPosition2D();

        double xdelta = 0, ydelta = 0;

        boolean done = false;
        while (!done) {
            lower.setLocation(context.spriteEnv.getLowerCorner());
            upper.setLocation(context.spriteEnv.getUpperCorner());

            xdelta = context.xdir * movementDistance;
            ydelta = context.ydir * movementDistance;

            lower.setLocation(lower.getX() + xdelta, lower.getY() + ydelta);
            upper.setLocation(upper.getX() + xdelta, upper.getY() + ydelta);

            boolean lowerIn = displayArea.contains(lower);
            boolean upperIn = displayArea.contains(upper);

            if (lowerIn && upperIn) {
                done = true;

            } else if (!lowerIn) {
                if (lower.x < displayArea.getMinX()) {
                    context.xdir = -context.xdir;
                } else if (lower.y < displayArea.getMinY()) {
                    context.ydir = -context.ydir;
                }
                System.out.println("lowerOut: " + context.xdir + "," + context.ydir);
                context.image = rotateIcon(context.image, 45);

            } else if (!upperIn) {
                if (upper.x > displayArea.getMaxX()) {
                    context.xdir = -context.xdir;
                } else if (upper.y > displayArea.getMaxY()) {
                    context.ydir = -context.ydir;
                }
                System.out.println("upperOut: " + context.xdir + "," + context.ydir);
                context.image = rotateIcon(context.image, 90);
            }
        }

        context.spriteEnv.translate(xdelta, ydelta);
    }

    // Paint the sprite: before displaying the sprite image we
    // cache that part of the background map image that will be
    // covered by the sprite.
    private void paintSprite(Graphics2D gr2D, DrawingContext context) {
        Rectangle bounds = getSpriteScreenPos(context);
        context.spriteBackground = getBaseImage().getData(bounds);
        gr2D.drawImage(context.image, bounds.x, bounds.y, null);
    }

    // Set the sprite's intial position
    private void setSpritePosition(DrawingContext context) {
        ReferencedEnvelope worldBounds = null;
        worldBounds = getMapContent().getMaxBounds();

        CoordinateReferenceSystem crs = worldBounds.getCoordinateReferenceSystem();

        Rectangle screenBounds = getVisibleRect();
        int w = context.image.getWidth(null);
        int h = context.image.getHeight(null);

        int x = screenBounds.x + rand.nextInt(screenBounds.width - w);
        int y = screenBounds.y + rand.nextInt(screenBounds.height - h);

        Rectangle r = new Rectangle(x, y, w, h);
        AffineTransform tr = getScreenToWorldTransform();
        Rectangle2D rworld = tr.createTransformedShape(r).getBounds2D();
        
        //Rectangle2D rworld = tr.create
        
        System.out.println(rworld); 
        
        // 38.8951° N, 77.0367
        double x1 = 38.8951;
        double y1 = -77.0367;
        double x2 = x1+rworld.getWidth();
        double y2 = y1-rworld.getHeight();
        
        ReferencedEnvelope bbox = new ReferencedEnvelope( x1,y1, x2, y2, crs );

        //context.spriteEnv = new ReferencedEnvelope(rworld, crs);
        context.spriteEnv = bbox;
    }

    // Get the position of the sprite as screen coordinates
    private Rectangle getSpriteScreenPos(DrawingContext context) {
        AffineTransform tr = getWorldToScreenTransform();

        Point2D lowerCorner = new Point2D.Double(context.spriteEnv.getMinX(), context.spriteEnv.getMinY());
        Point2D upperCorner = new Point2D.Double(context.spriteEnv.getMaxX(), context.spriteEnv.getMaxY());

        Point2D p0 = tr.transform(lowerCorner, null);
        Point2D p1 = tr.transform(upperCorner, null);

        Rectangle r = new Rectangle();
        r.setFrameFromDiagonal(p0, p1);
        return r;
    }
    
    private Image rotateIcon(Image icon, int angle) {
        int w = icon.getWidth(null);
        int h = icon.getHeight(null);
        int type = BufferedImage.TYPE_INT_RGB;  // other options, see api
        BufferedImage image = new BufferedImage(h, w, type);
        Graphics2D g2 = image.createGraphics();
        double x = (h - w)/2.0;
        double y = (w - h)/2.0;
        AffineTransform at = AffineTransform.getTranslateInstance(x, y);
        at.rotate(Math.toRadians(angle), w/2.0, h/2.0);
        g2.drawImage(icon, at, null);
        g2.dispose();
        icon = new ImageIcon(image).getImage();
        return icon;
    }

    // Main application method
    public static void main(String[] args) throws Exception {
        JFrame frame = new JFrame("Animation example");
        FlyingSaucer app = new FlyingSaucer(frame);
        
        //frame.setExtendedState(Frame.MAXIMIZED_BOTH);
        //frame.setUndecorated(true);
        
        //frame.setSize(640, 480);
        frame.setSize(2048, 1024);
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

}