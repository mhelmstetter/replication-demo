package com.mongodb.flightdemo;

import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DrawingContext extends JComponent {

    protected static final Logger logger = LoggerFactory.getLogger(DrawingContext.class);

    boolean firstDisplay = true;
    private Raster spriteBackground;
    int xdir = 1;
    int ydir = 1;
    // The rectangle (in world coordinates) that defines the flying
    // saucer's current position
    private ReferencedEnvelope spriteEnv;
    
    private final static Image GREEN_PLANE = new ImageIcon(DrawingContext.class.getResource("/images/plane-green.png")).getImage();
    private final static Image BLUE_PLANE = new ImageIcon(DrawingContext.class.getResource("/images/plane-blue.png")).getImage();
    private final static Image RED_DOT = new ImageIcon(DrawingContext.class.getResource("/images/dot-red.png")).getImage();
    
    private Image image;

    private FlightInfo flightInfo;

    private Rectangle screenPosition;

    public DrawingContext(FlightInfo flightInfo) {
        this.flightInfo = flightInfo;
        if (flightInfo.getAirline() != null) {
            this.image = GREEN_PLANE;
        } else {
            this.image = BLUE_PLANE;
        }
        //this.image = RED_DOT;
        

	Dimension size = new Dimension(image.getWidth(null), image.getHeight(null));
	setPreferredSize(size);
	setMinimumSize(size);
	setMaximumSize(size);
	setSize(size);
	setLayout(null);
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
    
    protected void paintComponent(Graphics g) {
	g.drawImage(image, 0, 0, null);
    }

    // Paint the sprite: before displaying the sprite image we
    // cache that part of the background map image that will be
    // covered by the sprite.
    public void paintSprite(Graphics2D gr2D) {
	//        Rectangle bounds = getSpriteScreenPos();
        //context.setSpriteBackground(getBaseImage().getData(bounds));
	//        gr2D.drawImage(image, bounds.x, bounds.y, null);
    }
    
    // Erase the sprite by replacing the background map section that
    // was cached when the sprite was last drawn.
    public void eraseSprite(Graphics2D gr2D) {
	//        if (getSpriteBackground() != null) {
        if (1 == 0) {

            Rectangle rect = getSpriteBackground().getBounds();
            //logger.debug("erase() " + rect);
            BufferedImage image = new BufferedImage(rect.width, rect.height, BufferedImage.TYPE_INT_ARGB);

            Raster child = getSpriteBackground().createChild(rect.x, rect.y, rect.width, rect.height, 0, 0,
                    null);

            image.setData(child);

            //gr2D.setBackground(getBackground());
            gr2D.clearRect(rect.x, rect.y, rect.width, rect.height);
            gr2D.drawImage(image, rect.x, rect.y, null);
            // context.setSpriteBackground(null);
        } else {
            //logger.debug(context + " eraseSprite() null background");
        }
    }

    public void setSpritePosition(AffineTransform xx, CoordinateReferenceSystem crs, Raster spriteBackground) {

        setSpriteBackground(spriteBackground);
        Point2D newPositionInScreenCoordinates = xx.transform(
                new Point2D.Double(flightInfo.getLon(), flightInfo.getLat()), null);

        // logger.debug(flightInfo + " " + newPositionInScreenCoordinates);
        Rectangle2D newRectScreen = new Rectangle2D.Double(newPositionInScreenCoordinates.getX(),
                newPositionInScreenCoordinates.getY(), image.getWidth(null), image.getHeight(null));
        
        this.screenPosition = newRectScreen.getBounds();

	this.setLocation((int)newPositionInScreenCoordinates.getX(), (int)newPositionInScreenCoordinates.getY());
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