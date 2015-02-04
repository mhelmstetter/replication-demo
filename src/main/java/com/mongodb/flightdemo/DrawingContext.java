package com.mongodb.flightdemo;

import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.AffineTransformOp;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.geotools.referencing.GeodeticCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DrawingContext extends JComponent {

    protected static final Logger logger = LoggerFactory.getLogger(DrawingContext.class);

    private final static Image GREEN_PLANE = new ImageIcon(DrawingContext.class.getResource("/images/plane-green.png")).getImage();
    private final static Image BLUE_PLANE = new ImageIcon(DrawingContext.class.getResource("/images/plane-blue.png")).getImage();
    private final static Image RED_DOT = new ImageIcon(DrawingContext.class.getResource("/images/dot-red.png")).getImage();
    
    private Image image;
    private double angle = 0;

    private FlightInfo flightInfo;

    private CoordinateReferenceSystem crs;

    public DrawingContext(FlightInfo flightInfo, CoordinateReferenceSystem crs) {
        this.flightInfo = flightInfo;
	this.crs = crs;

        if (flightInfo.getAirline() != null) {
            this.image = GREEN_PLANE;
        } else {
            this.image = BLUE_PLANE;
        }

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

    protected void paintComponent(Graphics g) {
	Graphics2D g2d = (Graphics2D)g;

	AffineTransform tx = AffineTransform.getRotateInstance(angle, image.getWidth(null) / 2, image.getHeight(null) / 2);
	g2d.drawImage(image, tx, null);
    }

    public void setSpritePosition(AffineTransform xx) {
        Point2D newPositionInScreenCoordinates = xx.transform(
                new Point2D.Double(flightInfo.getLon(), flightInfo.getLat()), null);

	int newX  = (int)newPositionInScreenCoordinates.getX();
	int newY  = (int)newPositionInScreenCoordinates.getY();

	// center the image
	newX = newX - this.image.getWidth(null) / 2;
	newY = newY - this.image.getHeight(null) / 2;

	Point currentLoc = this.getLocation();
	Point newLoc = new Point(newX, newY);

	this.setLocation(newLoc);
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
        this.changePosition(flightInfo);
    }

    @Override
    public String toString() {
        // return "DrawingContext [spriteEnv=" + spriteEnv + ", flightInfo=" +
        // flightInfo + "]";
        return flightInfo.getFlightNum();
    }

    public void changePosition(FlightInfo flightInfo) {
	if (this.flightInfo != null) {
	    try {
		GeodeticCalculator gc = new GeodeticCalculator(crs);
		gc.setStartingGeographicPoint(this.flightInfo.getLon(), this.flightInfo.getLat());
		gc.setDestinationGeographicPoint(flightInfo.getLon(), flightInfo.getLat());

		// in degrees so we convert to radians
		this.angle = gc.getAzimuth() * Math.PI / 180;
	    } catch (IllegalArgumentException e) {
		logger.error("Error calculating angle: " + e.getMessage());
	    }
	}


        this.flightInfo = flightInfo;        
    }

}