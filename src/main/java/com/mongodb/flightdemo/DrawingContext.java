package com.mongodb.flightdemo;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.Raster;

import javax.swing.ImageIcon;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DrawingContext {

    protected static final Logger logger = LoggerFactory.getLogger(DrawingContext.class);

    boolean firstDisplay = true;
    private Raster spriteBackground;
    int xdir = 1;
    int ydir = 1;
    // The rectangle (in world coordinates) that defines the flying
    // saucer's current position
    private ReferencedEnvelope spriteEnv;
    
    private final static Image GREEN_PLANE = new ImageIcon(DrawingContext.class.getResource("/images/plane.png")).getImage();
    
    private Image image;

    private FlightInfo flightInfo;

    private Rectangle screenPosition;

    public DrawingContext(FlightInfo flightInfo) {
        this.flightInfo = flightInfo;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((flightInfo == null) ? 0 : flightInfo.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DrawingContext other = (DrawingContext) obj;
        if (flightInfo == null) {
            if (other.flightInfo != null)
                return false;
        } else if (!flightInfo.equals(other.flightInfo))
            return false;
        return true;
    }

    public boolean isFirstDisplay() {
        return firstDisplay;
    }

    public void setFirstDisplay(boolean firstDisplay) {
        this.firstDisplay = firstDisplay;
    }

    public Raster getSpriteBackground() {
        return spriteBackground;
    }

    private void setSpriteBackground(Raster spriteBackground) {
        this.spriteBackground = spriteBackground;
//        if (spriteBackground != null) {
//            logger.debug(this + " setSpriteBackground() " + spriteBackground.getBounds());
//        } else {
//            logger.debug(this + " setSpriteBackground() null");
//        }
    }

    public int getXdir() {
        return xdir;
    }

    public void setXdir(int xdir) {
        this.xdir = xdir;
    }

    public int getYdir() {
        return ydir;
    }

    public void setYdir(int ydir) {
        this.ydir = ydir;
    }

    public ReferencedEnvelope getSpriteEnv() {
        return spriteEnv;
    }
    
    // Paint the sprite: before displaying the sprite image we
    // cache that part of the background map image that will be
    // covered by the sprite.
    public void paintSprite(Graphics2D gr2D) {
        Rectangle bounds = getSpriteScreenPos();
        //context.setSpriteBackground(getBaseImage().getData(bounds));
        gr2D.drawImage(image, bounds.x, bounds.y, null);
    }

    public void setSpritePosition(AffineTransform xx, CoordinateReferenceSystem crs, Raster spriteBackground) {

        setSpriteBackground(spriteBackground);
        Point2D newPositionInScreenCoordinates = xx.transform(
                new Point2D.Double(flightInfo.getLon(), flightInfo.getLat()), null);

        // logger.debug(flightInfo + " " + newPositionInScreenCoordinates);
        Rectangle2D newRectScreen = new Rectangle2D.Double(newPositionInScreenCoordinates.getX(),
                newPositionInScreenCoordinates.getY(), 16, 16);

        //CoordinateReferenceSystem crs = getMapContent().getMaxBounds().getCoordinateReferenceSystem();
        ReferencedEnvelope bbox = new ReferencedEnvelope(flightInfo.getLon(), flightInfo.getLon() + 3,
                flightInfo.getLat(), flightInfo.getLat() + 3, crs);
        // logger.debug("bbox: " + bbox);

        // Rectangle bounds = getSpriteScreenPos(bbox);
        setSpriteEnv(bbox, xx);

    }

    private void setSpriteEnv(ReferencedEnvelope spriteEnv, AffineTransform tr) {
        this.spriteEnv = spriteEnv;

        Point2D lowerCorner = new Point2D.Double(spriteEnv.getMinX(), spriteEnv.getMinY());
        Point2D upperCorner = new Point2D.Double(spriteEnv.getMaxX(), spriteEnv.getMaxY());

        Point2D p0 = tr.transform(lowerCorner, null);
        Point2D p1 = tr.transform(upperCorner, null);

        Rectangle r = new Rectangle();
        r.setFrameFromDiagonal(p0, p1);
        this.screenPosition = r;
    }

    public Rectangle getSpriteScreenPos() {
        return screenPosition;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public FlightInfo getFlightInfo() {
        return flightInfo;
    }

    public void setFlightInfo(FlightInfo flightInfo) {
        this.flightInfo = flightInfo;
    }

    @Override
    public String toString() {
        // return "DrawingContext [spriteEnv=" + spriteEnv + ", flightInfo=" +
        // flightInfo + "]";
        return flightInfo.getFlightNum();
    }

    public void changePosition(FlightInfo flightInfo) {
        this.flightInfo = flightInfo;
        
    }

}