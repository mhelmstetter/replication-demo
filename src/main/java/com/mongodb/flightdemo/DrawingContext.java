package com.mongodb.flightdemo;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JToolTip;

import org.geotools.referencing.GeodeticCalculator;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DrawingContext extends JComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8255354107758728717L;

	protected static final Logger logger = LoggerFactory.getLogger(DrawingContext.class);

	private final static Image GREEN_PLANE = new ImageIcon(DrawingContext.class.getResource("/images/plane-green.png"))
			.getImage();
	private final static Image BLUE_PLANE = new ImageIcon(DrawingContext.class.getResource("/images/plane-blue.png"))
			.getImage();
	private final static Image RED_PLANE = new ImageIcon(DrawingContext.class.getResource("/images/plane-red.png"))
			.getImage();
	private final static Image RED_DOT = new ImageIcon(DrawingContext.class.getResource("/images/dot-red.png"))
			.getImage();

	private Image image = GREEN_PLANE;
	private double angle = 0;

	private int delay = 0;

	private FlightInfo flightInfo;

	private CoordinateReferenceSystem crs;
	private GeodeticCalculator gc;

	public DrawingContext(FlightInfo flightInfo, CoordinateReferenceSystem crs) {
		this.flightInfo = flightInfo;
		this.crs = crs;
		this.gc = new GeodeticCalculator(crs);

		// if (flightInfo.getAirline() != null) {
		// this.image = GREEN_PLANE;
		// } else {
		// this.image = BLUE_PLANE;
		// }

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
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		AffineTransform tx = AffineTransform.getRotateInstance(angle, image.getWidth(null) / 2,
				image.getHeight(null) / 2);
		g2d.drawImage(image, tx, null);
	}

	public void setSpritePosition(AffineTransform xx) {
		Point2D newPositionInScreenCoordinates = xx.transform(
				new Point2D.Double(flightInfo.getLon(), flightInfo.getLat()), null);

		int newX = (int) newPositionInScreenCoordinates.getX();
		int newY = (int) newPositionInScreenCoordinates.getY();

		// center the image
		newX = newX - this.image.getWidth(null) / 2;
		newY = newY - this.image.getHeight(null) / 2;

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

	@Override
	public String toString() {
		// return "DrawingContext [spriteEnv=" + spriteEnv + ", flightInfo=" +
		// flightInfo + "]";
		return flightInfo.getFlightNum();
	}

	public void changePosition(double previousLat, double previousLon) {
		
		if (this.flightInfo != null) {
			this.setToolTipText(String.format("<html>%s %s<br>%s %s<br>%s %s</html>", flightInfo.getFlightNum(),
					flightInfo.getAircraft(), flightInfo.getAltitude(), flightInfo.getGroundSpeed(),
					flightInfo.getFromIata(), flightInfo.getToIata()));
			
			try {
				
				//logger.debug("changePosition " + previousLon + " -> " + flightInfo.getLon());
				gc.setStartingGeographicPoint(previousLon, previousLat);
				gc.setDestinationGeographicPoint(flightInfo.getLon(), flightInfo.getLat());

				// in degrees so we convert to radians
				this.angle = gc.getAzimuth() * Math.PI / 180;
			} catch (IllegalArgumentException e) {
				logger.error("Error calculating angle: " + e.getMessage());
			}
		}
	}

	public int getDelay() {
		return delay;
	}

	public void setDelay(int delay) {
		this.delay = delay;
		if (delay > 10) {
			this.image = RED_PLANE;
		}
	}

	@Override
	public JToolTip createToolTip() {
		JToolTip tip = super.createToolTip();
		tip.setForeground(Color.GREEN);
		tip.setBackground(Color.BLACK);
		// tip.setFont(new Font("Arial", Font.BOLD,36));
		return tip;
	}

}